package com.linklab.emmanuelogunjirin.wearablelocalization;

// Imports

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.estimote.coresdk.observation.region.RegionUtils;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.linklab.emmanuelogunjirin.wearablelocalization.DataLogger.writeToFile;

/* ------------------------------- Special way to log data for the estimote.. (This was moved from Jamie's File and was just used) PLEASE DO NOT REMOVE ------------------------------- */

@SuppressWarnings("ALL")    // Service wide suppression for the Errors.=
public class EstimoteService extends Service
{
    private final Preferences Preference = new Preferences();     // Gets an instance from the preferences module.
    private final SystemInformation SystemInformation = new SystemInformation();  // Gets an instance from the system information module
    private BeaconManager beaconManager;        // This is the beacon manager
    private BeaconRegion region;        // This is the becon range to be looked at
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")       // Suppresses a warning
    private ArrayList<Beacon> eas;      // An array list to look at
    private Date starttime = Calendar.getInstance().getTime();      // Gets the time that the service was started
    private StringBuilder strBuilder;       // This is the string builder to build the string.
    private final String Estimote = Preference.Estimote;     // Gets the sensors from preferences.
    private final String Subdirectory_Estimote = Preference.Subdirectory_Estimote;        // This is where the estimote is kept

    @Override
    public void onCreate()      // When the service is started
    {
        Log.i("Estimote", "Starting Estimote Service");     // Logs on Console.

        CheckFiles();   // Checks Files

        super.onCreate();       // Creates the service.
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);     // Calls a wakelock from the system
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLockTag:");    // Makes it a partial wakelock
        wakeLock.acquire();     // Acquires the wakelock
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)        // When the service is called to start
    {
        CheckFiles();   // Checks Files

        eas = new ArrayList<>();        // Makes an array
        strBuilder = new StringBuilder();       // Initiates a string builder variable
        beaconManager = new BeaconManager(this);        // Initiates the beaconmanger

        region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);        // This is the region that it is looking in
        beaconManager.connect(new BeaconManager.ServiceReadyCallback()      // Calls the beacon to connecvt
        {
            @Override
            public void onServiceReady()        // When the service is ready
            {
                beaconManager.startRanging(region);     // It starts ranging the region
            }
        });

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener()      // The beacon sets the range
        {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<com.estimote.coresdk.recognition.packets.Beacon> list)        // Once a beacon is discovered
            {
                if (!list.isEmpty())        // If the list is not empty
                {
                    int t = 0;      // Initiates a variable
                    for (com.estimote.coresdk.recognition.packets.Beacon beacon : list)     // For every device in the list
                    {
                        Date dt = Calendar.getInstance().getTime();     // Get the date
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);      // Put the date in the format listed
                        String time1 = sdf.format(dt);      // Get the time
                        eas.add(new Beacon(t++, list.size(), beacon.getRssi(), RegionUtils.computeAccuracy(beacon), time1));        // Add the following information
                        strBuilder.append(beacon.getMajor());       // Get the major id
                        strBuilder.append(",");     // Append a comma
                        strBuilder.append(beacon.getRssi());        // Get s the RSSI of the device
                        strBuilder.append(",");     // Append a comma
                        strBuilder.append(RegionUtils.computeAccuracy(beacon));     // Gets the distance associated with the rssi value
                        strBuilder.append(",");     // Append a comma
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);     // Gets a date format listed
                        String sametime = sdf2.format(Calendar.getInstance().getTime());        // Gets the time
                        strBuilder.append(sametime);        // Appends the time.
                        strBuilder.append("\n");        // Append a endline.

                        if (strBuilder != null)     // If the stringbuilder is not null
                        {
                            starttime=Calendar.getInstance().getTime();     // Get an instance of the time.
                            new writethread(strBuilder,starttime).start();      // Writes the information to the file
                            strBuilder = new StringBuilder();       // Starts a new string builder
                        }
                    }
                }
            }
        });

        super.onStartCommand(intent, flags, startId);       // Starts the service
        return START_NOT_STICKY;        // Returns the service.
    }

    class writethread extends Thread        // this is the write thread
    {
        final Date st;      // A variable for the date
        final StringBuilder buf;        // A variable for the stringbuilder

        writethread(StringBuilder sb, Date starttime)       // Writes the thread
        {
            this.st = starttime;        // Sets the variable
            this.buf = sb;      // Sets the write thread
        }

        @Override
        public void run()       // Runs the following
        {
            try     // Tries to run the following.
            {
                String str = String.valueOf(buf);       // Sets the writethread

                if (buf.length() == 0)      // This is the lenght of the write thread
                    return;         // Returns the it.

                boolean check = writeToFile(st, str);       // returns the boolean

                if (check)  // If check
                {
                    // Do nothing
                }
            }

            catch (Exception ex)        // Tries to catch the error.
            {
                // Do nothing
            }
        }
    }

    @Override
    public void onDestroy()     // If the service is called to be destroyed
    {
        Log.i("Estimote", "Destroying Estimote Service");     // Logs on Console.

        beaconManager.stopRanging(region);      // Starts ranging the region
        stopForeground(true);       // Stops the service
        super.onDestroy();      // Destroys the service.
    }

    @Override
    public IBinder onBind(Intent intent)        //  Calls an intent on the service
    {
        throw new UnsupportedOperationException("Not yet implemented");     // Does not bind it
    }

    private void CheckFiles()       // Checks that the files that are needed are there
    {
        File estimote = new File(Preference.Directory + SystemInformation.Estimote_Path);     // Gets the path to the Sensors from the system.
        if (estimote.exists())      // If the file exists
        {
            Log.i("Estimote Sensor", "No Header Created");     // Logs to console
        }
        else        // If the file does not exist
        {
            Log.i("Estimote Sensor", "Creating Header");     // Logs on Console.

            DataLogger dataLogger = new DataLogger(Subdirectory_Estimote, Estimote, Preference.Estimote_Data_Headers);        /* Logs the Sensors data in a csv format */
            dataLogger.LogData();       // Saves the data to the directory.
        }
    }
}