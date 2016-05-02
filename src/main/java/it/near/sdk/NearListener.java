package it.near.sdk;

/**
 * @author cattaneostefano
 */
public interface NearListener {

    /**
     * Notify listener of ranged beacon inside his proximity
     *
     */
    public abstract void onContentToDisplay();

}
