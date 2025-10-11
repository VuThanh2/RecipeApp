package RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class RecipeFilterItem implements Serializable {
    public List<String> categories = new ArrayList<>();
    public int minCalories = 0;
    public int maxCalories = Integer.MAX_VALUE;
    public int minProtein = 0;
    public int maxProtein = Integer.MAX_VALUE;
    public int minCarbs = 0;
    public int maxCarbs = Integer.MAX_VALUE;
    public int minFat = 0;
    public int maxFat = Integer.MAX_VALUE;
}

