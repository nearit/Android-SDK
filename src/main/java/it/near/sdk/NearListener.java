package it.near.sdk;

/**
 * Created by cattaneostefano on 24/03/16.
 */
public interface NearListener {

    /**
     * Notify listener of ranged beacon inside his proximity
     *
     */
    public abstract void onContentToDisplay();

}
