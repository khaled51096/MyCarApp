package com.example.carapp.Pojo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.carapp.R;
import com.example.carapp.Ui.Settings;
import com.example.carapp.Ui.Welcome;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class CustomersMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    private Button customer_logout;
    private Button customer_settings;
    private Button callAcar;
    private String customerid;
    private FirebaseUser currentuser;
    private FirebaseAuth mauth;
    private DatabaseReference customerdatabaseref;
    private DatabaseReference driveravailableref;
    private DatabaseReference driverlocationref;
    private DatabaseReference driversref;
    private LatLng customerpickuplocation;
    private int radius = 1;
    Marker drivermarker, pickupmarker;
    GeoQuery geoQuery;
    private Boolean driverfound = false, requesttype = false;
    private String driverfoundid;
    private ValueEventListener deriverlcationreflistener;
    private TextView txtname, txtphone, txtcarname;
    private CircleImageView profilepic;
    private RelativeLayout relativeLayout;
    private ImageView call_driver;

    private String phonenum = "";

    private ImageView logicon, seticon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);
        mauth = FirebaseAuth.getInstance();
        currentuser = mauth.getCurrentUser();
        customerdatabaseref = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        driveravailableref = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driverlocationref = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
        callAcar = (Button) findViewById(R.id.customer_call_car_btn);
        customer_logout = (Button) findViewById(R.id.customer_logout_btn);
        customer_settings = (Button) findViewById(R.id.customer_settings_btn);

        txtname = (TextView) findViewById(R.id.driver_name);
        txtphone = (TextView) findViewById(R.id.driver_phone);
        txtcarname = (TextView) findViewById(R.id.driver_car);
        profilepic = (CircleImageView) findViewById(R.id.img_driver);
        relativeLayout = (RelativeLayout) findViewById(R.id.driv_info);
        call_driver = (ImageView) findViewById(R.id.driver_phone_icon);

        call_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callintent = new Intent(Intent.ACTION_CALL);
                callintent.setData(Uri.parse("tel:"+phonenum));
                if (ActivityCompat.checkSelfPermission(CustomersMapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callintent);
            }
        });


        logicon = (ImageView)findViewById(R.id.logout_icon_cust);
        seticon = (ImageView)findViewById(R.id.setting_icon_cust);

        seticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customersetintent = new Intent(CustomersMapActivity.this, Settings.class);
                customersetintent.putExtra("type","Customers");
                startActivity(customersetintent);
            }
        });
        logicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mauth.signOut();
                CustomerLogout();

            }
        });


        customer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customersetintent = new Intent(CustomersMapActivity.this,Settings.class);
                customersetintent.putExtra("type","Customers");
                startActivity(customersetintent);
            }
        });
        customer_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mauth.signOut();
                CustomerLogout();

            }
        });
        callAcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(requesttype)
                {
                    requesttype = false;
                    geoQuery.removeAllListeners();
                    driverlocationref.removeEventListener(deriverlcationreflistener);

                    if(driverfound != null)
                    {
                        driversref = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Drivers").child(driverfoundid).child("CustomerRideId");
                        driversref.removeValue();
                        driverfoundid = null;
                    }

                    driverfound = false;
                    radius = 1;
                    customerid = currentuser.getUid();
                    GeoFire geoFire = new GeoFire(customerdatabaseref);
                    geoFire.removeLocation(customerid);

                    if(pickupmarker!=null)
                    {
                        pickupmarker.remove();
                    }
                    if(drivermarker!=null)
                    {
                        drivermarker.remove();
                    }
                    callAcar.setText("Call a Car");
                    relativeLayout.setVisibility(View.GONE);


                }
                else {
                    requesttype = true;
                    customerid = currentuser.getUid();
                    GeoFire geoFire = new GeoFire(customerdatabaseref);
                    geoFire.setLocation(customerid,new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()));

                    customerpickuplocation = new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
                   pickupmarker= mMap.addMarker(new MarkerOptions().position(customerpickuplocation)
                           .title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_accessibility_black_18)));

                    callAcar.setText("Getting Your Driver");
                    GetClosestDriver();

                }



            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void GetClosestDriver() {
        GeoFire geoFire = new GeoFire(driveravailableref);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customerpickuplocation.latitude,customerpickuplocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {
                if(!driverfound && requesttype)
                {
                    driverfound = true;
                    driverfoundid = key;

                    driversref = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverfoundid);
                    HashMap drivermap = new HashMap();
                    drivermap.put("CustomerRideId", customerid);
                    driversref.updateChildren(drivermap);

                    GettingDriverLocation();
                    callAcar.setText("Looking for Driver Location");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady()
            {
                if(!driverfound)
                {
                    radius = radius + 1;
                    GetClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void GettingDriverLocation()
    {
        deriverlcationreflistener= driverlocationref.child(driverfoundid).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists() && requesttype) {
                            List<Object> driverlocationmap = (List<Object>) dataSnapshot.getValue();
                            double locationlat = 0;
                            double locationlng = 0;
                            callAcar.setText("Driver Found");

                            relativeLayout.setVisibility(View.VISIBLE);
                            getAssigneddriverinformation();

                            if(driverlocationmap.get(0) != null)
                            {
                                locationlat = Double.parseDouble(driverlocationmap.get(0).toString());
                            }
                            if(driverlocationmap.get(1) != null)
                            {
                                locationlng = Double.parseDouble(driverlocationmap.get(1).toString());
                            }
                            LatLng driverlatlng = new LatLng(locationlat, locationlng);
                            if(drivermarker!=null)
                            {
                                drivermarker.remove();
                            }

                            Location location1 = new Location("");
                            location1.setLatitude(customerpickuplocation.latitude);
                            location1.setLongitude(customerpickuplocation.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(driverlatlng.latitude);
                            location2.setLongitude(driverlatlng.longitude);

                            Float distance = location1.distanceTo(location2);
                            if (distance<90)
                            {
                                callAcar.setText("Driver’s reached");
                            }
                            else {
                                callAcar.setText("Driver Found : " + String.valueOf(distance));

                            }


                            mMap.addMarker(new MarkerOptions().position(driverlatlng).title("Your Driver is Here")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_directions_car_black_36)));


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        lastlocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }
    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    private void CustomerLogout()
    {
        Intent welcomeintent = new Intent(CustomersMapActivity.this, Welcome.class);
        welcomeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeintent);
        finish();
    }
    private void getAssigneddriverinformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(driverfoundid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String car = dataSnapshot.child("car").getValue().toString();

                    txtname.setText(name);
                    txtphone.setText(phone);
                    phonenum=phone;
                    txtcarname.setText(car);

                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilepic);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
