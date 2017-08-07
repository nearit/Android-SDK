# How to run the sample

* Load the project in Android Studio
* Edit the app module `build.gradle` file to either depend on the local NearIT module, or on the published NearIt library. We raccomend to depend on the published library version.
```java
    // this is to depend on the published library
    compile 'it.near.sdk:nearit:X.Y.Z'
    // this is to depend on the local library
    compile project(':nearit')
```
* Add a google-services.json Firebase config file to the sample directory. When you create your firebase app project, use the sample applicationId (com.nearit.sample) or edit the applicationId in the app `build.gradle` file to use your own applicationId
* Add a `nearit_api` variable to the app `local.properties` file, like so:
```
nearit_api=my-secret-near-api-key
```
* Build and run the sample
