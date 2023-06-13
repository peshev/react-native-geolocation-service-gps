package com.agontuk.RNFusedLocation;

import android.annotation.SuppressLint;
import android.location.GnssStatus;

@SuppressLint("NewApi")
public class RNGnssSatellite implements RNSatellite {
  private final GnssStatus gnssStatus;
  private final int satelliteIndex;

  public RNGnssSatellite(GnssStatus gnssStatus, int satelliteIndex) {
    this.gnssStatus = gnssStatus;
    this.satelliteIndex = satelliteIndex;
  }

  @Override
  public int getSvid() {
    return gnssStatus.getSvid(satelliteIndex);
  }

  @Override
  public float getCn0DbHz() {
    return gnssStatus.getCn0DbHz(satelliteIndex);
  }

  @Override
  public float getElevationDegrees() {
    return gnssStatus.getElevationDegrees(satelliteIndex);
  }

  @Override
  public float getAzimuthDegrees() {
    return gnssStatus.getAzimuthDegrees(satelliteIndex);
  }

  @Override
  public boolean hasEphemerisData() {
    return gnssStatus.hasEphemerisData(satelliteIndex);
  }

  @Override
  public boolean hasAlmanacData() {
    return gnssStatus.hasAlmanacData(satelliteIndex);
  }

  @Override
  public boolean usedInFix() {
    return gnssStatus.usedInFix(satelliteIndex);
  }

  @Override
  public boolean hasCarrierFrequencyHz() {
    return gnssStatus.hasCarrierFrequencyHz(satelliteIndex);
  }

  @Override
  public float getCarrierFrequencyHz() {
    return gnssStatus.getCarrierFrequencyHz(satelliteIndex);
  }
}
