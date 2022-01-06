package com.yoreni.mineplugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.yoreni.mineplugin.mine.Mine;
import com.yoreni.mineplugin.mine.MineListener;
import com.yoreni.mineplugin.util.Yml;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.yoreni.mineplugin.util.MessageHandler;

public final class MinePlugin extends JavaPlugin
{
    public static WorldEditPlugin WORLD_EDIT = null;
    private static MinePlugin instance;
    private static MessageHandler messageHandler;
    private static Yml config;

    @Override
    public void onEnable()
    {
        instance = this;
        registerCommands();
        setupWorldEdit();
        setupConfigFiles();

        Mine.initMineList();
        Bukkit.getLogger().info(String.format("Loaded %d mine(s)", Mine.getMines().size()));

        BukkitTask updateMines = new UpdateMines().runTaskTimer(this,20, 20);
        Bukkit.getPluginManager().registerEvents(new MineListener(), this);
    }



    @Override
    public void onDisable()
    {
        Mine.saveMines();
        // Plugin shutdown logic
    }

    public static MinePlugin getInstance()
    {
        return instance;
    }

    public static MessageHandler getMessageHandler()
    {
        return messageHandler;
    }

    public static Yml getConfigFile() {
        return config;
    }

    private void registerCommands()
    {
        this.getCommand("mines").setExecutor(new MineCommands(this));
        this.getCommand("mines").setTabCompleter(new MineCommands(this));
    }

    private void setupConfigFiles()
    {
        config = new Yml(this, "config");
        config.setDefaultsFromJar();
        messageHandler = new MessageHandler(this);
    }

    private void setupWorldEdit()
    {
        if (getServer().getPluginManager().isPluginEnabled("WorldEdit"))
        {
            WORLD_EDIT = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        }
    }
}