package com.agontuk.RNFusedLocation;

import android.location.GpsSatellite;

@SuppressWarnings("deprecation")
public class RNGpsSatellite implements Satellite {
  private final GpsSatellite gpsSatellite;

  public RNGpsSatellite(GpsSatellite gpsSatellite) {
    this.gpsSatellite = gpsSatellite;
  }

  @Override
  public int getSvid() {
    return gpsSatellite.getPrn(); // TODO
  }

  @Override
  public float getCn0DbHz() {
    return gpsSatellite.getSnr();
  }

  @Override
  public float getElevationDegrees() {
    return gpsSatellite.getElevation();
  }

  @Override
  public float getAzimuthDegrees() {
    return gpsSatellite.getAzimuth();
  }

  @Override
  public boolean hasEphemerisData() {
    return gpsSatellite.hasEphemeris();
  }

  @Override
  public boolean hasAlmanacData() {
    return gpsSatellite.hasAlmanac();
  }

  @Override
  public boolean usedInFix() {
    return gpsSatellite.usedInFix();
  }

  @Override
  public boolean hasCarrierFrequencyHz() {
    return false;
  }

  @Override
  public float getCarrierFrequencyHz() {
    return 0;
  }
}
