package com.taxi_riderapplication.Callback;


import com.taxi_riderapplication.DriverGeoModel;

public interface IFirebaseDriverInfoListener {
    void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel);
}
