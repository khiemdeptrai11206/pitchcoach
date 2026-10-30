package com.pitchcoach.app.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;
import com.pitchcoach.app.R;

public final class ScreenFactory {
    private static final int PURPLE = Color.rgb(91,75,219);
    private static final int TEXT = Color.rgb(27,27,47);
    private static final int MUTED = Color.rgb(119,119,139);
    private static final int BG = Color.rgb(247,247,251);

    public interface Navigator { void go(String screen); }
    private ScreenFactory() {}

    public static View create(Context c, String screen, Navigator nav) {
        switch (screen) {
            case "login": return login(c, nav);
            case "home": return home(c, nav);
            case "create": return createPresentation(c, nav);
            case "detail": return detail(c, nav);
            case "record": return recording(c, nav);
            case "analyzing": return analyzing(c, nav);
            case "result": return result(c, nav);
            case "panel": return panel(c, nav);
            case "progress": return progress(c, nav);
            case "progress_detail": return progressDetail(c, nav);
            case "profile": return profile(c, nav);
            case "all": return allPresentations(c, nav);
            case "practice": return practice(c, nav);
            default: return home(c, nav);
        }
    }

    private static LinearLayout page(Context c) {
        LinearLayout root = new LinearLayout(c); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(c,20), dp(c,22), dp(c,20), dp(c,16)); root.setBackgroundColor(BG);
        return root;
    }
    private static int dp(Context c, int v) { return (int)(v*c.getResources().getDisplayMetrics().density + .5f); }
    private static TextView text(Context c, String value, int size, boolean bold, int color) {
        TextView t = new TextView(c); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }
    private static void heading(LinearLayout root, Context c, String title, Navigator nav, String back) {
        LinearLayout row = new LinearLayout(c); row.setGravity(Gravity.CENTER_VERTICAL);
        TextView b = text(c, "‹", 34, false, PURPLE); b.setGravity(Gravity.CENTER);
        row.addView(b, new LinearLayout.LayoutParams(dp(c,42), dp(c,44)));
        b.setOnClickListener(v -> nav.go(back));
        TextView t = text(c, title, 20, true, TEXT);
        row.addView(t, new LinearLayout.LayoutParams(0, dp(c,44), 1));
        root.addView(row);
    }
    private static void gap(LinearLayout root, Context c, int h) {
        View v = new View(c); root.addView(v, new LinearLayout.LayoutParams(1,dp(c,h)));
    }
    private static void label(LinearLayout root, Context c, String value) {
        TextView t=text(c,value,14,true,TEXT); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(c,38)); p.topMargin=dp(c,8); root.addView(t,p);
    }
    private static void card(LinearLayout root, Context c, String title, String body) {
        LinearLayout box = new LinearLayout(c); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(c,16),dp(c,14),dp(c,16),dp(c,14));
        box.setBackground(c.getDrawable(R.drawable.bg_card));
        TextView h=text(c,title,16,true,TEXT); box.addView(h);
        TextView b=text(c,body,13,false,MUTED); b.setPadding(0,dp(c,7),0,0); box.addView(b);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(c,12); root.addView(box,p);
    }
    private static Button button(Context c, String title, Runnable action, boolean primary) {
        Button b=new Button(c); b.setText(title); b.setAllCaps(false); b.setTextSize(14); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setTextColor(primary?Color.WHITE:TEXT); b.setBackground(c.getDrawable(primary?R.drawable.bg_primary_button:R.drawable.bg_outline_button));
        b.setPadding(dp(c,12),dp(c,4),dp(c,12),dp(c,4)); b.setOnClickListener(v->action.run());
        return b;
    }
    private static void action(LinearLayout root, Context c, String title, String target, Navigator nav) {
        Button b=button(c,title,()->nav.go(target),true); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(c,50)); p.topMargin=dp(c,8); root.addView(b,p);
    }
    private static View login(Context c, Navigator nav) {
        LinearLayout root=page(c); root.setGravity(Gravity.CENTER_HORIZONTAL);
        gap(root,c,95);
        TextView logo=text(c,"🎙",48,true,PURPLE); logo.setGravity(Gravity.CENTER);
        root.addView(logo,new LinearLayout.LayoutParams(-1,dp(c,90)));
        TextView title=text(c,"PitchCoach",32,true,PURPLE); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub=text(c,"Luyện thuyết trình cùng\nhuấn luyện viên AI",16,false,MUTED); sub.setGravity(Gravity.CENTER); root.addView(sub);
        gap(root,c,90);
        action(root,c,"Tiếp tục với Google  →","home",nav);
        action(root,c,"Dùng email","home",nav);
        TextView terms=text(c,"Bằng việc tiếp tục, bạn đồng ý với Điều khoản sử dụng và Chính sách quyền riêng tư.",11,false,MUTED);
        terms.setGravity(Gravity.CENTER); terms.setPadding(dp(c,8),dp(c,20),dp(c,8),0); root.addView(terms);
        return root;
    }
    private static View home(Context c, Navigator nav) {
        LinearLayout root=page(c);
        TextView welcome=text(c,"Thứ Hai, 26 tháng 10\nChào Minh! 👋",24,true,TEXT); root.addView(welcome);
        gap(root,c,16);
        card(root,c,"🔥 2 ngày liên tiếp","Sắp tới · còn 4 ngày");
        card(root,c,"Bảo vệ đồ án Mobile","👥 Giảng viên     ◷ 5 phút");
        action(root,c,"▶  Luyện ngay","detail",nav);
        gap(root,c,8); label(root,c,"Tiến độ của bạn");
        card(root,c,"2 lần luyện          7.1 điểm","↑ 0.8 điểm so với trước");
        label(root,c,"Các buổi khác");
        card(root,c,"Pitch ý tưởng CLB","Bạn bè · 3 phút · Hạn 05/11 · Điểm 6.3");
        action(root,c,"＋  Tạo buổi mới","create",nav);
        action(root,c,"Xem tất cả buổi","all",nav);
        gap(root,c,4); navBar(root,c,nav,"home");
        return root;
    }
    private static View createPresentation(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Buổi thuyết trình mới",nav,"home");
        ScrollView scroll=new ScrollView(c); LinearLayout form=new LinearLayout(c); form.setOrientation(LinearLayout.VERTICAL);
        EditText name=new EditText(c); name.setSingleLine(true); name.setText("Bảo vệ đồ án Mobile"); label(form,c,"Tên buổi"); form.addView(name);
        label(form,c,"Người nghe"); Spinner audience=new Spinner(c);
        audience.setAdapter(new ArrayAdapter<>(c,android.R.layout.simple_spinner_dropdown_item,new String[]{"Giảng viên","Bạn bè","Nhà tuyển dụng","Khác"})); form.addView(audience);
        label(form,c,"Thời lượng"); SeekBar duration=new SeekBar(c); duration.setMax(14); duration.setProgress(4); form.addView(duration);
        label(form,c,"Ngày thuyết trình"); EditText date=new EditText(c); date.setSingleLine(true); date.setText("30/10/2026"); form.addView(date);
        label(form,c,"Dàn ý của bạn"); EditText outline=new EditText(c); outline.setMinLines(6); outline.setGravity(Gravity.TOP); outline.setText("- Giới thiệu đề tài\n- Chức năng chính\n- Công nghệ sử dụng\n- Demo\n- Kết luận"); form.addView(outline);
        scroll.addView(form); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        action(root,c,"✨  Lưu và nhờ AI xem dàn ý","detail",nav); return root;
    }
    private static View detail(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Chi tiết buổi",nav,"home");
        gap(root,c,12); card(root,c,"BẢO VỆ ĐỒ ÁN MOBILE","30 THÁNG 10  ·  Giảng viên  ·  5 phút");
        label(root,c,"Dàn ý đề xuất");
        card(root,c,"1. Giới thiệu đề tài","2. Chức năng chính\n3. Công nghệ sử dụng\n4. Demo\n5. Kết luận");
        label(root,c,"Câu hỏi AI dự đoán");
        card(root,c,"Vì sao chọn MySQL mà không dùng Firestore hết?","Nếu AI trả sai thì hệ thống xử lý thế nào?");
        action(root,c,"🎙  Bắt đầu luyện tập","record",nav);
        action(root,c,"Xem tiến độ chi tiết","progress_detail",nav); return root;
    }
    private static View recording(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Đang luyện tập",nav,"detail");
        gap(root,c,30); TextView status=text(c,"●  ĐANG THU ÂM",13,true,0xffa64f1b); status.setGravity(Gravity.CENTER); root.addView(status);
        TextView mic=text(c,"🎙",76,true,PURPLE); mic.setGravity(Gravity.CENTER); root.addView(mic,new LinearLayout.LayoutParams(-1,dp(c,180)));
        TextView time=text(c,"02:35  /  05:00",22,true,TEXT); time.setGravity(Gravity.CENTER); root.addView(time);
        gap(root,c,20); card(root,c,"✦ Tốc độ hiện tại tốt","Hãy giữ nhịp này. Nói rõ ràng và hướng mắt về người nghe.");
        action(root,c,"Ⅱ  Tạm dừng","record",nav);
        action(root,c,"■  Dừng và phân tích","analyzing",nav); return root;
    }
    private static View analyzing(Context c, Navigator nav) {
        LinearLayout root=page(c); root.setGravity(Gravity.CENTER);
        TextView icon=text(c,"✦",58,true,PURPLE); icon.setGravity(Gravity.CENTER); root.addView(icon);
        TextView title=text(c,"AI đang phân tích bài nói",22,true,TEXT); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub=text(c,"Đang nghe bài nói...\nĐang đối chiếu dàn ý...\nĐang chấm điểm...",14,false,MUTED); sub.setGravity(Gravity.CENTER); root.addView(sub);
        ProgressBar progress=new ProgressBar(c); root.addView(progress);
        root.postDelayed(()->nav.go("result"),2500); return root;
    }
    private static View result(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Kết quả phân tích",nav,"detail");
        TextView score=text(c,"8.0 / 10",38,true,PURPLE); score.setGravity(Gravity.CENTER); root.addView(score);
        TextView caption=text(c,"Bài nói tốt! Bạn đã trình bày rõ ràng.",14,false,MUTED); caption.setGravity(Gravity.CENTER); root.addView(caption);
        gap(root,c,15); card(root,c,"Điểm mạnh","• Giọng nói rõ ràng\n• Bố cục hợp lý\n• Tốc độ nói tốt");
        card(root,c,"Cần cải thiện","• Bỏ từ đệm ở đầu câu\n• Kết luận rõ ràng hơn\n• Bổ sung hướng phát triển");
        action(root,c,"🎙  Luyện lần tiếp theo","record",nav);
        action(root,c,"Thử hội đồng AI","panel",nav); return root;
    }
    private static View panel(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Hội đồng AI",nav,"result");
        card(root,c,"Giảng viên khó tính","Nếu AI trả sai thì hệ thống xử lý thế nào?");
        card(root,c,"Bạn tò mò","Firebase khác MySQL chỗ nào, nói dễ hiểu được không?");
        card(root,c,"Nhà tuyển dụng","Nếu có 10.000 người dùng cùng lúc thì sao?");
        action(root,c,"Trả lời câu hỏi 1","progress",nav); return root;
    }
    private static View progress(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Tiến bộ",nav,"home");
        card(root,c,"Điểm trung bình 7.1/10","↑ 0.8 điểm so với tuần trước");
        card(root,c,"Số buổi luyện 12","Tuần này bạn đã luyện 2 lần");
        label(root,c,"Lịch sử luyện tập");
        card(root,c,"Bảo vệ đồ án Mobile · 8.0","Hôm nay · 5 phút");
        card(root,c,"Pitch ý tưởng CLB · 6.3","20/10 · 3 phút");
        action(root,c,"Xem phân tích chi tiết","progress_detail",nav);
        navBar(root,c,nav,"progress"); return root;
    }
    private static View progressDetail(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Chi tiết tiến bộ",nav,"progress");
        card(root,c,"Điểm số qua các lần luyện","6.2  →  6.8  →  7.1  →  8.0");
        card(root,c,"Đã cải thiện","Từ đệm giảm 78% sau 3 lần luyện.");
        card(root,c,"Mục tiêu tiếp theo","Kết luận rõ ràng hơn, nêu hướng phát triển.");
        action(root,c,"Xem kết quả gần nhất","result",nav);
        action(root,c,"Luyện lại","record",nav); return root;
    }
    private static View profile(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Hồ sơ",nav,"home");
        TextView avatar=text(c,"M",28,true,PURPLE); avatar.setGravity(Gravity.CENTER); root.addView(avatar,new LinearLayout.LayoutParams(-1,dp(c,70)));
        card(root,c,"Minh Nguyễn","minh@example.com");
        card(root,c,"Tài khoản","Thông tin cá nhân · Cài đặt · Thông báo");
        action(root,c,"Đăng xuất","login",nav); navBar(root,c,nav,"profile"); return root;
    }
    private static View allPresentations(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Tất cả buổi",nav,"home");
        card(root,c,"Bảo vệ đồ án Mobile","Giảng viên · 5 phút · 30/10");
        action(root,c,"Mở buổi này","detail",nav);
        card(root,c,"Pitch ý tưởng CLB","Bạn bè · 3 phút · 05/11");
        action(root,c,"Mở buổi này","detail",nav);
        action(root,c,"＋  Tạo buổi mới","create",nav); return root;
    }
    private static View practice(Context c, Navigator nav) {
        LinearLayout root=page(c); heading(root,c,"Luyện tập",nav,"home");
        card(root,c,"LUYỆN TIẾP · Bảo vệ đồ án Mobile","Điểm gần nhất 8.0 · 5 phút");
        action(root,c,"▶  Luyện ngay","record",nav);
        card(root,c,"Luyện nhanh","Bắt đầu ngay");
        action(root,c,"Hội đồng AI","panel",nav);
        action(root,c,"Chọn buổi để luyện","all",nav);
        navBar(root,c,nav,"practice"); return root;
    }
    private static void navBar(LinearLayout root, Context c, Navigator nav, String active) {
        LinearLayout row=new LinearLayout(c); row.setGravity(Gravity.CENTER); row.setPadding(0,dp(c,12),0,0);
        String[][] items={{"⌂","Trang chủ","home"},{"🎙","Luyện tập","practice"},{"▥","Tiến bộ","progress"},{"◉","Hồ sơ","profile"}};
        for(String[] item:items) {
            TextView t=text(c,item[0]+"\n"+item[1],10,active.equals(item[2]),active.equals(item[2])?PURPLE:MUTED);
            t.setGravity(Gravity.CENTER); row.addView(t,new LinearLayout.LayoutParams(0,dp(c,48),1));
            t.setOnClickListener(v->nav.go(item[2]));
        }
        root.addView(row);
    }
}
