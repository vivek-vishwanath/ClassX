package com.example.classx;


import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class DrawableBackground extends Drawable {

    private final Paint paint;
    private int rounded = 8;

    public DrawableBackground() {
        // Set up color and text size
        paint = new Paint();
    }
    public DrawableBackground(int r, int g, int b) {
        this();
        paint.setARGB(255, r, g, b);
    }
    public DrawableBackground(int r, int g, int b, int rounded) {
        this();
        paint.setARGB(255, r, g, b);
        this.rounded = rounded;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void draw(Canvas canvas) {
        // Get the drawable's bounds
        int width = getBounds().width();
        int height = getBounds().height();
        Log.wtf("(MyDrawable.java:33)", "Width = " + width + ", Height = " + height);

        canvas.drawRoundRect(0, 0, width, height, rounded, rounded, paint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}