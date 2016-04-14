package it.near.sdk.Recipes;

import org.altbeacon.beacon.Region;

import it.near.sdk.Models.Matching;

/**
 * Created by cattaneostefano on 24/03/16.
 */
public interface NearNotifier {
    public abstract void onRuleFullfilled(Matching matching);
    public abstract void onEnterRegion(Region region);
    public abstract void onExitRegion(Region region);
}
