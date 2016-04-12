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

}
