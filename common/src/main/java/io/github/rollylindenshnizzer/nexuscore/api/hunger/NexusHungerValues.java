package io.github.rollylindenshnizzer.nexuscore.api.hunger;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class NexusHungerValues {
    private static final Map<ResourceLocation, Values> VALUES = new ConcurrentHashMap<>();

    private NexusHungerValues() {
    }

    public static void set(ResourceLocation foodId, int nutrition, float saturation) {
        validateNutrition(nutrition);
        validateSaturation(saturation);
        VALUES.put(Objects.requireNonNull(foodId, "foodId"), new Values(nutrition, saturation));
    }

    public static void setNutrition(ResourceLocation foodId, int nutrition) {
        validateNutrition(nutrition);
        ResourceLocation id = Objects.requireNonNull(foodId, "foodId");
        VALUES.compute(id, (ignored, current) -> new Values(nutrition, current == null ? null : current.saturation()));
    }

    public static void setSaturation(ResourceLocation foodId, float saturation) {
        validateSaturation(saturation);
        ResourceLocation id = Objects.requireNonNull(foodId, "foodId");
        VALUES.compute(id, (ignored, current) -> new Values(current == null ? null : current.nutrition(), saturation));
    }

    public static void clear(ResourceLocation foodId) {
        VALUES.remove(Objects.requireNonNull(foodId, "foodId"));
    }

    public static FoodProperties apply(Item item, FoodProperties original) {
        return apply(BuiltInRegistries.ITEM.getKey(item), original);
    }

    public static FoodProperties apply(ResourceLocation foodId, FoodProperties original) {
        if (original == null) {
            return null;
        }
        Values values = VALUES.get(foodId);
        if (values == null) {
            return original;
        }
        int nutrition = values.nutrition() == null ? original.nutrition() : values.nutrition();
        float saturation = values.saturation() == null ? original.saturation() : values.saturation();
        if (nutrition == original.nutrition() && Float.compare(saturation, original.saturation()) == 0) {
            return original;
        }
        return new FoodProperties(nutrition, saturation, original.canAlwaysEat(), original.eatSeconds(), original.usingConvertsTo(), original.effects());
    }

    private static void validateNutrition(int nutrition) {
        if (nutrition < 0) {
            throw new IllegalArgumentException("nutrition must be at least 0");
        }
    }

    private static void validateSaturation(float saturation) {
        if (!Float.isFinite(saturation) || saturation < 0.0F) {
            throw new IllegalArgumentException("saturation must be a finite value of at least 0");
        }
    }

    private record Values(Integer nutrition, Float saturation) {
    }
}
