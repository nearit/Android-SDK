# Best Practice on Permission Request

To fully use NearIT features, the app needs to have ACCESS_FINE_LOCATION permissions. It also needs the location settings to be enabled. The Bluetooth settings are necessary just for beacons discovery. We ask for the ACCESS_FINE_LOCATION permissions in the manifest file of the library, so apps targeting up to Lollipop, should be fine.

If you are targeting a more modern Android version (you should), you must ask the user for location permissions at run-time. You can follow the Android documentation available on the topic.
[Requesting permissions](https://developer.android.com/training/permissions/requesting.html)

Additionally to the location permission request, you should also ask the user to turn the location system settings on. This can be done programmatically via system handled dialogs and without leaving the app.
[Changing location settings](https://developer.android.com/training/location/change-location-settings.html)

The last check should be on the Bluetooth system settings. Just like with the location settings, you can ask the user to turn Bluetooth on through a system dialog.
[Turning Bluetooth on](https://developer.android.com/guide/topics/connectivity/bluetooth.html#SettingUp)
