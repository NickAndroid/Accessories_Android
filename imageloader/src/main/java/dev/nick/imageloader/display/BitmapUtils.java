/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package dev.nick.imageloader.display;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * {@link Bitmap} specific helpers.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public final class BitmapUtils {

    /* Initial blur radius. */
    private static final int DEFAULT_RADIUS = 8;

    /**
     * This class is never instantiated
     */
    private BitmapUtils() {
    }

    /**
     * Takes a bitmap and creates a new slightly blurry version of it.
     *
     * @param sentBitmap The {@link Bitmap} to blur.
     * @return A blurred version of the given {@link Bitmap}.
     */
    public static Bitmap createBlurredBitmap(final Bitmap sentBitmap) {
        return createBlurredBitmap(sentBitmap, DEFAULT_RADIUS);
    }

    /**
     * Takes a bitmap and creates a new slightly blurry version of it.
     *
     * @param sentBitmap The {@link Bitmap} to blur.
     * @param radius     Radius of the blur effect.
     * @return A blurred version of the given {@link Bitmap}.
     */
    public static Bitmap createBlurredBitmap(final Bitmap sentBitmap, int radius) {
        if (sentBitmap == null) {
            return null;
        }

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        final Bitmap mBitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        final int w = mBitmap.getWidth();
        final int h = mBitmap.getHeight();

        final int[] pix = new int[w * h];
        mBitmap.getPixels(pix, 0, w, 0, 0, w, h);

        final int wm = w - 1;
        final int hm = h - 1;
        final int wh = w * h;
        final int div = radius + radius + 1;

        final int r[] = new int[wh];
        final int g[] = new int[wh];
        final int b[] = new int[wh];
        final int vmin[] = new int[Math.max(w, h)];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;

        int divsum = div + 1 >> 1;
        divsum *= divsum;
        final int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = i / divsum;
        }

        yw = yi = 0;

        final int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        final int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = p & 0x0000ff;
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = p & 0x0000ff;

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = 0xff000000 | dv[rsum] << 16 | dv[gsum] << 8 | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        mBitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return mBitmap;
    }

    /**
     * This is only used when the launcher shortcut is created.
     *
     * @param bitmap The artist, album, genre, or playlist image that's going to
     *               be cropped.
     * @param size   The new size.
     * @return A {@link Bitmap} that has been resized and cropped for a launcher
     * shortcut.
     */
    public static final Bitmap resizeAndCropCenter(final Bitmap bitmap, final int size) {
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();
        if (w == size && h == size) {
            return bitmap;
        }

        final float mScale = (float) size / Math.min(w, h);

        final Bitmap mTarget = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        final int mWidth = Math.round(mScale * bitmap.getWidth());
        final int mHeight = Math.round(mScale * bitmap.getHeight());
        final Canvas mCanvas = new Canvas(mTarget);
        mCanvas.translate((size - mWidth) / 2f, (size - mHeight) / 2f);
        mCanvas.scale(mScale, mScale);
        final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        mCanvas.drawBitmap(bitmap, 0, 0, paint);
        return mTarget;
    }

    public static final Bitmap blackAndWhited(Bitmap in) {
        int width = in.getWidth();
        int height = in.getHeight();

        int[] pixels = new int[width * height];
        in.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBmp;
    }

    public static Bitmap roundCorner(Bitmap in, float roundPx) {
        Bitmap newBmp = Bitmap.createBitmap(in.getWidth(), in.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBmp);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, in.getWidth(), in.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(in, rect, rect, paint);
        return newBmp;
    }
}
