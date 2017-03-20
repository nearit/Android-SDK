package it.near.sdk.Recipes.Models;

import it.near.sdk.MorpheusNear.Resource;
import it.near.sdk.Trackings.Events;

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
