package it.near.sdk.recipes;

/**
 * Created by cattaneostefano on 06/03/2017.
 */

public interface NearITEventHandler {
    void onSuccess();
    void onFail(int statusCode, String error);
}
