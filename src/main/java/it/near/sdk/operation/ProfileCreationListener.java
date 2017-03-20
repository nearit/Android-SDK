package it.near.sdk.Operation;

/**
 * @author cattaneostefano.
 */
public interface ProfileCreationListener {
    void onProfileCreated(boolean created, String profileId);
    void onProfileCreationError(String error);
}
