# Testing

To help with testing, you can manually trigger a recipe.
The `NearItManager` object has a getter for the `RecipesManager`. 
With this object you can get the list of recipes with the method:

```java
nearItManager.getRecipesManager().getRecipes()
```
Once you pick the recipe you want to test use this method to trigger it:

```java
String id = recipe.getId();
nearItManager.processRecipe(id);
```
