package me.hex.duelsplus.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class KitReader {

    private final FileConfiguration config;

    public KitReader(FileConfiguration fileConfiguration) {
        config = fileConfiguration;
    }

    public ArrayList<Kit> readKits() {

        ArrayList<Kit> kits = new ArrayList<>();

        ConfigurationSection section = config.getConfigurationSection("kits");

        if (section == null) {
            Bukkit.getLogger().warning("There are no kits input, please check them");
            return kits;
        }

        for (String kitSectionName : section.getKeys(false)) {

            ArrayList<ItemStack> armorContent = new ArrayList<>();
            ItemStack helmet = new ItemStack(Material.AIR);
            ItemStack chestPlate = new ItemStack(Material.AIR);
            ItemStack leggings = new ItemStack(Material.AIR);
            ItemStack boots = new ItemStack(Material.AIR);
            ArrayList<ItemStack> inventoryContent = new ArrayList<>();

            ConfigurationSection kitSection = section.getConfigurationSection(kitSectionName);

            if (kitSection == null) {
                Bukkit.getLogger().warning("No Kits Loaded!");
                return kits;
            }

            ConfigurationSection armorSection = kitSection.getConfigurationSection("armor_content");

            if (armorSection == null) {
                Bukkit.getLogger().warning("armorSection is null for the kit: " + kitSectionName);
                continue;
            }

            for (String pieceName : armorSection.getKeys(false)) {

                if (armorSection.getConfigurationSection(pieceName) == null) {
                    Bukkit.getLogger().info(pieceName + " in the kit " + kitSectionName + " does not exist");
                    continue;
                }

                switch (pieceName) {
                    case "helmet" -> {
                        helmet = new ItemStack(Material.valueOf(
                                Objects.requireNonNull(armorSection.getConfigurationSection("helmet")).
                                        getString("material")),
                                Objects.requireNonNull(armorSection.getConfigurationSection("helmet")).
                                        getInt("amount"));
                        armorContent.add(helmet);
                    }
                    case "chestPlate" -> {
                        chestPlate = new ItemStack(Material.valueOf(
                                Objects.requireNonNull(armorSection.getConfigurationSection("chestPlate"))
                                        .getString("material")),
                                Objects.requireNonNull(armorSection.getConfigurationSection("chestPlate")).
                                        getInt("amount"));
                        armorContent.add(chestPlate);
                    }
                    case "leggings" -> {
                        leggings = new ItemStack(Material.valueOf(
                                Objects.requireNonNull(armorSection.getConfigurationSection("leggings")).
                                        getString("material")),
                                Objects.requireNonNull(armorSection.getConfigurationSection("leggings")).
                                        getInt("amount"));
                        armorContent.add(leggings);
                    }
                    case "boots" -> {
                        boots = new ItemStack(Material.valueOf(
                                Objects.requireNonNull(armorSection.getConfigurationSection("boots")).
                                        getString("material")),
                                Objects.requireNonNull(armorSection.getConfigurationSection("boots")).
                                        getInt("amount"));
                        armorContent.add(boots);
                    }
                }
            }
            ConfigurationSection kitInventoryContent = kitSection.getConfigurationSection("inventory_content");
            if (kitInventoryContent == null) {
                Bukkit.getLogger().warning("inventory_content is null for the kit: " + kitSectionName);
                continue;
            }

            for (String slot : kitInventoryContent.getKeys(false)) {
                inventoryContent.add(new ItemStack(Material.valueOf(
                        Objects.requireNonNull(Objects.requireNonNull(kitSection
                                                .getConfigurationSection("inventory_content"))
                                        .getConfigurationSection(slot))
                                .getString("material")),
                        Objects.requireNonNull(Objects.requireNonNull(kitSection
                                                .getConfigurationSection("inventory_content"))
                                        .getConfigurationSection(slot))
                                .getInt("amount")));
            }

            Kit kit = new Kit(
                    kitSectionName, armorContent,
                    helmet, chestPlate, leggings, boots,
                    inventoryContent);
            kits.add(kit);
        }

        return kits;
    }

    public Kit getDefaultKit() {
        for (Kit kit : readKits()) {
            if (kit.getName().equalsIgnoreCase(config.getString("default_kit"))) {
                return kit;
            }
        }

        return null;
    }

    public Kit valueOf(String name) {
        for (Kit kit : readKits()) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }
        return null;
    }

    public boolean doesKitExist(String name) {
        boolean found = false;
        for (Kit kit : readKits()) {
            if (kit.getName().equalsIgnoreCase(name)) {
                found = true;
                break;
            }
        }
        return found;
    }


}
