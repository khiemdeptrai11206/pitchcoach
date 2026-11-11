package com.pitchcoach.app.view.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.pitchcoach.app.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class LineChartView extends View {

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint zonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint zoneLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final DecimalFormat valueFormat =
            new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US));
    private float[] values = new float[0];
    private String[] labels = new String[0];
    private float minValue = 0f;
    private float maxValue = 10f;
    private boolean showTargetZone = false;
    private float zoneMin = 120f;
    private float zoneMax = 150f;
    private int lineColor = Color.parseColor("#5B4BDB");
    private float revealProgress = 1f;
    private ValueAnimator animator;

    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setColor(context.getColor(R.color.chart_grid));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2.5f));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(lineColor);
        labelPaint.setColor(context.getColor(R.color.text_primary));
        labelPaint.setTextSize(sp(9));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        axisPaint.setColor(context.getColor(R.color.text_muted));
        axisPaint.setTextSize(sp(9));
        axisPaint.setTextAlign(Paint.Align.CENTER);
        zonePaint.setStyle(Paint.Style.FILL);
        zonePaint.setColor(context.getColor(R.color.zone_green));
        zoneLabelPaint.setColor(context.getColor(R.color.zone_text));
        zoneLabelPaint.setTextSize(sp(8));
        zoneLabelPaint.setTextAlign(Paint.Align.RIGHT);
        Typeface bold = ResourcesCompat.getFont(context, R.font.inter_bold);
        Typeface regular = ResourcesCompat.getFont(context, R.font.inter_regular);
        if (bold != null) {
            labelPaint.setTypeface(bold);
        }
        if (regular != null) {
            axisPaint.setTypeface(regular);
            zoneLabelPaint.setTypeface(regular);
        }
    }

    public void setData(float[] values, String[] labels, int color, float minValue,
                        float maxValue, boolean showTargetZone) {
        this.values = values;
        this.labels = labels;
        this.lineColor = color;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.showTargetZone = showTargetZone;
        linePaint.setColor(color);
        pointPaint.setColor(color);
        startReveal();
    }

    public void setTargetZone(float zoneMin, float zoneMax) {
        this.zoneMin = zoneMin;
        this.zoneMax = zoneMax;
        invalidate();
    }

    private void startReveal() {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(650);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            revealProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.length == 0) {
            return;
        }
        float left = dp(30);
        float right = getWidth() - dp(18);
        float top = dp(24);
        float bottom = getHeight() - dp(26);
        float chartWidth = right - left;
        float chartHeight = bottom - top;
        float range = Math.max(0.01f, maxValue - minValue);

        if (showTargetZone) {
            float zoneTop = valueToY(Math.min(zoneMax, maxValue), top, chartHeight, range);
            float zoneBottom = valueToY(Math.max(zoneMin, minValue), top, chartHeight, range);
            RectF zoneRect = new RectF(left, zoneTop, right, zoneBottom);
            canvas.drawRoundRect(zoneRect, dp(7), dp(7), zonePaint);
            canvas.drawText(getContext().getString(R.string.chart_zone_label),
                    right - dp(4), zoneTop + dp(11), zoneLabelPaint);
        }

        for (int line = 0; line < 3; line++) {
            float y = top + chartHeight * line / 2f;
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        linePath.reset();
        float[] xs = new float[values.length];
        float[] ys = new float[values.length];
        for (int index = 0; index < values.length; index++) {
            xs[index] = left + chartWidth * index / Math.max(1, values.length - 1);
            ys[index] = valueToY(values[index], top, chartHeight, range);
            if (index == 0) {
                linePath.moveTo(xs[index], ys[index]);
            } else {
                linePath.lineTo(xs[index], ys[index]);
            }
        }

        float revealX = left + chartWidth * revealProgress;
        int save = canvas.save();
        canvas.clipRect(left - dp(4), 0, revealX + dp(4), getHeight());
        canvas.drawPath(linePath, linePaint);
        for (int index = 0; index < values.length; index++) {
            if (xs[index] > revealX + dp(2)) {
                continue;
            }
            canvas.drawCircle(xs[index], ys[index], dp(5), pointPaint);
            Paint pointOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
            pointOutline.setStyle(Paint.Style.STROKE);
            pointOutline.setStrokeWidth(dp(2));
            pointOutline.setColor(Color.WHITE);
            canvas.drawCircle(xs[index], ys[index], dp(5), pointOutline);
            canvas.drawText(valueFormat.format(values[index]),
                    xs[index], ys[index] - dp(10), labelPaint);
        }
        canvas.restoreToCount(save);

        if (labels.length == values.length) {
            for (int index = 0; index < labels.length; index++) {
                canvas.drawText(labels[index], xs[index], getHeight() - dp(7), axisPaint);
            }
        }
    }

    private float valueToY(float value, float top, float chartHeight, float range) {
        return top + (maxValue - value) / range * chartHeight;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }
}
