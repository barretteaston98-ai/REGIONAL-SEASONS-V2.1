package dev.easton.regionalseasons;

import java.util.Locale;

public enum Season {
    SPRING("Spring", "§a"),
    SUMMER("Summer", "§e"),
    AUTUMN("Autumn", "§6"),
    WINTER("Winter", "§b");

    public final String display;
    public final String color;

    Season(String display, String color) {
        this.display = display;
        this.color = color;
    }

    public static Season fromName(String name) {
        for (Season season : values()) {
            if (season.name().equalsIgnoreCase(name) || season.display.equalsIgnoreCase(name)) {
                return season;
            }
        }
        return null;
    }

    public static Season fromIndex(int index) {
        return values()[Math.floorMod(index, values().length)];
    }
}
