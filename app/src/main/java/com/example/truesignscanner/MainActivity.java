package com.example.truesignscanner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collections;

public class MainActivity extends BasicsActivity {

    private final long delayTimeMillis = 2000;
    private long lastDecodeTimeMillis = 0;
    private CodeScanner mCodeScanner;
    private CodeScannerView camPreview;
    private FloatingActionButton btnGoToReviewPackage;

    private void createCodeScanner(){

        if(mCodeScanner != null)
            return;

        Log.d("CreateCS","Starting create code scanner");
        mCodeScanner = new CodeScanner(MainActivity.this, camPreview,0);
        mCodeScanner.setFormats(Collections.singletonList(BarcodeFormat.DATA_MATRIX));
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                if (System.currentTimeMillis() - lastDecodeTimeMillis > delayTimeMillis)
                {

                    Log.d("onDecode","attempt to write data");
                    runOnUiThread(()->packManager.writeInWritingPackage(result.getText()));
                    // Измененеие цвета рамки
                    Log.d("onDecode","light indication");
                    new Thread(){
                        @Override
                        public synchronized void start() {
                            super.start();
                        }

                        @Override
                        public void run() {
                            runOnUiThread(() -> camPreview.setFrameColor(Color.GREEN));
                            try {
                                sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(() -> camPreview.setFrameColor(Color.parseColor("#FFEB3B")));
                            super.run();
                        }
                    }.start();

                    lastDecodeTimeMillis = System.currentTimeMillis();
                    Log.d("onDecode","recognize time in millis: " + lastDecodeTimeMillis);
                }else{
                    Log.d("onDecode","delay time.Start preview");
                }
                runOnUiThread(() -> mCodeScanner.startPreview());
            }
        });
    }

    private void createBtnGoToReviewPackage(){
        btnGoToReviewPackage.setOnClickListener(v -> {
            startActivity(new Intent(this,ReviewPackages.class)); });
    }

    private void createCameraPreview(){
        if(PermissionManager.checkCameraPermission(this)){
            createCodeScanner();
        }else{
            PermissionManager.requestCameraPermission(this, () -> {
                createCodeScanner();
                return false;
            });
        }
    }

    private void runPackManager(){
        if (!packManager.isAlive()){
            packManager.setCurrentActivity(MainActivity.this);
            if(PermissionManager.checkReadStoragePermission(this)
                    && PermissionManager.checkWriteStoragePermission(this)){
                packManager.start();
            }else {
                PermissionManager.requestReadAndWriteStoragePermission(this,()->{
                    packManager.start();
                    return false;
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camPreview = findViewById(R.id.scanner_view);
        btnGoToReviewPackage = findViewById(R.id.goToReviewPackage);
    }

    @Override
    protected void onStart() {
        this.runPackManager();
        this.createBtnGoToReviewPackage();
        this.createCameraPreview();
        super.onStart();
    }

    @Override
    protected void onResume() {
        if(PermissionManager.checkCameraPermission(this)) {
            if (mCodeScanner != null) {
                mCodeScanner.startPreview();
            } else {
                createCodeScanner();
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mCodeScanner != null){
            mCodeScanner.stopPreview();
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mCodeScanner != null){
            mCodeScanner.stopPreview();
            mCodeScanner.releaseResources();
        }
        super.onDestroy();
    }
}