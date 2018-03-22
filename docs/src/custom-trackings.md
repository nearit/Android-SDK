## Trackings
NearIT allows to track user engagement events on recipes. Any recipe has at least two default events:

  - **Notified**: the user *received* a notification
  - **Engaged**: the user *tapped* on the notification
  
Usually the SDK tracks those events automatically, but if you write custom code to show notification or content (i.e. to receive Beacon interaction content) please make sure that at least the "**notified**" event is tracked.
<br>**Warning:** Failing in tracking this event cause some NearIT features to not work.

You can track **default or custom events** using the "**sendTracking**" method:

<div class="code-java">
// notified - notification received
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS);

// engaged - notification tapped
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.ENGAGED_STATUS);

// custom recipe event
NearItManager.getInstance().sendTracking(trackingInfo, "my awesome custom event");
</div>
<div class="code-kotlin">
// notified - notification received
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.NOTIFIED_STATUS)

// engaged - notification tapped
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.ENGAGED_STATUS)

// custom recipe event
NearItManager.getInstance().sendTracking(trackingInfo, "my awesome custom event")
</div>