package it.near.sdk.recipes.models;

import it.near.sdk.morpheusnear.Resource;
import it.near.sdk.trackings.Events;

/**
 * @author cattaneostefano
 */
public class PulseAction extends Resource {
    public PulseAction() {
    }

    public boolean isForeground(){
        return getId().equals(Events.RANGE_FAR) ||
                getId().equals(Events.RANGE_NEAR) ||
                getId().equals(Events.RANGE_IMMEDIATE);
    }
}
