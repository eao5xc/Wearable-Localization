package com.linklab.emmanuelogunjirin.wearablelocalization;

import android.os.Environment;

@SuppressWarnings("ALL")    // Service wide suppression for the Errors.
public class Preferences        // System wide one stop place to set all settings for a particular individual
{
    /* ------------------------------------------------------------------------------- Settings for Deployment, Read Notes Carefully ------------------------------------------------------------------------------*/

    // There should be **NO CHARACTERS OTHER THAN LETTERS, NUMBERS, - or _ ** in file or directory names!
    public String DeviceID = "DEVICE-IDENTIFICATION";        // Internal ID of Device assigned to Dyad
    public String DeploymentID = "P3D1-V";      // Deployment ID
    public String Directory = Environment.getExternalStorageDirectory() + "/Wearable-Localization/";        // Directory on the watch where all files are saved

    // Settings for Vibration | Time is in ms |
    public int HapticFeedback = 20;           // How should the system vibrate when a button is clicked

    // Settings for Estimote | Time is in ms |
    public long ESSampleDuration = 15 * 1000;           // How long should estimote be measured each time?
    public long ESMeasurementInterval = 15 * 60 * 100;          // Every how often should a measurement be taken?

    /* Settings for Changing Individual File Name <----------------------------------------- This is where you change the file names, it updates everywhere */
    public String Estimote = "Estimote_Data.csv";      // This is the Estimote File

    /* Settings for Changing the Subdirectories in the Main Directory */
    public String Subdirectory_Estimote = "Estimote";      // This is where the estimote is kept

    /* Headers to individual files that are being logged to <--------------------------------------------- This is the order that the headers will appear in */
    public String Estimote_Data_Headers = "Estimote ID, RSSI, Calculated Distance, Date --- Time";       // Column Headers for Estimote_Data
}
