package com.yoreni.mineplugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.yoreni.mineplugin.util.MessageHandler;

public final class MinePlugin extends JavaPlugin
{
    /**
     * The file location where the data and config will be
     */
    public final String pluginFolder = "plugins/" + this.getName();

    private static MinePlugin instance;
    public static WorldEditPlugin WORLD_EDIT = null;
    private static MessageHandler messageHandler;

    Yaml config;

    @Override
    public void onEnable()
    {
        instance = this;
        //Bukkit.getLogger().info(Util.getListOfBlocks().toString());
        this.getCommand("mines").setExecutor(new MineCommands(this));
        this.getCommand("mines").setTabCompleter(new MineCommands(this));

        if (getServer().getPluginManager().isPluginEnabled("WorldEdit"))
        {
            WORLD_EDIT = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        }

        config = new Yaml("config", pluginFolder);
        config.setConfigSettings(ConfigSettings.PRESERVE_COMMENTS);
        config.addDefaultsFromInputStream(getClass().getResourceAsStream("/config.yml"));
        messageHandler = new MessageHandler(this);

        Mine.initMineList();
        Bukkit.getLogger().info(String.format("Loaded %d mine(s)", Mine.getMines().size()));

        //this resets the mines if they are secldued to reset every x mins
        BukkitTask updateMines = new UpdateMines().runTaskTimer(this,20, 20);
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
}