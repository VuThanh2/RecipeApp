package RecipeManager;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class RecipeDataManager {
    private static final String RECIPES_FILE_NAME = "recipes.json";

    public static void CreateJsonFileIfEmpty(Context context) {
        File file = new File(context.getFilesDir(), RECIPES_FILE_NAME);
        if (!file.exists()) {
            saveData(context, new JSONArray());
        }
    }

    public static JSONArray loadRecipes(Context context) {
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            FileInputStream fileInputStream = context.openFileInput(RECIPES_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();
            fileInputStream.close();
            return new JSONArray(jsonBuilder.toString());
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static void saveData(Context context, JSONArray jsonArray) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(RECIPES_FILE_NAME, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonArray.toString().getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addRecipe(Context context, JSONObject recipe) {
        JSONArray recipes = loadRecipes(context);
        recipes.put(recipe);
        saveData(context, recipes);
    }

    public static void deleteRecipe(Context context, int index) {
        JSONArray recipes = loadRecipes(context);
        JSONArray newList = new JSONArray();

        for (int i = 0; i < recipes.length(); i++) {
            if (i != index) {
                try {
                    newList.put(recipes.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        saveData(context, newList);
    }

    public static void updateRecipe(Context context, int index, JSONObject updatedRecipe) {
        JSONArray recipes = loadRecipes(context);
        try {
            recipes.put(index, updatedRecipe);
            saveData(context, recipes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
