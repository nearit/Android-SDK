package it.near.sdk.Push;

import it.near.sdk.Reactions.Event;

/**
 * Action for communicate push tap.
 *
 * @author cattaneostefano.
 */
public class OpenPushEvent extends Event{
    public static final String PLUGIN_NAME = "OpenPush";

    String id;

    /**
     * Default constructor.
     * @param id push identifier.
     */
    public OpenPushEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getPlugin() {
        return PLUGIN_NAME;
    }
}
