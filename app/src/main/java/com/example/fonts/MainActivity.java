package com.example.fonts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 业务功能主界面
 *
 * @since 2024-12-4
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Fonts";

    private static final String KHMER_DIR = "khmer";

    private static final String THAI_DIR = "thai";

    private static final String PC_ENGINE_FONT_DIR = "PCEngine/.fonts";

    private static final int REQUEST_ID = 123321;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button khmerBtn = findViewById(R.id.khmer_btn);
        Button thaiBtn = findViewById(R.id.tai_btn);
        khmerBtn.setOnClickListener(v -> importFont(KHMER_DIR));
        thaiBtn.setOnClickListener(v -> importFont(THAI_DIR));
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkFileAccessPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.no_file_write_permission_text, Toast.LENGTH_LONG).show();
        }
    }

    private void checkFileAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // R版本以上，需要手动授予所有文件访问权限
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, R.string.no_all_file_access_permission_text, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            // R版本以下，申请外部存储读写权限
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ID);
            }
        }
    }

    private void importFont(String folder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Log.w(TAG, "have no all file access permission.");
                Toast.makeText(this, R.string.no_all_file_access_permission_text, Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "have no file write permission.");
                Toast.makeText(this, R.string.no_file_write_permission_text, Toast.LENGTH_LONG).show();
                return;
            }
        }

        File fontDir = new File(Environment.getExternalStorageDirectory(), PC_ENGINE_FONT_DIR);
        if (!fontDir.exists() && !fontDir.mkdir()) {
            Log.w(TAG, "create font dir failed.");
            return;
        }
        String[] fileNameArr = null;
        try {
            fileNameArr = getAssets().list(folder);
        } catch (IOException e) {
            Log.w(TAG, "assets file list get error");
            return;
        }
        if (fileNameArr == null) {
            Log.w(TAG, "assets file not exist.");
            return;
        }
        for (String fileName : fileNameArr) {
            try (InputStream inputStream = getAssets().open(folder + File.separator + fileName);
                 FileOutputStream fos = new FileOutputStream(new File(fontDir, fileName));) {
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                Log.e(TAG, "copy file failed.");
            }
        }
    }
}