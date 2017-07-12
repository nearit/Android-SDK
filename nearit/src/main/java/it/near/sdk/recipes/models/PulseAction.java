package it.near.sdk.recipes.models;

import it.near.sdk.morpheusnear.Resource;
import it.near.sdk.geopolis.trackings.Events;

/**
 * @author cattaneostefano
 */
public class PulseAction extends Resource {
    public PulseAction() {
    }

    public boolean isForeground(){
        return  getId().equals(Events.RANGE_NEAR.event) ||
                getId().equals(Events.RANGE_IMMEDIATE.event);
    }
}
