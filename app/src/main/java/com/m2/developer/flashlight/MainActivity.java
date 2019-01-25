package com.m2.developer.flashlight;


import android.annotation.SuppressLint;
import android.content.ContentResolver;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import android.provider.Settings.System;

import at.markushi.ui.CircleButton;

import static android.Manifest.permission.CAMERA;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    CircleButton on, off;
    Camera cam;
    Parameters p;
    boolean status;
    SeekBar seekBar;
    boolean hasFlash;
    private int brightness;

    private ContentResolver Conresolver;

    private Window window;

    public static final int RequestPermissionCode = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {

                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Brightness Permission");
                    alertDialogBuilder.setMessage("This permission is required please allow an app to modify system settings Click 'Yes'");
                    alertDialogBuilder.setPositiveButton("yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                    startActivity(intent);
                                }
                            });

                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }

            hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if (!hasFlash) {
                // device doesn't support flash
                // Show alert message and close the application
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Error");
                alertDialogBuilder.setMessage("Sorry, your device doesn't support flash light!");
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                finish();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                return;
            }
        } catch (Exception e) {
            e.getMessage();
        }
        try {
            if (!checkPermission()) {
                requestPermission();
            }

            init();

            getCamera();
        } catch (Exception e) {
            e.getMessage();
        }
        try {
            Conresolver = getContentResolver();
            window = getWindow();
            seekBar.setMax(255);
            seekBar.setKeyProgressIncrement(1);
        } catch (Exception e) {
            e.getMessage();
        }
        try {
            brightness = System.getInt(Conresolver, System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }

        seekBar.setProgress(brightness);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    System.putInt(Conresolver, System.SCREEN_BRIGHTNESS, brightness);
                    WindowManager.LayoutParams layoutpars = window.getAttributes();
                    layoutpars.screenBrightness = brightness / (float) 255;
                    window.setAttributes(layoutpars);
                } catch (Exception e) {
                    e.getMessage();
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    if (progress <= 20) {
                        brightness = 20;
                    } else {
                        brightness = progress;
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        });

    }

    private void init() {
        try {
            on = findViewById(R.id.bt1);
            off = findViewById(R.id.bt2);
            seekBar = findViewById(R.id.seekBar);

            on.setOnClickListener(this);
            off.setOnClickListener(this);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @SuppressLint("LongLogTag")
    private void getCamera() {
        try {
            if (cam == null) {
                try {
                    cam = Camera.open();
                    p = cam.getParameters();
                } catch (RuntimeException e) {
                    Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt1:
                try {
                    if (!status) {
                        turnOnFlash();
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
                break;
            case R.id.bt2:
                try {
                    if (status) {
                        turnOffFlash();
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
                break;
        }
    }

    private void turnOnFlash() {
        try {
            if (!status) {
                if (cam == null || p == null) {
                    return;
                }

                p = cam.getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
                status = true;

                // changing button/switch image
                off.setVisibility(View.VISIBLE);
                on.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }


    // Turning Off flash
    private void turnOffFlash() {
        try {
            if (status) {
                if (cam == null || p == null) {
                    return;
                }

                p = cam.getParameters();
                p.setFlashMode(Parameters.FLASH_MODE_OFF);
                cam.setParameters(p);
                cam.stopPreview();
                status = false;

                // changing button/switch image
                on.setVisibility(View.VISIBLE);
                off.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // on pause turn off the flash
            turnOffFlash();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // on resume turn on the flash
            if (status)
                turnOnFlash();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            // on starting the app get the camera params
            getCamera();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            // on stop release the camera
            if (cam != null) {
                cam.release();
                cam = null;
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void requestPermission() {
        try {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA}, RequestPermissionCode);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:
                try {
                    if (grantResults.length > 0) {
                        boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if (!CameraPermission) {
                            Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
                break;
        }
    }

    public boolean checkPermission() {
        try {
            int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
            return FirstPermissionResult == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            e.getMessage();
        }
        return false;
    }
}
