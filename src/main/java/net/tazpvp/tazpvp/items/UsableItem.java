package net.tazpvp.tazpvp.items;

import lombok.Getter;
import lombok.Setter;
import net.tazpvp.tazpvp.items.usables.azureVapor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public abstract class UsableItem {

    public static final List<UsableItem> customItemList = new ArrayList<>();

    public final String name;
    public final String[] description;
    public final Material material;

    public UsableItem(String name, String[] description, Material material) {
        this.name = name;
        this.description = description;
        this.material = material;
    }

    public abstract void onRightClick(Player p, ItemStack item);
    public abstract void onLeftClick(Player p, ItemStack item);
    public abstract void onLeftClick(Player p, ItemStack item, Player target);
    public abstract void onLeftClick(Player p, ItemStack item, Block b);

    public static void registerCustomItems() {
        customItemList.add(new azureVapor());
    }

    public static UsableItem getCustomItem(ItemStack item) {
        for (UsableItem customItem : UsableItem.customItemList) {
            if (item.getItemMeta() == null) return null;
            Bukkit.broadcastMessage(item.getItemMeta().getDisplayName());
            if (customItem.getMaterial() == item.getType() && customItem.getName().equalsIgnoreCase(item.getItemMeta().getDisplayName())) {
                return customItem;
            }
        }
        return null;
    }
}
