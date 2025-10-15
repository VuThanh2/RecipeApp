package RecipeManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recipe implements Serializable {
    private String id;
    private String title;
    private String category;
    private String ingredients;
    private String instructions;
    private int imageResId;
    private boolean pinned;
    private int globalIndex = -1;
    private int calories;
    private int carbs;
    private int fat;
    private int protein;
    private String owner;

    private List<RecipeItem> items;

    public Recipe() {
    }

    public Recipe(String id, String title, String category, String ingredients, String instructions, int imageResId) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageResId = imageResId;
        this.pinned = false;
    }

    public Recipe(String id, String title, String category, List<RecipeItem> items, String instructions, int imageResId) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.items = items;
        this.instructions = instructions;
        this.imageResId = imageResId;
        this.pinned = false;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner == null ? "" : owner.trim().toLowerCase();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getImage() {
        return imageResId;
    }

    public void setImage(int imageResId) {
        this.imageResId = imageResId;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public int getGlobalIndex() {
        return globalIndex;
    }

    public void setGlobalIndex(int globalIndex) {
        this.globalIndex = globalIndex;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public int getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public int getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public List<RecipeItem> getItems() {
        if (items == null) items = new ArrayList<>();
        return items;
    }

    public void setItems(List<RecipeItem> items) {
        this.items = items;
    }

    public static class Ingredient implements Serializable {
        private String id;
        private String name;
        private String unit; // e.g., g, kg, ml, l, pcs
        private List<String> tags;

        public Ingredient() {
        }

        public Ingredient(String id, String name) {
            this.id = id;
            this.name = name;
            this.unit = "";
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public List<String> getTags() {
            if (tags == null) tags = new ArrayList<>();
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    public static class RecipeItem implements Serializable {
        private Ingredient ingredient;
        private String quantity;

        public RecipeItem() {
        }

        public RecipeItem(Ingredient ingredient, String quantity) {
            this.ingredient = ingredient;
            this.quantity = quantity;
        }

        public Ingredient getIngredient() {
            return ingredient;
        }

        public void setIngredient(Ingredient ingredient) {
            this.ingredient = ingredient;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }
    }
}
