# Installation #

Minimum Requirements:
- Android API level: 15+

To start using the SDK, include this in your app *build.gradle*

```java
dependencies {
    compile 'it.near.sdk.core:nearitsdk:2.0.5'
}
```

In the *onCreate* method of your Application class, initialize a *NearItManager* object, passing the API key as a String


```java
@Override
public void onCreate() {
  super.onCreate();
  nearItManager = new NearItManager(this, getResources().getString(R.string.nearit_api_key));
}
```

You can find the API key on the [NearIT web interface](https://go.nearit.com/), under the "SDK Integration" section.
