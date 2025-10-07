package RecipeManager;

import java.io.Serializable;

public class Recipe implements Serializable {
    private String title;
    private String category;
    private String ingredients;
    private String instructions;
    private int imageResId;
    private boolean pinned;
    private int globalIndex = -1;

    public Recipe(String title, String category, String ingredients, String instructions, int imageResId) {
        this.title = title;
        this.category = category;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageResId = imageResId;
        this.pinned = false;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public int getImage() { return imageResId; }
    public void setImage(int imageResId) { this.imageResId = imageResId; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public int getGlobalIndex() { return globalIndex; }
    public void setGlobalIndex(int globalIndex) { this.globalIndex = globalIndex; }
}

