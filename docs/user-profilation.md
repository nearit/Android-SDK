
To profile users, you need to either create a new profile in our server or pass us a profileId obtained from your authentication services in the SDK.

To register an user in our platform call the method
```java
NearItUserProfile.createNewProfile(context, new ProfileCreationListener() {
    @Override
    public void onProfileCreated() {
        // your profile was created
    }
                                            
    @Override
    public void onProfileCreationError(String error) {
        // there was an error
    }
});
```
Calling this method multiple times will results in multiple profiles being created, each time with no profilation data.

To be sure to call this method only when necessary, check if you already created a profile with this method
```java
String profileId = NearItUserProfile.getProfileId(context);
```
If the result is null, it means that no profile is associated with the app installation.

After the profile is created set user data
```java
NearItUserProfile.setUserData(context, "name", "John", new UserDataNotifier() {
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

If you have multiple data properties, set them in batch
```java
HashMap<String, String> userDataMap = new HashMap<>();
userDataMap.put("name", "John");
userDataMap.put("age", "23");           // set everything as String
userDataMap.put("saw_tutorial", "true") // even booleans, the server has all the right logic
NearItUserProfile.setBatchUserData(context, userDataMap, new UserDataNotifier() {
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

If you want to set a profileId manually (if it's coming from your user management systems) use the method
```java
NearItUserProfile.setProfileId(context, profileId);
```

If you want to reset your profile use this method
```java
NearItUserProfile.resetProfileId(context)
```
Further calls to NearItUserProfile.getProfileId(context) will return null.
