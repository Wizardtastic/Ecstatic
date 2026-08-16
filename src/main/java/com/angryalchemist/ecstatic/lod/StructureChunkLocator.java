package com.angryalchemist.ecstatic.lod;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public final class StructureChunkLocator {
    private StructureChunkLocator() {
    }

    public static List<ChunkPos> candidateStartChunks(ServerLevel level, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        long seed = state.getLevelSeed();
        Set<ChunkPos> candidates = new LinkedHashSet<>();

        for (Holder<StructureSet> holder : state.possibleStructureSets()) {
            StructurePlacement placement = ((StructureSet)holder.value()).placement();
            if (placement instanceof RandomSpreadStructurePlacement spread) {
                int spacing = spread.spacing();
                if (spacing > 0) {
                    int minCellX = Math.floorDiv(minChunkX, spacing);
                    int maxCellX = Math.floorDiv(maxChunkX, spacing);
                    int minCellZ = Math.floorDiv(minChunkZ, spacing);
                    int maxCellZ = Math.floorDiv(maxChunkZ, spacing);

                    for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
                        for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                            ChunkPos candidate = spread.getPotentialStructureChunk(seed, cellX * spacing, cellZ * spacing);
                            if (candidate.x >= minChunkX
                                && candidate.x <= maxChunkX
                                && candidate.z >= minChunkZ
                                && candidate.z <= maxChunkZ
                                && placement.isStructureChunk(state, candidate.x, candidate.z)) {
                                candidates.add(candidate);
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(candidates);
    }
}
