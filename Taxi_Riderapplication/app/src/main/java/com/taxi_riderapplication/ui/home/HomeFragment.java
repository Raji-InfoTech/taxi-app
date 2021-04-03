package com.taxi_riderapplication.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
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


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.taxi_riderapplication.R;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

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
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient (getContext ( ));
        if (ActivityCompat.checkSelfPermission (getContext ( ), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (getContext ( ), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates (locationRequest, locationCallback, Looper.myLooper ( ));


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
                        params.setMargins (0,0,0,50);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make (getView (),permissionDeniedResponse.getPermissionName ()+"need enable",Snackbar.LENGTH_SHORT).show ();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check ();

        try {
            boolean success =googleMap.setMapStyle (MapStyleOptions.loadRawResourceStyle (getContext (),R.raw.uber_maps_style));
            if(!success)
                Log.e ("Error","style parsing error");

        }catch (Resources.NotFoundException e){
            Log.e ("Error",e.getMessage ());
        }


    }
}