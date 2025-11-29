package com.artillexstudios.axinventoryrestore.utils;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.YamlDocument;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.MergeRule;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ConfigDefaults {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDefaults.class);
    private static final UpdaterSettings MISSING_DEFAULTS = UpdaterSettings.builder()
            .setKeepAll(true)
            .setMergeRule(MergeRule.MAPPING_AT_SECTION, true)
            .setMergeRule(MergeRule.SECTION_AT_MAPPING, true)
            .build();

    private ConfigDefaults() {
    }

    public static boolean addMissing(Config config) {
        YamlDocument document = config.getBackingDocument();
        YamlDocument defaults = document.getDefaults();
        if (defaults == null || document.getRoutesAsStrings(true).containsAll(defaults.getRoutesAsStrings(true))) {
            return true;
        }

        // Fork and upstream releases can share a version ID but introduce different keys.
        // Merge missing defaults without versioning, preserving existing values and the upstream ID.
        try {
            return document.update(MISSING_DEFAULTS);
        } catch (IOException exception) {
            LOGGER.error("Could not add missing defaults to {}!", document.getFile(), exception);
            return false;
        }
    }
}
