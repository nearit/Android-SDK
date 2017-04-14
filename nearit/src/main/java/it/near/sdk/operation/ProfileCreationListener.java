package it.near.sdk.operation;

/**
 * @author cattaneostefano.
 */
public interface ProfileCreationListener {
    void onProfileCreated(boolean created, String profileId);

    void onProfileCreationError(String error);
}
