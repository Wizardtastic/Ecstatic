package com.angryalchemist.ecstatic.storage;

import com.angryalchemist.ecstatic.Constants;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class LodStoragePaths {
    private static final int MIN_FINGERPRINT_FILES_PRESENT = 1;

    private LodStoragePaths() {
    }

    public static Path dimensionStorageDir(Path worldRoot, ResourceKey<Level> dimension) {
        String dimensionDirName = dimension.location().getNamespace() + "_" + dimension.location().getPath();
        Path current = worldRoot.resolve("ecstatic").resolve(dimensionDirName);
        if (Files.isDirectory(current)) {
            return current;
        }

        Path legacy = findLegacyDimensionStorageDir(worldRoot, dimensionDirName);
        return legacy != null ? legacy : current;
    }

    private static Path findLegacyDimensionStorageDir(Path worldRoot, String dimensionDirName) {
        if (!Files.isDirectory(worldRoot)) {
            return null;
        }

        try (DirectoryStream<Path> topLevel = Files.newDirectoryStream(worldRoot)) {
            for (Path candidate : topLevel) {
                if (Files.isDirectory(candidate) && !candidate.getFileName().toString().equals("ecstatic")) {
                    Path candidateDimensionDir = candidate.resolve(dimensionDirName);
                    if (looksLikeRegionStorageDir(candidateDimensionDir)) {
                        return candidateDimensionDir;
                    }
                }
            }
        } catch (IOException e) {
            Constants.LOG.warn("Ecstatic: failed to scan {} for pre-existing LOD data; treating as a fresh install for this dimension", worldRoot, e);
        }

        return null;
    }

    private static boolean looksLikeRegionStorageDir(Path dimensionDir) {
        if (!Files.isDirectory(dimensionDir)) {
            return false;
        }

        int present = 0;

        for (int level = 0; level <= 5; level++) {
            if (Files.isRegularFile(regionFile(dimensionDir, level))) {
                present++;
            }
        }

        return present >= 1;
    }

    public static Path regionFile(Path dimensionStorageDir, int lodLevel) {
        return dimensionStorageDir.resolve("lod" + lodLevel + ".dat");
    }
}
