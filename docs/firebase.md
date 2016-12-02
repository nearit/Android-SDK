To enable push notification with our platform you need to integrate firebase in your app.

We encourage to follow the official Firebase documentation.
As of late 2016, these are the instructions to integrate firebase:
* Create a project in the firebase console. Pick a name for your project and select your company country.
* Inside the project, select "Add Firebase to your Android app"
* Be sure to enter the right package name of your app
* Move the google-services.json file of the project in your app module root folder (not in the project root folder!)
* Add the proper dependencies in the project and app gradle file.

In the setting section of your Firebase project, you will find the cloud messaging server key to enter in the "Push notification" section of our CMS.

Do not follow FCM-specific instructions: that part of the process is automatically handled by our library. We ask FCM for a device token and sync it with the NearIT user. We also already include the proper services in our manifest.

If you experience build problems make sure to include the 9.6.1 version of any gms dependency.
Example:
```
compile 'com.google.android.gms:play-services-analytics:9.6.1'
```
