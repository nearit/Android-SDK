package it.near.sdk.Operation;

/**
 * @author cattaneostefano.
 */
public interface ProfileCreationListener {
    public abstract void onProfileCreated(boolean created, String profileId);
    public abstract void onProfileCreationError(String error);
}
