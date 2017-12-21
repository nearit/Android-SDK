# Handle In-app Content

After an user **taps on a notification**, you will receive content through an intent to your app launcher activity.
 
If you want to check whether the intent carries NearIT content, use this method:

<div class="code-java">
boolean hasNearItContent = NearUtils.carriesNearItContent(intent);
</div>
<div class="code-kotlin">
var hasNearItContent = NearUtils.carriesNearItContent(intent)
</div>
To extract the content from an intent use the utility method:
<div class="code-java">
NearUtils.parseContents(intent, contentListener);
</div>
<div class="code-kotlin">
NearUtils.parseContents(intent, contentListener)
</div>

If you want to customize the behavior of background notification see [this page](custom-bkg-notification.md).

## Intent catching

A good strategy to handle the intent, is in the **onNewIntent** activity method, but also check for **onPostCreate** method, so that you can cover most combinations of activity *launch mode* and activity foreground status. 
<div class="code-java">
@Override
protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    onNewIntent(getIntent());
}
@Override
protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (intent != null &&
            intent.getExtras() != null &&
            NearUtils.carriesNearItContent(intent)) {
        // we got a NearIT intent
        NearUtils.parseCoreContents(intent, this);
    }
}
</div>
<div class="code-kotlin">
override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    onNewIntent(intent)
}
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    if (intent != null &&
            intent.extras != null &&
            NearUtils.carriesNearItContent(intent)) {
        // we got a NearIT intent
        NearUtils.parseCoreContents(intent, this)
    }
}
</div>

If your launcher is a splash activity, and you wish to handle our intent in another activity, you should pass the intent extras with the NearIT content to the next activity. For example:
<div class="code-java">
@Override
protected void onNewIntent(Intent intent) {
    Intent nextActivityIntent = new Intent(this, NextActivity.class);
    nextActivityIntent.putExtras(intent.getExtras());
    // eventually you can add other extras and flags to the intent
    startActivity(nextActivityIntent);
}
</div>
<div class="code-kotlin">
override fun onNewIntent(intent: Intent?) {
    intent?.let {
        val nextActivityIntent = Intent(this, NextActivity::class.java)
        nextActivityIntent.putExtras(it.extras)
        // eventually you can add other extras and flags to the intent
        startActivity(nextActivityIntent) 
    }
}
</div>

## Content Objects

For each callback method of the *contentListener* you will receive a different content object.
Every object has a `notificationMessage` public field and a `getId()` getter method.
Here are the public fields for every other one:

- `SimpleNotification` with the following fileds:
    - `message` returns the notification message (it is the same as `notificationMessage`)
    
- `Content` for the notification with content, with the following getters and fields:
    - `title` returns the content title
    - `contentString` returns the text content
    - `cta` returns a `ContentLink` with a label and url fields.
    - `getImageLink()` returns an *ImageSet* object containing the links for the image
    
- `Feedback` with the following getters and fields:
    - `question` returns the feedback request string
    - `getRecipeId()` returns the recipeId associated with the feedback (you'll need it for answer it)
To give a feedback call this method:

<div class="code-java">
// rating must be an integer between 1 and 5, and you can set a comment string.
NearItManager.getInstance().sendEvent(new FeedbackEvent(feedback, rating, "Awesome"));
// the sendEvent method is available in 2 variants: with or without explicit callback handler. Example:
NearItManager.getInstance().sendEvent(new FeedbackEvent(...), responseHandler);
</div>
<div class="code-kotlin">
// rating must be an integer between 1 and 5, and you can set a comment string.
NearItManager.getInstance().sendEvent(FeedbackEvent(feedback, rating, "Awesome"))
// the sendEvent method is available in 2 variants: with or without explicit callback handler. Example:
NearItManager.getInstance().sendEvent(FeedbackEvent(...), responseHandler)
</div>
    
- `Coupon` with the following getters and fields:
    - `getTitle()` returns the coupon title
    - `description` returns the coupon description
    - `value` returns the value string
    - `expires_at` returns the expiring date (as a string), might be null
    - `getExpiresAtDate()` returns a the expiring Date object. Since coupon validity period is timezone related, consider showing the time of day.
    - `redeemable_from` returns the validity start date (as a string), might be null
    - `getRedeemableFromDate()` returns the validity start Date object. Since coupon validity period is timezone related, consider showing the time of day.
    - `getIconSet()` returns an *ImageSet* object containing the source links for the icon
    - `getSerial()` returns the serial code of the single coupon as a string
    - `getClaimedAt()` returns the claimed date (when the coupon was earned) of the coupon as a string
    - `getClaimedAtDate()` returns the claimed Date object.
    - `getRedeemedAt()` returns the redeemed date (when the coupon was used) of the coupon as a string
    - `getRedeemedAtDate()` returns the redeemed Date object.
    
- `CustomJSON` with the following fields:
    - `content` returns the json content as an *HashMap<String, Object>* (just like Gson)

## Fetch current user coupon

We handle the complete emission and redemption coupon cycle in our platform, and we deliver a coupon content only when a coupon is emitted (you will not be notified of recipes when a profile has already received the coupon, even if the coupon is still valid).
You can ask the library to fetch the list of all the user current coupons with the method:
<div class="code-java">
NearItManager.getInstance().getCoupons(new CouponListener() {
	@Override
	public void onCouponsDownloaded(List<Coupon> list) {
	}
	@Override
	public void onCouponDownloadError(String s) {
    }
});
</div>
<div class="code-kotlin">
NearItManager.getInstance().getCoupons(object : CouponListener {
    override fun onCouponsDownloaded(coupons: MutableList<Coupon>?) {
    }
    override fun onCouponDownloadError(s: String) {
    }
})
</div>
The method will also return already redeemed coupons so you get to decide to filter them if necessary.


