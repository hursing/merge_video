package com.example.jump1jump;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Administrator
 */
public final class Utility {
    static Bitmap getAssetBitmap(Context context, String assetName) {
        try {
            InputStream fis = context.getAssets().open("owl.png");
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean copyAssetFileToPath(Context context, String assetName, String targetPath) {
        return copyAssetFileToPath(context, assetName, targetPath, false);
    }

    static boolean copyAssetFileToPath(Context context, String assetName, String targetPath, boolean overwrite) {
        try {
            File target = new File(targetPath);
            if (target.exists()) {
                if (overwrite) {
                    target.delete();
                } else {
                    return false;
                }
            }

            InputStream inputStream = context.getAssets().open(assetName);
            FileOutputStream outputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
