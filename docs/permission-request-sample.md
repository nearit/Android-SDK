# Best practice on permission request

To fully make use of NearIT features, the app needs to have ACCESS_FINE_LOCATION permissions. It also needs the location settings to be enabled. The Bluetooth settings are necessary just for beacons discovery. We ask for the ACCESS_FINE_LOCATION permissions in the manifest file of the library, so apps targeting up to Lollipop, should be fine.

If you are targeting a more modern Android version (you should), you must ask the user for location permissions at run-time. You can follow the Android documentation available on the subject, but we feel the issue is significant enough to include our own sample with the best practices on the subject.

This code was extensively used on a variety of devices, covering different versions and localizations. The sample asks for the right *location permissions* to be enabled for the app and also asks the user to turn the *location services* on. Those are very different issues and the difference can be easily overlooked. It also asks for Bluetooh and BLE location enabling in a way that covers all the different Android versions. All the communication with the user is done through system handled pop-ups so all the messages are localized and the manufacturer customization are valid. A user can give consent and turn everything on without leaving the app, or opening the notification drawer.

Another key objective of this sample is to only asks for the minimum necessary permissions.

You can find the full sample [here](https://github.com/nearit/Android-samples/blob/master/Activities/PermissionsActivity.java) with sensible comments.
