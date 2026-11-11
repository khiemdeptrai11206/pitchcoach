package com.pitchcoach.backend.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pitchcoach.backend.model.PracticeSessionDocument;
import com.pitchcoach.backend.model.PresentationDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * 1. Phân tích dàn ý bài thuyết trình & sinh câu hỏi dự đoán bằng Gemini
     */
    public void enrichPresentationWithGemini(PresentationDocument doc) {
        if (apiKey == null || apiKey.isBlank()) {
            return;
        }
        try {
            String prompt = "Bạn là chuyên gia huấn luyện thuyết trình AI (PitchCoach). "
                    + "Người dùng tạo bài thuyết trình với thông tin sau:\n"
                    + "- Chủ đề: " + doc.getTitle() + "\n"
                    + "- Đối tượng người nghe: " + doc.getAudienceType() + "\n"
                    + "- Thời lượng mục tiêu: " + doc.getTargetDurationMin() + " phút\n"
                    + "- Dàn ý sơ bộ: " + (doc.getRawOutline() != null ? doc.getRawOutline() : "") + "\n\n"
                    + "Hãy trả về DUY NHẤT một chuỗi JSON hợp lệ (không bọc markdown) theo cấu trúc:\n"
                    + "{\n"
                    + "  \"aiOutlineFeedback\": \"Nhận xét ngắn gọn 1-2 câu về bố cục dàn ý\",\n"
                    + "  \"outlineItems\": [\n"
                    + "    {\"sectionTitle\": \"1. Mở đầu...\", \"content\": \"Ý chính...\", \"estimatedSeconds\": 120}\n"
                    + "  ],\n"
                    + "  \"predictedQuestions\": [\n"
                    + "    \"Câu hỏi dự đoán 1?\",\n"
                    + "    \"Câu hỏi dự đoán 2?\",\n"
                    + "    \"Câu hỏi dự đoán 3?\"\n"
                    + "  ]\n"
                    + "}";

            JsonObject root = callGeminiJson(prompt, null, null);
            if (root == null) {
                return;
            }

            if (hasString(root, "aiOutlineFeedback")) {
                doc.setAiOutlineFeedback(getString(root, "aiOutlineFeedback", ""));
            }

            if (root.has("outlineItems") && root.get("outlineItems").isJsonArray()) {
                List<PresentationDocument.OutlineItemDto> items = new ArrayList<>();
                int idx = 1;
                for (JsonElement el : root.getAsJsonArray("outlineItems")) {
                    if (el.isJsonObject()) {
                        JsonObject node = el.getAsJsonObject();
                        items.add(new PresentationDocument.OutlineItemDto(
                                "out_" + idx,
                                idx,
                                getString(node, "sectionTitle", "Phần " + idx),
                                getString(node, "content", ""),
                                getInt(node, "estimatedSeconds", 120)
                        ));
                        idx++;
                    }
                }
                if (!items.isEmpty()) {
                    doc.setOutlineItems(items);
                }
            }

            if (root.has("predictedQuestions") && root.get("predictedQuestions").isJsonArray()) {
                List<PresentationDocument.PredictedQuestionDto> questions = new ArrayList<>();
                int idx = 1;
                for (JsonElement qEl : root.getAsJsonArray("predictedQuestions")) {
                    questions.add(new PresentationDocument.PredictedQuestionDto(
                            "q_" + idx, idx, qEl.getAsString()));
                    idx++;
                }
                if (!questions.isEmpty()) {
                    doc.setPredictedQuestions(questions);
                }
            }
        } catch (Exception e) {
            log.warn("Gemini outline enrichment fallback: {}", e.getMessage());
        }
    }

    /**
     * 2. Phân tích file ghi âm bài thuyết trình bằng Gemini (Multimodal Audio + Text)
     */
    public boolean analyzePracticeSessionWithGemini(PracticeSessionDocument session,
                                                    byte[] audioBytes,
                                                    String mimeType) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        try {
            String prompt = "Bạn là giám khảo chấm điểm thuyết trình AI của ứng dụng PitchCoach. "
                    + (audioBytes != null && audioBytes.length > 0
                    ? "Hãy nghe file ghi âm bài thuyết trình đính kèm (thời lượng khoảng "
                    + session.getDurationSeconds() + " giây), gỡ băng từng câu tiếng Việt, "
                    : "Hãy tạo kết quả phân tích cho bài luyện tập thuyết trình, ")
                    + "phát hiện các từ đệm/từ thừa ('ờ', 'à', 'thì là', 'kiểu như'), chấm điểm và đặt 3 câu hỏi hội đồng phản biện.\n"
                    + "Trả về DUY NHẤT một JSON hợp lệ theo đúng cấu trúc sau:\n"
                    + "{\n"
                    + "  \"overallScore\": 8.2,\n"
                    + "  \"scoreContent\": 8,\n"
                    + "  \"scoreStructure\": 8,\n"
                    + "  \"scoreClarity\": 9,\n"
                    + "  \"scoreDelivery\": 8,\n"
                    + "  \"speakingSpeedWpm\": 138.0,\n"
                    + "  \"fillerWordCount\": 3,\n"
                    + "  \"aiSummaryGood\": \"Nhận xét điểm tốt...\",\n"
                    + "  \"aiSummaryMissing\": \"Nhận xét điểm cần bổ sung...\",\n"
                    + "  \"transcriptLines\": [\n"
                    + "    {\"sentence\": \"Câu nói 1...\", \"highlightFiller\": null, \"startMs\": 0, \"endMs\": 5000},\n"
                    + "    {\"sentence\": \"Câu nói có từ ờ...\", \"highlightFiller\": \"ờ\", \"startMs\": 5000, \"endMs\": 10000}\n"
                    + "  ],\n"
                    + "  \"improvements\": [\n"
                    + "    {\"title\": \"Tiêu đề góp ý\", \"evidence\": \"Dẫn chứng cụ thể trong bài nói\"}\n"
                    + "  ],\n"
                    + "  \"panelQa\": [\n"
                    + "    {\"panelistName\": \"Giảng viên hướng dẫn\", \"panelistInitials\": \"GV\", \"panelistTone\": 0, \"questionText\": \"Câu hỏi 1?\"},\n"
                    + "    {\"panelistName\": \"Khán giả tò mò\", \"panelistInitials\": \"KG\", \"panelistTone\": 1, \"questionText\": \"Câu hỏi 2?\"},\n"
                    + "    {\"panelistName\": \"Nhà tuyển dụng\", \"panelistInitials\": \"TD\", \"panelistTone\": 2, \"questionText\": \"Câu hỏi 3?\"}\n"
                    + "  ]\n"
                    + "}";

            JsonObject root = callGeminiJson(prompt, audioBytes, mimeType);
            if (root == null) {
                return false;
            }

            session.setOverallScore(getDouble(root, "overallScore", 8.0));
            session.setScoreContent(getInt(root, "scoreContent", 8));
            session.setScoreStructure(getInt(root, "scoreStructure", 8));
            session.setScoreClarity(getInt(root, "scoreClarity", 9));
            session.setScoreDelivery(getInt(root, "scoreDelivery", 7));
            session.setSpeakingSpeedWpm(getDouble(root, "speakingSpeedWpm", 140.0));
            session.setFillerWordCount(getInt(root, "fillerWordCount", 3));
            session.setAiSummaryGood(getString(root, "aiSummaryGood", "Trình bày rõ ràng, đúng trọng tâm."));
            session.setAiSummaryMissing(getString(root, "aiSummaryMissing", "Cần tiết chế tốc độ ở phần kết."));

            if (root.has("transcriptLines") && root.get("transcriptLines").isJsonArray()) {
                List<PracticeSessionDocument.TranscriptLineDto> lines = new ArrayList<>();
                int idx = 0;
                for (JsonElement el : root.getAsJsonArray("transcriptLines")) {
                    if (el.isJsonObject()) {
                        JsonObject node = el.getAsJsonObject();
                        String highlight = hasString(node, "highlightFiller")
                                ? getString(node, "highlightFiller", null)
                                : null;
                        if (highlight != null && highlight.isBlank()) {
                            highlight = null;
                        }
                        lines.add(new PracticeSessionDocument.TranscriptLineDto(
                                "tr_" + (idx + 1),
                                idx,
                                getString(node, "sentence", ""),
                                highlight,
                                getInt(node, "startMs", idx * 5000),
                                getInt(node, "endMs", (idx + 1) * 5000)
                        ));
                        idx++;
                    }
                }
                if (!lines.isEmpty()) {
                    session.setTranscriptLines(lines);
                }
            }

            if (root.has("improvements") && root.get("improvements").isJsonArray()) {
                List<PracticeSessionDocument.ImprovementDto> imps = new ArrayList<>();
                int idx = 1;
                for (JsonElement el : root.getAsJsonArray("improvements")) {
                    if (el.isJsonObject()) {
                        JsonObject node = el.getAsJsonObject();
                        imps.add(new PracticeSessionDocument.ImprovementDto(
                                "imp_" + idx,
                                idx,
                                getString(node, "title", ""),
                                getString(node, "evidence", ""),
                                false
                        ));
                        idx++;
                    }
                }
                if (!imps.isEmpty()) {
                    session.setImprovements(imps);
                }
            }

            if (root.has("panelQa") && root.get("panelQa").isJsonArray()) {
                List<PracticeSessionDocument.PanelQaDto> panel = new ArrayList<>();
                int idx = 0;
                for (JsonElement el : root.getAsJsonArray("panelQa")) {
                    if (el.isJsonObject()) {
                        JsonObject node = el.getAsJsonObject();
                        panel.add(new PracticeSessionDocument.PanelQaDto(
                                "qa_" + (idx + 1),
                                idx,
                                getString(node, "panelistName", "Giám khảo AI"),
                                getString(node, "panelistInitials", "AI"),
                                getInt(node, "panelistTone", idx % 3),
                                getString(node, "questionText", "")
                        ));
                        idx++;
                    }
                }
                if (!panel.isEmpty()) {
                    session.setPanelQa(panel);
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("Gemini session analysis fallback: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 3. Chấm điểm câu trả lời Hội đồng phản biện AI bằng Gemini
     */
    public boolean evaluatePanelAnswerWithGemini(PracticeSessionDocument.PanelQaDto qaItem,
                                                 byte[] audioBytes,
                                                 String mimeType,
                                                 String userTranscript) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }
        try {
            String prompt = "Bạn là giám khảo '" + qaItem.getPanelistName() + "' trong hội đồng phản biện. "
                    + "Câu hỏi bạn đã đặt ra là: \"" + qaItem.getQuestionText() + "\".\n"
                    + (userTranscript != null && !userTranscript.isBlank()
                    ? "Câu trả lời của thí sinh: \"" + userTranscript + "\".\n"
                    : "Hãy nghe file âm thanh câu trả lời đính kèm của thí sinh.\n")
                    + "Hãy chấm điểm và trả về DUY NHẤT một JSON hợp lệ theo cấu trúc:\n"
                    + "{\n"
                    + "  \"answerTranscript\": \"Nội dung gỡ băng câu trả lời của thí sinh\",\n"
                    + "  \"answerScore\": 8.5,\n"
                    + "  \"feedbackGood\": \"Điểm mạnh trong câu trả lời (1-2 câu)\",\n"
                    + "  \"feedbackImprove\": \"Điểm cần bổ sung để thuyết phục hơn (1-2 câu)\"\n"
                    + "}";

            JsonObject root = callGeminiJson(prompt, audioBytes, mimeType);
            if (root == null) {
                return false;
            }
            qaItem.setAnswerTranscript(getString(root, "answerTranscript",
                    userTranscript != null ? userTranscript : ""));
            qaItem.setAnswerScore(getDouble(root, "answerScore", 8.5));
            qaItem.setFeedbackGood(getString(root, "feedbackGood", "Trả lời đúng trọng tâm câu hỏi."));
            qaItem.setFeedbackImprove(getString(root, "feedbackImprove", "Nên bổ sung thêm số liệu dẫn chứng."));
            return true;
        } catch (Exception e) {
            log.warn("Gemini panel answer evaluation fallback: {}", e.getMessage());
            return false;
        }
    }

    private JsonObject callGeminiJson(String promptText, byte[] audioBytes, String mimeType)
            throws Exception {
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();

        if (audioBytes != null && audioBytes.length > 0) {
            JsonObject inlineDataPart = new JsonObject();
            JsonObject inlineData = new JsonObject();
            inlineData.addProperty("mime_type",
                    (mimeType != null && !mimeType.isBlank()) ? mimeType : "audio/mp4");
            inlineData.addProperty("data", Base64.getEncoder().encodeToString(audioBytes));
            inlineDataPart.add("inline_data", inlineData);
            parts.add(inlineDataPart);
        }

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", promptText);
        parts.add(textPart);

        contentObj.add("parts", parts);
        contents.add(contentObj);
        requestBody.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("response_mime_type", "application/json");
        generationConfig.addProperty("temperature", 0.4);
        requestBody.add("generationConfig", generationConfig);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey.trim();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.warn("Gemini API returned status {}: {}", response.statusCode(), response.body());
            return null;
        }

        JsonObject respRoot = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!respRoot.has("candidates") || respRoot.getAsJsonArray("candidates").isEmpty()) {
            return null;
        }
        JsonObject firstCandidate = respRoot.getAsJsonArray("candidates").get(0).getAsJsonObject();
        JsonObject content = firstCandidate.getAsJsonObject("content");
        if (content == null || !content.has("parts") || content.getAsJsonArray("parts").isEmpty()) {
            return null;
        }
        JsonObject part0 = content.getAsJsonArray("parts").get(0).getAsJsonObject();
        if (!hasString(part0, "text")) {
            return null;
        }

        String rawJson = part0.get("text").getAsString().trim();
        if (rawJson.startsWith("```")) {
            rawJson = rawJson.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
        }
        return JsonParser.parseString(rawJson).getAsJsonObject();
    }

    private boolean hasString(JsonObject obj, String key) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull();
    }

    private String getString(JsonObject obj, String key, String defaultVal) {
        return hasString(obj, key) ? obj.get(key).getAsString() : defaultVal;
    }

    private int getInt(JsonObject obj, String key, int defaultVal) {
        try {
            return hasString(obj, key) ? obj.get(key).getAsInt() : defaultVal;
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private double getDouble(JsonObject obj, String key, double defaultVal) {
        try {
            return hasString(obj, key) ? obj.get(key).getAsDouble() : defaultVal;
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
