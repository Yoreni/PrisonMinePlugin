package com.yoreni.mineplugin;

import com.yoreni.mineplugin.mine.Mine;
import com.yoreni.mineplugin.mine.MineResetCondition;
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

import java.util.*;

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
            try
            {
                if(args.length == 0)
                {
                    showHelpMenu(sender);
                }
//TODO fix the indentation
            else if(args[0].equalsIgnoreCase("create"))
            {
               handleCreateSubcommand(sender, args);
               return true;
            }
            else if(args[0].equalsIgnoreCase("add"))
            {
                handleAddSubcommand(sender, args);
                return true;
            }
            else if(args[0].equalsIgnoreCase("remove"))
            {
                handleRemoveSubcommand(sender, args);
                return true;
            }
            else if(args[0].equalsIgnoreCase("reset"))
            {
                handleResetSubcommand(sender, args);
                return true;
            }
            else if(args[0].equalsIgnoreCase("resize"))
            {
                handleResizeSubcommand(sender, args);
                return true;
            }
            else if(args[0].equalsIgnoreCase("info"))
            {
                handleInfoSubcommand(sender, args);
                return true;
            }
            else if(args[0].equalsIgnoreCase("list"))
            {
                handleListSubcommand(sender, args);
                return true;
            }
            else if(args[0].equalsIgnoreCase("rename"))
            {
                handleRenameSubcommand(sender, args);
                return true;
            }
            else if(args[0].equalsIgnoreCase("delete"))
            {
                handleDeleteSubcommand(sender, args);
                return true;
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

                if (args[2].equalsIgnoreCase("auto"))
                {
                    if (args[3].equalsIgnoreCase("timed"))
                    {
                        if(args.length < 5)
                        {
                            sender.sendMessage("Usage /mines settings <name> autoReset timed <minutes>");
                            return true;
                        }

                        Mine mine = validateMine(args[1], sender);

                        int resetInterval = Integer.parseInt(args[4]);
                        mine.setResetInterval(resetInterval);

                        if(resetInterval > 0)
                        {
                            MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-interval-change-success",
                                    new Placeholder("%mine%", args[1]),
                                    new Placeholder("%time%", String.valueOf(resetInterval)));
                        }
                        else
                        {
                            MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-interval-disable-success",
                                    new Placeholder("%mine%", args[1]));
                        }
                    }
                    else if (args[3].equalsIgnoreCase("percent"))
                    {
                        if(args.length < 5)
                        {
                            sender.sendMessage("Usage /mines settings <name> autoReset percent <percent>");
                            return true;
                        }

                        Mine mine = validateMine(args[1], sender);

                        int resetPercentage = Integer.parseInt(args[4]);

                        if(resetPercentage >= 0 && resetPercentage < 100)
                        {
                            mine.setResetPercentage(resetPercentage);
                            MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-percent-change-success",
                                    new Placeholder("%mine%", args[1]),
                                    new Placeholder("%percent%", Util.doubleToPercent(resetPercentage / 100D, 0)));
                        }
                        else
                        {
                            MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-percent-invalid-number");
                        }
                     }
                      else if (args[3].equalsIgnoreCase("disable"))
                     {
                         Mine mine = validateMine(args[1], sender);

                         mine.disableAutoReset();
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

                        Player player = (Player) sender;
                        Location loc = player.getLocation();

                        World world = loc.getWorld();
                        int x = loc.getBlockX();
                        int y = loc.getBlockY();
                        int z = loc.getBlockZ();
                        Location teleportPosition = new Location(world, x, y, z, loc.getYaw(), loc.getPitch());

                        mine.setTeleportPosition(teleportPosition);
                        MinePlugin.getMessageHandler().sendMessage(sender, "mine-tp-location-change-success",
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
            catch(InputMismatchException exception)
            {
                MinePlugin.getMessageHandler().sendMessage(sender, exception.getMessage());
                return true;
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
            String[] commands = {"create","add", "reset", "remove", "resize", "settings", "info", "list", "help", "rename", "delete"};
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
                String[] commands = {"autoReset", "teleportLocation"};
                StringUtil.copyPartialMatches(args[2], Arrays.asList(commands), showOnTabComplete);
                return showOnTabComplete;
            }
        }
        else if (args.length == 4)
        {
            if(args[0].equalsIgnoreCase("settings") && args[2].equalsIgnoreCase("autoReset"))
            {
                String[] commands = {"timed", "percent","disable"};
                StringUtil.copyPartialMatches(args[3], Arrays.asList(commands), showOnTabComplete);
                return showOnTabComplete;
            }
        }

        return new ArrayList<String>();
    }

    private void showHelpMenu(CommandSender sender)
    {
        MinePlugin.getMessageHandler().sendMessage(sender, "help-menu");
    }

    private void handleCreateSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.create"))
        {
            throw new InputMismatchException("no-perms");
        }

        if(args.length < 2)
        {
            throw new InputMismatchException("usage /mines create <name>");
        }

        WorldEditRegion region = getWERegion((Player) sender);
        Shape shape = createShape(region, args.length < 3 ? "cuboid" : args[2]);;

        if(Mine.get(args[1]) != null)
        {
            MinePlugin.getMessageHandler().sendMessage(sender, "pick-another-name",
                    new Placeholder("%mine%", args[1]));
            return;
        }

        Mine mine = Mine.createMine(shape, args[1]);

        MinePlugin.getMessageHandler().sendMessage(sender, "mine-created-success",
                new Placeholder("%mine%", args[1]));
    }

    private void handleAddSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.add"))
        {
            throw new InputMismatchException("no-perms");
        }

        if(args.length < 4)
        {
            throw new InputMismatchException("usage /mines add <name> <block> <percent>");
        }

        //getting and validating the mine
        Mine mine = validateMine(args[1], sender);

        if(!Util.getListOfBlocks().contains(args[2]))
        {
            throw new InputMismatchException("invalid-block");
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

    private void handleRemoveSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.remove"))
        {
            throw new InputMismatchException("no-perms");
        }

        if (args.length < 3)
        {
            throw new InputMismatchException("usage /mines remove <name> <block>");
        }

        //getting and validating the mine
        Mine mine = validateMine(args[1], sender);

        //if its star we will remove every block
        if(args[2].equals("*"))
        {
            mine.getCompostion().removeAllBlocks();
            MinePlugin.getMessageHandler().sendMessage(sender, "every-block-remove-success",
                    new Placeholder("%mine%", mine.getName()));
            return;
        }

        // otherwise we will try to remove the block specified
        if (!Util.getListOfBlocks().contains(args[2]))
        {
            throw new InputMismatchException("invalid-block");
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

    private void handleResetSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.reset"))
        {
            throw new InputMismatchException("no-perms");
        }

        if(args.length < 2)
        {
            throw new InputMismatchException("usage /mines reset <name>");
        }

        //getting and validating the mine
        Mine mine = validateMine(args[1], sender);

        mine.reset();
        MinePlugin.getMessageHandler().sendMessage(sender, "mine-reset-success",
                new Placeholder("%mine%", args[1]));
    }

    private void handleResizeSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.resize"))
        {
            throw new InputMismatchException("no-perms");
        }

        if(args.length < 2)
        {
            throw new InputMismatchException("usage /mines resize <name>");
        }

        Mine mine = validateMine(args[1], sender);
        WorldEditRegion region = getWERegion((Player) sender);
        Shape shape = createShape(region, args.length < 3 ? mine.getShape().getName() : args[2]);;

        mine.setShape(shape);
        sender.sendMessage("Mine " + args[1] + " resized.");
    }

    private void handleInfoSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.info"))
        {
            throw new InputMismatchException("no-perms");
        }

        if(args.length < 2)
        {
            throw new InputMismatchException("usage /mines info <name>");
        }

        Mine mine = validateMine(args[1], sender);

        MessageHandler messageHandler = MinePlugin.getMessageHandler();
        ArrayList<String> infoLines = new ArrayList<String>();
        infoLines.add(messageHandler.get("mine-info.title",
                new Placeholder("%mine%", mine.getName())
        ));

        final double percentMined = mine.getBlocksBroken() / (double) mine.getShape().getVolume();
        String minedText = messageHandler.get("mine-info.mined-info",
                new Placeholder("%mined%", Util.toCommas(mine.getBlocksBroken())),
                new Placeholder("%volume%", Util.toCommas(mine.getShape().getVolume())),
                new Placeholder("%percentmined%", Util.doubleToPercent(1 - percentMined,0)),
                new Placeholder("%percentleft%", Util.doubleToPercent(percentMined,0)));
        infoLines.add(minedText);

        if(mine.getResetCondition() == MineResetCondition.TIMED_INTERVAL)
        {
            String text = messageHandler.get("mine-info.reset-info-time",
                    new Placeholder("%interval%", mine.getResetInterval() + ""),
                    new Placeholder("%timeleft%", Util.formatTime(mine.getTimeUntillNextReset())));
            infoLines.add(text);
        }
        else if(mine.getResetCondition() == MineResetCondition.PERCENT_EMPTY)
        {
            String text = messageHandler.get("mine-info.reset-info-percentage",
                    new Placeholder("%resetpercentage%", Util.doubleToPercent(mine.getResetPercentage(), 0)));
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
    }

    private void handleListSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.list"))
        {
            throw new InputMismatchException("no-perms");
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
                player.spigot().sendMessage(component);
            }
            else
            {
                sender.sendMessage(component.getText());
            }
        }
    }

    private void handleRenameSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.rename"))
        {
            throw new InputMismatchException("no-perms");
        }

        if(args.length < 2)
        {
            throw new InputMismatchException("usage /mines rename <mine> <new name>");
        }

        Mine mine = validateMine(args[1], sender);

        String newName = args[2];
        if(Mine.get(newName) != null)
        {
            MinePlugin.getMessageHandler().sendMessage(sender, "pick-another-name",
                    new Placeholder("%mine%", args[1]));
            return;
        }

        Yml yaml = new Yml(MinePlugin.getInstance(), "mines/" + mine.getName());
        yaml.rename(newName);
        yaml.set("name", newName);
        mine.setName(newName);

        MinePlugin.getMessageHandler().sendMessage(sender, "mine-rename-success",
                new Placeholder("%oldname%", args[1]),
                new Placeholder("%newname%", newName));
    }

    private void handleDeleteSubcommand(CommandSender sender, String[] args)
    {
        if (!sender.hasPermission("prisonmines.admin.delete"))
        {
            throw new InputMismatchException("no-perms");
        }

        if (args.length < 2)
        {
            throw new InputMismatchException("usage /mines delete <mine>");
        }

        Mine mine = validateMine(args[1], sender);

        if(args.length >= 3 && args[2].equals("confirm"))
        {
            Yml yaml = new Yml(MinePlugin.getInstance(), "mines/" + mine.getName());
            yaml.delete();
            Mine.getMines().remove(mine);
            MinePlugin.getMessageHandler().sendMessage(sender, "mine-delete-success",
                    new Placeholder("%mine%", mine.getName()));
        }
        else
        {
            MinePlugin.getMessageHandler().sendMessage(sender, "mine-delete-prompt",
                    new Placeholder("%mine%", mine.getName()));
        }
    }

    private Mine validateMine(String mineName, CommandSender sender)
    {
        Mine mine = Mine.get(mineName);
        if(mine == null)
        {
            String message = MinePlugin.getMessageHandler().get("invalid-mine",
                    new Placeholder("%mine%", mineName));
            throw new InputMismatchException(message);
        }

        return mine;
    }

    private WorldEditRegion getWERegion(Player player)
    {
        WorldEditRegion region = new WorldEditRegion(player);
        if(!region.hasValidRegion())
        {
            throw new InputMismatchException("must-make-WE-region");
        }

        return region;
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
            throw new InputMismatchException("invalid-shape");
        }
    }
}
