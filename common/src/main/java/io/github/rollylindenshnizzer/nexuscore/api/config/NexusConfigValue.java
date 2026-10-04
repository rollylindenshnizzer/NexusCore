package io.github.rollylindenshnizzer.nexuscore.api.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NexusConfigValue<T> {
    public enum Kind {
        BOOLEAN, INTEGER, DOUBLE, STRING
    }

    private final String path;
    private final Kind kind;
    private final T defaultValue;
    private final Double minimum;
    private final Double maximum;
    private final String pageId;
    private final String headingId;
    private final String subheadingId;
    private NexusConfigText labelText;
    private NexusConfigText descriptionText = NexusConfigText.empty();
    private String optionTranslationPrefix;
    private List<String> staticDropdownValues = List.of();
    private Supplier<? extends Collection<String>> dropdownValuesSupplier;
    private boolean restrictDropdownValues;
    private List<String> autofillValues = List.of();
    private Function<String, ResourceLocation> optionTextEffect;
    private Function<T, ResourceLocation> valueTextEffect;
    private Function<T, NexusConfigText> currentEffect;
    private List<NexusConfigStatusLabel> labels = List.of();
    private T value;

    NexusConfigValue(String path, Kind kind, T defaultValue, Double minimum, Double maximum, String pageId, String headingId, String subheadingId) {
        this.path = path;
        this.kind = kind;
        this.defaultValue = defaultValue;
        this.minimum = minimum;
        this.maximum = maximum;
        this.pageId = pageId;
        this.headingId = headingId;
        this.subheadingId = subheadingId;
        this.labelText = NexusConfigText.literal(path);
        this.value = defaultValue;
    }

    public String path() {
        return path;
    }

    public Kind kind() {
        return kind;
    }

    public T get() {
        return value;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public Double minimum() {
        return minimum;
    }

    public Double maximum() {
        return maximum;
    }

    public String pageId() {
        return pageId;
    }

    public String headingId() {
        return headingId;
    }

    public String subheadingId() {
        return subheadingId;
    }

    public NexusConfigText labelText() {
        return labelText;
    }

    public NexusConfigText descriptionText() {
        return descriptionText;
    }

    public Component label() {
        return labelText.component();
    }

    public Component description() {
        return descriptionText.component();
    }

    public NexusConfigText optionLabelText(String option) {
        NexusConfigText text = optionTranslationPrefix == null || optionTranslationPrefix.isBlank() ? NexusConfigText.literal(option) : NexusConfigText.translatable(optionTranslationPrefix + "." + option);
        ResourceLocation effect = optionTextEffect == null ? null : optionTextEffect.apply(option);
        return effect == null ? text : text.withTextEffect(effect);
    }

    public Component optionLabel(String option) {
        return optionLabelText(option).component();
    }

    public NexusConfigText currentValueText() {
        NexusConfigText text = usesDropdown() ? optionLabelText(displayValue()) : NexusConfigText.literal(displayValue());
        ResourceLocation effect = valueTextEffect == null ? null : valueTextEffect.apply(value);
        return effect == null ? text : text.withTextEffect(effect);
    }

    public NexusConfigText currentEffectText() {
        return currentEffect == null ? NexusConfigText.empty() : Objects.requireNonNullElse(currentEffect.apply(value), NexusConfigText.empty());
    }

    public List<NexusConfigStatusLabel> labels() {
        return labels;
    }

    public List<String> dropdownValues() {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        values.add(String.valueOf(defaultValue));
        if (value != null) {
            values.add(String.valueOf(value));
        }
        values.addAll(staticDropdownValues);
        if (dropdownValuesSupplier != null) {
            Collection<String> supplied = dropdownValuesSupplier.get();
            if (supplied != null) {
                supplied.stream().filter(Objects::nonNull).forEach(values::add);
            }
        }
        return List.copyOf(values);
    }

    public List<String> autofillValues() {
        return autofillValues;
    }

    public boolean usesDropdown() {
        return kind == Kind.STRING && (!staticDropdownValues.isEmpty() || dropdownValuesSupplier != null);
    }

    public NexusConfigValue<T> translation(String translationKey, String descriptionTranslationKey) {
        labelText = translationKey == null || translationKey.isBlank() ? NexusConfigText.literal(path) : NexusConfigText.translatable(translationKey);
        descriptionText = descriptionTranslationKey == null || descriptionTranslationKey.isBlank() ? NexusConfigText.empty() : NexusConfigText.translatable(descriptionTranslationKey);
        return this;
    }

    public NexusConfigValue<T> translation(String translationKey) {
        return translation(translationKey, null);
    }

    public NexusConfigValue<T> text(NexusConfigText labelText, NexusConfigText descriptionText) {
        this.labelText = Objects.requireNonNull(labelText, "labelText");
        this.descriptionText = descriptionText == null ? NexusConfigText.empty() : descriptionText;
        return this;
    }

    public NexusConfigValue<T> text(NexusConfigText labelText) {
        return text(labelText, NexusConfigText.empty());
    }

    public NexusConfigValue<T> labelTextEffect(ResourceLocation effectId) {
        labelText = labelText.withTextEffect(effectId);
        return this;
    }

    public NexusConfigValue<T> descriptionTextEffect(ResourceLocation effectId) {
        descriptionText = descriptionText.withTextEffect(effectId);
        return this;
    }

    public NexusConfigValue<T> valueTextEffect(Function<T, ResourceLocation> effectProvider) {
        valueTextEffect = effectProvider;
        return this;
    }

    public NexusConfigValue<T> currentEffect(Function<T, Component> effectProvider) {
        currentEffect = effectProvider == null ? null : value -> NexusConfigText.of(effectProvider.apply(value));
        return this;
    }

    public NexusConfigValue<T> currentEffectText(Function<T, NexusConfigText> effectProvider) {
        currentEffect = effectProvider;
        return this;
    }

    public NexusConfigValue<T> labels(NexusConfigStatusLabel... labels) {
        this.labels = List.copyOf(Arrays.asList(labels));
        return this;
    }

    public NexusConfigValue<T> optionTranslationPrefix(String optionTranslationPrefix) {
        this.optionTranslationPrefix = optionTranslationPrefix;
        return this;
    }

    public NexusConfigValue<T> optionTextEffect(Function<String, ResourceLocation> effectProvider) {
        optionTextEffect = effectProvider;
        return this;
    }

    public NexusConfigValue<T> dropdown(String... values) {
        if (kind != Kind.STRING) {
            throw new IllegalStateException("Dropdown values are only supported for string config entries");
        }
        staticDropdownValues = List.copyOf(Arrays.asList(values));
        restrictDropdownValues = true;
        if (!staticDropdownValues.contains(String.valueOf(defaultValue))) {
            throw new IllegalArgumentException("Dropdown does not contain default value " + defaultValue + " for " + path);
        }
        return this;
    }

    public NexusConfigValue<T> dropdown(Supplier<? extends Collection<String>> values) {
        if (kind != Kind.STRING) {
            throw new IllegalStateException("Dropdown values are only supported for string config entries");
        }
        dropdownValuesSupplier = Objects.requireNonNull(values, "values");
        restrictDropdownValues = false;
        return this;
    }

    public NexusConfigValue<T> autofill(String... values) {
        if (kind != Kind.STRING) {
            throw new IllegalStateException("Autofill values are only supported for string config entries");
        }
        autofillValues = List.copyOf(Arrays.asList(values));
        return this;
    }

    public void reset() {
        value = defaultValue;
    }

    public boolean setFromString(String raw) {
        try {
            Object parsed = switch (kind) {
                case BOOLEAN -> parseBoolean(raw);
                case INTEGER -> clamp(Integer.parseInt(raw.trim())).intValue();
                case DOUBLE -> clamp(Double.parseDouble(raw.trim()));
                case STRING -> parseString(raw);
            };
            value = (T) parsed;
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public String displayValue() {
        Object current = value;
        if (current instanceof Double number) {
            return BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(current);
    }

    JsonElement toJson() {
        return switch (kind) {
            case BOOLEAN -> new JsonPrimitive((Boolean) value);
            case INTEGER -> new JsonPrimitive((Integer) value);
            case DOUBLE -> new JsonPrimitive((Double) value);
            case STRING -> new JsonPrimitive((String) value);
        };
    }

    void read(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            reset();
            return;
        }
        try {
            Object parsed = switch (kind) {
                case BOOLEAN -> element.getAsBoolean();
                case INTEGER -> clamp(element.getAsInt()).intValue();
                case DOUBLE -> clamp(element.getAsDouble());
                case STRING -> parseString(element.getAsString());
            };
            value = (T) parsed;
        } catch (RuntimeException exception) {
            reset();
        }
    }

    private String parseString(String raw) {
        if (restrictDropdownValues && !staticDropdownValues.contains(raw)) {
            throw new IllegalArgumentException(raw);
        }
        return raw;
    }

    private Number clamp(double number) {
        double clamped = number;
        if (minimum != null) {
            clamped = Math.max(minimum, clamped);
        }
        if (maximum != null) {
            clamped = Math.min(maximum, clamped);
        }
        return clamped;
    }

    private static boolean parseBoolean(String raw) {
        String normalized = raw.trim();
        if ("true".equalsIgnoreCase(normalized)) {
            return true;
        }
        if ("false".equalsIgnoreCase(normalized)) {
            return false;
        }
        throw new IllegalArgumentException(normalized);
    }
}
