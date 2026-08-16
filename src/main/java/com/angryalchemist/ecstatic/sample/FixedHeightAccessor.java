package com.angryalchemist.ecstatic.sample;

import net.minecraft.world.level.LevelHeightAccessor;

public record FixedHeightAccessor(int minBuildHeight, int maxBuildHeight) implements LevelHeightAccessor {
    public int getHeight() {
        return this.maxBuildHeight - this.minBuildHeight;
    }

    public int getMinBuildHeight() {
        return this.minBuildHeight;
    }

    public int getMaxBuildHeight() {
        return this.maxBuildHeight;
    }
}
