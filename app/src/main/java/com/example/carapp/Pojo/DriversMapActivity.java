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

import java.util.List;

public class DriversMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    private Button driver_settings;
    private Button driver_logout;
    private FirebaseAuth mauth;
    private FirebaseUser currentuser;
    private DatabaseReference assignedcustomerref, assignedpickup;
    private String driverid, customerid = "";
    private Boolean currentdriverstatus = false;
    Marker pickupmarker;
    private ValueEventListener assignedpickuplistener;
    private ImageView icon_set, icon_log;

    private TextView txtname, txtphone;
    private CircleImageView profilepic;
    private RelativeLayout relativeLayout;
    private String phonenum = "";
    private ImageView call_customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_map);
        mauth = FirebaseAuth.getInstance();
        currentuser = mauth.getCurrentUser();
        driverid = mauth.getCurrentUser().getUid();

        driver_logout = (Button) findViewById(R.id.driver_logout_btn);
        driver_settings = (Button) findViewById(R.id.driver_settings_btn);

        txtname = (TextView) findViewById(R.id.customer_name);
        txtphone = (TextView) findViewById(R.id.customer_phone);
        profilepic = (CircleImageView) findViewById(R.id.img_customer);
        relativeLayout = (RelativeLayout) findViewById(R.id.cust_info);
        call_customer = (ImageView) findViewById(R.id.customer_phone_icon);

        call_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callintent = new Intent(Intent.ACTION_CALL);
                callintent.setData(Uri.parse("tel:"+phonenum));
                if (ActivityCompat.checkSelfPermission(DriversMapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callintent);
            }
        });

        icon_set= (ImageView)findViewById(R.id.setting_icon_driv);
        icon_log = (ImageView)findViewById(R.id.logout_icon_driv);

        icon_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent driversetintent = new Intent(DriversMapActivity.this, Settings.class);
                driversetintent.putExtra("type","Drivers");
                startActivity(driversetintent);
            }
        });
        icon_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentdriverstatus = true;
                DiscoonectDriver();

                mauth.signOut();

                DriverLogout();
            }
        });

        driver_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent driversetintent = new Intent(DriversMapActivity.this,Settings.class);
                driversetintent.putExtra("type","Drivers");
                startActivity(driversetintent);
            }
        });
        driver_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentdriverstatus = true;
                DiscoonectDriver();

                mauth.signOut();
                
                DriverLogout();
            }
        });
        GetAssignedCustomerRequest();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void GetAssignedCustomerRequest()
    {
        assignedcustomerref = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverid).child("CustomerRideId");
        assignedcustomerref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) 
            {
                if(dataSnapshot.exists())
                {
                    customerid = dataSnapshot.getValue().toString();
                    
                    GetAssignedCustomerPickupLocation();

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedcustomerinformation();
                }
                else
                {
                    customerid = "";
                    if (pickupmarker!=null)
                    {
                        pickupmarker.remove();
                    }
                    if (assignedpickuplistener!=null)
                    {
                      assignedpickup.removeEventListener(assignedpickuplistener);
                    }
                    relativeLayout.setVisibility(View.GONE);
                }
                
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void GetAssignedCustomerPickupLocation()
    {
        assignedpickup = FirebaseDatabase.getInstance().getReference().child("Customers Requests")
                .child(customerid).child("l");

      assignedpickuplistener =  assignedpickup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    List<Object> customerlocationmap = (List<Object>) dataSnapshot.getValue();
                    double locationlat = 0;
                    double locationlng = 0;
                    if(customerlocationmap.get(0) != null)
                    {
                        locationlat = Double.parseDouble(customerlocationmap.get(0).toString());
                    }
                    if(customerlocationmap.get(1) != null)
                    {
                        locationlng = Double.parseDouble(customerlocationmap.get(1).toString());
                    }
                    LatLng driverlatlng = new LatLng(locationlat, locationlng);
                   pickupmarker= mMap.addMarker(new MarkerOptions().position(driverlatlng).title("Customer PickUp Location")
                   .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_accessibility_black_18)));
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
        if(getApplicationContext() != null)
        {
            lastlocation = location;
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            String userid = currentuser.getUid();

            DatabaseReference Deriversavailref = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
            GeoFire geoFireavail = new GeoFire(Deriversavailref);


            DatabaseReference Deriversworkref = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
            GeoFire geoFirework = new GeoFire(Deriversworkref);


            switch (customerid)
            {
                case "":
                    geoFirework.removeLocation(userid);
                    geoFireavail.setLocation(userid,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;

                default:
                    geoFireavail.removeLocation(userid);
                    geoFirework.setLocation(userid,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;
            }





        }

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
        if(!currentdriverstatus)
        {
            DiscoonectDriver();
        }
    }

    private void DiscoonectDriver() {
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference Deriversavailref = FirebaseDatabase.getInstance().getReference().child("Drivers available");

        GeoFire geoFire = new GeoFire(Deriversavailref);
        geoFire.removeLocation(userid);
    }
    private void DriverLogout() {
        Intent welcomeintent = new Intent(DriversMapActivity.this, Welcome.class);
        welcomeintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeintent);
        finish();
    }
    private void getAssignedcustomerinformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers")
                .child(customerid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    txtname.setText(name);
                    txtphone.setText(phone);
                    phonenum=phone;

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
