package it.near.sdk.geopolis.beacons;

import org.junit.Test;

import it.near.sdk.geopolis.geofences.GeoFenceNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BeaconNodeTest {

    @Test
    public void shouldRecognizeBeacon_whenBeacon() {
        BeaconNode beacon = new BeaconNode();
        beacon.setProximityUUID("dummy_prox_id");
        beacon.setMajor(12345);
        beacon.setMinor(54321);
        assertThat(BeaconNode.isBeacon(beacon), is(true));
    }

    @Test
    public void shouldNotRecognizeBeacon_whenRegion() {
        BeaconNode region = new BeaconNode();
        region.setProximityUUID("dummy_prox_id");
        assertThat(BeaconNode.isBeacon(region), is(false));
        region.setMajor(98765);
        assertThat(BeaconNode.isBeacon(region), is(false));
    }

    @Test
    public void shouldNotRecognizeBeacon_whenGeofence() {
        GeoFenceNode geofence = new GeoFenceNode();
        geofence.setLatitude(1.2345);
        geofence.setLongitude(9.8765);
        geofence.setRadius(1.2345);
        assertThat(BeaconNode.isBeacon(geofence), is(false));
    }

}
