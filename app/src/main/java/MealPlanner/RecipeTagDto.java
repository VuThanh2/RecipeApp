package MealPlanner;


public class RecipeTagDto {
    public String id;
    public String title;

    public RecipeTagDto(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
