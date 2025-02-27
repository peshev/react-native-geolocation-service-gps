package com.agontuk.RNFusedLocation;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

public class SatelliteStatus {
  private final List<Satellite> satellites;

  public SatelliteStatus(List<Satellite> satellites) {
    this.satellites = satellites;
  }

  public List<Satellite> getSatellites() {
    return satellites;
  }

  public WritableMap serialize() {
    WritableMap result = Arguments.createMap();
    WritableArray satellitesArray = Arguments.createArray();
    for (Satellite satellite : getSatellites()) {
      WritableMap satelliteMap = Arguments.createMap();
      satelliteMap.putInt("svid", satellite.getSvid());
      satelliteMap.putDouble("cn0DbHz", satellite.getCn0DbHz());
      satelliteMap.putDouble("elevationDegrees", satellite.getElevationDegrees());
      satelliteMap.putDouble("azimuthDegrees", satellite.getAzimuthDegrees());
      satelliteMap.putBoolean("hasEphemerisData", satellite.hasEphemerisData());
      satelliteMap.putBoolean("hasAlmanacData", satellite.hasAlmanacData());
      satelliteMap.putBoolean("usedInFix", satellite.usedInFix());
      satelliteMap.putBoolean("hasCarrierFrequencyHz", satellite.hasCarrierFrequencyHz());
      satelliteMap.putDouble("carrierFrequencyHz", satellite.getCarrierFrequencyHz());
      satellitesArray.pushMap(satelliteMap);
    }
    result.putArray("satellites", satellitesArray);
    return result;
  }
}
