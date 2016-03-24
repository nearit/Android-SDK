package it.near.sdk;

import it.near.sdk.Models.Content;
import it.near.sdk.Models.Matching;

/**
 * Created by cattaneostefano on 24/03/16.
 */
public interface ContentListener {

    /**
     * We also pass the matching along, to enable future elaborations
     *
     * @param content
     * @param matching
     */
    public abstract void onContentToDisplay(Content content, Matching matching);
}
