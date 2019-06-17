package com.linklab.emmanuelogunjirin.wearablelocalization;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;

import java.io.File;

@SuppressLint("WakelockTimeout")
public class MainActivity extends WearableActivity
{
    private final Preferences Preferences = new Preferences();
    private final SystemInformation SystemInformation = new SystemInformation();        // Gets an instance from the system information module
    private Button estimoteRanger;
    private Vibrator vibrator;
    private int HapticFeedback = Preferences.HapticFeedback;
    private final String Estimote = Preferences.Estimote;     // Gets the sensors from preferences.
    private final String Subdirectory_Estimote = Preferences.Subdirectory_Estimote;        // This is where the estimote is kept
    private boolean Switch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckPermissions();     // Checks the permissions needed for the device to save files and operate within normal parameter.
        CheckFiles();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLockTag:");
        wakeLock.acquire();

        Log.i("Main Activity", "Application Initiated");     // Logs on Console.

        estimoteRanger = findViewById(R.id.Estimote_Button);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        final Intent EstimService = new Intent(getBaseContext(), EstimoteService.class);        // Creates an intent for calling the Estimote Timer service.
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();      // Gets the bluetooth system on the watch
        if (!bluetooth.isEnabled())     // If the bluetooth is not enabled on the watch
        {
            bluetooth.enable();     // Enable it.
        }

        View.OnClickListener ClickRange = new View.OnClickListener()        // Listens for the button to be clicked
        {
            @SuppressLint("SetTextI18n")
            public void onClick(View v)     // When the button is clicked
            {
                vibrator.vibrate(HapticFeedback);
                Switch = !Switch;

                if (Switch)
                {
                    String data =  ("Main Activity Start Range Button Clicked at " + SystemInformation.getTimeStamp());       // This is the format it is logged at.
                    DataLogger datalog = new DataLogger(Subdirectory_Estimote, Estimote, data);      // Logs it into a file called System Activity.
                    datalog.LogData();      // Saves the data into the directory.

                    Log.i("Main Activity", "Ranging Button Clicked");     // Logs on Console.

                    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                    animator.setDuration(2000);

                    final float[] hsv;
                    final int[] runColor = new int[1];
                    hsv = new float[3];
                    hsv[1] = 1;
                    hsv[2] = 1;

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation)
                        {
                            hsv[0] = 360 * animation.getAnimatedFraction();
                            runColor[0] = Color.HSVToColor(hsv);
                            estimoteRanger.setBackgroundColor(runColor[0]);
                        }
                    });

                    animator.setRepeatCount(Animation.INFINITE);
                    animator.start();

                    estimoteRanger.setText("Scanning for Beacons");

                    startService(EstimService);
                }

                else
                {
                    String data =  ("Main Activity Stop Range Button Clicked at " + SystemInformation.getTimeStamp());       // This is the format it is logged at.
                    DataLogger datalog = new DataLogger(Subdirectory_Estimote, Estimote, data);      // Logs it into a file called System Activity.
                    datalog.LogData();      // Saves the data into the directory.

                    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                    animator.setDuration(2000);

                    final float[] hsv;
                    final int[] runColor = new int[1];
                    hsv = new float[3];
                    hsv[1] = 1;
                    hsv[2] = 1;

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation)
                        {
                            hsv[0] = 360 * animation.getAnimatedFraction();
                            runColor[0] = Color.HSVToColor(hsv);
                            estimoteRanger.setBackgroundColor(Color.BLACK);
                        }
                    });

                    animator.setRepeatCount(Animation.INFINITE);
                    animator.start();

                    estimoteRanger.setText("Click to Start Localization");

                    stopService(EstimService);
                }
            }
        };

        estimoteRanger.setOnClickListener(ClickRange);

        setAmbientEnabled();        // Enables Always-on
    }

    public void CheckPermissions()
    {
        Log.i("Main Activity", "Checking Permissions");     // Logs on Console.

        String[] Required_Permissions =     // Checks if Device has permission to work on device.
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,     // This is to access the storage
                        Manifest.permission.READ_EXTERNAL_STORAGE,      // This is to access the storage
                        Manifest.permission.BODY_SENSORS,       // This is to access the sensors of the device
                        Manifest.permission.ACCESS_COARSE_LOCATION,     // This is to access the location in a general sense
                        Manifest.permission.ACCESS_FINE_LOCATION,       // This is to access the location in a more specific manner
                        Manifest.permission.BLUETOOTH,      // This is to access th bluetooth
                        Manifest.permission.BLUETOOTH_ADMIN     // This is access the bluetooth and allow changes
                };

        boolean needPermissions = false;        // To begin the permission is set to false.

        for (String permission : Required_Permissions)     // For each of the permission listed above.
        {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)       // Check if they have permission to work on the device.
            {
                needPermissions = true;     // if they do, grant them permission
            }
        }

        if (needPermissions)        // When they have permission
        {
            ActivityCompat.requestPermissions(this, Required_Permissions,0);     // Allow them to work on device.
        }
    }

    private void CheckFiles()
    {
        Log.i("Main Activity", "Checking Files");     // Logs on Console.

        File estimote = new File(Preferences.Directory + SystemInformation.Estimote_Path);     // Gets the path to the Sensors from the system.
        if (estimote.exists())      // If the file exists
        {
            Log.i("Estimote Sensor", "No Header Created");     // Logs to console
        }
        else        // If the file does not exist
        {
            Log.i("Estimote Sensor", "Creating Header");     // Logs on Console.

            DataLogger dataLogger = new DataLogger(Subdirectory_Estimote, Estimote, Preferences.Estimote_Data_Headers);        /* Logs the Sensors data in a csv format */
            dataLogger.LogData();       // Saves the data to the directory.
        }
    }

    private boolean isRunningEstimote()        // A general file that checks if estimote is running.
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);     // Starts the activity manager to check the service called.
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))        // For each service called by the running service.
        {
            if (EstimoteService.class.getName().equals(service.service.getClassName()))      // It checks if it is running.
            {
                return true;        // Returns true
            }
        }
        return false;       // If not, it returns false.
    }

    @Override
    public void onResume()      // When the system resumes
    {
        CheckPermissions();     // Checks that all the permissions needed are enabled. If not, it request them.
        CheckFiles();

        super.onResume();       // Forces the resume.
    }

    @Override
    protected void onStop()     // To stop the activity.
    {
        if (isRunningEstimote())
        {
            estimoteRanger.performClick();
        }
        super.onStop();     // It stops the activity.
    }
}
