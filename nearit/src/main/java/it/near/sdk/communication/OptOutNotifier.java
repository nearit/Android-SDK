package it.near.sdk.communication;

/**
 * @author federico.boschini
 */

public interface OptOutNotifier {
    public void onSuccess();
    public void onFailure(String error);
}
