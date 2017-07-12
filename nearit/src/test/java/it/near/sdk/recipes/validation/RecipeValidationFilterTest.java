package it.near.sdk.recipes.validation;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import it.near.sdk.recipes.models.Recipe;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipeValidationFilterTest {

    private RecipeValidationFilter recipeValidationFilter;

    @Mock
    private Validator firstValidator;
    @Mock
    private Validator secondValidator;
    @Mock
    private Validator thirdValidator;


    @Before
    public void setUp() throws Exception {
        List<Validator> validators = Lists.newArrayList(firstValidator, secondValidator, thirdValidator);
        recipeValidationFilter = new RecipeValidationFilter(validators);
    }

    @Test(expected = NullPointerException.class)
    public void whenPassingNullForRecipes_throws() throws Exception{
        recipeValidationFilter.filterRecipes(null);
    }

    @Test
    public void whenNoRecipesArePassed_noneReturns() {
        List<Recipe> recipes = Collections.emptyList();
        recipeValidationFilter.filterRecipes(recipes);
        assertThat(recipes, hasSize(0));
    }

    @Test
    public void whenAllValidatorsAlwaysReturnTrue_allRecipesPass() {
        when(firstValidator.validate(any(Recipe.class))).thenReturn(true);
        when(secondValidator.validate(any(Recipe.class))).thenReturn(true);
        when(thirdValidator.validate(any(Recipe.class))).thenReturn(true);
        List<Recipe> recipes = Lists.newArrayList(
                new Recipe(),
                new Recipe(),
                new Recipe()
        );
        recipeValidationFilter.filterRecipes(recipes);
        assertThat(recipes, hasSize(3));
    }

    @Test
    public void whenOneValidatorAlwaysReturnsFalse_allRecipesFail() {
        when(firstValidator.validate(any(Recipe.class))).thenReturn(false);
        when(secondValidator.validate(any(Recipe.class))).thenReturn(true);
        when(thirdValidator.validate(any(Recipe.class))).thenReturn(true);
        List<Recipe> recipes = Lists.newArrayList(
                new Recipe(),
                new Recipe(),
                new Recipe()
        );
        recipeValidationFilter.filterRecipes(recipes);
        assertThat(recipes, hasSize(0));
    }

    @Test
    public void whenASingleRecipeShouldFail_thenIsFiltered() {
        Recipe shouldFailRecipeForFirstValidator = new Recipe();
        Recipe shouldPassRecipe = new Recipe();
        when(firstValidator.validate(any(Recipe.class))).thenReturn(true);
        when(firstValidator.validate(shouldFailRecipeForFirstValidator)).thenReturn(false);
        when(secondValidator.validate(any(Recipe.class))).thenReturn(true);
        when(thirdValidator.validate(any(Recipe.class))).thenReturn(true);
        List<Recipe> recipes = Lists.newArrayList(shouldFailRecipeForFirstValidator, shouldPassRecipe);
        recipeValidationFilter.filterRecipes(recipes);
        assertThat(recipes, hasSize(1));
        assertThat(recipes, hasItem(shouldPassRecipe));
        assertThat(recipes, not(hasItem(shouldFailRecipeForFirstValidator)));
    }

    @Test
    public void whenNoValidatorsAreSet_AllRecipesPass()  {
        // reset validation filter
        recipeValidationFilter = new RecipeValidationFilter(Lists.<Validator>newArrayList());
        List<Recipe> recipes = Lists.newArrayList(
                new Recipe(),
                new Recipe(),
                new Recipe()
        );
        recipeValidationFilter.filterRecipes(recipes);
        assertThat(recipes, hasSize(3));
    }
}