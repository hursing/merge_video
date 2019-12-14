package com.example.jump1jump;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String mTmpPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.ani_float).setOnClickListener(this);
        findViewById(R.id.swing).setOnClickListener(this);
        findViewById(R.id.scale).setOnClickListener(this);
        findViewById(R.id.open_video).setOnClickListener(this);
        findViewById(R.id.flicker).setOnClickListener(this);
        findViewById(R.id.bounce).setOnClickListener(this);
        findViewById(R.id.flyoff).setOnClickListener(this);
        findViewById(R.id.rotate).setOnClickListener(this);
        findViewById(R.id.flip).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.open_video) {
            playVideo();
            return;
        }

        SnapGenerator.BitmapInfo bitmapInfo = new SnapGenerator.BitmapInfo();
        switch (v.getId()) {
            case R.id.ani_float:
                bitmapInfo.animationType = SnapGenerator.ANIMATION_FLOAT;
                break;
            case R.id.swing:
                bitmapInfo.animationType = SnapGenerator.ANIMATION_SWING;
                break;
            case R.id.scale:
                bitmapInfo.animationType = SnapGenerator.ANIMATION_SCALE;
                break;
            case R.id.flicker:
                bitmapInfo.animationType = SnapGenerator.ANIMATION_FLICKER;
                break;
            case R.id.bounce:
                bitmapInfo.animationType = SnapGenerator.ANIMATION_BOUNCE;
                break;
            case R.id.flyoff:
                bitmapInfo.animationType = SnapGenerator.ANIMATION_FLYOFF;
                break;
            case R.id.rotate:
                bitmapInfo.animationType = SnapGenerator.ANIMATION_ROTATE;
                break;
            case R.id.flip:
                bitmapInfo.animationType = SnapGenerator.ANIMATION_FLIP;
                break;
            default:
                break;
        }
        Toast.makeText(MainActivity.this, "start " + bitmapInfo.animationType, Toast.LENGTH_SHORT).show();

        try {
            FileInputStream fis = new FileInputStream(mTmpPath + "fairy.png");
            Bitmap bitmap  = BitmapFactory.decodeStream(fis);
            fis.close();
            bitmapInfo.bitmap = bitmap;
            SnapGenerator.generate(mTmpPath + "bg.mp4", bitmapInfo, new SnapGenerator.OnFinishedListener() {
                @Override
                public void onFinished(final boolean success) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "result " + success, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playVideo() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String path = mTmpPath + "done.mp4";
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(this, "com.example.jump1jump.fileprovider", file);
        }
        intent.setDataAndType(uri, "video/*");
        startActivity(intent);
    }
}
