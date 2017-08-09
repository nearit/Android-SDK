# Installation #

Minimum Requirements:
- Android API level: 15+

To start using the SDK, include this in your app *build.gradle*

```java
dependencies {
    compile 'it.near.sdk:nearit:2.1.39'
}
```

In the *onCreate* method of your Application class, initialize a *NearItManager* object, passing the API key as a String


```java
@Override
public void onCreate() {
  super.onCreate();
  NearItManager.init(this, "NEARIT API KEY");
  // calling this method on the Application onCreate is absolutely MANDATORY
}
```
You can find the API key on [NearIT web interface](https://go.nearit.com/), under the "SDK Integration" section.
Calling this method in the application onCreate callback is **MANDATORY**. You must not use lazy instantiation or use any other callback (in the Application or in any Activity class) to initialize the library.
In your app, you can access the NearItManager instance with 
```java
NearItManager.getInstance(context)
```

The initialization process for `NearItManager` will try to sync the recipes with our servers. If you need to sync the recipes configuration more often than you call the constructor, call this method:

```java
NearItManager.getInstance(context).refreshConfigs();
```

If you need feedback on whether the refresh was successfull or not, you can use this other version of the method:

```java
NearItManager.getInstance(context).refreshConfigs(recipeRefreshListener);
```
