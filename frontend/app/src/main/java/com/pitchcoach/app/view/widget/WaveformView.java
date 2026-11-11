package com.pitchcoach.app.view.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class WaveformView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int barCount = 21;
    private int barColor = 0xFF5B4BDB;
    private float phase = 0f;
    private boolean paused = false;
    private boolean attached = false;
    private ValueAnimator animator;

    public WaveformView(Context context) {
        this(context, null);
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveformView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        barPaint.setColor(barColor);
        barPaint.setStyle(Paint.Style.FILL);
    }

    public void setBarCount(int barCount) {
        this.barCount = barCount;
        invalidate();
    }

    public void setBarColor(int color) {
        this.barColor = color;
        barPaint.setColor(color);
        invalidate();
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            stopAnimator();
        } else if (attached) {
            startAnimator();
        }
        invalidate();
    }

    public boolean isPaused() {
        return paused;
    }

    private void startAnimator() {
        stopAnimator();
        animator = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2));
        animator.setDuration(1050);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        animator.start();
    }

    private void stopAnimator() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (!paused) {
            startAnimator();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        stopAnimator();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float barWidth = 3f * density;
        float gap = 4f * density;
        float totalWidth = barCount * barWidth + (barCount - 1) * gap;
        float startX = (getWidth() - totalWidth) / 2f;
        float centerY = getHeight() / 2f;
        float maxBarHeight = Math.min(getHeight() * 0.72f, 36f * density);
        float minBarHeight = 15f * density;
        barPaint.setColor(barColor);
        barPaint.setAlpha(paused ? 90 : 255);
        for (int index = 0; index < barCount; index++) {
            double wave = Math.abs(Math.sin(phase + index * 0.62));
            float scale = paused ? 0.45f : (0.25f + 0.75f * (float) wave);
            float height = minBarHeight + (maxBarHeight - minBarHeight) * scale;
            float x = startX + index * (barWidth + gap);
            canvas.drawRoundRect(x, centerY - height / 2f, x + barWidth,
                    centerY + height / 2f, barWidth / 2f, barWidth / 2f, barPaint);
        }
    }
}
