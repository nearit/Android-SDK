# User Profiling

NearIT creates an anonymous profile for every user of your app. You can choose to add data to user profile. This data will be available inside recipes to allow the creation of user targets.

## Add User-data to NearIT

You can set user data with this method:
<div class="code-java">
NearItManager.getInstance().setUserData("name", "John");
</div>
<div class="code-kotlin">
NearItManager.getInstance().setUserData("name", "John")
</div>

If you have multiple data properties, set them in batch:
<div class="code-java">
HashMap<String, String> userDataMap = new HashMap<>();
userDataMap.put("name", "John");
userDataMap.put("age", "23");           // set everything as String
userDataMap.put("saw_tutorial", "true") // even booleans, the server has all the right logic
NearItManager.getInstance().setBatchUserData(userDataMap);
</div>
<div class="code-kotlin">
val userDataMap: HashMap<String, String> = hashMapOf()
with(userDataMap) {
    put("name", "John")
    put("age", "23")            // set everything as String
    put("saw_tutorial", "true") // even booleans, the server has all the right logic
}
NearItManager.getInstance().setBatchUserData(userDataMap)
</div>

If you want to set a **date** as a value, you have to use the format "MM-DD-YYYY".

## Save the profile ID!

If you can, we recommend you to **store the NearIT profileID** in your CRM database for two main reasons:

- it allows you to link our analytics to your users
- it allows to associate all the devices of an user to the same NearIT profile.

You probably have a sign in system in your app, you should request the profile ID from NearIT before the user signs in:
<div class="code-java">
NearItManager.getInstance().getProfileId(new NearItUserProfile.ProfileFetchListener() {
    @Override
    public void onProfileId(String profileId) {
        NearLog.d(TAG, "save this on your CRM: " + profileId);
    }
    @Override
    public void onError(String error) {
        // there was an error
    }
});
</div>
<div class="code-kotlin">
NearItManager.getInstance().getProfileId(object : NearItUserProfile.ProfileFetchListener {
    override fun onProfileId(profileId: String) {
        NearLog.d(TAG, "save this on your CRM: " + profileId)
    }
    override fun onError(error: String) {
        // there was an error
    }
})
</div>

If you detect that your user already has a NearIT profile ID in your CRM database (i.e. after a sign in), you should pass the profile ID to NearIT:
<div class="code-java">
NearItManager.getInstance().setProfileId(profileId);
</div>
<div class="code-kotlin">
NearItManager.getInstance().profileId = profileId
</div>

Whenever a user **sings out** from your app, you should reset the NearIT profileID:
<div class="code-java">
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
</div>
<div class="code-kotlin">
NearItManager.getInstance().resetProfileId(object : NearItUserProfile.ProfileFetchListener {
    override fun onProfileId(profileId: String) {
        // a new empty profile was generated
    }
    override fun onError(error: String) {
        // your local profile was wiped, but no new profile was created
    }
})
</div>
