package com.yoreni.mineplugin;

import com.yoreni.mineplugin.util.*;
import com.yoreni.mineplugin.util.shape.Cuboid;
import com.yoreni.mineplugin.util.shape.Cylinder;
import com.yoreni.mineplugin.util.shape.Shape;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MineCommands implements CommandExecutor, TabCompleter
{
    MinePlugin main = null;

    public MineCommands(MinePlugin main)
    {
        this.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if(label.equalsIgnoreCase("mines"))
        {
            if(args.length == 0)
            {
                showHelpMenu(sender);
            }

            else if(args[0].equalsIgnoreCase("create"))
            {
                if(!sender.hasPermission("prisonmines.admin.create"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                if(args.length < 2)
                {
                    sender.sendMessage("usage /mines create <name>");
                    return true;
                }

                WorldEditRegion region = new WorldEditRegion((Player) sender);
                if(!region.hasValidRegion())
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "must-make-WE-region");
                    return true;
                }

                String shapeName = args.length < 3 ? "cuboid" : args[2];
                Shape shape;
                try
                {
                    shape = createShape(region, shapeName);
                }
                catch (IllegalArgumentException exception)
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "invalid-shape");
                    return true;
                }

                if(Mine.get(args[1]) != null)
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "pick-another-name",
                            new Placeholder("%mine%", args[1]));
                    return true;
                }

                Mine mine = Mine.createMine(shape, args[1]);

                MinePlugin.getMessageHandler().sendMessage(sender, "mine-created-success",
                        new Placeholder("%mine%", args[1]));
            }
            else if(args[0].equalsIgnoreCase("add"))
            {
                if(!sender.hasPermission("prisonmines.admin.add"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                if(args.length < 4)
                {
                    sender.sendMessage("usage /mines add <name> <block> <percent>");
                    return true;
                }

                //getting and validating the mine
                Mine mine = validateMine(args[1], sender);
                if(mine == null)
                    return true;

                if(!Util.getListOfBlocks().contains(args[2]))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "invalid-block");
                    return true;
                }

                Material block = Material.matchMaterial(args[2]);
                double percent = Double.parseDouble(args[3]) / 100;
                mine.getCompostion().addBlock(block, percent);

                MinePlugin.getMessageHandler().sendMessage(sender, "block-add-success",
                        new Placeholder("%mine%", mine.getName()),
                        new Placeholder("%percentfilled%", Util.doubleToPercent(percent, 2)),
                        new Placeholder("%block%", Util.materialToEnglish(block)),
                        new Placeholder("%percentleft%", Util.doubleToPercent(1 - mine.getCompostion().getTotalComostion(), 2)));
            }
            else if(args[0].equalsIgnoreCase("remove"))
            {
                if(!sender.hasPermission("prisonmines.admin.remove"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                if (args.length < 3)
                {
                    sender.sendMessage("usage /mines remove <name> <block>");
                    return true;
                }

                //getting and validating the mine
                Mine mine = validateMine(args[1], sender);
                if (mine == null)
                    return true;

                //if its star we will remove every block
                if(args[2].equals("*"))
                {
                    mine.getCompostion().removeAllBlocks();
                    MinePlugin.getMessageHandler().sendMessage(sender, "every-block-remove-success",
                            new Placeholder("%mine%", mine.getName()));
                    return true;
                }

                // otherwise we will try to remove the block specified
                if (!Util.getListOfBlocks().contains(args[2]))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "invalid-block");
                    return true;
                }

                Material block = Material.matchMaterial(args[2]);
                if(mine.getCompostion().hasBlock(block))
                {
                    mine.getCompostion().removeBlock(block);
                    MinePlugin.getMessageHandler().sendMessage(sender, "block-remove-success",
                            new Placeholder("%mine%", mine.getName()),
                            new Placeholder("%block%", Util.materialToEnglish(block)),
                            new Placeholder("%percentleft%", Util.doubleToPercent(1 - mine.getCompostion().getTotalComostion(), 2)));
                }
            }
            else if(args[0].equalsIgnoreCase("reset"))
            {
                if(!sender.hasPermission("prisonmines.admin.reset"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                if(args.length < 2)
                {
                    sender.sendMessage("usage /mines reset <name>");
                    return true;
                }

                //getting and validating the mine
                Mine mine = validateMine(args[1], sender);
                if(mine == null)
                    return true;

                mine.reset();
                MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-success",
                        new Placeholder("%mine%", args[1]));
            }
            else if(args[0].equalsIgnoreCase("resize"))
            {
                if(!sender.hasPermission("prisonmines.admin.resize"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                if(args.length < 2)
                {
                    sender.sendMessage("usage /mines resize <name>");
                    return true;
                }

                Mine mine = validateMine(args[1], sender);
                if(mine == null)
                    return true;

                WorldEditRegion region = new WorldEditRegion((Player) sender);
                if(!region.hasValidRegion())
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "must-make-WE-region");
                    return true;
                }
                String shapeName = args.length < 3 ? mine.getShape().getName() : args[2];

                Shape shape;
                try
                {
                    shape = createShape(region, shapeName);
                }
                catch (IllegalArgumentException exception)
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "invalid-shape");
                    return true;
                }


                mine.setShape(shape);
                sender.sendMessage("Mine " + args[1] + " resized.");
            }
            else if(args[0].equalsIgnoreCase("info"))
            {
                if(!sender.hasPermission("prisonmines.admin.info"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                if(args.length < 2)
                {
                    sender.sendMessage("usage /mines info <name>");
                    return true;
                }

                Mine mine = validateMine(args[1], sender);
                if(mine == null)
                    return true;

                MessageHandler messageHandler = MinePlugin.getMessageHandler();
                ArrayList<String> infoLines = new ArrayList<String>();
                infoLines.add(messageHandler.get("mine-info.title",
                        new Placeholder("%mine%", mine.getName())
                ));
                if(mine.getResetInterval() > 0)
                {
                    String text = messageHandler.get("mine-info.reset-info",
                            new Placeholder("%interval%", mine.getResetInterval() + ""),
                            new Placeholder("%timeleft%", Util.formatTime(mine.getTimeUntillNextReset())));
                    infoLines.add(text);
                }
                infoLines.add("");
                infoLines.add(messageHandler.get("mine-info.contents"));
                for(Material block : mine.getCompostion().getBlocks())
                {
                    String blockName = Util.materialToEnglish(block);
                    String formattedPercent = Util.doubleToPercent(mine.getCompostion().getChance(block), 2);

                    String text = messageHandler.get("mine-info.block-list",
                            new Placeholder("%block%", blockName),
                            new Placeholder("%percent%", formattedPercent));
                    infoLines.add(text);
                }

                //print it to the player
                for(String line : infoLines)
                {
                    sender.sendMessage(line);
                }
                return true;
            }
            else if(args[0].equalsIgnoreCase("list"))
            {
                if(!sender.hasPermission("prisonmines.admin.list"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                MessageHandler messageHandler = MinePlugin.getMessageHandler();

                ArrayList<TextComponent> infoLines = new ArrayList<TextComponent>();
                TextComponent title = new TextComponent("Mines");
                title.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                title.setBold(true);
                infoLines.add(title);
                //TODO net.kyori.adventure.text.Component
                for(Mine mine : Mine.getMines())
                {
                    TextComponent tc = new TextComponent("  - " + mine.getName());
                    tc.setClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND, "/mines info " + mine.getName()));
                    tc.setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT
                            ,new ComponentBuilder(messageHandler.get("mine-list.click-for-info")).create())
                    );

                    infoLines.add(tc);
                }

                for(TextComponent component : infoLines)
                {
                    if(sender instanceof Player)
                    {
                        Player player = (Player) sender;
                        player.sendMessage(component);
                    }
                    else
                    {
                        sender.sendMessage(component.getText());
                    }
                }
            }
            else if(args[0].equalsIgnoreCase("rename")) {
                if(!sender.hasPermission("prisonmines.admin.rename"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                if(args.length < 2)
                {
                    sender.sendMessage("usage /mines rename <mine> <new name>");
                    return true;
                }

                Mine mine = validateMine(args[1], sender);
                if(mine == null)
                    return true;

                String newName = args[2];
                if(Mine.get(newName) != null)
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "pick-another-name",
                            new Placeholder("%mine%", args[1]));
                    return true;
                }

                Yml yaml = new Yml(MinePlugin.getInstance(), "mines/" + mine.getName());
                yaml.rename(newName);
                yaml.set("name", newName);
                mine.setName(newName);

                MinePlugin.getMessageHandler().sendMessage(sender, "mine-rename-success",
                        new Placeholder("%oldname%", args[1]),
                        new Placeholder("%newname%", newName));
            }
            else if(args[0].equalsIgnoreCase("help"))
            {
                showHelpMenu(sender);
            }
            else if(args[0].equalsIgnoreCase("settings"))
            {
                if(!sender.hasPermission("prisonmines.admin.settings"))
                {
                    MinePlugin.getMessageHandler().sendMessage(sender, "no-perms");
                    return true;
                }

                if (args[2].equalsIgnoreCase("resetInterval"))
                {
                    if(args.length < 4)
                    {
                        sender.sendMessage("Usage /mines settings <name> resetInterval <minutes>");
                        return true;
                    }

                    Mine mine = validateMine(args[1], sender);
                    if(mine == null)
                        return true;

                    int resetInterval = Integer.parseInt(args[3]);
                    mine.setResetInterval(resetInterval);

                    if(resetInterval > 0)
                    {
                        MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-interval-change-success",
                                new Placeholder("%mine%", args[1]),
                                new Placeholder("time", String.valueOf(resetInterval)));
                    }
                    else
                    {
                        MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-interval-disable-success",
                                new Placeholder("%mine%", args[1]));
                    }
                }
                else if (args[2].equalsIgnoreCase("teleportLocation"))
                {
                    if(!(sender instanceof Player))
                    {
                        MinePlugin.getMessageHandler().sendMessage(sender, "command-only-for-players");
                        return true;
                    }

                    Mine mine = validateMine(args[1], sender);
                    if(mine == null)
                        return true;

                    Player player = (Player) sender;
                    Location loc = player.getLocation();

                    World world = loc.getWorld();
                    int x = loc.getBlockX();
                    int y = loc.getBlockY();
                    int z = loc.getBlockZ();
                    Location teleportPosition = new Location(world, x, y, z, loc.getYaw(), loc.getPitch());

                    mine.setTeleportPosition(teleportPosition);
                    MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-interval-change-success",
                            new Placeholder("%mine%", args[1]),
                            new Placeholder("%x%", String.valueOf(x)),
                            new Placeholder("%y%", String.valueOf(y)),
                            new Placeholder("%z%", String.valueOf(z)),
                            new Placeholder("%world%", world.getName()));
                }
            }
            else
            {
                showHelpMenu(sender);
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin"))
        {
            return null;
        }

        List<String> showOnTabComplete = new ArrayList<String>();

        if (args.length == 1)
        {
            String[] commands = {"create","add", "reset", "remove", "resize", "settings", "info", "list", "help"};
            StringUtil.copyPartialMatches(args[0], Arrays.asList(commands), showOnTabComplete);
            return showOnTabComplete;
        }
        else if (args.length == 2)
        {
            final List<String> subcommandBlacklist = Arrays.asList("create", "list", "help");
            if(!subcommandBlacklist.contains(args[0]))
            {
                List<String> options = new ArrayList<String>();
                for(Mine mine : Mine.getMines())
                {
                    options.add(mine.getName());
                }

                StringUtil.copyPartialMatches(args[1], options, showOnTabComplete);
                return showOnTabComplete;
            }
            else
            {
                return new ArrayList<String>();
            }
        }
        else if (args.length == 3)
        {
            if(args[0].equalsIgnoreCase("add"))
            {
                List<String> options = Util.getListOfBlocks();
                StringUtil.copyPartialMatches(args[2], options, showOnTabComplete);
                return showOnTabComplete;
            }
            else if(args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("resize"))
            {
                String[] shapes = {"cuboid", "cylinder"};
                StringUtil.copyPartialMatches(args[2], Arrays.asList(shapes), showOnTabComplete);
                return showOnTabComplete;
            }
            else if(args[0].equalsIgnoreCase("remove"))
            {
                Mine mine = Mine.get(args[1]);
                if(mine != null)
                {
                    Set<Material> blocks = mine.getCompostion().getBlocks();
                    List<String> options = new ArrayList<String>();
                    for(Material block : blocks)
                    {
                        String materialName = block.getKey().asString().split(":")[1];
                        options.add(materialName);
                    }

                    StringUtil.copyPartialMatches(args[2], options, showOnTabComplete);
                    return showOnTabComplete;
                }
            }
            else if(args[0].equalsIgnoreCase("settings"))
            {
                String[] commands = {"resetInterval", "teleportLocation"};
                StringUtil.copyPartialMatches(args[2], Arrays.asList(commands), showOnTabComplete);
                return showOnTabComplete;
            }
        }

        return new ArrayList<String>();
    }

    private void showHelpMenu(CommandSender sender)
    {
        MinePlugin.getMessageHandler().sendMessage(sender, "help-menu");
    }

    private Mine validateMine(String mineName, CommandSender sender)
    {
        Mine mine = Mine.get(mineName);
        if(mine == null)
        {
            MinePlugin.getMessageHandler().sendMessage(sender, "invalid-mine",
                    new Placeholder("%mine%", mineName));
            return null;
        }

        return mine;
    }

    private Shape createShape(WorldEditRegion region, String type)
    {
        if(type.equalsIgnoreCase("cuboid"))
        {
            return new Cuboid(region.getPos1(), region.getPos2());
        }
        else if(type.equalsIgnoreCase("cylinder"))
        {
            return new Cylinder(region.getPos1(), region.getPos2());
        }
        else
        {
            throw new IllegalArgumentException("Invalid shape type");
        }
    }
}
