package repositories.repositoryInterfaces;

import models.MealType;
import models.Menu;
import models.Recipe;

import java.util.Collection;

/**
 * Created by Kaempe on 28-03-2017.
 */
public interface IMenuRepository extends IRepository<Menu>
{
    MealType getMealTypeFor(int id);
    Collection<Recipe> getRecipesFor(int id);
    boolean isRelationValid(int id);
}
