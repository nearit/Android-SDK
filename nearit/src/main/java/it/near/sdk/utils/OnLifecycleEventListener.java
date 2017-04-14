package it.near.sdk.utils;

/**
 * @author claudiosuardi
 */
public abstract class OnLifecycleEventListener {
    public abstract void onForeground();

    public abstract void onBackground();
}