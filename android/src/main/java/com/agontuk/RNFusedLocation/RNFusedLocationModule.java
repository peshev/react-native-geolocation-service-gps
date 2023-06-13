package com.agontuk.RNFusedLocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RNFusedLocationModule extends ReactContextBaseJavaModule implements ActivityEventListener, LocationChangeListener {
  public static final String TAG = "RNFusedLocation";
  private final HashMap<LocationProvider, PendingLocationRequest> pendingRequests;
  @Nullable
  private LocationProvider continuousLocationProvider;

  public RNFusedLocationModule(ReactApplicationContext reactContext) {
    super(reactContext);

    reactContext.addActivityEventListener(this);
    this.pendingRequests = new HashMap<>();

    Log.i(TAG, TAG + " initialized");
  }

  @NonNull
  @Override
  public String getName() {
    return TAG;
  }

  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    if (continuousLocationProvider != null &&
      continuousLocationProvider.onActivityResult(requestCode, resultCode)
    ) {
      return;
    }

    Set<LocationProvider> providers = pendingRequests.keySet();

    for (LocationProvider locationProvider : providers) {
      if (locationProvider.onActivityResult(requestCode, resultCode)) {
        return;
      }
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    //
  }

  @Override
  public void onLocationChange(LocationProvider locationProvider, Location location) {
    WritableMap locationData = LocationUtils.locationToMap(location);

    if (locationProvider.equals(continuousLocationProvider)) {
      emitEvent("geolocationDidChange", locationData);
      return;
    }

    PendingLocationRequest request = pendingRequests.get(locationProvider);

    if (request != null) {
      request.successCallback.invoke(locationData);
      pendingRequests.remove(locationProvider);
    }
  }

  @Override
  public void onLocationError(LocationProvider locationProvider, LocationError error, @Nullable String message) {
    WritableMap errorData = LocationUtils.buildError(error, message);

    if (locationProvider.equals(continuousLocationProvider)) {
      emitEvent("geolocationError", errorData);
      return;
    }

    PendingLocationRequest request = pendingRequests.get(locationProvider);

    if (request != null) {
      request.errorCallback.invoke(errorData);
      pendingRequests.remove(locationProvider);
    }
  }

  @ReactMethod
  public void getCurrentPosition(ReadableMap options, final Callback success, final Callback error) {
    ReactApplicationContext context = getContext();

    if (!LocationUtils.hasLocationPermission(context)) {
      error.invoke(LocationUtils.buildError(LocationError.PERMISSION_DENIED, null));
      return;
    }

    LocationOptions locationOptions = LocationOptions.fromReadableMap(options);
    final LocationProvider locationProvider = createLocationProvider(locationOptions.isForceLocationManager());

    pendingRequests.put(locationProvider, new PendingLocationRequest(success, error));
    locationProvider.getCurrentLocation(locationOptions);
  }

  @ReactMethod
  public void startObserving(ReadableMap options) {
    ReactApplicationContext context = getContext();

    if (!LocationUtils.hasLocationPermission(context)) {
      emitEvent(
        "geolocationError",
        LocationUtils.buildError(LocationError.PERMISSION_DENIED, null)
      );
      return;
    }

    LocationOptions locationOptions = LocationOptions.fromReadableMap(options);

    if (continuousLocationProvider == null) {
      continuousLocationProvider = createLocationProvider(locationOptions.isForceLocationManager());
    }

    continuousLocationProvider.requestLocationUpdates(locationOptions);
  }

  @ReactMethod
  public void stopObserving() {
    if (continuousLocationProvider != null) {
      continuousLocationProvider.removeLocationUpdates();
      continuousLocationProvider = null;
    }
  }

  @ReactMethod
  public void addListener(String eventName) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  private final Map<String, Object> satelliteStatusCallbacks = new HashMap<>();

  @SuppressLint("MissingPermission")
  @ReactMethod
  public String observeSatelliteStatus(Callback callback) {
    LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
    Object callbackObject;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      GnssStatus.Callback gnssStatusCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
          callback.invoke(new RNGnssStatus(IntStream
            .range(0, status.getSatelliteCount())
            .mapToObj(i -> new RNGnssSatellite(status, i))
            .collect(Collectors.toList())).serialize());
        }
      };
      locationManager.registerGnssStatusCallback(gnssStatusCallback);
      callbackObject = gnssStatusCallback;
    } else {
      GpsStatus.Listener gpsStatusListener = event -> {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        if (gpsStatus != null) {
          List<RNSatellite> satellites = new ArrayList<>();
          //noinspection deprecation
          for (GpsSatellite satellite : gpsStatus.getSatellites()) {
            satellites.add(new RNGpsSatellite(satellite));
          }
          callback.invoke(new RNGnssStatus(satellites).serialize());
        }
      };
      locationManager.addGpsStatusListener(gpsStatusListener);
      callbackObject = gpsStatusListener;
    }

    String key = callbackObject.toString();
    satelliteStatusCallbacks.put(key, callbackObject);
    return key;
  }

  @ReactMethod
  void stopObservingSatelliteStatus(String key) {
    LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
    Object callbackObject = satelliteStatusCallbacks.get(key);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
      callbackObject instanceof GnssStatus.Callback) {
      locationManager.unregisterGnssStatusCallback((GnssStatus.Callback) callbackObject);
    } else if (callbackObject instanceof GpsStatus.Listener) {
      locationManager.removeGpsStatusListener((GpsStatus.Listener) callbackObject);
    }
  }

  private LocationProvider createLocationProvider(boolean forceLocationManager) {
    ReactApplicationContext context = getContext();
    boolean playServicesAvailable = LocationUtils.isGooglePlayServicesAvailable(context);

    if (forceLocationManager || !playServicesAvailable) {
      return new LocationManagerProvider(context, this);
    }

    return new FusedLocationProvider(context, this);
  }

  private void emitEvent(String eventName, WritableMap data) {
    getContext().getJSModule(RCTDeviceEventEmitter.class).emit(eventName, data);
  }

  private ReactApplicationContext getContext() {
    return getReactApplicationContext();
  }

  private static class PendingLocationRequest {
    final Callback successCallback;
    final Callback errorCallback;

    public PendingLocationRequest(Callback success, Callback error) {
      this.successCallback = success;
      this.errorCallback = error;
    }
  }
}
