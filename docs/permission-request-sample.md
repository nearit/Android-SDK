# Best practice on permission request

To fully make use of NearIT features, you need to have ACCESS_FINE_LOCATION permissions. We ask for those permissions in the manifest file of the library, so for Android target up to Lollipop, you should be fine.
If you are targeting a more modern Android version (you should), you must ask the user for the location permissions at run-time. You can follow the Android documentation available on the subject, but we feel the issue is significant enough to include our own sample with the best practices on the subject.
This code was extensively used on a variety of devices, covering different versions and localizations.

You can find the full sample [here](https://github.com/nearit/Android-samples/blob/master/Activities/PermissionsActivity.java) with sensible comments.
