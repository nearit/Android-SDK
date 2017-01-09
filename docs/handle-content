# Handle recipe content #

NearIT takes care of delivering content at the right time, you will just need to handle content presentation. 

## Foreground content ##

To receive foreground contents (e.g. ranging recipes) set a proximity listener with the method
```java
{
    ...
    nearItManager.addProximityListener(this);
    // remember to remove the listener when the object is being destroyed with 
    // nearItManager.removeProximityListener(this);
    ...
}

@Override
public void foregroundEvent(Parcelable content, Recipe recipe) {
    // handle the event
    // if you show the notification to the user track the recipe as notified with
    // Recipe.sendTracking(getApplicationContext(), recipe.getId(), Recipe.NOTIFIED_STATUS);
    // when the user interacts with the content, track the event with
    // Recipe.sendTracking(getApplicationContext(), recipe.getId(), Recipe.ENGAGED_STATUS);
}   
```

## Background content ##

Once you have added at least one of the receivers for any background working trigger (ADD LINK) you will be delivered the actual content through an intent that will call your app launcher activity and carry some extras.
To extract the content from an intent use the utility method:
```java
NearUtils.parseCoreContents(intent, coreContentListener);
```

Recipes tracks themselves as notified, but you need to track the tap event, by calling
```java
Recipe.sendTracking(getApplicationContext(), recipeId, Recipe.ENGAGED_STATUS);
```

## Foreground vs Background ##
Recipes either deliver content in the background or in the foreground but not both. Check this table to see how you will be notified.

| Type of trigger                  | Delivery           |
|----------------------------------|--------------------|
| Push (immediate or scheduled)    | Background intent  |
| Enter and Exit on geofences      | Background intent  |
| Enter and Exit on beacon regions | Background intent  |
| Enter in a specific beacon range | Proximity listener (foreground) |

If you want to customize the behavior of background notification see [this page](docs/custom-background-notifications.md)
