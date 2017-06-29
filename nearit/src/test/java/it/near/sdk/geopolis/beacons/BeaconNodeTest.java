package it.near.sdk.geopolis.beacons;

import org.junit.Test;

import it.near.sdk.geopolis.geofences.GeoFenceNode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BeaconNodeTest {

    @Test
    public void shouldRecognizeBeacon_whenBeacon() {
        BeaconNode beacon = new BeaconNode();
        beacon.proximityUUID = "dummy_prox_id";
        beacon.major = 12345;
        beacon.minor = 54321;
        assertThat(BeaconNode.isBeacon(beacon), is(true));
    }

    @Test
    public void shouldNotRecognizeBeacon_whenRegion() {
        BeaconNode region = new BeaconNode();
        region.proximityUUID = "dummy_prox_id";
        assertThat(BeaconNode.isBeacon(region), is(false));
        region.major = 98765;
        assertThat(BeaconNode.isBeacon(region), is(false));
    }

    @Test
    public void shouldNotRecognizeBeacon_whenGeofence() {
        GeoFenceNode geofence = new GeoFenceNode();
        geofence.latitude = 1.2345;
        geofence.longitude = 9.8765;
        geofence.radius = 1.2345;
        assertThat(BeaconNode.isBeacon(geofence), is(false));
    }

}
