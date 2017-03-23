package controllers;

import models.Recipe;
import models.wrapper_models.Recipes;
import org.sql2o.Sql2o;
import repositories.RecipeRepository;
import repositories.repositoryInterfaces.IRecipeRepository;

import java.util.Collection;

import static jsonUtil.JsonUtil.fromJson;
import static jsonUtil.JsonUtil.json;
import static jsonUtil.JsonUtil.toJson;
import static spark.Spark.*;

/**
 * Created by Kaempe on 22-03-2017.
 */
public class RecipeController
{
    private IRecipeRepository recipeRepository;

    public RecipeController(Sql2o sql2o){
        recipeRepository = new RecipeRepository(sql2o);

        get("/recipes", (req, res) ->
        {
            Collection<Recipe> recipes = recipeRepository.getAll();
            if (recipes.size() != 0){
                res.status(200);
                return  new Recipes(recipes);
            }
            res.status(204);
            return new String("No recipes found in the database");
        }, json());

        get("/recipes/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            Recipe recipe = recipeRepository.get(id);

            if (recipe != null) {
                res.status(200);
                return recipe;
            }
            res.status(204);
            return new String("No recipe with id "+ id +" found");
        }, json());

        post("/recipes", (req, res) -> {
            Recipe recipe = fromJson(req.body(),Recipe.class);

            int id = recipeRepository.create(recipe);

            if (id != 0)
            {
                res.status(200);
                return id;
            }
            res.status(400);
            return new String("Recipe not created");
        }, json());

        put("/recipes/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            Recipe recipe = fromJson(req.body(),Recipe.class);
            recipe.setRecipeId(id);
            boolean result = recipeRepository.update(recipe);

            if (result)
            {
                res.status(200);
                return new String("Recipe " + id + " Updated");
            }
            res.status(400);
            return new String("Recipe not updated");
        }, json());

        delete("/recipes/:id", (req, res) -> {
            int id ;
            try{
                id = Integer.parseInt(req.params(":id"));
            }catch (Exception e)
            {
                res.status(400);
                return new String("the id must be an integer");
            }
            boolean result = recipeRepository.delete(id);
            if (result)
            {
                res.status(200);
                return result;
            }
            res.status(500);
            return new String("Could not delete Recipe with id " + id);
        },json());


        after((req, res) -> res.type("application/json"));

        exception(IllegalArgumentException.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(e.getMessage()));
            res.type("application/json");
        });
    }
}
