package it.near.sdk.Operation;

/**
 * @author cattaneostefano.
 */
public interface UserDataNotifier {
    public abstract void onDataCreated();
    public abstract void onDataNotSetError(String error);
}
