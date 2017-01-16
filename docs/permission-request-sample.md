# Best practice on permission request

To fully make use of NearIT features, you need to have ACCESS_FINE_LOCATION permissions. We ask for those permissions in the manifest file of the library, so for Android target up to Lollipop, you should be fine.
If you are targeting a more modern Android version (you should), you must ask the user for the location permissions at run-time. You can follow the Android documentation available on the subject, but we feel the issue is significant enough to include our own sample with the best practices on the subject.
This code was extensively used on a variety of devices, covering different versions and localizations. The code asks for the right *location permissions* to enabled for the app and also asks the user to turn the *location services* on. Those are very different issues and the difference can be easily overlooked. It also asks for Bluetooh and BLE location enabling in a way that covers all the different Android versions. All the communication with the user is done through system handled pop-ups so all the messages are localized and the manufacturer customization are valid.

You can find the full sample [here](https://github.com/nearit/Android-samples/blob/master/Activities/PermissionsActivity.java) with sensible comments.
