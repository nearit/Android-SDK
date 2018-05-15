## Trackings
NearIT allows to track user engagement events on recipes. Any recipe has at least two default events:

  - **Received**: the user *received* a notification
  - **Opened**: the user *tapped* on the notification
  
Usually the SDK tracks those events automatically, but if you write custom code to show notification or content (i.e. to receive Beacon interaction content) please make sure that at least the "**Received**" event is tracked.
<br>**Warning:** Failing in tracking this event cause some NearIT features to not work.

You can track **default or custom events** using the "**sendTracking**" method:

<div class="code-java">
// received - notification received
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.RECEIVED);

// opened - notification tapped
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.OPENED);

// custom recipe event
NearItManager.getInstance().sendTracking(trackingInfo, "my awesome custom event");
</div>
<div class="code-kotlin">
// received - notification received
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.RECEIVED)

// opened - notification tapped
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.OPENED)

// custom recipe event
NearItManager.getInstance().sendTracking(trackingInfo, "my awesome custom event")
</div>

### Content CTA conversion trackings

When manually dealing with *CTA* objects from the **Content with attachments** you can add this custom trigger on the recipe to track the click conversions on the link:

<div class="code-java">
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.CTA_TAPPED);
</div>
<div class="code-kotlin">
NearItManager.getInstance().sendTracking(trackingInfo, Recipe.CTA_TAPPED)
</div>
