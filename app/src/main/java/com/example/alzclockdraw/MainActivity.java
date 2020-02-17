/*Created by Nisanth Mathew James for Hochschule Anhalt - 2020*/

package com.example.alzclockdraw;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.EditText;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    String patientID = null;
    String patientName = null;
    String patientAddress = null;
    String patientPhone = null;
    String patientInfo = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab); //instantiate drawing space
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                extractpatientdetails();
                if (!patientID.isEmpty()&& !patientName.isEmpty()) {
                    savepatientdetails();
                    Intent j = new Intent();
                    j.setClass(MainActivity.this, drawingspace.class);
                    j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    j.putExtra("patientid",patientID); // sending patient id to the next activity
                    startActivity(j);
                }
                else {
                    Snackbar.make(view, "Fill in the details before proceeding", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
        /*******************************Side pane activity***********************/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_patlist) {
            //action
            Log.d("path", String.valueOf(Environment.getDataDirectory()));

        } else if (id == R.id.nav_About) { // info about clock draw test
            Intent about = new Intent();
            about.setClass(MainActivity.this, About.class);
            about.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(about);
        } else if (id == R.id.nav_share) {
            Intent share = new Intent();
            share.setClass(MainActivity.this, SendEmail.class);
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity(share);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /**********************************Extracting Data from UI******************************/
    private void extractpatientdetails(){
        EditText patientid = (EditText) findViewById(R.id.patid);
        patientID = patientid.getText().toString();
        EditText patientname = (EditText) findViewById(R.id.patname);
        patientName = patientname.getText().toString();
        EditText patientaddress = (EditText) findViewById(R.id.pataddress);
        patientAddress = patientaddress.getText().toString();
        EditText patientphone = (EditText) findViewById(R.id.patphone);
        patientPhone = patientphone.getText().toString();
        EditText patientinfo = (EditText) findViewById(R.id.comments);
        patientInfo = patientinfo.getText().toString();
    }

    /******************************extract and save patient informations*********************/
    private void savepatientdetails(){

        /*Saving data into .txt file*/
        File myDir = new File(Environment.getExternalStorageDirectory(), "/PatientData/");
        if(!myDir.exists()){
            if(myDir.mkdirs()) {
                Log.d("path", String.valueOf(myDir));
            }
            else
                Log.d("path not created", String.valueOf(myDir));
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = patientID +".txt";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete ();
        try {
            FileWriter out = new FileWriter(file);
            out.append("Patient ID: " + patientID + "\n"
                        + "Patient Name: " + patientName +"\n"
                        + "Patient Address: " + patientAddress + "\n"
                        + "Phone: " + patientPhone + "\n"
                        + "Comments: +" + patientInfo );
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
