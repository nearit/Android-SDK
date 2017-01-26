## Pro-Guard

If you are using pro-guard to minify your app, add this configuration in the proguard-rules.pro file for your app.
This will stop proguard from excluding the NearIT SDK classes from your final artifact.
This is mandatory to minify the app and still be able to use the SDK.

```
-dontwarn it.near.sdk.**
-keep class it.near.sdk.** { *; }
-keep interface it.near.sdk.** { *; }
```

## Logging

If you are scanning beacons you probably noticed the flood of logging messages, with either the `ScanRecord` or the `BluetoothLeScanner` tag. Those logs are created by the operating system and are due to always appear when doing BLE scans. To stop them from appearing in the logcat stream, create a custom filter and filter the two tags with a regex. Here's a screenshot of our filter.

![logfilter](logfilter.png "")
