package com.linklab.emmanuelogunjirin.wearablelocalization;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;

public class MainActivity extends WearableActivity
{
    private Button estimoteRanger;
    private boolean Switch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("Main Activity", "Application Initiated");     // Logs on Console.

        estimoteRanger = findViewById(R.id.Estimote_Button);

        View.OnClickListener ClickRange = new View.OnClickListener()        // Listens for the button to be clicked
        {
            @SuppressLint("SetTextI18n")
            public void onClick(View v)     // When the button is clicked
            {
                Switch = !Switch;

                if (Switch)
                {
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
                }
                else
                {
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
                }
            }
        };

        estimoteRanger.setOnClickListener(ClickRange);

        setAmbientEnabled();        // Enables Always-on
    }
}
