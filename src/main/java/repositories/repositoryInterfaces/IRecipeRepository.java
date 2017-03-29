package repositories.repositoryInterfaces;

import models.Ingredient;
import models.MeasuredIngredient;
import models.Recipe;
import models.RecipeType;

import java.util.Collection;

/**
 * Created by Kaempe on 20-03-2017.
 */
public interface IRecipeRepository extends IRepository<Recipe>
{
    RecipeType getRecipeTypeFor(int id);
    Collection<MeasuredIngredient> getMeasuredIngredientsFor(int id);
    Ingredient getIngredientFor(int id);
    void failDeleteIfRelationsExist(int id);
    boolean isRelationValid(int id);
}
