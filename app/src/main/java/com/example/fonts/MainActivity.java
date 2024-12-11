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
import android.widget.Spinner;
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

    private static final String VIET_DIR = "viet";

    private static final String PC_ENGINE_DIR = "PCEngine";

    private static final String FONT_DIR = ".fonts";

    private static final int REQUEST_ID = 123321;

    private static final int KHMER_INDEX = 1;

    private static final int THAI_INDEX = 2;

    private static final int VIET_INDEX = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = findViewById(R.id.spinner);
        Button importBtn = findViewById(R.id.import_btn);
        importBtn.setOnClickListener(v -> {
            Log.i(TAG, "select index" + spinner.getSelectedItemPosition() + ", "
                + spinner.getSelectedItem().toString());
            switch (spinner.getSelectedItemPosition()) {
                case KHMER_INDEX:
                    importFont(KHMER_DIR);
                    break;
                case THAI_INDEX:
                    importFont(THAI_DIR);
                    break;
                case VIET_INDEX:
                    importFont(VIET_DIR);
                    break;
                default:
                    Toast.makeText(this, R.string.need_choose_language, Toast.LENGTH_LONG).show();
                    break;
            }
        });
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
            Toast.makeText(this, R.string.no_file_access_permission_text, Toast.LENGTH_LONG).show();
        }
    }

    private void checkFileAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // R版本以上，需要手动授予所有文件访问权限
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, R.string.no_file_access_permission_text, Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, R.string.no_file_access_permission_text, Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "have no file write permission.");
                Toast.makeText(this, R.string.no_file_access_permission_text, Toast.LENGTH_LONG).show();
                return;
            }
        }

        File pcEngineDir = new File(Environment.getExternalStorageDirectory(), PC_ENGINE_DIR);
        if (!pcEngineDir.exists() && !pcEngineDir.mkdir()) {
            Log.w(TAG, "create pc engine dir failed.");
            Toast.makeText(this, R.string.unknown_fail_text, Toast.LENGTH_LONG).show();
            return;
        }
        File fontDir = new File(pcEngineDir, FONT_DIR);
        if (!fontDir.exists() && !fontDir.mkdir()) {
            Log.w(TAG, "create font dir failed.");
            Toast.makeText(this, R.string.unknown_fail_text, Toast.LENGTH_LONG).show();
            return;
        }
        String[] fileNameArr = null;
        try {
            fileNameArr = getAssets().list(folder);
        } catch (IOException e) {
            Log.w(TAG, "assets file list get error");
            Toast.makeText(this, R.string.unknown_fail_text, Toast.LENGTH_LONG).show();
            return;
        }
        if (fileNameArr == null) {
            Log.w(TAG, "assets file not exist.");
            Toast.makeText(this, R.string.unknown_fail_text, Toast.LENGTH_LONG).show();
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
        Toast.makeText(this, R.string.success_text, Toast.LENGTH_LONG).show();
    }
}