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
    private final Preferences Preferences = new Preferences();      // Gets a reference to the preferences class
    private final SystemInformation SystemInformation = new SystemInformation();        // Gets an instance from the system information module
    private Button estimoteRanger;         // This is the estimote ranger
    private Vibrator vibrator;      // This is the vibrator
    private int HapticFeedback = Preferences.HapticFeedback;        // This is the haptic feedback that the system uses.
    private final String Estimote = Preferences.Estimote;     // Gets the sensors from preferences.
    private final String Subdirectory_Estimote = Preferences.Subdirectory_Estimote;        // This is where the estimote is kept
    private boolean Switch = false;     // This is the switch that goes back and forth for the button.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);         // This is the creation of an instance of the app
        setContentView(R.layout.activity_main);     // Starts the device with the layout specified

        CheckPermissions();     // Calls the check permission module
        CheckFiles();       // Calls the check files module.

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);     // Initiates an instance of the wakelock
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyWakeLockTag:");       // Makes the screen stay awake as long as the app is running
        wakeLock.acquire();     // Acquires the wakelock and hold it

        Log.i("Main Activity", "Application Initiated");     // Logs on Console.

        estimoteRanger = findViewById(R.id.Estimote_Button);        // This is the estimote ranger button
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);       // This is the vibrator instance from the system.

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
                Log.i("Main Activity", "Ranging Button Clicked");     // Logs on Console.

                vibrator.vibrate(HapticFeedback);       // Vibrate for the set amount listed in preferences.
                Switch = !Switch;       // Switch the boolean value associated with

                if (Switch)     // If the switch is true
                {
                    String data =  ("Main Activity Start Range Button Clicked at " + SystemInformation.getTimeStamp());       // This is the format it is logged at.
                    DataLogger datalog = new DataLogger(Subdirectory_Estimote, Estimote, data);      // Logs it into a file called System Activity.
                    datalog.LogData();      // Saves the data into the directory.

                    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);       // This is an animator that
                    animator.setDuration(2000);     // Keeps the current color fading for the listed time in milliseconds

                    final float[] hsv;      // A list of possible colors
                    final int[] runColor = new int[1];      // A new list of colors parsed through
                    hsv = new float[3];     // A new list of color to got through
                    hsv[1] = 1;     // The first color to choose.
                    hsv[2] = 1;     // The last color to choose

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()       // Listen for an update from the colors
                    {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation)      // On a change from the color list
                        {
                            hsv[0] = 360 * animation.getAnimatedFraction();     // Get the color to be changed to
                            runColor[0] = Color.HSVToColor(hsv);        // Run the color through the animator
                            estimoteRanger.setBackgroundColor(runColor[0]);     // Sets the estimote ranger button the color given
                        }
                    });

                    animator.setRepeatCount(Animation.INFINITE);        // Lets the animator to run through the colors given forever.
                    animator.start();       // Starts the animation

                    estimoteRanger.setText("Scanning for Beacons");     // Sets the text on the screen

                    startService(EstimService);     // Starts the estimote service
                }

                else
                {
                    String data =  ("Main Activity Stop Range Button Clicked at " + SystemInformation.getTimeStamp());       // This is the format it is logged at.
                    DataLogger datalog = new DataLogger(Subdirectory_Estimote, Estimote, data);      // Logs it into a file called System Activity.
                    datalog.LogData();      // Saves the data into the directory.

                    ValueAnimator animator = ValueAnimator.ofFloat(0, 1);       // This is an animator that
                    animator.setDuration(2000);     // Keeps the current color fading for the listed time in milliseconds

                    final float[] hsv;      // A list of possible colors
                    final int[] runColor = new int[1];      // A new list of colors parsed through
                    hsv = new float[3];     // A new list of color to got through
                    hsv[1] = 1;     // The first color to choose.
                    hsv[2] = 1;     // The last color to choose

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()       // Listen for an update from the colors
                    {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation)      // On a change from the color list
                        {
                            hsv[0] = 360 * animation.getAnimatedFraction();     // Get the color to be changed to
                            runColor[0] = Color.HSVToColor(hsv);        // Run the color through the animator
                            estimoteRanger.setBackgroundColor(Color.BLACK);     // Sets the estimote ranger button the color given
                        }
                    });

                    animator.setRepeatCount(Animation.INFINITE);        // Lets the animator to run through the colors given forever.
                    animator.start();       // Starts the animation

                    estimoteRanger.setText("Click to Start Localization");      // Sets the text on the text view.

                    stopService(EstimService);      // Stops the estimote service
                }
            }
        };

        estimoteRanger.setOnClickListener(ClickRange);      // Ties the estimote ranging button the the click view

        setAmbientEnabled();        // Enables Always-on
    }

    public void CheckPermissions()      // Checks the permissions associated with the need for access to hardware on the device.
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

    private void CheckFiles()       // Checks that the necessary files needed to be logged to are made by the system
    {
        Log.i("Main Activity", "Checking Files");     // Logs on Console.

        File estimote = new File(Preferences.Directory + SystemInformation.Estimote_Path);     // Gets the path to the Sensors from the system.
        if (!estimote.exists())      // If the file exists
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
        CheckPermissions();     // Calls the check permission method.
        CheckFiles();       // Calls the check files method

        super.onResume();       // Forces the resume.
    }

    @Override
    protected void onStop()     // To stop the activity.
    {
        if (isRunningEstimote())        // If the estimote service is running.
        {
            estimoteRanger.performClick();      // Click the button to end the service.
        }
        super.onStop();     // It stops the activity.
    }
}
