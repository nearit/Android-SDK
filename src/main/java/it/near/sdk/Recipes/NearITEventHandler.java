package it.near.sdk.Recipes;

/**
 * Created by cattaneostefano on 06/03/2017.
 */

public interface NearITEventHandler {
    public void onSuccess();
    public void onFail(int statusCode, String error);
}
