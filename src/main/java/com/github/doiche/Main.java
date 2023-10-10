package com.github.doiche;

import com.github.doiche.command.CubeCommand;
import com.github.doiche.command.LogCommand;
import com.github.doiche.command.StatusCommand;
import com.github.doiche.listener.EquipmentListener;
import com.github.doiche.listener.UserListener;
import com.github.doiche.object.cube.CubeRegistry;
import com.github.doiche.object.status.StatusRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        StatusRegistry.read();
        CubeRegistry.read();
        getServer().getPluginManager().registerEvents(new UserListener(), this);
        getServer().getPluginManager().registerEvents(new EquipmentListener(), this);
        getCommand("status").setExecutor(new StatusCommand());
        getCommand("cube").setExecutor(new CubeCommand());
        getCommand("log").setExecutor(new LogCommand());
    }
}
