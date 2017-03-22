import controllers.IngredientController;
import controllers.MealTypeController;
import controllers.RecipeTypeController;
import org.sql2o.Sql2o;
import controllers.AllergyController;
import repositories.IngredientRepository;
import repositories.RecipeRepository;

/**
 * Created by Kaempe on 19-02-2017.
 */
public class Main{
    public final static String DB_URL = "mysql://80.255.6.114:3306/AirshipOne";
    //public final static String DB_URL = "localhost:3306/AirshipOne";
    public final static String DB_USER = "AirshipOneUser";
    public final static String DB_PASS = "123456";

    public static void main( String[] args) {
        Sql2o sql2o = new Sql2o(DB_URL, DB_USER, DB_PASS);
        new AllergyController(sql2o);
        new IngredientController(sql2o);
        new RecipeTypeController(sql2o);
        new MealTypeController(sql2o);
        new RecipeRepository(sql2o);
    }
}

