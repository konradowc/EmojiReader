package com.example.emojireader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/*
Get SMS permission and launch GraphActivity (which contains main functionality)
 */

public class MainActivity extends AppCompatActivity
{
    private int REQ_READ_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void mainButtonClick(View v)
    {
        setSMSPermission(); // attempt to set permission
        if(ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) // permission set
        {
            // can continue program
            // need to grab text messages
            Toast.makeText(this, "Permission accepted!", Toast.LENGTH_SHORT).show();

            // launch new activity use bundle
            startActivity(new Intent(this, GraphActivity.class));
        }
        else
        {
            Toast.makeText(this, "Permission denied, please allow", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSMSPermission()
    {
        if(ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) // permission not set
        {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_SMS}, REQ_READ_SMS);
        }
    }
}