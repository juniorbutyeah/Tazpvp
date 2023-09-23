package net.tazpvp.tazpvp.booster;

import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ActiveBoosterManager {
    private static ActiveBoosterManager instance;

    public static ActiveBoosterManager getInstance() {
        return Objects.requireNonNullElse(instance, new ActiveBoosterManager());
    }

    private final Map<BoosterTypes, Long> timeLeft;

    private ActiveBoosterManager() {
        this.timeLeft = new HashMap<>();

        for (final BoosterTypes boosterType : BoosterTypes.values()) {
            this.timeLeft.put(boosterType, System.currentTimeMillis());
        }
    }

    public void addTimeToBooster(final BoosterTypes boosterType, final TimeUnit timeUnit, final long amount) {
        if (!timeLeft.containsKey(boosterType)) {
            timeLeft.put(boosterType, System.currentTimeMillis());
        }

        final long millisToAdd = timeUnit.toMillis(amount);

        timeLeft.put(boosterType, getTimeLeft(boosterType) + millisToAdd);

        Bukkit.getLogger().info(timeLeft.toString());
    }

    public long getTimeLeft(final BoosterTypes boosterType) {
        final long currentTime = System.currentTimeMillis();
        final long storedTime = timeLeft.get(boosterType);

        return Math.max(currentTime, storedTime);
    }

    public boolean isActive(final BoosterTypes boosterType) {
        return timeLeft.get(boosterType) > System.currentTimeMillis();
    }

    public BoosterBonus calculateBonus(final double base, final List<BoosterTypes> boostersToCalculateWith) {
        double result = base;
        final List<Double> multipliers = new ArrayList<>();

        Bukkit.getLogger().info(timeLeft.toString());

        for (final BoosterTypes boosterType : boostersToCalculateWith) {
            if (isActive(boosterType)) {
                result *= boosterType.getMultiplier();
                multipliers.add(boosterType.getMultiplier() - 1);
            }
        }

        return new BoosterBonus(base, multipliers, result);
    }
}
