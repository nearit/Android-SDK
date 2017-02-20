# Firebase Help

To enable push notification with our platform you need to integrate firebase in your app.
We encourage to follow the official Firebase documentation.

[Official Firebase Documentation](https://firebase.google.com/)


As of late 2016, these are the instructions to integrate firebase:

- Create a project in the firebase console. Pick a name for your project and select your company country. (If you already had a project from the old console you can import it to Firebase)

- Inside the project, select "Add Firebase to your Android app"

- Be sure to enter the right package name of your app

- Move the *google-services.json* file of the project in your **app module** root folder (not in the project root folder!)

- Add the proper dependencies in the project and app gradle file.


In the setting section of your Firebase project, you can find the cloud messaging server key to enter in the "Push notification" section of our web interface.

Do not follow FCM-specific instructions: our library automatically handles this part of the process. We ask FCM servers for a device token and sync it with NearIT user. Moreover, we have already included the proper services in our manifest.

If you experience build or runtime problems with google play services components, make sure to include the 10.0.1 version of any gms dependency in your app.
Example:
```
compile 'com.google.android.gms:play-services-analytics:10.0.1'
```
