# User Profiling

NearIT creates an anonymous profile for every user of your app. You can choose to add data to user profile. This data will be available inside recipes to allow the creation of user targets.

## Send User-Data to NearIT

We automatically create an anonymous profile for every installation of the app. You can get your profileId like this:
```java
NearItManager.getInstance().getProfileId(new NearItUserProfile.ProfileFetchListener() {
            @Override
            public void onProfileId(String profileId) {
                NearLog.d(TAG, "your profile: " + profileId);
            }

            @Override
            public void onError(String error) {

            }
        });
```
If the result is null, it means that no profile is associated with the app installation (probably due to a network error). The SDK will re-try to create a profile at every start, and every time a new user data is set.

After the profile is created, you can set user data:
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
NearItManager.getInstance().resetProfileId(new NearItUserProfile.ProfileFetchListener() {
            @Override
            public void onProfileId(String profileId) {
                NearLog.d(TAG, "profile reset: " + profileId);
            }

            @Override
            public void onError(String error) {

            }
        });
```
Be aware that this profile will have no user data.

## Save the profile ID!

If you can, we recommend you to store the NearIT profileID in your CRM database for two main reasons:

- it allows you to link our analytics to your users
- it allows to associate all the devices of an user to the same NearIT profile.


Getting the local profile ID of an user is easy:
```java
NearItManager.getInstance().getProfileId(new NearItUserProfile.ProfileFetchListener() {
            @Override
            public void onProfileId(String profileId) {
                NearLog.d(TAG, "save this on your CRM: " + profileId);
            }

            @Override
            public void onError(String error) {

            }
        });
```


If you detect that your user already has a NearIT profileID in your CRM database (i.e. after a login), you should manually write it on a local app installation:
```java
NearItManager.getInstance().setProfileId(profileId);
```