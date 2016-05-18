package it.near.sdk.Operation;

/**
 * @author cattaneostefano.
 */
public interface DataPointNotifier {
    public abstract void onDataPointCreated();
    public abstract void onDataPointNotSetError(String error);
}
