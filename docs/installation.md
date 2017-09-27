# Installation #

Minimum Requirements:
- Android API level: 15+

To start using the SDK, include this in your app *build.gradle*

```java
dependencies {
    compile 'it.near.sdk:nearit:2.2.4'
}
```

In the project *build.gradle* make sure to include the following:
```java
buildscript {
    dependencies {
    ...
    classpath 'com.google.gms:google-services:3.1.0'
    }
}
allprojects {
    repositories {
        maven { url "https://maven.google.com" }
    }
}
```

In AndroidManifest.xml, add the following element as a child of the `<application>` element, by inserting it just before the closing `</application>` tag:

```xml
<meta-data
       android:name="near_api_key"
       android:value="<your-near-api-key>" />
```

You can find your API key on [NearIT web interface](https://go.nearit.com/), under the "SDK Integration" section.


##Manual Configuration Refresh##

The SDK **initialization is done automatically** and handles the task of syncing the recipes with our servers when your app starts up.
<br>However, if you need to sync the recipes configuration more often, you can call this method:

```java
NearItManager.getInstance().refreshConfigs();
```

If you need feedback on whether the refresh was successful or not, you can use this other version of the method:

```java
NearItManager.getInstance().refreshConfigs(recipeRefreshListener);
```
