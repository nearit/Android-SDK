package it.near.sdk.Operation;

/**
 * @author cattaneostefano.
 */
public interface ProfileCreationListener {
    public abstract void onProfileCreated();
    public abstract void onProfileCreationError(String error);
}
