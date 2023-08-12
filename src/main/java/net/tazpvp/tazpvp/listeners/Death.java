package net.tazpvp.tazpvp.listeners;

import net.tazpvp.tazpvp.utils.objects.bosses.BossManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class Death implements Listener {
    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof WitherSkeleton) {
            if (entity.getUniqueId().equals(BossManager.getBosses().get(0).getZorg().getUniqueId())) {
                BossManager.getBosses().remove(0);
                BossManager.respawnZorg();
            }
        }
    }
}
