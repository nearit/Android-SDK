If you are using pro-guard to minify your app, add this configuration in the proguard-rules.pro file for your app.
This will stop proguard from excluding the NearIT SDK classes from your final artifact.

```
-dontwarn it.near.sdk.**
-keep class it.near.sdk.** { *; }
-keep interface it.near.sdk.** { *; }
```