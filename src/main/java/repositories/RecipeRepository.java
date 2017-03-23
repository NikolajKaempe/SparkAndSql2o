package repositories;

import models.Ingredient;
import models.MeasuredIngredient;
import models.Recipe;
import models.RecipeType;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import repositories.repositoryInterfaces.IRecipeRepository;

import java.util.Collection;

/**
 * Created by Kaempe on 20-03-2017.
 */
public class RecipeRepository implements IRecipeRepository {

    private Sql2o sql2o;

    public RecipeRepository(Sql2o sql2o){
        this.sql2o = sql2o;
    }

    @Override
    public Collection<Recipe> getAll() {
        Collection<Recipe> recipes;
        String sql =
                "SELECT recipeId, recipeName, recipeDescription, recipeImageFilePath " +
                        "FROM Recipes";
        try{
            Connection con = sql2o.open();
            recipes = con.createQuery(sql)
                    .executeAndFetch(Recipe.class);
            recipes.forEach(recipe -> recipe.setRecipeType(this.getRecipeTypeFor(recipe.getRecipeId())));
            recipes.forEach(recipe -> recipe.setMeasuredIngredients(this.getMeasuredIngredientsFor(recipe.getRecipeId())));
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return recipes;
    }

    @Override
    public Recipe get(int id) {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No recipe found with id " + id);
        }
        Recipe recipe;
        String sql =
                "SELECT recipeId, recipeName, recipeDescription, recipeImageFilePath " +
                        "FROM Recipes WHERE recipeId = :id";
        try{
            Connection con = sql2o.open();
            recipe = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Recipe.class);
            recipe.setRecipeType(this.getRecipeTypeFor(recipe.getRecipeId()));
            recipe.setMeasuredIngredients(this.getMeasuredIngredientsFor(recipe.getRecipeId()));
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return recipe;
    }

    @Override
    public int create(Recipe model) {
        int id;
        this.failIfInvalid(model);
        String sql =
                "INSERT INTO Recipes (recipeName, recipeDescription, recipeImageFilePath, recipeTypeId) " +
                        "VALUES (:recipeName, :recipeDescription, :recipeImageFilePath, :recipeTypeId)";
        String sqlRelations =
                "INSERT INTO MeasuredIngredients (recipeId, ingredientId, amount, measure) " +
                        "VALUES (:recipeId, :ingredientId, :amount, :measure)";
        try{
            Connection con = sql2o.beginTransaction();
            id = Integer.parseInt(con.createQuery(sql, true)
                    .bind(model)
                    .addParameter("recipeTypeId",model.getRecipeType().getRecipeTypeId())
                    .executeUpdate().getKey().toString());
            model.getMeasuredIngredients().forEach(ingredient ->
                    con.createQuery(sqlRelations)
                            .addParameter("recipeId",id)
                            .addParameter("ingredientId",ingredient.getIngredient().getIngredientId())
                            .addParameter("amount",ingredient.getAmount())
                            .addParameter("measure",ingredient.getMeasure())
                            .executeUpdate());
            con.commit();
        }catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }

        return id;
    }

    @Override
    public boolean update(Recipe model) {
        if (!this.exists(model.getRecipeId())){
            throw new IllegalArgumentException("No recipe found with id: " + model.getRecipeId());
        }
        failIfInvalid(model);

        String sql =
                "UPDATE Recipes SET " +
                        "recipeName = :recipeName, " +
                        "recipeDescription = :recipeDescription, " +
                        "recipeImageFilePath = :recipeImageFilePath " +
                        "WHERE recipeId = :recipeId";
        String sqlRecipeType =
                "UPDATE Recipes SET " +
                        "recipeTypeId = :recipeTypeId " +
                        "WHERE recipeId = :id";

        String sqlRelationsToDelete =
                "DELETE FROM MeasuredIngredients WHERE " +
                        "recipeId = :id";

        String sqlRelationsToUpdate =
                "INSERT INTO MeasuredIngredients (recipeId, ingredientId, amount, measure) " +
                        "VALUES (:recipeId, :ingredientId, :amount, :measure )";

        try{
            Connection con = sql2o.beginTransaction();
            con.createQuery(sql)
                    .bind(model)
                    .addParameter("recipeId",model.getRecipeId())
                    .executeUpdate();
            con.createQuery(sqlRecipeType)
                    .addParameter("recipeTypeId",model.getRecipeType().getRecipeTypeId())
                    .addParameter("id",model.getRecipeId())
                    .executeUpdate();
            con.createQuery(sqlRelationsToDelete)
                    .addParameter("id",model.getRecipeId())
                    .executeUpdate();
            model.getMeasuredIngredients().forEach(ingredient ->
                    con.createQuery(sqlRelationsToUpdate)
                            .addParameter("recipeId",model.getRecipeId())
                            .addParameter("ingredientId",ingredient.getIngredient().getIngredientId())
                            .addParameter("amount",ingredient.getAmount())
                            .addParameter("measure",ingredient.getMeasure())
                            .executeUpdate());
            con.commit();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        if (!this.exists(id)){
            throw new IllegalArgumentException("No Recipe found with id: " + id);
        }
        //failDeleteIfRelationsExist(id); // TODO create method when menus are introduced
        Connection con;
        String sqlRelationsToDelete =
                "DELETE FROM MeasuredIngredients WHERE " +
                        "recipeId = :id";

        String sql =
                "DELETE FROM Recipes WHERE " +
                        "recipeId = :id ";
        try{
            con = sql2o.beginTransaction();
            con.createQuery(sqlRelationsToDelete)
                    .addParameter("id",id)
                    .executeUpdate();
            con.createQuery(sql)
                    .addParameter("id",id)
                    .executeUpdate();
            con.commit();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean exists(int id) {
        Recipe recipe;

        String sql = "SELECT recipeId " +
                "FROM Recipes " +
                "WHERE recipeId = :id";
        try{
            Connection con = sql2o.open();
            recipe = con.createQuery(sql)
                    .addParameter("id",id)
                    .executeAndFetchFirst(Recipe.class);
            if (recipe != null) return true;
            return false;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private RecipeType getRecipeTypeFor(int recipeId){
        RecipeType recipeType;
        String sql =
                "SELECT * FROM RecipeTypes " +
                        "WHERE recipeTypeId IN (" +
                "SELECT recipeTypeId FROM Recipes " +
                        "WHERE recipeTypeId = :id" +
                        ")";
        try{
            Connection con = sql2o.open();
            recipeType = con.createQuery(sql)
                    .addParameter("id",recipeId)
                    .executeAndFetchFirst(RecipeType.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return recipeType;
    }

    private Collection<MeasuredIngredient> getMeasuredIngredientsFor(int recipeId){
        Collection<MeasuredIngredient> ingredients;
        String sql =
                "SELECT measuredIngredientId, amount, measure FROM MeasuredIngredients " +
                        "WHERE recipeId = :id";
        try{
            Connection con = sql2o.open();
            ingredients = con.createQuery(sql)
                    .addParameter("id",recipeId)
                    .executeAndFetch(MeasuredIngredient.class);
            ingredients.forEach(ingredient -> ingredient.setIngredient(getIngredientFor(recipeId)));
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return ingredients;
    }

    private Ingredient getIngredientFor(int recipeId){
        Ingredient ingredient;
        String sql =
                "SELECT * FROM Ingredients " +
                        "WHERE ingredientId IN (" +
                        "SELECT ingredientId FROM MeasuredIngredients " +
                        "WHERE recipeId= :id" +
                        ")";
        try{
            Connection con = sql2o.open();
            ingredient = con.createQuery(sql)
                    .addParameter("id",recipeId)
                    .executeAndFetchFirst(Ingredient.class);
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        return ingredient;
    }

    private void failIfInvalid(Recipe recipe){
        if ( recipe == null)
        {
            throw new IllegalArgumentException("recipe cannot be null");
        }
        if (recipe.getRecipeName() == null || recipe.getRecipeName().length() < 1) {
            throw new IllegalArgumentException("Parameter `name` cannot be empty");
        }
        if (recipe.getRecipeDescription() == null || recipe.getRecipeDescription().length() < 1) {
            throw new IllegalArgumentException("Parameter `description` cannot be empty");
        }
        if (recipe.getRecipeType() == null ) {
            throw new IllegalArgumentException("Parameter `recipeType` cannot be null");
        }
        if (recipe.getRecipeType().getRecipeTypeId() == 0) {
            throw new IllegalArgumentException("Parameter `recipeType` has wrong id");
        }
        for (MeasuredIngredient measuredIngredient : recipe.getMeasuredIngredients()){
            if (measuredIngredient.getAmount() <= 0){
                throw new IllegalArgumentException("Parameter `measuredIngredient` amount must be greater than 0");
            }
            if (measuredIngredient.getMeasure() == null || measuredIngredient.getMeasure().length() < 1){
                throw new IllegalArgumentException("Parameter `measure` cannot be empty");
            }
            if (measuredIngredient.getIngredient() == null){
                throw new IllegalArgumentException("Parameter `ingredient` cannot be null");
            }
            if (measuredIngredient.getIngredient().getIngredientId() == 0){
                throw new IllegalArgumentException("Parameter `ingredient` has wrong id");
            }
        }
    }

}
