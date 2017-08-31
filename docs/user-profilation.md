# User Profiling

NearIT creates an anonymous profile for every user of your app. You can choose to add data to user profile. This data will be available inside recipes to allow the creation of user targets.

## Send User-Data to NearIT

We automatically create an anonymous profile for every installation of the app. You can check that a profile was created by checking the existance of a profile ID.
```java
String profileId = NearItManager.getInstance().getProfileId();
```
If the result is null, it means that no profile is associated with the app installation (probably due to a network error). The SDK will re-try to create a profile at every start, and every time a new user data is set.

After the profile is created set user data:
```java
NearItManager.getInstance().setUserData("name", "John", new UserDataNotifier() {
    @Override
    public void onDataCreated() {
        // data was set/created                                                
    }
                                                       
    @Override
    public void onDataNotSetError(String error) {
        // there was an error                        
    }
});
```

If you have multiple data properties, set them in batch:
```java
HashMap<String, String> userDataMap = new HashMap<>();
userDataMap.put("name", "John");
userDataMap.put("age", "23");           // set everything as String
userDataMap.put("saw_tutorial", "true") // even booleans, the server has all the right logic
NearItManager.getInstance().setBatchUserData(userDataMap, new UserDataNotifier() {
            @Override
            public void onDataCreated() {
                // data was set/created 
            }

            @Override
            public void onDataNotSetError(String error) {

            }
        });
```
If you try to set user data before creating a profile the error callback will be called.

If you want to reset your profile use this method:
```java
NearItManager.getInstance().resetProfileId()
```
Further calls to *getProfileId()* will return null.
A creation of a new profile after the reset will create a profile with no user data.

## Link NearIT profiles with an external User Database

You might want to link users in your CRM database with NearIT profiles. You can do it by storing the NearIT profileID in your CRM database. This way, you can link our analytics with your own user base and associate all the devices of an user to the same NearIT profile.
Furthermore, if you detect that your user already has a NearIT profileID in your CRM database, you can manually set it on a local app installation with the method:
```java
NearItManager.getInstance().setProfileId(profileId);
```
You can then set the relevant user-data to this profile with the aforementioned methods.

Please keep in mind that you will be responsible of storing our profile identifier in your system.
