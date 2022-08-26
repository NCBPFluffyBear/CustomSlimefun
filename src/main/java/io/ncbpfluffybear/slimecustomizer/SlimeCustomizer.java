package io.ncbpfluffybear.slimecustomizer;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.ncbpfluffybear.slimecustomizer.objects.SCMenu;
import io.ncbpfluffybear.slimecustomizer.objects.WindowsExplorerStringComparator;
import io.ncbpfluffybear.slimecustomizer.registration.Categories;
import io.ncbpfluffybear.slimecustomizer.registration.Generators;
import io.ncbpfluffybear.slimecustomizer.registration.Items;
import io.ncbpfluffybear.slimecustomizer.registration.Machines;
import io.ncbpfluffybear.slimecustomizer.registration.MobDrops;
import io.ncbpfluffybear.slimecustomizer.registration.SolarGenerators;
import lombok.SneakyThrows;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This used to be a smol boi. Now it has grown.
 *
 * @author NCBPFluffyBear
 */
public class SlimeCustomizer extends JavaPlugin implements SlimefunAddon {

    public static SlimeCustomizer instance;
    public static File itemsFolder;

    private static final Map<ItemStack[], Pair<RecipeType, String>> existingRecipes = new HashMap<>();
    private static final Map<String, ItemGroup> allCategories = new HashMap<>();

    @Override
    public void onEnable() {

        instance = this;
        itemsFolder = new File(this.getDataFolder(), "saveditems");

        // Read something from your config.yml
        Config cfg = new Config(this);

        if (cfg.getBoolean("options.auto-update") && getDescription().getVersion().startsWith("DEV - ")) {
            new GitHubBuildsUpdater(this, getFile(), "NCBPFluffyBear/SlimeCustomizer/master/").start();
        }

        final Metrics metrics = new Metrics(this, 9841);

        /* File generation */
        final File categoriesFile = new File(getInstance().getDataFolder(), "categories.yml");
        copyFile(categoriesFile, "categories");

        final File itemsFile = new File(getInstance().getDataFolder(), "items.yml");
        copyFile(itemsFile, "items");

        final File mobDropsFile = new File(getInstance().getDataFolder(), "mob-drops.yml");
        copyFile(mobDropsFile, "mob-drops");


        final File machinesFile = new File(getInstance().getDataFolder(), "machines.yml");
        copyFile(machinesFile, "machines");


        final File generatorsFile = new File(getInstance().getDataFolder(), "generators.yml");
        copyFile(generatorsFile, "generators");


        final File solarGeneratorsFile = new File(getInstance().getDataFolder(), "solar-generators.yml");
        copyFile(solarGeneratorsFile, "solar-generators");


        /*
        final File passiveMachinesFile = new File(getInstance().getDataFolder(), "passive-machines.yml");
        if (!passiveMachinesFile.exists()) {
            try {
                Files.copy(this.getClass().getResourceAsStream("/passive-machines.yml"), passiveMachinesFile.toPath());
            } catch (IOException e) {
                getInstance().getLogger().log(Level.SEVERE, "Failed to copy default passive-machines.yml file", e);
            }
        }

         */

        if (!itemsFolder.exists()) {
            try {
                Files.createDirectory(itemsFolder.toPath());
            } catch (IOException e) {
                getInstance().getLogger().log(Level.WARNING, "Failed to create saveditems folder", e);
            }
        }

        Config categories = new Config(this, "categories.yml");
        Config items = new Config(this, "items.yml");
        Config machines = new Config(this, "machines.yml");
        Config generators = new Config(this, "generators.yml");
        Config solarGenerators = new Config(this, "solar-generators.yml");
        Config passiveMachines = new Config(this, "passive-machines.yml");
        Config mobDrops = new Config(this, "mob-drops.yml");

        this.getCommand("slimecustomizer").setTabCompleter(new SCTabCompleter());

        Bukkit.getLogger().info("[SlimeCustomizer] " + ChatColor.BLUE + "Setting up custom stuff...");
        if (!Categories.register(categories)) {
            return;
        }
        if (!Items.register(items)) {
            return;
        }
        if (!Machines.register(machines)) {
            return;
        }
        if (!Generators.register(generators)) {
            return;
        }
        if (!SolarGenerators.register(solarGenerators)) {
            return;
        }
        if (!MobDrops.register(mobDrops)) {
            return;
        }
        Bukkit.getPluginManager().registerEvents(new Events(), instance);
    }

    @Override
    @SneakyThrows
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && args[0].equals("saveitem")) {
            onSaveItem((Player) sender);
        } else if (sender instanceof Player && args[0].equals("give") && args.length > 2) {
            onGiveItem((Player) sender, args);
        } else if (sender instanceof Player && args[0].equals("getsaveditem") && args.length > 1) {
            onGetSavedItem((Player) sender);
        } else {
            Utils.send(sender, "&eAll commands can be found at &9" + Links.COMMANDS);
        }
        return true;
    }

    @SneakyThrows
    private void onSaveItem(Player player) {
        if (!Utils.checkPermission(player, "slimecustomizer.admin")) {
            return;
        }

        int id = 0;
        File itemFile = new File(getInstance().getDataFolder().getPath() + "/saveditems", id + ".yml");
        while (itemFile.exists()) {
            id++;
            itemFile = new File(getInstance().getDataFolder().getPath() + "/saveditems", id + ".yml");
        }

        if (!itemFile.createNewFile()) {
            getInstance().getLogger().severe("Failed to create config for item " + id);
        }

        Config itemFileConfig = new Config(this, "saveditems/" + id + ".yml");
        itemFileConfig.setValue("item", player.getInventory().getItemInMainHand());
        itemFileConfig.save();
        Utils.send(player, "&eYour item has been saved to " +
            itemFile.getPath() +
            ". Please refer to " +
            "&9" +
            Links.USING_CUSTOM_ITEMS
        );
    }

    @SneakyThrows
    private void onGiveItem(@Nonnull Player player, @Nonnull String... args) {
        if (!Utils.checkPermission(player, "slimecustomizer.admin")) {
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            Utils.send(player, "&cThat player could not be found!");
            return;
        }

        SlimefunItem sfItem = SlimefunItem.getById(args[2].toUpperCase());
        if (sfItem == null) {
            Utils.send(player, "&cThat Slimefun item could not be found!");
            return;
        }

        int amount;

        if (args[3] != null) {

            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {
                amount = 1;
            }
        } else {
            amount = 1;
        }

        giveItems(player, target, sfItem, amount);
    }

    @SneakyThrows
    private void onGetSavedItem(@Nonnull Player player, @Nonnull String... args) {
        if (!Utils.checkPermission(player, "slimecustomizer.admin")) {
            return;
        }

        if (args[1].equals("gui")) {
            List<Pair<String, ItemStack>> items = new ArrayList<>();
            items.add(new Pair<>(null, null));

            String[] fileNames = itemsFolder.list();
            if (fileNames != null) {
                for (int i = 0; i < fileNames.length; i++) {
                    fileNames[i] = fileNames[i].replace(".yml", "");
                }

                Arrays.sort(fileNames, new WindowsExplorerStringComparator());

                for (String id : fileNames) {
                    items.add(new Pair<>(id, Utils.retrieveSavedItem(id, 1, false)));
                }

                int page = 1;
                SCMenu menu = new SCMenu("&a&lSaved Items");
                menu.setSize(54);
                populateMenu(menu, items, page, player);
                menu.setPlayerInventoryClickable(false);
                menu.setBackgroundNonClickable(false);
                menu.open(player);
            }
        } else {
            if (args.length < 4) {
                Utils.send(player, "&c/sc getsaveditem gui | <item_id> <player_name> <amount>");
                return;
            }

            String savedID = args[1];

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                Utils.send(player, "&cThat player could not be found!");
                return;
            }

            int amount;

            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {
                amount = 1;
            }

            ItemStack item = Utils.retrieveSavedItem(savedID, amount, false);
            if (item != null) {
                HashMap<Integer, ItemStack> leftovers = target.getInventory().addItem(item);
                for (ItemStack leftover : leftovers.values()) {
                    target.getWorld().dropItem(target.getLocation(), leftover);
                }

                Utils.send(player, "&bYou have given " + target.getName() + " &a" + amount + " &bof &7\"&a" +
                    savedID + "&7\"");
            } else {
                Utils.send(player, "&cThat saveditem could not be found!");
            }
        }
    }

    /**
     * Populates the saveditem gui. 45 items per page.
     *
     * @param menu  the SCMenu to populate
     * @param items the List of items
     * @param page  the page number
     * @param p     the player that will be viewing this menu
     */
    @ParametersAreNonnullByDefault
    private void populateMenu(SCMenu menu, List<Pair<String, ItemStack>> items, int page, Player p) {
        for (int i = 45; i < 54; i++) {
            menu.replaceExistingItem(i, ChestMenuUtils.getBackground());
        }

        menu.wipe(0, 44, true);

        for (int i = 0; i < 45; i++) {
            int itemIndex = i + 1 + (page - 1) * 45;
            ItemStack item = getItemOrNull(items, itemIndex);
            if (item == null) {
                continue;
            }

            ItemMeta im = item.getItemMeta();
            if (im == null) {
                Utils.notify("An item has no metadata! Is it corrupted? " + items.get(itemIndex).getFirstValue());
                continue;
            }
            List<String> lore = im.getLore();

            if (lore == null) {
                lore = new ArrayList<>(Arrays.asList("", Utils.color("&bID: " + items.get(itemIndex).getFirstValue()),
                                                     Utils.color("&a> Click to get this item")
                ));
            } else {
                lore.addAll(new ArrayList<>(Arrays.asList("",
                                                          Utils.color("&bID: " + items.get(itemIndex).getFirstValue()),
                                                          Utils.color("&a> Click to get this item")
                )));
            }

            im.setLore(lore);
            item.setItemMeta(im);
            menu.replaceExistingItem(i, item);
            menu.addMenuClickHandler(i, (pl, s, is, action) -> {
                HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(getItemOrNull(items, itemIndex));
                for (ItemStack leftover : leftovers.values()) {
                    p.getWorld().dropItem(p.getLocation(), leftover);
                }
                return false;
            });
        }

        if (page != 1) {
            menu.replaceExistingItem(46, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&aPrevious Page"));
            menu.addMenuClickHandler(46, (pl, s, is, action) -> {
                populateMenu(menu, items, page - 1, p);
                return false;
            });
        }

        if (getItemOrNull(items, 45 * page) != null) {
            menu.replaceExistingItem(52, new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&aNext Page"));
            menu.addMenuClickHandler(52, (pl, s, is, action) -> {
                populateMenu(menu, items, page + 1, p);
                return false;
            });
        }

    }

    @Nullable
    private ItemStack getItemOrNull(@Nonnull List<Pair<String, ItemStack>> items, int index) {
        ItemStack item;
        try {
            item = items.get(index).getSecondValue().clone();
        } catch (IndexOutOfBoundsException e) {
            item = null;
        }
        return item;
    }

    @ParametersAreNonnullByDefault
    private void giveItems(CommandSender s, Player p, SlimefunItem sfItem, int amount) {
        p.getInventory().addItem(new CustomItemStack(sfItem.getRecipeOutput(), amount));
        Utils.send(s, "&bYou have given " + p.getName() + " &a" + amount + " &7\"&b" + sfItem.getItemName() + "&7\"");
    }

    private void copyFile(@Nonnull File file, @Nonnull String name) {
        if (!file.exists()) {
            try {
                Files.copy(this.getClass().getResourceAsStream("/" + name + ".yml"), file.toPath());
            } catch (IOException e) {
                getInstance().getLogger().log(Level.SEVERE, "Failed to copy default " + name + ".yml file", e);
            }
        }
    }

    @Override
    public void onDisable() {
        // Logic for disabling the plugin...
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/NCBPFluffyBear/SlimeCustomizer/issues";
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    public static SlimeCustomizer getInstance() {
        return instance;
    }

    public static Map<ItemStack[], Pair<RecipeType, String>> getExistingRecipes() {
        return existingRecipes;
    }

    public static Map<String, ItemGroup> getAllCategories() {
        return allCategories;
    }
}
