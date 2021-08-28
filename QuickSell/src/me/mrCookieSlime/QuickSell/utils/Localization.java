package me.mrCookieSlime.QuickSell.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import de.leonhard.storage.Yaml;
import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Localization {

    private Yaml file;

    protected boolean allowUnicodes = true;

    /**
     * Creates a new Localization Object for the specified Plugin
     *
     * @param plugin
     *            The Plugin this Localization is made for
     */
    public Localization(@NonNull Plugin plugin) {
        //this.file = new File("plugins/" + plugin.getDescription().getName().replace(" ", "_"), "messages.yml");
        this.file = new Yaml("messages", "plugins/" + plugin.getDescription().getName().replace(" ", "_"));
        file.addDefaultsFromInputStream(plugin.getClass().getResourceAsStream("/messages.yml"));
        //this.config = new Config(file);
    }

    /*
     * Creates a new Localization Object for the specified Plugin
     *
     * @param plugin
     *            The Plugin this Localization is made for
     * @param name
     *            The Name of the file you want to use
     */
   /* public Localization(@NonNull Plugin plugin, @NonNull String name) {
        this.file = new File("plugins/" + plugin.getDescription().getName().replace(" ", "_"), name);
        this.config = new Config(file);
    }*/

    /*
     * Creates a new Localization Object for the specified Plugin
     *
     * @param plugin
     *            The Plugin this Localization is made for
     * @param name
     *            The Name of the file you want to use
     * @param allowUnicodes
     *            Whether Unicodes are allowed
     */
    /*  THESE ARE NOT USED
    public Localization(@NonNull Plugin plugin, @NonNull String name, boolean allowUnicodes) {
        this.file = new File("plugins/" + plugin.getDescription().getName().replace(" ", "_") + "/" + name);
        this.config = new Config(file);
        this.allowUnicodes = allowUnicodes;
    }

    public Localization(@NonNull File file) {
        this.file = file;
        this.config = new Config(file);
    }*/

    public String getPrefix() {
        return file.get("prefix") != null ? getMessage("prefix") : "";
    }

    /**
     * Sets the Default Message/s for the specified Key
     *
     * @param key
     *            The Key of those Messages
     * @param messages
     *            The Messages which this key will refer to by default
     */
    public void setDefaultMessages(String key, String... messages) {
        setDefaultMessages(key, Arrays.asList(messages));
    }

    public List<String> setDefaultMessages(String key, List<String> messages)
    {
        if (file.get(key) == null) {
            file.set(key, messages);
        }

        return getMessages(key);
    }

    /**
     * Sets the Default Message/s for the specified Key
     *
     * @param key
     *            The Key of those Message
     * @param message
     *            The Message which this key will refer to by default
     * @return The message previously assigned, if none was set then the message passed.
     */
    public String setDefaultMessage(String key, String message) {
        String msg = getMessage(message);

        if (msg == null) {
            file.set(key, message);
            return allowUnicodes ? translateUnicodes(message) : message;
        }

        return msg;
    }

    /**
     * Sets the default Message Prefix
     *
     * @param prefix
     *            The Prefix by default
     */
    public void setPrefix(String prefix) {
        setDefaultMessage("prefix", prefix);
    }

    /**
     * Returns the Strings referring to the specified Key
     *
     * @param key
     *            The Key of those Messages
     * @return The List this key is referring to
     */
    public List<String> getMessages(String key) {
        if (!allowUnicodes) {
            return file.getStringList(key);
        } else {
            return file.getStringList(key).stream().map(Localization::translateUnicodes).collect(Collectors.toList());
        }
    }

    /**
     * Returns the Strings referring to the specified Key
     *
     * @param key
     *            The Key of those Messages
     * @return The Array of messages this key is referring to
     */
    public String[] getMessagesArray(String key) {
        if (!allowUnicodes) {
            List<String> list = file.getStringList(key);
            return list.toArray(new String[0]);
        }

        return file.getStringList(key).stream().map(Localization::translateUnicodes).toArray(String[]::new);
    }

    /**
     * Returns the String referring to the specified Key
     *
     * @param key
     *            The Key of those Messages
     * @return The Message this key is referring to
     */
    public String getMessage(String key) {
        return allowUnicodes ? translateUnicodes(file.getString(key)) : file.getString(key);
    }

    public void sendMessage(CommandSender sender, String key, boolean addPrefix) {
        String prefix = addPrefix ? getPrefix() : "";
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + getMessage(key)));
    }

    public void sendMessage(CommandSender sender, String key, boolean addPrefix, Variable... vars) {
        String prefix = addPrefix ? getPrefix() : "";
        String message = getMessage(key);

        for (Variable var:vars) {
            message = var.apply(message);
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public void sendMessage(CommandSender sender, String key, boolean addPrefix, UnaryOperator<String> function) {
        String prefix = addPrefix ? getPrefix() : "";
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + function.apply(getMessage(key))));
    }

    public void sendMessages(CommandSender sender, String key, boolean addPrefix, UnaryOperator<String> function) {
        String prefix = addPrefix ? getPrefix() : "";

        for (String translation : getMessages(key)) {
            translation = function.apply(translation);

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + translation));
        }
    }

    public void sendMessage(CommandSender sender, String key) {
        String prefix = getPrefix();
        String message = ChatColor.translateAlternateColorCodes('&', prefix + getMessage(key));

        if (sender instanceof Player) {
            sender.sendMessage(message);
        } else {
            sender.sendMessage(ChatColor.stripColor(message));
        }
    }

    public void sendMessages(CommandSender sender, String key) {
        String prefix = getPrefix();

        for (String translation : getMessages(key)) {
            String message = ChatColor.translateAlternateColorCodes('&', prefix + translation);

            if (sender instanceof Player) {
                sender.sendMessage(message);
            } else {
                sender.sendMessage(ChatColor.stripColor(message));
            }
        }
    }

    public void sendActionBar(Player player, String key, Variable... vars)
    {
        String message = getMessage(key);

        for (Variable var:vars)
        {
            message = var.apply(message);
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',  message)));

    }

    public void broadcastMessage(String key, boolean addPrefix, UnaryOperator<String> function) {
        String prefix = addPrefix ? getPrefix() : "";
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + function.apply(getMessage(key))));
    }

    public void broadcastMessages(String key, boolean addPrefix, UnaryOperator<String> function) {
        String prefix = addPrefix ? getPrefix() : "";

        for (String translation : getMessages(key)) {
            translation = function.apply(translation);

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + translation));
        }
    }

    public void broadcastMessage(String key) {
        String prefix = getPrefix();
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + getMessage(key)));
    }

    public void broadcastMessages(String key) {
        String prefix = getPrefix();

        for (String translation : getMessages(key)) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + translation));
        }
    }

    /**
     * Reloads the messages.yml File
     */
    public void reload()
    {

    }

    /**
     * Saves this Localization to its File
     */
    public void save()
    {

    }

    protected static String translateUnicodes(String str) {
        if (str == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        char[] chars = str.toCharArray();
        int i = 0;

        while (i < chars.length) {
            if (chars[i] == '[' && i + 1 < chars.length && chars[i + 1] == 'u') {
                i += 2;

                CharSequence unicode = new StringBuilder();
                int j;

                for (j = 0; j < 6 && i < chars.length; j++, i++) {
                    if (chars[i] == ']') {
                        unicode = String.valueOf((char) Integer.parseInt(unicode.toString(), 16));
                        break;
                    } else {
                        ((StringBuilder) unicode).append(chars[i]);
                    }
                }

                builder.append(unicode);
            } else {
                builder.append(chars[i]);
            }

            i++;
        }

        return builder.toString();
    }

}
