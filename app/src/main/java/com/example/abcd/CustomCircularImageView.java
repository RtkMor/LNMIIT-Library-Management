package com.example.abcd;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class CustomCircularImageView extends AppCompatImageView {

    private final Path clipPath = new Path();
    private final RectF rect = new RectF();

    public CustomCircularImageView(Context context) {
        super(context);
    }

    public CustomCircularImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCircularImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Clip the image to a circular shape
        clipPath.reset();
        rect.set(0, 0, getWidth(), getHeight());
        clipPath.addCircle(rect.centerX(), rect.centerY(), Math.min(rect.width(), rect.height()) / 2, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}