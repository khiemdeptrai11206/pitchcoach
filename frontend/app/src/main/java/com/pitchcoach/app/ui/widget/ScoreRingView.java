package com.pitchcoach.app.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.pitchcoach.app.R;

import java.util.Locale;

public class ScoreRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scoreTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outOfTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private final float strokeWidth;
    private float targetScore = 8f;
    private float animatedScore = 0f;
    private ValueAnimator animator;

    public ScoreRingView(Context context) {
        this(context, null);
    }

    public ScoreRingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokeWidth = context.getResources().getDisplayMetrics().density * 9f;
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(context.getColor(R.color.track));
        valuePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setStrokeWidth(strokeWidth);
        valuePaint.setStrokeCap(Paint.Cap.ROUND);
        valuePaint.setColor(context.getColor(R.color.primary));
        scoreTextPaint.setColor(context.getColor(R.color.text_primary));
        scoreTextPaint.setTextSize(sp(31));
        scoreTextPaint.setTextAlign(Paint.Align.LEFT);
        outOfTextPaint.setColor(context.getColor(R.color.text_muted));
        outOfTextPaint.setTextSize(sp(11));
        outOfTextPaint.setTextAlign(Paint.Align.LEFT);
        Typeface bold = ResourcesCompat.getFont(context, R.font.inter_bold);
        Typeface regular = ResourcesCompat.getFont(context, R.font.inter_regular);
        if (bold != null) {
            scoreTextPaint.setTypeface(bold);
        }
        if (regular != null) {
            outOfTextPaint.setTypeface(regular);
        }
    }

    public void setScore(float score) {
        targetScore = Math.max(0f, Math.min(10f, score));
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, targetScore);
        animator.setDuration(1200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            animatedScore = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void setScoreImmediately(float score) {
        targetScore = score;
        animatedScore = score;
        invalidate();
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f - strokeWidth;
        float cx = width / 2f;
        float cy = height / 2f;
        arcBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawArc(arcBounds, -90f, 360f, false, trackPaint);
        canvas.drawArc(arcBounds, -90f, 360f * (animatedScore / 10f), false, valuePaint);

        String score = String.format(Locale.US, "%.1f", animatedScore);
        String outOf = getContext().getString(R.string.result_score_out_of);
        float scoreWidth = scoreTextPaint.measureText(score);
        float outWidth = outOfTextPaint.measureText(outOf);
        float totalWidth = scoreWidth + dp(3) + outWidth;
        float startX = cx - totalWidth / 2f;
        Paint.FontMetrics metrics = scoreTextPaint.getFontMetrics();
        float baseline = cy - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(score, startX, baseline, scoreTextPaint);
        Paint.FontMetrics outMetrics = outOfTextPaint.getFontMetrics();
        float outBaseline = cy - (outMetrics.ascent + outMetrics.descent) / 2f + dp(6);
        canvas.drawText(outOf, startX + scoreWidth + dp(3), outBaseline, outOfTextPaint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }
}
