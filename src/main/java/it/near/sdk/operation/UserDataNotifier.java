package it.near.sdk.operation;

/**
 * @author cattaneostefano.
 */
public interface UserDataNotifier {
    void onDataCreated();
    void onDataNotSetError(String error);
}
