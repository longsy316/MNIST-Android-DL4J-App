package com.example.pawan.mnist;

/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.PointF;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawan.mnist.view.DrawModel;
import com.example.pawan.mnist.view.DrawView;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "MainActivity";
    String internalStorage = null;
    String modelLocation = null;
    final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;

    private static final int PIXEL_WIDTH = 28;

    private TextView mResultText;

    private float mLastX;

    private float mLastY;

    private DrawModel mModel;
    private DrawView mDrawView;

    private Button detectButton;

    private PointF mTmpPoint = new PointF();

    MultiLayerNetwork model;



    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mModel = new DrawModel(PIXEL_WIDTH, PIXEL_WIDTH);

        mDrawView = (DrawView) findViewById(R.id.view_draw);
        mDrawView.setModel(mModel);
        mDrawView.setOnTouchListener(this);

        detectButton = findViewById(R.id.buttonDetect);
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDetectClicked();
            }
        });

        View clearButton = findViewById(R.id.buttonClear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClicked();
            }
        });

        mResultText = (TextView) findViewById(R.id.textResult);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
        } else {
            try {
                LoadModel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    try {
                        LoadModel();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Storage permission is required.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }

    private void LoadModel() throws InterruptedException {
        Toast.makeText(this, "Loading Model. Wait!!", Toast.LENGTH_LONG).show();
        internalStorage = getFilesDir().toString();
        modelLocation = internalStorage+"/mnistmodel.zip";

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    model = ModelSerializer.restoreMultiLayerNetwork(modelLocation, false);

//                    InputStream stream = getAssets().open("mnistmodel.zip", AssetManager.ACCESS_BUFFER);

//                    model = ModelSerializer.restoreMultiLayerNetwork(stream, false);
                    Log.d(TAG, "Load Success");
                } catch (IOException e1) {
                    try {
                        copyAssets();
                        model = ModelSerializer.restoreMultiLayerNetwork(modelLocation, false);
//                        InputStream stream = getAssets().open("mnistmodel.zip", AssetManager.ACCESS_BUFFER);
//                        model = ModelSerializer.restoreMultiLayerNetwork(stream);
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    e1.printStackTrace();
                }
                makeButtonVisible();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                            "Now Draw", Toast.LENGTH_SHORT).show();
                        }
                    });

            }
        };

        thread.start();
    }

    private void copyAssets() throws IOException {
        System.out.println("Copyting");
        AssetManager assetManager = getAssets();
        String[] files = null;

        try {
            files = assetManager.list("");
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        for(String fileName : files) {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = assetManager.open(fileName);
                out = new FileOutputStream(modelLocation);
                copyFiles(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void copyFiles(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        mDrawView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDrawView.onPause();
        super.onPause();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(event);
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }
        return false;
    }

    private void processTouchDown(MotionEvent event) {
        mLastX = event.getX();
        mLastY = event.getY();
        mDrawView.calcPos(mLastX, mLastY, mTmpPoint);
        float lastConvX = mTmpPoint.x;
        float lastConvY = mTmpPoint.y;
        mModel.startLine(lastConvX, lastConvY);
    }

    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mDrawView.calcPos(x, y, mTmpPoint);
        float newConvX = mTmpPoint.x;
        float newConvY = mTmpPoint.y;
        mModel.addLineElem(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
        mDrawView.invalidate();
    }

    private void processTouchUp() {
        mModel.endLine();
    }

    private void onDetectClicked() {

        new MyTask(mDrawView, model, mResultText, detectButton).execute();
    }

    private void onClearClicked() {
        mModel.clear();
        mDrawView.reset();
        mDrawView.invalidate();

        mResultText.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

class MyTask extends AsyncTask<Void, Void, Void> {

    DrawView mDrawView;
    String val;
    MultiLayerNetwork model;
    TextView mResultText;
    Button detectButton;
    MyTask(DrawView mDrawView, MultiLayerNetwork model, TextView mResultText, Button detectButton){
        this.mDrawView = mDrawView;
        this.val = "";
        this.model = model;
        this.mResultText = mResultText;
        this.detectButton = detectButton;
        this.detectButton.setEnabled(false);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        float pixels[] = mDrawView.getPixelData();
        for(int i=0; i<pixels.length; i++) {
            //standardise
            pixels[i] /= 255.0;
        }
        int[] shape = new int[]{1, 784};
        INDArray testVector = Nd4j.create(pixels, shape,'c');

        int[] result = model.predict(testVector);
        this.val = result[0]+"";
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        mResultText.setText(val);
        this.detectButton.setEnabled(true);
    }


}

