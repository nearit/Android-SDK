package it.near.sdk.recipes;

public interface NearITEventHandler {
    void onSuccess();

    void onFail(int statusCode, String error);
}
