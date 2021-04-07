package com.taxi_riderapplication.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.taxi_riderapplication.Callback.IFirebaseDriverInfoListener;
import com.taxi_riderapplication.Callback.IFirebaseFailedListener;
import com.taxi_riderapplication.Common;
import com.taxi_riderapplication.DriverGeoModel;
import com.taxi_riderapplication.DriverInfoModel;
import com.taxi_riderapplication.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.SchedulerRunnableIntrospection;
import io.reactivex.rxjava3.schedulers.Schedulers;



public class HomeFragment extends Fragment implements OnMapReadyCallback, IFirebaseDriverInfoListener, IFirebaseFailedListener {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private boolean firstTime = true;

    private double distance = 1.0;//default km
    private static final double LIMIT_RANGE = 10.0;//km
    private Location previewslocation, currentlocation;//cal the distance
    IFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    IFirebaseFailedListener iFirebaseFailedListener;
    private String cityname;


    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates (locationCallback);
        super.onDestroy ( );
    }

    @Override
    public void onResume() {
        super.onResume ( );
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate (R.layout.fragment_home, container, false);
        init ( );
        mapFragment = (SupportMapFragment) getChildFragmentManager ( ).findFragmentById (R.id.map);
        mapFragment.getMapAsync (this);

        return root;
    }

    private void init() {

        iFirebaseFailedListener = this;
        iFirebaseDriverInfoListener = this;


        locationRequest = new LocationRequest ( );
        locationRequest.setSmallestDisplacement (10f);
        locationRequest.setInterval (5000);
        locationRequest.setFastestInterval (3000);
        locationRequest.setPriority (locationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback ( ) {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult (locationResult);
                LatLng latLng = new LatLng (locationResult.getLastLocation ( ).getLatitude ( ), locationResult.getLastLocation ( ).getLongitude ( ));
                mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (latLng, 18f));

                if (firstTime) {

                    previewslocation = currentlocation = locationResult.getLastLocation ( );
                    firstTime = false;
                } else {
                    previewslocation = currentlocation;
                    currentlocation = locationResult.getLastLocation ( );
                }
                if (previewslocation.distanceTo (currentlocation) / 1000 <= LIMIT_RANGE)
                    loadAvailableDrivers ( );
                else {

                }
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient (getContext ( ));
        if (ActivityCompat.checkSelfPermission (getContext ( ), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (getContext ( ), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates (locationRequest, locationCallback, Looper.myLooper ( ));
        loadAvailableDrivers ();

    }

    private void loadAvailableDrivers() {
        if (ActivityCompat.checkSelfPermission (getContext (), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (getContext (), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make (getView ( ), getString (R.string.permission_require), Snackbar.LENGTH_SHORT).show ( );
            return;
        }
        fusedLocationProviderClient.getLastLocation ( )
                .addOnFailureListener (e -> Snackbar.make (getView ( ), e.getMessage ( ), Snackbar.LENGTH_SHORT).show ( ))
                .addOnSuccessListener (location -> {

                    Geocoder geocoder=new Geocoder (getContext (), Locale.getDefault ());
                    List<Address> addressList;
                    try {
                        addressList=geocoder.getFromLocation (location.getLatitude (),location.getLongitude (),1);
                    cityname=addressList.get (0).getLocality ();
                        DatabaseReference databaseReference= FirebaseDatabase.getInstance ()
                                .getReference (Common.DRIVERS_LOCATION_REFFERNCES)
                                .child (cityname);
                        GeoFire gf=new GeoFire (databaseReference);
                        GeoQuery geoQuery=gf.queryAtLocation (new GeoLocation (location.getLatitude (),
                                location.getLongitude ()),distance);
                        geoQuery.removeAllListeners ();
                        geoQuery.addGeoQueryEventListener (new GeoQueryEventListener ( ) {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                Common.driversFound.add(new DriverGeoModel ( key,location ));
                            }

                            @Override
                            public void onKeyExited(String key) {

                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {

                            }

                            @Override
                            public void onGeoQueryReady() {
                                if(distance <= LIMIT_RANGE){
                                    distance++;
                                    loadAvailableDrivers ();
                                }
                                else {
                                    distance =1.0;
                                    addDriverMarker();
                                }

                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {
                                Snackbar.make (getView (),error.getMessage (),Snackbar.LENGTH_SHORT).show ();

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace ( );
                        Snackbar.make (getView ( ),e.getMessage (), Snackbar.LENGTH_SHORT).show ( );
                    }

                });
    }

    private void addDriverMarker() {
        if(Common.driversFound.size () > 0){

            Observable.fromIterable (Common.driversFound)
                    .subscribeOn (Schedulers.newThread ()).subscribe (driverGeoModel -> {
                        findDriverBykey(driverGeoModel);
            },throwable -> {
                Snackbar.make (getView ( ),throwable.getMessage (), Snackbar.LENGTH_SHORT).show ( );
            },()->{

            });



        }
        else
            {


                Snackbar.make (getView ( ),getString (R.string.drivers_not_found), Snackbar.LENGTH_SHORT).show ( );
        }
    }

    private void findDriverBykey(DriverGeoModel driverGeoModel) {
        FirebaseDatabase.getInstance ().getReference (Common.DRIVERS_INFO_REFFERENCE)
                .child (driverGeoModel.getKey ())
                .addListenerForSingleValueEvent (new ValueEventListener ( ) {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren ())
                        {
                            driverGeoModel.setDriverInfoModel (snapshot.getValue (DriverInfoModel.class));
                            iFirebaseDriverInfoListener.onDriverInfoLoadSuccess (driverGeoModel);
                        }
                        else {
                            iFirebaseFailedListener.onFirebaseLoadFailed (getString (R.string.Not_found_key)+driverGeoModel.getKey ());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iFirebaseFailedListener.onFirebaseLoadFailed (error.getMessage ());
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Dexter.withContext (getContext ( ))
                .withPermission (Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener (new PermissionListener ( ) {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission (getContext ( ), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (getContext ( ), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mMap.setMyLocationEnabled (true);
                        mMap.getUiSettings ( ).setMyLocationButtonEnabled (true);
                        mMap.setOnMyLocationButtonClickListener (() -> {
                            if (ActivityCompat.checkSelfPermission (getContext (), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (getContext (), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return false;
                            }
                            fusedLocationProviderClient.getLastLocation ( )
                                    .addOnFailureListener (e -> Snackbar.make (getView ( ), e.getMessage ( ), Snackbar.LENGTH_SHORT)
                                            .show ( )).addOnSuccessListener (location -> {

                                LatLng ulg=new LatLng(location.getLatitude (),location.getLongitude ());
                                mMap.animateCamera (CameraUpdateFactory.newLatLngZoom (ulg,18f));

                            });
                            return true;
                        });

                        View locationbutton=((View)mapFragment.getView ().findViewById (Integer.parseInt ("1"))
                                .getParent ()).findViewById (Integer.parseInt ("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)locationbutton.getLayoutParams ();
                        params.addRule (RelativeLayout.ALIGN_PARENT_TOP,0);
                        params.addRule (RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                        params.setMargins (0,0,0,250);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make (getView (),permissionDeniedResponse.getPermissionName ()+"need enable",Snackbar.LENGTH_SHORT).show ();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check ();

        mMap.getUiSettings ().setZoomControlsEnabled (true);

        try {
            boolean success =googleMap.setMapStyle (MapStyleOptions.loadRawResourceStyle (getContext (),R.raw.uber_maps_style));
            if(!success)
                Log.e ("Error","style parsing error");

        }catch (Resources.NotFoundException e){
            Log.e ("Error",e.getMessage ());
        }


    }

    @Override
    public void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel) {
        if(!Common.markerlist.containsKey (driverGeoModel.getKey ()))
            Common.markerlist.put(driverGeoModel.getKey (),mMap.addMarker (new MarkerOptions ().position (new LatLng (driverGeoModel.getGeoLocation ().latitude,driverGeoModel.getGeoLocation ().longitude))
            .flat (true)
            .title (Common.buildname(driverGeoModel.getDriverInfoModel ().getName ())).snippet (driverGeoModel.getDriverInfoModel ().getPhonenumber ())
                    .icon (BitmapDescriptorFactory.fromResource (R.drawable.ic_car))));

        if(!TextUtils.isEmpty (cityname)){
            DatabaseReference driverlocation = FirebaseDatabase.getInstance ().getReference (Common.DRIVERS_LOCATION_REFFERNCES)
                    .child (cityname).child (driverGeoModel.getKey ());
            driverlocation.addValueEventListener (new ValueEventListener ( ) {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChildren ()){
                    if(Common.markerlist.get (driverGeoModel.getKey ()) != null)
                            Common.markerlist.get (driverGeoModel.getKey ()).remove ();
                    Common.markerlist.remove (driverGeoModel.getKey ());
                    driverlocation.removeEventListener (this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make (getView (),error.getMessage (),Snackbar.LENGTH_SHORT).show ();
                }
            });
        }
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Snackbar.make (getView (),message,Snackbar.LENGTH_SHORT).show ();
    }
}