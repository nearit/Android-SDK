package it.near.sdk;

import org.altbeacon.beacon.Region;

import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;

/**
 * Created by cattaneostefano on 24/03/16.
 */
public interface NearListener {

    /**
     * Notify listener of ranged beacon inside his proximity
     *
     * @param content
     * @param matching
     */
    public abstract void onContentToDisplay(Content content, Matching matching);

    /**
     * Notify listener of region entered and assigned content
     *
     *  @param content
     * @param region
     */
    public abstract void onRegionEntered(Region region, Content content);

    /**
     * Notify listener of region exited and assigned content (mostly null)
     *
     * @param region
     * @param content
     */
    public abstract void onRegionExited(Region region, Content content);
}
