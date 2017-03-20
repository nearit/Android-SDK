package it.near.sdk.Operation;

/**
 * @author cattaneostefano.
 */
public interface UserDataNotifier {
    void onDataCreated();
    void onDataNotSetError(String error);
}
