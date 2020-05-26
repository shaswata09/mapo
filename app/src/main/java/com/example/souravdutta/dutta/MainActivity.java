package com.example.souravdutta.dutta;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.graphhopper.util.Helper;
import com.graphhopper.util.ProgressListener;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private boolean activityLoaded = false;
    private File mapsFolder;
    private Spinner localSpinner;
    private Button localButton;
    private String currentArea = "berlin";
    private volatile boolean prepareInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(checkPermission())
            continueActivity();


    }
    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermission())
            continueActivity();
        //mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      /* if (hopper != null)
           hopper.close();

        hopper = null;
        // necessary?
        System.gc();

        // Cleanup VTM
        mapView.map().destroy();*/
    }

    //checking permissions
    boolean checkPermission(){
        if (activityLoaded) { return true; }
        String sPermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String sPermission2 = android.Manifest.permission.ACCESS_FINE_LOCATION;
        String sPermission3 = android.Manifest.permission.ACCESS_COARSE_LOCATION;
        String sPermission4 = android.Manifest.permission.CALL_PHONE;
        String sPermission5 = android.Manifest.permission.INTERNET;
        String sPermission6 = android.Manifest.permission.ACCESS_NETWORK_STATE;
        String sPermission7 = android.Manifest.permission.ACCESS_WIFI_STATE;
        String sPermission8 = android.Manifest.permission.SEND_SMS;
        String sPermission9 = android.Manifest.permission.READ_SMS;
        String sPermission10 = android.Manifest.permission.RECEIVE_SMS;
        String sPermission11 = android.Manifest.permission.READ_PHONE_STATE;

        if (!Permission.checkPermission(sPermission, this)
                || !Permission.checkPermission(sPermission2, this)
                || !Permission.checkPermission(sPermission3, this)
                || !Permission.checkPermission(sPermission4, this)
                || !Permission.checkPermission(sPermission5, this)
                || !Permission.checkPermission(sPermission6, this)
                || !Permission.checkPermission(sPermission7, this)
                || !Permission.checkPermission(sPermission8, this)
                || !Permission.checkPermission(sPermission9, this)
                || !Permission.checkPermission(sPermission10, this)
                || !Permission.checkPermission(sPermission11, this))
        {
            Permission.startRequest(new String[]{sPermission, sPermission2, sPermission3, sPermission4, sPermission5, sPermission6, sPermission7, sPermission8, sPermission9, sPermission10, sPermission11}, true, this);
        }
        if (Permission.checkPermission(sPermission, this) && Permission.checkPermission(sPermission2, this))
        {   return true;    }

        else
        {   return false;    }
    }

    //building maps list
    boolean continueActivity()
    {
        localSpinner = (Spinner) findViewById(R.id.locale_area_spinner);
        localButton = (Button) findViewById(R.id.locale_button);
        final EditText input = new EditText(this);
        input.setText(currentArea);
        mapsFolder =getDefaultMapsDirectory(this);
        if (mapsFolder==null) { return false; }
        if ((!mapsFolder.exists()))
            mapsFolder.mkdirs();

        chooseAreaFromLocal();

        return false;
    }

    //getting maps folder
    public File getDefaultMapsDirectory(Context context)
    {
        File defmapsFolder;
        // greater or equal to Kitkat
        if (Build.VERSION.SDK_INT >= 19)
        {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                logUser("GraphHopper is not usable without an external storage!");
                return null;
            }
            defmapsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "/graphhopper/maps/");
            return defmapsFolder;
        }
        else
        {
            defmapsFolder = new File(Environment.getExternalStorageDirectory(), "/graphhopper/maps/");
            return defmapsFolder;
        }
    }

    //choosing map area from local memory
    private void chooseAreaFromLocal() {
        List<String> nameList = new ArrayList<>();
        String[] files = mapsFolder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename != null
                        && (filename.endsWith(".ghz") || filename
                        .endsWith("-gh"));
            }
        });
        Collections.addAll(nameList, files);

        if (nameList.isEmpty())
            return;

        chooseArea(localButton, localSpinner, nameList,
                new MySpinnerListener() {
                    @Override
                    public void onSelect(String selectedArea, String selectedFile) {
                        initFiles(selectedArea);
                    }
                });
    }

    //choosing map area and generating list spinner
    private void chooseArea(Button button, final Spinner spinner,
                            List<String> nameList, final MySpinnerListener myListener) {
        final Map<String, String> nameToFullName = new TreeMap<>();
        for (String fullName : nameList) {
            String tmp = Helper.pruneFileEnd(fullName);
            if (tmp.endsWith("-gh"))
                tmp = tmp.substring(0, tmp.length() - 3);

            tmp = AndroidHelper.getFileName(tmp);
            nameToFullName.put(tmp, fullName);
        }
        nameList.clear();
        nameList.addAll(nameToFullName.keySet());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, nameList);
        spinner.setAdapter(spinnerArrayAdapter);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object o = spinner.getSelectedItem();
                if (o != null && o.toString().length() > 0 && !nameToFullName.isEmpty()) {
                    String area = o.toString();
                    myListener.onSelect(area, nameToFullName.get(area));
                } else {
                    myListener.onSelect(null, null);
                }
            }
        });


    }



    public interface MySpinnerListener {
        void onSelect(String selectedArea, String selectedFile);
    }

    private void initFiles(String area) {
        prepareInProgress = true;
        currentArea = area;
        OfflineMap.loadMap(area, mapsFolder, this);
       // downloadingFiles();
    }

    private void logUser(String str) {
        log(str);
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    private void log(String str) {
        Log.i("GH", str);
    }
}

