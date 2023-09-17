/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2022, n-tdi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.tazpvp.tazpvp.events;

import lombok.Getter;
import net.tazpvp.tazpvp.Tazpvp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import world.ntdi.nrcore.NRCore;
import world.ntdi.nrcore.utils.world.WorldUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Getter
public abstract class Event implements Listener {
    @Getter
    public static List<UUID> participantList = new ArrayList<>();
    @Getter
    public static List<UUID> aliveList = new ArrayList<>();

    private final UUID uuid;

    private final String NAME;

    public Event(@Nonnull final String NAME) {
        this.NAME = NAME;
        this.uuid = UUID.randomUUID();
        Tazpvp.getInstance().getServer().getPluginManager().registerEvents(this, Tazpvp.getInstance());
    }

    public void finalizeGame(Player player) {
        if (aliveList.contains(player)) {
            end(player);
        } else {
            end(null);
        }
    }

    public abstract void begin();

    public void end(@Nullable Player winner) {
        if (winner != null)
            Bukkit.broadcastMessage(winner.getDisplayName() + " won");
        else
            Bukkit.broadcastMessage("Nobody won");
        Bukkit.getServer().getScheduler().runTaskLater(Tazpvp.getInstance(), () -> {
            for (UUID id : participantList) {
                Bukkit.getPlayer(id).teleport(NRCore.config.spawn);
            }
            new WorldUtil().deleteWorld(uuid + "-" + getNAME());
        }, 5 * 20);
        HandlerList.unregisterAll(this);
        participantList.clear();
        aliveList.clear();
        Tazpvp.event = null;
    }
}
