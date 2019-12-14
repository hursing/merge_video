package com.example.jump1jump;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import VideoHandle.EpEditor;
import VideoHandle.OnEditorListener;

public final class SnapGenerator {


    public interface OnFinishedListener {
        void onFinished(boolean success);
    }

    public static final int ANIMATION_FLOAT = 0;
    public static final int ANIMATION_SWING = 1;
    public static final int ANIMATION_SCALE = 2;
    public static final int ANIMATION_FLICKER = 3;
    public static final int ANIMATION_BOUNCE = 4;
    public static final int ANIMATION_FLYOFF = 5;
    public static final int ANIMATION_ROTATE = 6;
    public static final int ANIMATION_FLIP = 7;

    public static class BitmapInfo {
        Bitmap bitmap;
        Rect targetRect;
        int animationType;
    }

    public static void generate(final String bgVideoPath, final BitmapInfo bitmapInfo, final OnFinishedListener listener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String tmpPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp/";

                int frameCount = 0;
                switch (bitmapInfo.animationType) {
                    case ANIMATION_SWING:
                        frameCount = generateSwingAnimation(tmpPath, bitmapInfo);
                        break;
                    case ANIMATION_SCALE:
                        frameCount = generateScaleAnimation(tmpPath, bitmapInfo);
                        break;
                    case ANIMATION_FLICKER:
                        frameCount = generateFlickerAnimation(tmpPath, bitmapInfo);
                        break;
                    case ANIMATION_BOUNCE:
                        frameCount = generateBounceAnimation(tmpPath, bitmapInfo);
                        break;
                    case ANIMATION_ROTATE:
                        frameCount = generateRotateAnimation(tmpPath, bitmapInfo);
                        break;
                    case ANIMATION_FLIP:
                        frameCount = generateFlipAnimation(tmpPath, bitmapInfo);
                        break;
                    case ANIMATION_FLOAT:
                    default:
                        frameCount = generateFloatAnimation(tmpPath, bitmapInfo);
                        break;
                }
                if (frameCount > 0) {
                    mergeVideoAndBitmaps(tmpPath, bgVideoPath, frameCount, bitmapInfo.targetRect, listener);
                } else {
                    listener.onFinished(false);
                }
            }
        };
        new Thread(runnable).start();
    }

    // 漂浮动画。1秒。在原图高度的110%范围内做上下移动。
    private static int generateFloatAnimation(String tmpPath, BitmapInfo bitmapInfo) {
        Bitmap raw = bitmapInfo.bitmap;
        int animationArea = (int)(raw.getHeight() * 0.1f);
        Canvas canvas = new Canvas();
        for (int i = 0; i < 30; ++i) {
            Bitmap bmp = Bitmap.createBitmap(raw.getWidth(), raw.getHeight() + animationArea, raw.getConfig());
            canvas.setBitmap(bmp);
            float top;
            if (i < 15) {
                top = animationArea * (1 - i / 15.0f);
            } else {
                top = animationArea * (i - 15) / 15.0f;
            }
            canvas.drawBitmap(raw, 0, top, null);
            if (!saveBitmap(bmp, tmpPath + i + ".png")) {
                return 0;
            }
        }
        return 30;
    }

    // 摇摆动画，1秒，以底边中点为圆心，旋转正负10度
    private static int generateSwingAnimation(String tmpPath, BitmapInfo bitmapInfo) {
        Bitmap raw = bitmapInfo.bitmap;
        int animationArea = (int)(raw.getWidth() * 0.2f);
        Canvas canvas = new Canvas();
        for (int i = 0; i < 30; ++i) {
            Bitmap bmp = Bitmap.createBitmap(raw.getWidth() + animationArea, raw.getHeight() + animationArea, raw.getConfig());
            canvas.setBitmap(bmp);
            canvas.save();
            float degrees;
            if (i < 15) {
                degrees = -10 + 20 * (1 - i / 15f);
            } else {
                degrees = -10 + 20 *  (i - 15) / 15.0f;
            }
            canvas.rotate(degrees, canvas.getWidth() / 2, raw.getHeight() + animationArea / 2);
            canvas.drawBitmap(raw, animationArea / 2, animationArea / 2, null);
            canvas.restore();
            if (!saveBitmap(bmp, tmpPath + i + ".png")) {
                return 0;
            }
        }
        return 30;
    }

    private static int generateScaleAnimation(String tmpPath, BitmapInfo bitmapInfo) {
        Bitmap raw = bitmapInfo.bitmap;
        int animationArea = (int)(raw.getWidth() * 0.1f);
        Canvas canvas = new Canvas();
        for (int i = 0; i < 30; ++i) {
            Bitmap bmp = Bitmap.createBitmap(raw.getWidth() + animationArea, raw.getHeight() + animationArea, raw.getConfig());
            canvas.setBitmap(bmp);
            canvas.save();
            float scale;
            if (i < 15) {
                scale = 0.9f + 0.2f * (1 - i / 15f);
            } else {
                scale = 0.9f + 0.2f *  (i - 15) / 15.0f;
            }
            canvas.scale(scale, scale, canvas.getWidth() / 2, canvas.getHeight());
            canvas.drawBitmap(raw, animationArea / 2, animationArea / 2, null);
            canvas.restore();
            if (!saveBitmap(bmp, tmpPath + i + ".png")) {
                return 0;
            }
        }
        return 30;
    }

    private static int generateFlickerAnimation(String tmpPath, BitmapInfo bitmapInfo) {
        Paint paint = new Paint();
        Bitmap raw = bitmapInfo.bitmap;
        Canvas canvas = new Canvas();
        for (int i = 0; i < 30; ++i) {
            Bitmap bmp = Bitmap.createBitmap(raw.getWidth(), raw.getHeight(), raw.getConfig());
            canvas.setBitmap(bmp);
            int alpha;
            if (i < 15) {
                alpha = (int)(255 * (1f - i / 15f));
            } else {
                alpha = (int)(255 * (i - 15) / 15f);
            }
            paint.setAlpha(alpha);
            canvas.drawBitmap(raw, 0, 0, paint);
            if (!saveBitmap(bmp, tmpPath + i + ".png")) {
                return 0;
            }
        }
        return 30;
    }

    /**
     * 弹跳动画：前15帧缩小，15~30帧变回原状，30-45帧向上位移，45-60帧恢复原状，60-90帧不动
     * @param tmpPath
     * @param bitmapInfo
     * @return
     */
    private static int generateBounceAnimation(String tmpPath, BitmapInfo bitmapInfo) {
        Bitmap raw = bitmapInfo.bitmap;
        int animationArea = (int)(raw.getHeight() * 0.5f);
        Canvas canvas = new Canvas();
        for (int i = 0; i < 90; ++i) {
            Bitmap bmp = Bitmap.createBitmap(raw.getWidth(), raw.getHeight() + animationArea, raw.getConfig());
            canvas.setBitmap(bmp);
            if (i > 60) {
                canvas.drawBitmap(raw, 0, animationArea, null);
            } else if (i > 45) {
                float top = animationArea * (1 - ((60 - i) / 15f));
                canvas.drawBitmap(raw,0, top, null);
            } else if (i > 30) {
                float top = animationArea * ((45 - i) / 15f);
                canvas.drawBitmap(raw, 0, top, null);
            } else if (i > 15) {
                canvas.save();
                float scale = 0.8f + 0.2f * ((i - 15) / 15f);
                canvas.scale(1, scale, canvas.getWidth() / 2, canvas.getHeight());
                canvas.drawBitmap(raw, 0, animationArea, null);
                canvas.restore();
            } else {
                canvas.save();
                float scale = 0.8f + 0.2f * (1 - i / 15f);
                canvas.scale(1, scale, canvas.getWidth() / 2, canvas.getHeight());
                canvas.drawBitmap(raw, 0, animationArea, null);
                canvas.restore();
            }
            if (!saveBitmap(bmp, tmpPath + i + ".png")) {
                return 0;
            }
        }
        return 90;
    }

    private static int generateRotateAnimation(String tmpPath, BitmapInfo bitmapInfo) {
        Bitmap raw = bitmapInfo.bitmap;
        Canvas canvas = new Canvas();
        for (int i = 0; i < 60; ++i) {
            Bitmap bmp = Bitmap.createBitmap(raw.getWidth() * 120 / 100, raw.getHeight() * 120 / 100, raw.getConfig());
            canvas.setBitmap(bmp);
            canvas.save();
            canvas.rotate(360 * i / 60, canvas.getWidth() / 2, canvas.getHeight() / 2);
            canvas.drawBitmap(raw, raw.getWidth() * 10 / 100, raw.getHeight() * 10 / 100, null);
            canvas.restore();
            if (!saveBitmap(bmp, tmpPath + i + ".png")) {
                return 0;
            }
        }
        return 60;
    }

    private static int generateFlipAnimation(String tmpPath, BitmapInfo bitmapInfo) {
        Bitmap raw = bitmapInfo.bitmap;
        Camera camera = new Camera();
        Matrix matrix = new Matrix();
        for (int i = 0; i < 60; ++i) {
            Bitmap bmp;
            if (i == 15 || i == 45) {
                bmp = Bitmap.createBitmap(raw.getWidth(), raw.getHeight(), raw.getConfig());
            } else {
                camera.save();
                camera.translate(raw.getWidth() / 2, raw.getHeight() / 2, 0);
                camera.rotateY(360 * i / 60);
                camera.getMatrix(matrix);
                bmp = Bitmap.createBitmap(raw, 0, 0, raw.getWidth(), raw.getHeight(), matrix, true);
                camera.restore();
            }
            if (!saveBitmap(bmp, tmpPath + i + ".png")) {
                return 0;
            }
        }
        return 60;
    }

    private static void mergeVideoAndBitmaps(String tmpPath,
                                             String bgVideoPath,
                                             int imgFrameCount,
                                             Rect targetRect,
                                             final OnFinishedListener listener) {
        int duration = getLocalVideoDuration(bgVideoPath);
        float seconds = duration / 1000f;
        float loop = seconds / (imgFrameCount / 30f) - 1;
        if (targetRect == null || targetRect.isEmpty()) {
            targetRect = new Rect(200, 200, 400, 400);
        }
        String cmd = String.format(Locale.US,
                "-i %s -i %s -filter_complex [1]scale=%d:%d[pip];[pip]loop=loop=%.3f:size=%d[p2p];[0][p2p]overlay=%d:%d -preset ultrafast -y -t %.3f %s",
                bgVideoPath,
                tmpPath + "%d.png",
                targetRect.width(),
                targetRect.height(),
                loop,
                targetRect.left,
                targetRect.top,
                imgFrameCount,
                seconds,
                tmpPath + "done.mp4");
        EpEditor.execCmd(cmd, duration, new OnEditorListener() {
            @Override
            public void onSuccess() {
                listener.onFinished(true);
            }

            @Override
            public void onFailure() {
                listener.onFinished(false);
            }

            @Override
            public void onProgress(float progress) {

            }
        });
    }

    private static boolean saveBitmap(Bitmap bmp, String filename) {
        try {
            FileOutputStream outputStream = new FileOutputStream(filename);
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(outputStream);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bufferOutStream);
            bufferOutStream.flush();
            bufferOutStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static int getLocalVideoDuration(String videoPath) {
        int duration;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            duration = Integer.parseInt(mmr.extractMetadata
                    (MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return duration;
    }
}
