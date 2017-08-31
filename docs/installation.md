# Installation #

Minimum Requirements:
- Android API level: 15+

To start using the SDK, include this in your app *build.gradle*

```java
dependencies {
    compile 'it.near.sdk:nearit:2.2.0'
}
```
In AndroidManifest.xml, add the following element as a child of the `<application>` element, by inserting it just before the closing `</application>` tag:

```xml
<meta-data
       android:name="near_api_key"
       android:value="<your-near-api-key>" />
```
then, re-build your application.

You can find the API key on [NearIT web interface](https://go.nearit.com/), under the "SDK Integration" section.

In your app, you can access the NearItManager instance with 
```java
NearItManager.getInstance();
```

The initialization process for `NearItManager` will try to sync the recipes with our servers. If you need to sync the recipes configuration more often than you call the constructor, call this method:

```java
NearItManager.getInstance().refreshConfigs();
```

If you need feedback on whether the refresh was successfull or not, you can use this other version of the method:

```java
NearItManager.getInstance().refreshConfigs(recipeRefreshListener);
```
