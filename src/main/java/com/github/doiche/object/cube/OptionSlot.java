package com.github.doiche.object.cube;

import com.github.doiche.Main;
import org.bukkit.NamespacedKey;

public enum OptionSlot {
    FIRST,
    SECOND,
    THIRD
    ;

    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(Main.getInstance(), name().toLowerCase());
    }
}
