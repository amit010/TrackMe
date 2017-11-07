package com.example.amit.trackmeNew;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;

public class DriverInfoActivity extends MapForDriverInfo {

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private ArrayList<LatLng> locationListInfo = new ArrayList<LatLng>();

    private DatabaseReference mRef;
    private boolean isFirstEntry = true;
    private String driver_username;
    private TextView userNameText, driverContactText, codeText;

    //This will read the current location from the database and will move the camera to that location
    @Override
    protected void startDemo() {

        userNameText = findViewById(R.id.textusername);
        codeText = findViewById(R.id.textcode);
        driverContactText = findViewById(R.id.textcontact);



        Bundle bundle = getIntent().getExtras();
        driver_username = bundle.getString("username");

        DriverInfo(userNameText, driverContactText, codeText);

        DatabaseReference mDriverUID = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(driver_username).child("UID");
        mDriverUID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String driverUID = dataSnapshot.getValue().toString();

                mRef = FirebaseDatabase.getInstance().getReference().child("Location").child(driverUID).child("UserLocation").child("l");

                mRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String lat = dataSnapshot.child("0").getValue().toString();
                        String lon = dataSnapshot.child("1").getValue().toString();
                        double latitude, longitude;
                        latitude = Double.parseDouble(lat);
                        longitude = Double.parseDouble(lon);


                        if (isFirstEntry) {
                            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12));
                            isFirstEntry = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ReadFromFirebase();
    }


    private void DriverInfo(final TextView userNameText, final TextView driverContactText, final TextView codeText) {
        DatabaseReference mContactRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(driver_username);
        mContactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String contactNo = dataSnapshot.child("contact no").getValue().toString();
                String code = dataSnapshot.child("code").getValue().toString();
                driverContactText.setText(contactNo);
                userNameText.setText(driver_username);
                codeText.setText(code);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Reads all users location data fro firebase, creates the array list and calls the heatMapGenerator to generate the heatmap
    private void ReadFromFirebase() {

        DatabaseReference mDriverUIDRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child("username").child(driver_username);

        mDriverUIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String driverUID = dataSnapshot.child("UID").getValue().toString();

                System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP     " + driverUID);
                DatabaseReference mReference = FirebaseDatabase.getInstance().getReference().child("Clustering").child(driverUID);

                mReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            String lat = postSnapshot.child("Location").child("l").child("0").getValue().toString();
                            String lon = postSnapshot.child("Location").child("l").child("1").getValue().toString();

                            double latitude, longitude;
                            latitude = Double.parseDouble(lat);
                            longitude = Double.parseDouble(lon);

                            System.out.println("Latitudeeeeeeeeeeeeeeeeeeeeeeeeeee     " + latitude);

                            LatLng latLng = new LatLng(latitude, longitude);
                            locationListInfo.add(latLng);
                        }
                        System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeenddddddddddddddddddddddd");
                        printList(locationListInfo);
                        HeatMapGenerator(locationListInfo);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void printList(ArrayList<LatLng>locationList) {
        for(int i = 0; i < locationList.size(); i++) {
            System.out.println("llllllllllllllllllllllllllll      " + locationList.get(i));
        }
    }


    //generates the heatmap from the locations of the arraylist by using heatmap tile provider and tile overlay
    private void HeatMapGenerator(ArrayList<LatLng> locationList) {
        if (mProvider == null) {
            mProvider = new HeatmapTileProvider.Builder()
                    .data(locationList)
                    .build();
            mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

        } else {
            mProvider.setData(locationList);
            mOverlay.clearTileCache();
        }
    }
}
