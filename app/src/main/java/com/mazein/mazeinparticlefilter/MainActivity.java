package com.mazein.mazeinparticlefilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity
        extends AppCompatActivity
        implements IStepTrigger
{
    private Integer steps = 0;
    private ParticleFilter pf = new ParticleFilter(200, 50, 50);
    private SensorUnit mSensorUnit;

    private boolean WIFI_RESULTS_READY = false;

    private IntentFilter mIntentFilter;
    private WifiManager mWifiManager;
    private BroadcastReceiver mBroadcastReceiver;
    private ArrayList<ScanResult> mScanResults;

    private Vibrator vibrator;
    private MeasurementVector prev;
    private MeasurementVector current;

    private SensorData data;
    private String ACTIVE_FILE_NAME = "Particle Filter Output.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        FingerprintStore.loadFingerprints(this);
        super.onCreate(savedInstanceState);
        current = new MeasurementVector(0,0,0);

        mSensorUnit = new SensorUnit(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                double[] reqRSSIs = new double[3];

                Log.d("WLAN", "Receiving WLAN Scan results");
                mScanResults = (ArrayList<ScanResult>) mWifiManager.getScanResults();
                Log.d("WLAN", mWifiManager.getScanResults().toString());
                for(ScanResult result: mScanResults)
                {
                    if(result.BSSID.equals(AccessPointMacs.AP1_MAC))
                    {
                        reqRSSIs[0] = result.level;
                    }
                    else if(result.BSSID.equals(AccessPointMacs.AP2_MAC))
                    {
                        reqRSSIs[1] = result.level;
                    }
                    else if(result.BSSID.equals(AccessPointMacs.AP3_MAC))
                    {
                        reqRSSIs[2] = result.level;
                    }
                }

                current.set(reqRSSIs);
                WIFI_RESULTS_READY = true;
                startParticleFilterStep(SensorUnit.getSensorData());
            }
        };


        registerReceiver(mBroadcastReceiver, mIntentFilter);

        Button step = (Button)findViewById(R.id.step_button);
        step.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onStepDetected(SensorUnit.getSensorData());
            }
        });
        //mSensorUnit.registerStepListner(this);
        mSensorUnit.resume();

        prev = new MeasurementVector(0.0, 0.0, 0.0);
        // Write initial particles to file
        writeToFile(this, pf.visualize());
        mWifiManager.startScan();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);

        mSensorUnit.pause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter);
        mSensorUnit.resume();
    }

    @Override
    public void onStepDetected(SensorData data)
    {
        this.data = data;
        vibrator.vibrate(500);
        TextView status_tv = (TextView) (findViewById(R.id.status_tv));
        status_tv.setText("Wait for WiFi...");
        mWifiManager.startScan();
    }

    private void startParticleFilterStep(SensorData data)
    {
        steps++;
        TextView x_tv = (TextView)(findViewById(R.id.x_coord_tv));
        TextView y_tv = (TextView)(findViewById(R.id.y_coord_tv));
        TextView status_tv = (TextView) (findViewById(R.id.status_tv));

        status_tv.setText("Got WiFi, Particle Filter Started");
        //Log.d("WIFI", mWifiManager.getScanResults().toString());
        Log.i("ParticleFilter", "Step Detected!");
        double mean = getParticlesHeadingMean();
        double var = getParticlesHeadingVar(mean);
        Log.i("ParticleFilter", "Updating Weights");

        double[][] NEW_MEASUREMENTS = current.minus(prev).toMatrix();
        for(Particle p : pf.particles)
        {
            p.motionModelUpdate(data, mean, var);
            // #TODO send wifi stuff
            p.updateWeight(NEW_MEASUREMENTS);
        }
        Log.i("ParticleFilter", "Done Updating Weights");
        // TODO: Normalize weights
        writeToFile(this, "Before Resampling");
        writeToFile(this, pf.visualize());
        pf.resample();
        writeToFile(this, "After Resampling: " + steps.toString());
        writeToFile(this, pf.visualize());
        // Report new location
        Log.i("ParticleFilter", "Getting Average Location");
        State prediction = pf.getAvgLocation();
        status_tv.setText("Done..");
        x_tv.setText(String.valueOf(prediction.x));
        y_tv.setText(String.valueOf(prediction.y));
    }

    private double getParticlesHeadingMean()
    {
        double sum = 0.0d;
        for(Particle p : pf.particles)
        {sum += p.state.heading;}

        return sum/pf.particles.length;
    }

    private double getParticlesHeadingVar(double mean)
    {
        double sum = 0.0d;
        for(Particle p : pf.particles)
        {
            sum += (p.state.heading - mean)*(p.state.heading - mean);
        }
        return sum/pf.particles.length;
    }

    public boolean writeToFile(Context context, String mytext)
    {
        Log.i("FILE_WRITE", "SAVING");
        try {
            String MEDIA_MOUNTED = "mounted";
            String diskState = Environment.getExternalStorageState();
            if(diskState.equals(MEDIA_MOUNTED))
            {
                File dir = new File(Environment.getExternalStorageDirectory(), "MazeIn Particle Filter");
                if(!dir.exists())
                {
                    dir.mkdirs();
                }

                File outFile = new File(dir, ACTIVE_FILE_NAME + ".txt");

                //FileOutputStream fos = new FileOutputStream(outFile);

                BufferedWriter out = new BufferedWriter(new FileWriter(outFile, true));
                out.write(mytext + "\n");
                out.flush();
                out.close();

                return true;

            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
