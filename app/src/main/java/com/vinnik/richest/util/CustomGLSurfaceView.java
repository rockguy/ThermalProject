package com.vinnik.richest.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

public class CustomGLSurfaceView extends GLSurfaceView {
    public CustomGLSurfaceView(Context context) {
        super(context);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        paint.setARGB(1,255,255,255);
        canvas.drawRect(
                (float)(40),
                (float)(40),
                (float)(400),
                (float)(200),
                paint);

        holder.getSurface().unlockCanvasAndPost(canvas);
        super.surfaceChanged(holder, format, w, h);
    }
}
