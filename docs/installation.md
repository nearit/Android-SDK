# Installation #

Minimum Requirements:
- Android API level: 15+

To start using the SDK, include this in your app *build.gradle*

```java
dependencies {
    compile 'it.near.sdk:nearit:2.6.1_sample'
}
```

In the project *build.gradle* make sure to include the following:
```java
buildscript {
    dependencies {
    ...
    classpath 'com.google.gms:google-services:3.1.1'
    }
}
allprojects {
    repositories {
        maven { url "https://maven.google.com" }
    }
}
```

In your app module `build.gradle` make sure that your `compileSdkVersion` is at least 26.

In AndroidManifest.xml, add the following element as a child of the `<application>` element, by inserting it just before the closing `</application>` tag:

```xml
<meta-data
       android:name="near_api_key"
       android:value="<your-near-api-key>" />
```

You can find your API key on <a href="https://go.nearit.com/" target="_blank">**NearIT web interface**</a>, under the "SDK Integration" section.


## Enable Test Devices

Test Devices allow you to test NearIT features on single devices and is extremely useful for debugging.

Navigate **"Settings > Test devices"** section of NearIT and follow the instructions to enable this feature.

In the same section, you can send invite links to mail addresses. If users have the app installed, they can click the link on their smart-phone to be prompted with a request to enroll their device among the testers.

