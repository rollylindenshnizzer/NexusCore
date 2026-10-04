package io.github.rollylindenshnizzer.nexuscore.api.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import io.github.rollylindenshnizzer.nexuscore.NexusCore;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public final class NexusJsonConfig {
    public static final int CONFIG_API_VERSION = 2;
    public static final String CONFIG_API_VERSION_PATH = "_nexuscore.configApiVersion";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final String ownerModId;
    private final String id;
    private final LinkedHashMap<String, NexusConfigValue<?>> entries;
    private final LinkedHashMap<String, NexusConfigPage> pages;
    private final LinkedHashMap<String, NexusConfigHeading> headings;
    private final LinkedHashMap<String, NexusConfigSubheading> subheadings;
    private final LinkedHashMap<String, NexusConfigMetadataValue<?>> metadata;
    private final Path file;
    private int loadedConfigApiVersion = CONFIG_API_VERSION;

    private NexusJsonConfig(String ownerModId, String id, LinkedHashMap<String, NexusConfigValue<?>> entries, LinkedHashMap<String, NexusConfigPage> pages, LinkedHashMap<String, NexusConfigHeading> headings, LinkedHashMap<String, NexusConfigSubheading> subheadings, LinkedHashMap<String, NexusConfigMetadataValue<?>> metadata) {
        this.ownerModId = ownerModId;
        this.id = id;
        this.entries = entries;
        this.pages = pages;
        this.headings = headings;
        this.subheadings = subheadings;
        this.metadata = metadata;
        this.file = Platform.getConfigFolder().resolve(ownerModId).resolve(id + ".json");
    }

    public static Builder builder(String ownerModId, String id) {
        return new Builder(ownerModId, id);
    }

    public String ownerModId() {
        return ownerModId;
    }

    public String id() {
        return id;
    }

    public Path file() {
        return file;
    }

    public int configApiVersion() {
        NexusConfigMetadataValue<?> value = metadata.get(CONFIG_API_VERSION_PATH);
        return value != null && value.get() instanceof Number number ? number.intValue() : CONFIG_API_VERSION;
    }

    public int loadedConfigApiVersion() {
        return loadedConfigApiVersion;
    }

    public NexusConfigMetadataValue<?> metadata(String path) {
        return metadata.get(path);
    }

    public List<NexusConfigMetadataValue<?>> metadataValues() {
        return Collections.unmodifiableList(new ArrayList<>(metadata.values()));
    }

    public List<NexusConfigValue<?>> entries() {
        return Collections.unmodifiableList(new ArrayList<>(entries.values()));
    }

    public List<NexusConfigPage> pages() {
        return Collections.unmodifiableList(new ArrayList<>(pages.values()));
    }

    public NexusConfigPage page(String id) {
        return pages.get(id);
    }

    public List<NexusConfigHeading> headings(String pageId) {
        return headings.values().stream().filter(heading -> heading.pageId().equals(pageId)).toList();
    }

    public NexusConfigHeading heading(String id) {
        return headings.get(id);
    }

    public List<NexusConfigSubheading> subheadings(String headingId) {
        return subheadings.values().stream().filter(subheading -> subheading.headingId().equals(headingId)).toList();
    }

    public NexusConfigSubheading subheading(String id) {
        return subheadings.get(id);
    }

    public List<NexusConfigValue<?>> entries(String pageId) {
        return entries.values().stream().filter(value -> value.pageId().equals(pageId)).toList();
    }

    public List<NexusConfigValue<?>> entries(String pageId, String headingId, String subheadingId) {
        return entries.values().stream().filter(value -> value.pageId().equals(pageId) && value.headingId().equals(headingId) && java.util.Objects.equals(value.subheadingId(), subheadingId)).toList();
    }

    public NexusConfigValue<?> entry(String path) {
        return entries.get(path);
    }

    public synchronized void load() {
        entries.values().forEach(NexusConfigValue::reset);
        loadedConfigApiVersion = Files.isRegularFile(file) ? 1 : CONFIG_API_VERSION;
        boolean loaded = true;
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonElement rootElement = JsonParser.parseReader(reader);
                if (rootElement.isJsonObject()) {
                    JsonObject root = rootElement.getAsJsonObject();
                    JsonElement version = find(root, CONFIG_API_VERSION_PATH);
                    if (version != null && version.isJsonPrimitive() && version.getAsJsonPrimitive().isNumber()) {
                        loadedConfigApiVersion = version.getAsInt();
                    }
                    entries.forEach((path, value) -> value.read(find(root, path)));
                } else {
                    loaded = false;
                }
            } catch (Exception exception) {
                loaded = false;
                NexusCore.LOGGER.error("Failed to load config {} for {}", id, ownerModId, exception);
            }
        }
        save();
        if (loaded) {
            NexusCore.LOGGER.info("Loaded NexusCore JSON config {} for {} from {}", id, ownerModId, file);
        } else {
            NexusCore.LOGGER.warn("Recovered NexusCore JSON config {} for {} with default values at {}", id, ownerModId, file);
        }
        NexusCore.LOGGER.info("Initialized NexusCore config feature for {}", ownerModId);
    }

    public synchronized void save() {
        JsonObject root = new JsonObject();
        metadata.forEach((path, value) -> place(root, path, value.toJson()));
        entries.forEach((path, value) -> place(root, path, value.toJson()));
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException exception) {
            NexusCore.LOGGER.error("Failed to save config {} for {}", id, ownerModId, exception);
        }
    }

    private static JsonElement find(JsonObject root, String path) {
        String[] pieces = path.split("\\.");
        JsonObject current = root;
        for (int i = 0; i < pieces.length - 1; i++) {
            JsonElement next = current.get(pieces[i]);
            if (next == null || !next.isJsonObject()) {
                return null;
            }
            current = next.getAsJsonObject();
        }
        return current.get(pieces[pieces.length - 1]);
    }

    private static void place(JsonObject root, String path, JsonElement value) {
        String[] pieces = path.split("\\.");
        JsonObject current = root;
        for (int i = 0; i < pieces.length - 1; i++) {
            JsonElement next = current.get(pieces[i]);
            if (next == null || !next.isJsonObject()) {
                JsonObject child = new JsonObject();
                current.add(pieces[i], child);
                current = child;
            } else {
                current = next.getAsJsonObject();
            }
        }
        current.add(pieces[pieces.length - 1], value);
    }

    public static final class Builder {
        private static final String DEFAULT_PAGE = "general";
        private final String ownerModId;
        private final String id;
        private final LinkedHashMap<String, NexusConfigValue<?>> entries = new LinkedHashMap<>();
        private final LinkedHashMap<String, NexusConfigPage> pages = new LinkedHashMap<>();
        private final LinkedHashMap<String, NexusConfigHeading> headings = new LinkedHashMap<>();
        private final LinkedHashMap<String, NexusConfigSubheading> subheadings = new LinkedHashMap<>();
        private final LinkedHashMap<String, NexusConfigMetadataValue<?>> metadata = new LinkedHashMap<>();
        private String currentPage;
        private String currentHeading;
        private String currentSubheading;

        private Builder(String ownerModId, String id) {
            this.ownerModId = ownerModId;
            this.id = id;
            metadata.put(CONFIG_API_VERSION_PATH, new NexusConfigMetadataValue<>(CONFIG_API_VERSION_PATH, CONFIG_API_VERSION));
        }

        public Builder page(String pageId, String translationKey, String descriptionTranslationKey) {
            NexusConfigText title = translationKey == null || translationKey.isBlank() ? NexusConfigText.literal(pageId) : NexusConfigText.translatable(translationKey);
            NexusConfigText description = descriptionTranslationKey == null || descriptionTranslationKey.isBlank() ? NexusConfigText.empty() : NexusConfigText.translatable(descriptionTranslationKey);
            return page(pageId, title, description);
        }

        public Builder page(String pageId, String translationKey) {
            return page(pageId, translationKey, null);
        }

        public Builder page(String pageId) {
            return page(pageId, (String) null, null);
        }

        public Builder page(String pageId, NexusConfigText title, NexusConfigText description) {
            validateId(pageId, "page");
            if (pages.containsKey(pageId)) {
                throw new IllegalArgumentException("Duplicate config page " + pageId);
            }
            pages.put(pageId, new NexusConfigPage(pageId, title == null ? NexusConfigText.literal(pageId) : title, description == null ? NexusConfigText.empty() : description));
            currentPage = pageId;
            currentHeading = null;
            currentSubheading = null;
            return this;
        }

        public Builder heading(String headingId, String translationKey, String descriptionTranslationKey) {
            NexusConfigText title = translationKey == null || translationKey.isBlank() ? NexusConfigText.literal(headingId) : NexusConfigText.translatable(translationKey);
            NexusConfigText description = descriptionTranslationKey == null || descriptionTranslationKey.isBlank() ? NexusConfigText.empty() : NexusConfigText.translatable(descriptionTranslationKey);
            return heading(headingId, title, description);
        }

        public Builder heading(String headingId, String translationKey) {
            return heading(headingId, translationKey, null);
        }

        public Builder heading(String headingId) {
            return heading(headingId, (String) null, null);
        }

        public Builder heading(String headingId, NexusConfigText title, NexusConfigText description) {
            ensurePage();
            validateId(headingId, "heading");
            if (headings.containsKey(headingId)) {
                throw new IllegalArgumentException("Duplicate config heading " + headingId);
            }
            headings.put(headingId, new NexusConfigHeading(headingId, currentPage, title == null ? NexusConfigText.literal(headingId) : title, description == null ? NexusConfigText.empty() : description));
            currentHeading = headingId;
            currentSubheading = null;
            return this;
        }

        public Builder subheading(String subheadingId, String translationKey, String descriptionTranslationKey) {
            NexusConfigText title = translationKey == null || translationKey.isBlank() ? NexusConfigText.literal(subheadingId) : NexusConfigText.translatable(translationKey);
            NexusConfigText description = descriptionTranslationKey == null || descriptionTranslationKey.isBlank() ? NexusConfigText.empty() : NexusConfigText.translatable(descriptionTranslationKey);
            return subheading(subheadingId, title, description);
        }

        public Builder subheading(String subheadingId, String translationKey) {
            return subheading(subheadingId, translationKey, null);
        }

        public Builder subheading(String subheadingId) {
            return subheading(subheadingId, (String) null, null);
        }

        public Builder subheading(String subheadingId, NexusConfigText title, NexusConfigText description) {
            ensureHeading();
            validateId(subheadingId, "subheading");
            if (subheadings.containsKey(subheadingId)) {
                throw new IllegalArgumentException("Duplicate config subheading " + subheadingId);
            }
            subheadings.put(subheadingId, new NexusConfigSubheading(subheadingId, currentPage, currentHeading, title == null ? NexusConfigText.literal(subheadingId) : title, description == null ? NexusConfigText.empty() : description));
            currentSubheading = subheadingId;
            return this;
        }

        public NexusConfigValue<Boolean> booleanValue(String path, boolean defaultValue) {
            return add(path, NexusConfigValue.Kind.BOOLEAN, defaultValue, null, null);
        }

        public NexusConfigValue<Integer> integerValue(String path, int defaultValue, int minimum, int maximum) {
            return add(path, NexusConfigValue.Kind.INTEGER, defaultValue, (double) minimum, (double) maximum);
        }

        public NexusConfigValue<Double> doubleValue(String path, double defaultValue, double minimum, double maximum) {
            return add(path, NexusConfigValue.Kind.DOUBLE, defaultValue, minimum, maximum);
        }

        public NexusConfigValue<String> stringValue(String path, String defaultValue) {
            return add(path, NexusConfigValue.Kind.STRING, defaultValue, null, null);
        }

        private <T> NexusConfigValue<T> add(String path, NexusConfigValue.Kind kind, T defaultValue, Double minimum, Double maximum) {
            if (entries.containsKey(path)) {
                throw new IllegalArgumentException("Duplicate config path " + path);
            }
            ensureHeading();
            NexusConfigValue<T> value = new NexusConfigValue<>(path, kind, defaultValue, minimum, maximum, currentPage, currentHeading, currentSubheading);
            entries.put(path, value);
            return value;
        }

        public NexusJsonConfig build() {
            LinkedHashMap<String, NexusConfigPage> usedPages = new LinkedHashMap<>();
            LinkedHashMap<String, NexusConfigHeading> usedHeadings = new LinkedHashMap<>();
            LinkedHashMap<String, NexusConfigSubheading> usedSubheadings = new LinkedHashMap<>();
            pages.forEach((pageId, page) -> {
                if (entries.values().stream().anyMatch(value -> value.pageId().equals(pageId))) {
                    usedPages.put(pageId, page);
                }
            });
            headings.forEach((headingId, heading) -> {
                if (entries.values().stream().anyMatch(value -> value.headingId().equals(headingId))) {
                    usedHeadings.put(headingId, heading);
                }
            });
            subheadings.forEach((subheadingId, subheading) -> {
                if (entries.values().stream().anyMatch(value -> subheadingId.equals(value.subheadingId()))) {
                    usedSubheadings.put(subheadingId, subheading);
                }
            });
            return new NexusJsonConfig(ownerModId, id, new LinkedHashMap<>(entries), usedPages, usedHeadings, usedSubheadings, new LinkedHashMap<>(metadata));
        }

        private void ensurePage() {
            if (currentPage != null) {
                return;
            }
            pages.put(DEFAULT_PAGE, new NexusConfigPage(DEFAULT_PAGE, NexusConfigText.literal(DEFAULT_PAGE), NexusConfigText.empty()));
            currentPage = DEFAULT_PAGE;
        }

        private void ensureHeading() {
            ensurePage();
            if (currentHeading != null) {
                return;
            }
            String headingId = "_" + currentPage + "_general";
            NexusConfigPage page = pages.get(currentPage);
            headings.put(headingId, new NexusConfigHeading(headingId, currentPage, page.title(), NexusConfigText.empty()));
            currentHeading = headingId;
            currentSubheading = null;
        }

        private static void validateId(String id, String kind) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Config " + kind + " id cannot be blank");
            }
        }
    }
}
