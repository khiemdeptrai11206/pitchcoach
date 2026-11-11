package com.pitchcoach.app.view.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.pitchcoach.app.R;

public class AnalysisRingView extends View {

    private final Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outerTracePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerTracePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF outerBounds = new RectF();
    private final RectF innerBounds = new RectF();
    private float rotation = 0f;
    private ValueAnimator animator;

    public AnalysisRingView(Context context) {
        this(context, null);
    }

    public AnalysisRingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalysisRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float stroke = dp(3);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setStrokeWidth(stroke);
        outerPaint.setStrokeCap(Paint.Cap.ROUND);
        outerPaint.setColor(context.getColor(R.color.primary));
        outerTracePaint.setStyle(Paint.Style.STROKE);
        outerTracePaint.setStrokeWidth(stroke);
        outerTracePaint.setColor(0x335B4BDB);

        innerPaint.setStyle(Paint.Style.STROKE);
        innerPaint.setStrokeWidth(stroke);
        innerPaint.setStrokeCap(Paint.Cap.ROUND);
        innerPaint.setColor(context.getColor(R.color.accent));
        innerTracePaint.setStyle(Paint.Style.STROKE);
        innerTracePaint.setStrokeWidth(stroke);
        innerTracePaint.setColor(0x33FF8A3D);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(1600);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            rotation = (float) animation.getAnimatedValue();
            postInvalidateOnAnimation();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float outerInset = dp(9);
        float innerInset = dp(40);
        outerBounds.set(outerInset, outerInset, getWidth() - outerInset, getHeight() - outerInset);
        innerBounds.set(innerInset, innerInset, getWidth() - innerInset, getHeight() - innerInset);

        canvas.save();
        canvas.rotate(-90 + rotation, getWidth() / 2f, getHeight() / 2f);
        canvas.drawArc(outerBounds, 0f, 360f, false, outerTracePaint);
        canvas.drawArc(outerBounds, 0f, 290f, false, outerPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(-90 - rotation * 1.35f, getWidth() / 2f, getHeight() / 2f);
        canvas.drawArc(innerBounds, 0f, 360f, false, innerTracePaint);
        canvas.drawArc(innerBounds, 35f, 230f, false, innerPaint);
        canvas.restore();
    }
}
