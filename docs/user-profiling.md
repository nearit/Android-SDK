# User Profiling

NearIT creates an anonymous profile for every user of your app. You can choose to add data to user profile. This data will be available inside recipes to allow the creation of user targets.

## Add User-data to NearIT

You can set user data with this method:
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

If you want to set a **date** as a value, you have to use the format "MM-DD-YYYY".

## Save the profile ID!

If you can, we recommend you to **store the NearIT profileID** in your CRM database for two main reasons:

- it allows you to link our analytics to your users
- it allows to associate all the devices of an user to the same NearIT profile.

You probably have a sign in system in your app, you should request the profile ID from NearIT before the user signs in:
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

If you detect that your user already has a NearIT profile ID in your CRM database (i.e. after a sign in), you should pass the profile ID to NearIT:
```java
NearItManager.getInstance().setProfileId(profileId);
```

Whenever a user **sings out** from your app, you should reset the NearIT profileID:
```java
NearItManager.getInstance().resetProfileId(new NearItUserProfile.ProfileFetchListener() {
            @Override
            public void onProfileId(String profileId) {
                // a new empty profile was generated
            }

            @Override
            public void onError(String error) {
                // your local profile was wiped, but no new profile was created
            }
        });
```
