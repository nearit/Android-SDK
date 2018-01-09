## NearIT version

We encourage you to use the latest SDK version for all your supported platforms. If you are using a deprecated method from our library, switch to the suggested new method, as described in the deprecation description. We only support the latest major release with bug fixes.

## Certificates and keys

As you switch to your release build, be sure to switch all your certificates and keys.
If you are using different NearIT apps for your production and staging environments, be sure to switch the NearIT api key in your app. Also make sure that user data mapping is set up with the same keys in both environments.
If you are using different Firebase projects, switch your google-services.json in your app, and be sure to have the right Firebase Cloud Messaging Server Key in the corresponding NearIT app push notification section.
As a good practices we suggest that you map your different environments with Android flavours as it proves to be the most consistent way to maintain different environments configurations.

## Test Devices

To enable the Test Device feature, remember to include the necessary code (link alla sezione) on your manifest. Using test devices is also a good way to manually check if push notifications settings are working properly even in a production build.

## Privacy Policy

In you app listing privacy policy, please make sure to include this:
```Legalese```
