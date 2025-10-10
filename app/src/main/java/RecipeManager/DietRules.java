package RecipeManager;

import java.util.*;

public final class DietRules {
    private DietRules() {}

    private static final Set<String> EMPTY = Collections.emptySet();
    private static final Map<String, Set<String>> RULES = new HashMap<>();
    static {
        RULES.put("normal", EMPTY);
        RULES.put("vegan", new HashSet<>(Arrays.asList("meat","fish","dairy","egg")));
        RULES.put("keto", new HashSet<>(Arrays.asList("sugar","high-carb")));
        RULES.put("gluten_free", new HashSet<>(Arrays.asList("wheat","barley","rye","gluten")));
    }

    private static Set<String> forbiddenFor(String diet) {
        if (diet == null) return EMPTY;
        Set<String> s = RULES.get(diet.trim().toLowerCase());
        return s != null ? s : EMPTY;
    }

    public static boolean isAllowed(Recipe.Ingredient ing, String diet) {
        if (ing == null) return true;
        var tags = ing.getTags();
        if (tags == null || tags.isEmpty()) return true;
        var forbidden = forbiddenFor(diet);
        for (String t: tags) if (forbidden.contains(t)) return false;
        return true;
    }

    public static boolean recipeAllowed(Recipe r, String diet) {
        if (r == null || r.getItems() == null) return true;
        for (Recipe.RecipeItem it: r.getItems()) {
            if (!isAllowed(it.getIngredient(), diet)) return false;
        }
        return true;
    }

    public static String recipeViolationSummary(Recipe r, String diet) {
        if (r == null || r.getItems() == null) return "";
        var forbidden = forbiddenFor(diet);
        var hit = new LinkedHashSet<String>();
        for (Recipe.RecipeItem it: r.getItems()) {
            var tags = it.getIngredient() != null ? it.getIngredient().getTags() : null;
            if (tags == null) continue;
            for (String t: tags) if (forbidden.contains(t)) hit.add(t);
        }
        return String.join(", ", hit); // e.g. "meat, dairy"
    }
}