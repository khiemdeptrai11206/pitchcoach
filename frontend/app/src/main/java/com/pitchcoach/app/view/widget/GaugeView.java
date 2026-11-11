package com.pitchcoach.app.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.pitchcoach.app.R;

public class GaugeView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private float speed = 140f;
    private final float gaugeMin = 60f;
    private final float gaugeMax = 220f;

    public GaugeView(Context context) {
        this(context, null);
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float stroke = dp(9);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setColor(context.getColor(R.color.track));
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeWidth(stroke);
        targetPaint.setColor(context.getColor(R.color.green));
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setStrokeWidth(dp(4));
        needlePaint.setStrokeCap(Paint.Cap.ROUND);
        needlePaint.setColor(context.getColor(R.color.text_primary));
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        invalidate();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float stroke = dp(9);
        float cx = getWidth() / 2f;
        float radius = Math.min(getWidth() / 2f, getHeight()) - stroke;
        arcBounds.set(cx - radius, getHeight() - radius - stroke / 2f,
                cx + radius, getHeight() + radius - stroke / 2f);
        canvas.drawArc(arcBounds, 180f, 180f, false, trackPaint);
        float targetStart = 180f + (120f - gaugeMin) / (gaugeMax - gaugeMin) * 180f;
        float targetSweep = (150f - 120f) / (gaugeMax - gaugeMin) * 180f;
        canvas.drawArc(arcBounds, targetStart, targetSweep, false, targetPaint);

        float angle = 180f + (Math.max(gaugeMin, Math.min(gaugeMax, speed)) - gaugeMin)
                / (gaugeMax - gaugeMin) * 180f;
        double radians = Math.toRadians(angle);
        float needleLength = radius - dp(4);
        float needleStartX = cx;
        float needleStartY = getHeight() - stroke / 2f;
        float needleEndX = cx + (float) (Math.cos(radians) * needleLength);
        float needleEndY = getHeight() - stroke / 2f + (float) (Math.sin(radians) * needleLength);
        needleEndY = Math.min(needleEndY, getHeight() - dp(2));
        canvas.drawLine(needleStartX, needleStartY, needleEndX, needleEndY, needlePaint);
        canvas.drawCircle(needleStartX, needleStartY, dp(2), needlePaint);
    }
}
