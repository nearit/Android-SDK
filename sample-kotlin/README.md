# How to run the sample

* Load the project in Android Studio
* Add a google-services.json Firebase config file to the sample directory. When you create your firebase app project, use the sample applicationId (com.nearit.sample) or edit the applicationId in the app `build.gradle` file to use your own firebase project applicationId
* Add a `nearit_api` variable to the app `local.properties` file, like so:
```
nearit_api=my-secret-near-api-key
```
* Build and run the sample
