package me.hex.duelsplus.kits;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public record Kit(String name, ArrayList<ItemStack> armorContent,
                  ItemStack helmet, ItemStack chestPlate,
                  ItemStack leggings, ItemStack boots,
                  ArrayList<ItemStack> inventoryContent) {

    public String getName() {
        return name;
    }

    public ArrayList<ItemStack> getArmorContent() {
        return armorContent;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public ItemStack getChestPlate() {
        return chestPlate;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public ArrayList<ItemStack> getInventoryContent() {
        return inventoryContent;
    }
}
