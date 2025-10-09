package MealPlanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeManager {
    // Thay bằng dữ liệu thật của bạn khi cần
    public static List<RecipeTagDto> getAllTags() {
        return new ArrayList<>(Arrays.asList(
                new RecipeTagDto("r01","Pho"),
                new RecipeTagDto("r02","Salad Caesar"),
                new RecipeTagDto("r03","Pasta Bolognese"),
                new RecipeTagDto("r04","Fried Rice"),
                new RecipeTagDto("r05","Chicken Soup"),
                new RecipeTagDto("r06","Sushi Roll"),
                new RecipeTagDto("r07","Bun Cha")
        ));
    }
}
