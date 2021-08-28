package me.mrCookieSlime.QuickSell.utils;

import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import me.mrCookieSlime.QuickSell.QuickSell;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PluginUtils
{

    private Plugin plugin;
    private int id;
    private Yaml cfg;
    private Localization local;

    /**
     * Creates a new PluginUtils Instance for
     * the specified Plugin
     *
     * @param  plugin The Plugin for which this PluginUtils Instance is made for
     */
    public PluginUtils(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new PluginUtils Instance for
     * the specified Plugin
     *
     * @param  plugin The Plugin for which this PluginUtils Instance is made for
     */
    public PluginUtils(Plugin plugin, int id) {
        this(plugin);
        this.id = id;
    }

    /**
     * Returns the specified ID from Curse
     *
     * @return      Plugin ID
     */
    public int getPluginID() {
        return this.id;
    }

    /**
     * Automatically sets up the messages.yml for you
     */
    public void setupLocalization() {
        local = new Localization(plugin);
    }

    /**
     * Automatically sets up the config.yml for you
     */
    public void setupConfig()
    {
        cfg = new Yaml("config", QuickSell.getInstance().PLUGIN_FOLDER);
        cfg.setConfigSettings(ConfigSettings.PRESERVE_COMMENTS);
        cfg.addDefaultsFromInputStream(getClass().getResourceAsStream("/config.yml"));
    }

    /**
     * Returns the previously setup Config
     *
     * @return      Config of this Plugin
     */
    public Yaml getConfig() {
        return cfg;
    }

    /**
     * Returns the previously setup Localization
     *
     * @return      Localization for this Plugin
     */
    public Localization getLocalization() {
        return local;
    }

    public static int getInventorySpace(ItemStack item, Player player)
    {
        int invSpace = 0;
        for(int a = 0;a <= 35;a++)
        {
            if(player.getInventory().getItem(a) == null)
            {
                invSpace += 64;
                //player.sendMessage("[" + a + "] Is noththing");
            }
            else if(player.getInventory().getItem(a).isSimilar(item))
            {
                invSpace +=  64 - player.getInventory().getItem(a).getAmount();
                //player.sendMessage("[" + a + "] Is a gem x" + player.getInventory().getItem(a).getAmount());
            }
            else
            {
                //player.sendMessage("[" + a + "] Is something else x" + player.getInventory().getItem(a).getAmount());
            }
        }
        return invSpace;
    }
}
