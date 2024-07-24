package net.tazpvp.tazpvp.utils.player;

import lombok.Getter;
import lombok.Setter;
import net.tazpvp.tazpvp.Tazpvp;
import net.tazpvp.tazpvp.commands.gameplay.report.utils.ReportDebounce;
import net.tazpvp.tazpvp.commands.gameplay.report.utils.ReportLogger;
import net.tazpvp.tazpvp.data.entity.*;
import net.tazpvp.tazpvp.data.implementations.TalentServiceImpl;
import net.tazpvp.tazpvp.data.implementations.UserAchievementServiceImpl;
import net.tazpvp.tazpvp.data.implementations.UserRankServiceImpl;
import net.tazpvp.tazpvp.data.services.GuildService;
import net.tazpvp.tazpvp.data.services.TalentService;
import net.tazpvp.tazpvp.data.services.UserAchievementService;
import net.tazpvp.tazpvp.data.services.UserRankService;
import net.tazpvp.tazpvp.game.duels.Duel;
import net.tazpvp.tazpvp.npc.characters.NPC;
import net.tazpvp.tazpvp.utils.PlayerNameTag;
import net.tazpvp.tazpvp.utils.enums.CC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper for the player object which contains valuable methods exclusive to tazpvp
 */
public class PlayerWrapper {
    @Getter
    private final UUID uuid;
    private final UserRankService userRankService;
    private final GuildService guildService;
    @Getter @Setter
    private boolean launching;
    @Getter @Setter
    private boolean respawning;
    @Getter @Setter
    private boolean canRestore;
    @Getter @Setter
    private Duel duel;
    @Getter
    private final List<ReportDebounce> reportDebouncesList;
    @Getter
    private final List<ReportLogger> reportLoggerList;
    private PermissionAttachment permissionAttachment;
    @Getter @Setter
    private NPC receivedDialogue;
    @Getter @Setter
    private boolean npcDialogue;
    @Getter @Setter
    private boolean isAfk;
    @Getter @Setter
    private int coffinCount;
    @Getter @Setter
    private int killCount;
    @Getter @Setter
    private long timeOfLaunch;
    @Getter @Setter
    private long timeSinceAfk;
    @Getter @Setter
    private List<Material> blocksPlaced;
    @Getter @Setter
    private String lastMessageSent;
    @Getter @Setter
    private Duel spectating;
    @Getter
    private UserRankEntity userRankEntity; // After you set a value here make sure to call refresh()
    @Getter
    private UserAchievementEntity userAchievementEntity;
    @Getter
    private TalentEntity talentEntity;
    @Getter @Setter
    private boolean vanished;
    @Getter @Setter
    private boolean staffChatActive;
    @Getter
    private final ConcurrentHashMap<UUID, Duel> duelRequests;


    /**
     * Should only take UUID, all other values should not have to persist.
     * @param uuid UUID.
     */
    public PlayerWrapper(UUID uuid) {
        this.uuid = uuid;
        this.launching = false;
        this.respawning = false;
        this.canRestore = false;
        this.duel = null;
        this.reportDebouncesList = new ArrayList<>();
        this.reportLoggerList = new ArrayList<>();
        this.receivedDialogue = null;
        this.npcDialogue = false;
        this.coffinCount = 0;
        this.killCount = 0;
        this.timeOfLaunch = 0;
        this.blocksPlaced = new ArrayList<>();
        this.lastMessageSent = "";
        this.spectating = null;
        this.staffChatActive = false;

        this.userRankService = new UserRankServiceImpl();
        this.userRankEntity = userRankService.getOrDefault(getUuid());

        final UserAchievementService userAchievementService = new UserAchievementServiceImpl();
        this.userAchievementEntity = userAchievementService.getOrDefault(getUuid());

        final TalentService talentService = new TalentServiceImpl();
        this.talentEntity = talentService.getOrDefault(getUuid());

        this.guildService = Tazpvp.getInstance().getGuildService();

        this.duelRequests = new ConcurrentHashMap<>();

        refreshPermissions();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public String getGuildTag() {
        GuildEntity playerGuild = guildService.getGuildByPlayer(getUuid());
        if (playerGuild != null) {
            if (playerGuild.getTag() != null) {
                return CC.YELLOW + " [" + playerGuild.getTag().toUpperCase() + "]";
            }
        }
        return "";
    }

    public GameRankEntity getRank() {
        return this.userRankService.getHighestRank(this.userRankEntity);
    }

    public String getRankPrefix() {
        if (!getRank().getPrefix().isEmpty()) {
            return getRank().getPrefix();
        }
        return getCustomPrefix();
    }

    @Nullable
    public String getCustomPrefix() {
        return getRank().getPrefix();
    }

    public void setCustomPrefix(String prefix) {
        userRankEntity.setCustomPrefix(prefix);
        setRankEntity();
    }

    /**
     * Hide the player from ALL other players.
     */
    public void hidePlayer() {
        Bukkit.getOnlinePlayers().forEach(this::hidePlayer);
    }

    /**
     * Hide the target from the owner.
     * @param target The player to hide from the owner
     */
    public void hidePlayer(Player target) {
        Player owner = getPlayer();
        owner.hidePlayer(Tazpvp.getInstance(), target);
    }

    public void hideFromPlayer(Player target) {
        PlayerWrapper.getPlayer(target).hidePlayer(getPlayer());
    }

    public void hideFromPlayer() {
        Bukkit.getOnlinePlayers().forEach(player -> PlayerWrapper.getPlayer(player).hidePlayer(getPlayer()));
    }

    /**
     * Hide the player from ALL other players.
     */
    public void showPlayer() {
        Bukkit.getOnlinePlayers().forEach(target -> {
            if (!target.canSee(getPlayer())) {
                showPlayer(target);
            }
        });
    }

    /**
     * Show the target from the owner.
     * @param target The player to hide from the owner
     */
    public void showPlayer(Player target) {
        Player owner = getPlayer();
        owner.showPlayer(Tazpvp.getInstance(), target);
    }

    public void showFromPlayer(Player target) {
        PlayerWrapper.getPlayer(target).showPlayer(getPlayer());
    }

    public void showFromPlayer() {
        Bukkit.getOnlinePlayers().forEach(uuid -> {
            PlayerWrapper wrapper = PlayerWrapper.getPlayer(uuid);
            wrapper.showPlayer(getPlayer());
        });
    }

    public void reportPlayer(Player target, String reason) {
        this.reportDebouncesList.add(new ReportDebounce(target.getUniqueId(), System.currentTimeMillis()));
        PlayerWrapper.getPlayer(target).addReport(getUuid(), reason);
    }

    private void addReport(UUID reportee, String reason) {
        this.reportLoggerList.add(new ReportLogger(reportee, reason, System.currentTimeMillis()));
    }

    private void refreshPermissions() {
        if (Bukkit.getPlayer(getUuid()) == null) {
            return;
        }

        if (this.permissionAttachment != null) {
            this.permissionAttachment.remove();
        }
        this.permissionAttachment = getPlayer().addAttachment(Tazpvp.getInstance());
//        getRank().getPermissions().forEach(perm -> this.permissionAttachment.setPermission(perm, true));
        this.userRankService.getPermissions(getUserRankEntity())
                .forEach(perm -> this.permissionAttachment.setPermission(perm, true));

        this.permissionAttachment.getPermissible().recalculatePermissions();
    }


    public void setRankEntity() {
        this.userRankService.saveUserRankEntity(userRankEntity);
        refreshRankEntity();
    }

    public void refreshRankEntity() {
        this.userRankEntity = this.userRankService.getUserRankEntity(getUuid());
    }

    public void setUserAchievementEntity(final UserAchievementEntity achievementEntity) {
        final UserAchievementService userAchievementService = new UserAchievementServiceImpl();
        userAchievementService.saveUserAchievementEntity(achievementEntity);

        this.userAchievementEntity = achievementEntity;
    }

    public void setTalentEntity(final TalentEntity talentEntity) {
        final TalentService talentService = new TalentServiceImpl();
        talentService.saveTalentEntity(talentEntity);

        this.talentEntity = talentEntity;
    }

    public void refreshNametag() {
        Bukkit.getOnlinePlayers().forEach(plr -> new PlayerNameTag().initializePlayerNameTag(plr));
    }

    private static final WeakHashMap<UUID, PlayerWrapper> playerMap = new WeakHashMap<>();
    public static void addPlayer(Player p) {
        playerMap.put(p.getUniqueId(), new PlayerWrapper(p.getUniqueId()));
    }
    public static void addPlayer(UUID uuid) {
        playerMap.put(uuid, new PlayerWrapper(uuid));
    }
    public static void removePlayer(Player p) {
        playerMap.remove(p.getUniqueId(), new PlayerWrapper(p.getUniqueId()));
    }
    public static void removePlayer(UUID uuid) {
        playerMap.remove(uuid, new PlayerWrapper(uuid));
    }
    public static PlayerWrapper getPlayer(Player p) {
        return getPlayer(p.getUniqueId());
    }
    public static PlayerWrapper getPlayer(UUID uuid) {
        if (!playerMap.containsKey(uuid)) {
            addPlayer(uuid);
        }

        return playerMap.get(uuid);
    }
}