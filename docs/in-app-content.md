# Handle In-app Content

After an user **taps on a notification**, you will receive content through an intent to your app launcher activity.
If you want to just check if the intent carries NearIT content use this method:
```java
boolean hasNearItContent = NearUtils.carriesNearItContent(intent);
```
To extract the content from an intent use the utility method:
```java
NearUtils.parseCoreContents(intent, coreContentListener);
```


If you want to customize the behavior of background notification see [this page](custom-bkg-notification.md).

## Beacon Interaction Content
Beacon interaction (beacon ranging) is a peculiar trigger that works only when your app is in the foreground.<br>
To receive this kind of content set a **proximity listener** with the method:
```java
{
    ...
    NearItManager.getInstance().addProximityListener(this);
    // remember to remove the listener when the object is being destroyed with 
    // NearItManager.getInstance().removeProximityListener(this);
    ...
}

@Override
public void foregroundEvent(Parcelable content, TrackingInfo trackingInfo) {
    // handle the event
    // To extract the content and to have it automatically casted to the appropriate object type
    NearUtils.parseCoreContents(content, trackingInfo, coreContentListener);
}   
```
**Warning:** For this kind of content you will need to write the code for **Trackings** and to eventually show an **In-app notification**.


## Trackings
NearIT allows to track user engagement events on recipes. Any recipe has at least two default events:

  - **Notified**: the user *received* a notification
  - **Engaged**: the user *tapped* on the notification
  
Usually the SDK tracks those events automatically, but if you write custom code to show notification or content (i.e. to receive Beacon interaction content) please make sure that at least the "**notified**" event is tracked.
<br>**Warning:** Failing in tracking this event cause some NearIT features to not work.


You can track **default or custom events** using the "**sendTracking**" method:
 
```java
// notified - notification received
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS);

// engaged - notification tapped
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.ENGAGED_STATUS);

// custom recipe event
NearItManager.getInstance().sendTracking(trackingInfo, "my awesome custom event");
```

## Content Objects

For each callback method of the *coreContentListener* you will receive a different content object.
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
```java
// rating must be an integer between 1 and 5, and you can set a comment string.
NearItManager.getInstance().sendEvent(new FeedbackEvent(feedback, rating, "Awesome"));
// the sendEvent method is available in 2 variants: with or without explicit callback handler. Example:
NearItManager.getInstance().sendEvent(new FeedbackEvent(...), responseHandler);
```
    
- `Coupon` with the following getters and fields:
    - `name` returns the coupon title
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
```java
NearItManager.getInstance().getCoupons(new CouponListener() {
	@Override
	public void onCouponsDownloaded(List<Coupon> list) {

	}

	@Override
	public void onCouponDownloadError(String s) {

	}
});
```
The method will also return already redeemed coupons so you get to decide to filter them if necessary.


