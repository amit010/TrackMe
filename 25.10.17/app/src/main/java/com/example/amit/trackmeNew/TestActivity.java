package com.example.amit.trackmeNew;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TestActivity extends Activity {
    Button btn_start;
    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;
    TextView tv_latitude, tv_longitude, tv_address, tv_area, tv_locality;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    Double latitude, longitude;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        btn_start = (Button) findViewById(R.id.btn_start);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_latitude = (TextView) findViewById(R.id.tv_latitude);
        tv_longitude = (TextView) findViewById(R.id.tv_longitude);
        tv_area = (TextView) findViewById(R.id.tv_area);
        tv_locality = (TextView) findViewById(R.id.tv_locality);
        geocoder = new Geocoder(this, Locale.getDefault());
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        medit = mPref.edit();


        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolean_permission) {

                    if (mPref.getString("service", "").matches("")) {
                        medit.putString("service", "service").commit();

                        Intent intent = new Intent(getApplicationContext(), GoogleService.class);
                        startService(intent);

                    } else {
                        Toast.makeText(getApplicationContext(), "Service is already running", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
                }

            }
        });

        fn_permission();
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(TestActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            latitude = Double.valueOf(intent.getStringExtra("latutide"));
            longitude = Double.valueOf(intent.getStringExtra("longitude"));


            try {

                final String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                // String user_name;

                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UID").child(driverId).child("user name");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Post post = dataSnapshot.getValue(Post.class);
                        try {
                            final String user_name = (String) dataSnapshot.getValue();
                            System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU    " + user_name);
                            DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("UserName").child(user_name);


                            GeoFire geoFire = new GeoFire(ref2);
                            geoFire.setLocation("Location", new GeoLocation(latitude, longitude));
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            } catch (Exception e) {
            }


            List<Address> addresses;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String cityName = addresses.get(0).getAddressLine(0);
                String stateName = addresses.get(0).getAddressLine(1);
                String countryName = addresses.get(0).getAddressLine(2);

                tv_area.setText(addresses.get(0).getAdminArea());
                tv_locality.setText(stateName);
                tv_address.setText(countryName);


            } catch (IOException e1) {
                e1.printStackTrace();
            }


            tv_latitude.setText(latitude + "");
            tv_longitude.setText(longitude + "");
            tv_address.getText();


        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(GoogleService.str_receiver));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


}