# Best Practice on Permission Request

To fully use NearIT features, the app needs to have ACCESS_FINE_LOCATION permissions. It also needs the location settings to be enabled. The Bluetooth settings are necessary just for beacons discovery. We ask for the ACCESS_FINE_LOCATION permissions in the manifest file of the library, so apps targeting up to Lollipop, should be fine.

If you are targeting a more modern Android version (you should), you must ask the user for location permissions at run-time. You can follow the Android documentation available on the topic, but we feel the issue is significant enough to include our own sample with the best practices on the topic.

This code was extensively used on a variety of devices, covering different versions and localizations. The sample asks for the right *location permissions* to be enabled for the app and it also asks the user to turn the *location services* on. Those are very different issues and the difference can be easily overlooked. It also asks for Bluetooh and BLE location enabling in a way that covers all the different Android versions. All user interaction is done through system handled pop-ups so all the messages are localized and the manufacturer customization is applied. A user can give consent and turn everything on without leaving the app, or opening the notification drawer.

Another key goal of this sample is just to ask for the minimum necessary permissions.

You can find the full sample [here](https://github.com/nearit/Android-samples/blob/master/Activities/PermissionsActivity.java) with sensible comments.
