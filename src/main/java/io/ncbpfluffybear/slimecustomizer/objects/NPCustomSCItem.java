package io.ncbpfluffybear.slimecustomizer.objects;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.inventory.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The {@link NPCustomSCItem} class is a wrapper
 * for the {@link SlimefunItem}. Modified to not be placeable
 *
 * @author NCBPFluffyBear
 */
public class NPCustomSCItem extends SlimefunItem implements SCNotPlaceable {

    @ParametersAreNonnullByDefault
    public NPCustomSCItem(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, ItemStack output) {
        super(category, item, recipeType, recipe, output);
    }

    @ParametersAreNonnullByDefault
    public NPCustomSCItem(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }
}
