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

If you want to set a **date** as a value, you have to use the format `MM-DD-YYYY`.

If you want to set a multiple choice data point:
<div class="code-java">
NearMultipleChoiceDataPoint multipleChoice = new NearMultipleChoiceDataPoint();
multipleChoice.put("food", true);
multipleChoice.put("drink", true);
multipleChoice.put("exercise", false);
NearItManager.getInstance().setUserData("interests", multipleChoice);
</div>
<div class="code-kotlin">
val multipleChoice = NearMultipleChoiceDataPoint()
multipleChoice["food"] = true
multipleChoice["drink"] = true
multipleChoice["exercise"] = false
NearItManager.getInstance().setUserData("interests", multipleChoice)
</div>

**WARNING:** With multiple choice data points you should always provide an object with values for all the keys, even keys whose value did not change. Keys missing from the object will have their value overridden by `false`.

You can delete the value for a certain user data point, passing null as the value for its key:
<div class="code-java">
NearItManager.getInstance().setUserData("gender", (String) null);
NearItManager.getInstance().setUserData("interests", (NearMultipleChoiceDataPoint) null);
</div>
<div class="code-kotlin">
NearItManager.getInstance().setUserData("gender", null as NearMultipleChoiceDataPoint?)
NearItManager.getInstance().setUserData("interests", null as String?)
</div>

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

Whenever a user **signs out** from your app, you should reset the NearIT profileID:
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

## Opt-out

You can **opt-out** a profile and its device:
<div class="code-java">
NearItManager.getInstance().optOut(new OptOutNotifier() {
            @Override
            public void onSuccess() {
                //  opt-out successful
            }
            @Override
            public void onFailure(String error) {
                //  network call failed, opt-out will affect the device only
            }
        });
</div>
<div class="code-kotlin">
NearItManager.getInstance().optOut(object : OptOutNotifier {
            override fun onSuccess() {
                //  opt-out successful
            }
            override fun onFailure(error: String) {
                //  network call failed, opt-out will affect the device only
            }
        })
</div>

If the opt-out call is successful all the **user-data** and **trackings** will be deleted and the **SDK will cease to work** (the user's devices will not receive further notifications).