package com.mazein.mazeinparticlefilter;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity
        extends AppCompatActivity
        implements IStepTrigger
{
    private Integer steps = 0;
    // TODO: Change MAX and MIN X and Y
    private HeadingFusion headingUnit;

    private ParticleFilter pf;
    private SensorUnit mSensorUnit;

    private WebView mWebView;

    private boolean INITIALIZING = true;
    private boolean REFRESHING = false;

    private IntentFilter mIntentFilter;
    private WifiManager mWifiManager;
    private BroadcastReceiver mBroadcastReceiver;
    private ArrayList<ScanResult> mScanResults;

    private Vibrator vibrator;
    private MeasurementVector prev;
    private MeasurementVector current;

    private SensorData data;
    private String ACTIVE_FILE_NAME = "PFO_Hofburg.txt";
    // TODO: Initialize currentHeading to heading value of X-axis
    private double currentHeading;
    private double deltaHeading = 0.0d;


    private void checkPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can scan for WiFi.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog)
                    {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can scan for WiFi.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog)
                    {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs external storage access");
                builder.setMessage("Please grant access so this app can write results to file.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog)
                    {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.send_file)
        {
            return emailFiles();
        }
        else if (id == R.id.reset_file)
        {
            return resetFile();
        }
        else if (id == R.id.reload_webview)
        {
            mWebView.loadUrl("file:///android_asset/meininger/index.html");
        }
        return true;

    }

    private boolean emailFiles()
    {
        File dir = new File(Environment.getExternalStorageDirectory(), "MazeIn Particle Filter");
        if (!dir.exists())
        {
            dir.mkdirs();
            Toast.makeText(this, "Folder doesn't exist!", Toast.LENGTH_SHORT).show();
            return false;
        }
        File outFile = new File(dir, ACTIVE_FILE_NAME);
        if (!outFile.exists())
        {
            Toast.makeText(this, "File missing!", Toast.LENGTH_SHORT).show();
            return false;
        }
        ArrayList<Uri> uris = new ArrayList<Uri>();
        uris.add(Uri.fromFile(outFile));

        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        String timestamp = (DateFormat.format("dd-MM-yy hh:mm:ss", new java.util.Date()).toString());
        i.putExtra(Intent.EXTRA_SUBJECT, ACTIVE_FILE_NAME +
                "_" + timestamp);

        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_STREAM, uris);
        Toast.makeText(this, "Sending " + ACTIVE_FILE_NAME, Toast.LENGTH_SHORT).show();
        startActivity(Intent.createChooser(i, "Sending multiple attachments"));
        return false;
    }

    private boolean resetFile()
    {
        File dir = new File(Environment.getExternalStorageDirectory(), "MazeIn Particle Filter");
        if (!dir.exists())
        {
            dir.mkdirs();
            Toast.makeText(this, "Folder doesn't exist!", Toast.LENGTH_SHORT).show();
            return false;
        }
        File outFile = new File(dir, ACTIVE_FILE_NAME);
        if (!outFile.exists())
        {
            Toast.makeText(this, "File missing!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            outFile.delete();
        }
        Toast.makeText(this, ACTIVE_FILE_NAME + " Deleted!", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        resetFile();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        headingUnit = new HeadingFusion(this);
        pf = new ParticleFilter(600, 33.0d, 15.0d, 0.0d);

        // Parse fingerprints file
        FingerprintStore.loadFingerprints_4Wifi(this);

        // TODO: Change Initial Measurement Vector length
        current = new MeasurementVector(0.0, 0.0, 0.0);
        mSensorUnit = new SensorUnit(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // TODO: Change Measurement Vector length
                double[] measurmentValues = new double[4];

                Log.d("WLAN", "Receiving WLAN Scan results");
                mScanResults = (ArrayList<ScanResult>) mWifiManager.getScanResults();
                Log.d("WLAN", mWifiManager.getScanResults().toString());
                for(ScanResult result: mScanResults)
                {
                    if(result.BSSID.equals(AccessPointMacs.AP1_MAC))
                    {
                        // Normalization of readings
                        // TODO: Normalize readings on max in FPStore
                        measurmentValues[0] = (1 / (result.level / -37.0));
                    }
                    else if(result.BSSID.equals(AccessPointMacs.AP2_MAC))
                    {
                        Log.d("RSSI", String.valueOf(result.level));
                        measurmentValues[1] = (1 / (result.level / -52.0));
                    }
                    else if(result.BSSID.equals(AccessPointMacs.AP3_MAC))
                    {
                        measurmentValues[2] = (1 / (result.level / -38.0));
                    }
                    else if (result.BSSID.equals(AccessPointMacs.AP4_MAC))
                    {
                        measurmentValues[3] = (1 / (result.level / -51.0));
                    }
                }
                // TODO: Add 4th measurement parameter
                //measurmentValues[3] = SensorUnit.getMagneticMagnitude();
                current.set(measurmentValues);
                INITIALIZING = false;
                if (REFRESHING)
                {
                    Log.d("KNN", "Starting KNN");
                    KNN();
                    REFRESHING = false;
                }
                else
                {
                    startParticleFilterStep(SensorUnit.getSensorData());
                }
            }
        };


        registerReceiver(mBroadcastReceiver, mIntentFilter);

//        final Button step = (Button)findViewById(R.id.step_button);
//        step.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                step.setClickable(false);
//                onStepDetected(SensorUnit.getSensorData());
//                step.setClickable(true);
//                REFRESHING = false;
//            }
//        });
        final Button refresh = (Button) findViewById(R.id.refresh_readings);
        refresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // START KNN
                REFRESHING = true;
                mWifiManager.startScan();
            }
        });

        mWebView = (WebView) findViewById(R.id.webView);
        WebSettings mapWebSettings = mWebView.getSettings();
        mapWebSettings.setJavaScriptEnabled(true);
//        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

        mWebView.loadUrl("file:///android_asset/ipsn/index.html");
        //mSensorUnit.registerStepListner(this);
        mSensorUnit.resume();
        // TODO: SET INITIAL ORIENTATION VALUE
//        currentHeading = Math.toDegrees(mSensorUnit.getRAWHeading());
        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        currentHeading = Math.toDegrees(headingUnit.getOrientationValue());
        Toast.makeText(MainActivity.this, "Current Heading Value: " + String.valueOf(currentHeading), Toast.LENGTH_SHORT).show();
        prev = new MeasurementVector(0.0, 0.0, 0.0, 0.0);
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
        headingUnit.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter);
        mSensorUnit.resume();
        headingUnit.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        headingUnit.onStop();
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

    private void KNN()
    {
        JSONObject json = new JSONObject();
        try
        {
            for (int i = 0; i < 4; i++)
            {
                JSONObject singleResult = new JSONObject();
                singleResult.put("SSID", AccessPointMacs.vals.get(i));
                singleResult.put("place_id", 3);
                singleResult.put("BSSID", AccessPointMacs.vals.get(i));
                singleResult.put("RSSI", AccessPointMacs.normalization[i] / current.get()[i]);
                json.put(Integer.toString(i), singleResult);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        new SendToServer().execute(json);
    }

    private void startParticleFilterStep(SensorData data)
    {
//        deltaHeading = Math.toDegrees(data.ori[0]) - currentHeading;
        // TODO: NEW HEADING VALUE
        // headingUnit reports radian angles
        deltaHeading = Math.toDegrees(headingUnit.getOrientationValue()) - currentHeading;
        currentHeading = Math.toDegrees(headingUnit.getOrientationValue());

//        if(deltaHeading<10 && deltaHeading>-10)
//        {
//            deltaHeading = 0;
//        }
//        currentHeading = Math.toDegrees(data.ori[0]); //set next heading

        steps++;
        TextView x_tv = (TextView)(findViewById(R.id.x_coord_tv));
        TextView y_tv = (TextView)(findViewById(R.id.y_coord_tv));
        TextView status_tv = (TextView) (findViewById(R.id.status_tv));
        TextView heading_change_tv = (TextView) (findViewById(R.id.heading_tv));
        TextView pheading_tv = (TextView) (findViewById(R.id.pheading_tv));

        heading_change_tv.setText(String.valueOf(Math.round(deltaHeading)));

        status_tv.setText("Got WiFi, Particle Filter Started");
        //Log.d("WIFI", mWifiManager.getScanResults().toString());
        Log.i("ParticleFilter", "Step Detected!");
        double mean = getParticlesHeadingMean();
        double var = getParticlesHeadingVar(mean);
        Log.i("ParticleFilter", "Updating Weights");

        double[][] NEW_MEASUREMENTS = current.minus(prev).toMatrix();
        int pid = 0;
        for(Particle p : pf.particles)
        {
            p.motionModelUpdate(deltaHeading, mean, var);
            // TODO: send wifi stuff
            p.updateWeight3(current.toMatrix());
            addParticleToMap(p.state.x, p.state.y, pid);
            pid++;
        }
        Log.i("ParticleFilter", "Done Updating Weights");
        // TODO: Normalize weights
        writeToFile(this, "After Motion Model and Updating Weights");
        writeToFile(this, pf.visualize());
        pf.resample();
        writeToFile(this, "After Resampling: " + steps.toString());
        writeToFile(this, pf.visualize());
        // Report new location
        Log.i("ParticleFilter", "Getting Average Location");
        State prediction = pf.getAvgLocation();
        status_tv.setText("Done..");
        // Visualize resampled particles
        pid = 0;
        for (Particle p : pf.particles)
        {
            addParticleToMap(p.state.x, p.state.y, pid);
            pid++;
        }
        x_tv.setText(String.valueOf(prediction.x));
        y_tv.setText(String.valueOf(prediction.y));
        pheading_tv.setText(String.valueOf(Math.round(pf.particles[0].state.heading)));
        addMarkerToMap(prediction.x, prediction.y);
    }

    public void addMarkerToMap(double x, double y)
    {
        mWebView.loadUrl("javascript:removeMarker(202, jsonSource_entitiesLineString)");
        mWebView.loadUrl("javascript:addMarker(" + x + "," + y + ", 202, jsonSource_entitiesLineString)");
    }

    public void addParticleToMap(double x, double y, int id)
    {
        mWebView.loadUrl("javascript:removeMarker(" + id + ", jsonSource_entitiesLineString)");
        mWebView.loadUrl("javascript:addParticleMarker(" + x + "," + y + "," + id + ", jsonSource_entitiesLineString)");
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

                File outFile = new File(dir, ACTIVE_FILE_NAME);

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


    class SendToServer extends AsyncTask<JSONObject, Void, String>
    {
        private static final String SERVER_BASE_URL = "http://mazein.herokuapp.com/";
        private static final String WIFI_SERVER_CONTROLLER = "finger_prints";
        private static final String MAGNETIC_SERVER_CONTROLLER = "magnetics";
        private static final String WIFI_SERVER_ACTION = "localization";
        private static final String MAGNETIC_SERVER_ACTION = "localization";
        private static final String WIFI_SERVER_FP_KEY = "finger_print";
        private static final String MAGNETIC_SERVER_FP_KEY = "magnetic";
        private static final String WIFI_SERVER_ROUTE = "localization.json";
        private static final String MAGNETIC_SERVER_ROUTE = "magnetic/localization.json";
        private ProgressDialog serverDialog;
        private String requestPath;
        private String controllerPath;
        private String action;
        private String fpKey;

        private String makePostRequest(JSONObject finger_print, String pPath,
                                       String actionP, String controllerP, String fpKeyP) throws IOException
        {
            try
            {
                //final JSONobject which would be serialized to be sent to the server including wifiFingerprints JSONObject
                JSONObject requestJson = new JSONObject();
                requestJson.put("action", actionP);
                requestJson.put("controller", controllerP);
                requestJson.put(fpKeyP, finger_print);

                HttpURLConnection con = (HttpURLConnection) (new URL(SERVER_BASE_URL + pPath).openConnection());
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");

                Log.d("JSON_TO_SERVER", finger_print.toString());
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(requestJson.toString());
                Log.d("RequestJson", requestJson.toString());
                wr.flush();
                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }

                    br.close();
                    Log.i("JSON_TO_SERVER", "Response: " + sb.toString());
                    return sb.toString();
                }
                else
                {
                    System.out.println(con.getResponseMessage());
//                Toast.makeText(Fingerprinting.this, "Server Response: ERR", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e)
            {
                Log.e("POST_REQ", "Couldn't convert Fingerprint to JSON");
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected String doInBackground(JSONObject... params)
        {
            try
            {
                return makePostRequest(params[0],
                        WIFI_SERVER_ROUTE, WIFI_SERVER_ACTION, WIFI_SERVER_CONTROLLER, WIFI_SERVER_FP_KEY);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String coord)
        {
            // coord is in format "x,y"
            coord = coord.substring(1, coord.length() - 2);
            Log.d("SendToServerResult", coord);
            Random rand = new Random();
            String[] coord_split = coord.split(",");
            State mediod = new State(
                    Double.valueOf(coord_split[0]),
                    Double.valueOf(coord_split[1])
            );
            Toast.makeText(MainActivity.this, coord_split[0] + ", " + coord_split[1], Toast.LENGTH_SHORT).show();

            deltaHeading = headingUnit.getOrientationValue() - currentHeading;
            currentHeading = headingUnit.getOrientationValue();

            for (Particle p : pf.particles)
            {
                p.state.x = mediod.x + (rand.nextGaussian() * 3.0); // 5 meter error
                p.state.y = mediod.y + (rand.nextGaussian() * 3.0);
                p.weight = 1.0 / pf.particles.length;
                // Look in the X axis direction while refreshing.
                p.state.heading = 0.0d;
            }
            int pid = 0;
            for (Particle p : pf.particles)
            {
                addParticleToMap(p.state.x, p.state.y, pid);
                pid++;
            }
            addMarkerToMap(Double.valueOf(coord_split[0]), Double.valueOf(coord_split[1]));
            TextView heading_change_tv = (TextView) (findViewById(R.id.heading_tv));
            TextView pheading_tv = (TextView) (findViewById(R.id.pheading_tv));

            heading_change_tv.setText(String.valueOf(Math.round(deltaHeading)));
            pheading_tv.setText(String.valueOf(Math.round(pf.particles[0].state.heading)));
        }
    }

}
