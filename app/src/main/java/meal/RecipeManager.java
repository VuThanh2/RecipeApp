package meal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeManager {
    // Thay bằng dữ liệu thật của bạn khi cần
    public static List<RecipeTag> getAllTags() {
        return new ArrayList<>(Arrays.asList(
                new RecipeTag("r01","Pho"),
                new RecipeTag("r02","Salad Caesar"),
                new RecipeTag("r03","Pasta Bolognese"),
                new RecipeTag("r04","Fried Rice"),
                new RecipeTag("r05","Chicken Soup"),
                new RecipeTag("r06","Sushi Roll"),
                new RecipeTag("r07","Bun Cha")
        ));
    }
}
