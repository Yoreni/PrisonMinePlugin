package com.yoreni.mineplugin;

import com.yoreni.mineplugin.mine.Mine;
import com.yoreni.mineplugin.mine.MineComposition;
import com.yoreni.mineplugin.mine.MineResetCondition;
import com.yoreni.mineplugin.util.*;
import com.yoreni.mineplugin.util.shape.Shape;
import com.yoreni.mineplugin.util.shape.ShapeManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
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
    final MinePlugin main;

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
                    return true;
                }
                else if(args[0].equalsIgnoreCase("settings"))
                {
                    handleSettingsSubcommand(sender, args);
                    return true;
                }
                else
                {
                    showHelpMenu(sender);
                    return true;
                }
            }
            catch(InputMismatchException exception)
            {
                MessageHandler.getInstance().sendMessage(sender, exception.getMessage());
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

        List<String> showOnTabComplete = new ArrayList<>();

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
                List<String> options = new ArrayList<>();
                for(Mine mine : Mine.getMines())
                {
                    options.add(mine.getName());
                }

                StringUtil.copyPartialMatches(args[1], options, showOnTabComplete);
                return showOnTabComplete;
            }
            else
            {
                return new ArrayList<>();
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
                StringUtil.copyPartialMatches(args[2], ShapeManager.getShapeNames(), showOnTabComplete);
                return showOnTabComplete;
            }
            else if(args[0].equalsIgnoreCase("remove"))
            {
                Mine mine = Mine.get(args[1]);
                if(mine != null)
                {
                    Set<Material> blocks = mine.getCompostion().getBlocks();
                    List<String> options = new ArrayList<>();
                    for(Material block : blocks)
                    {
                        //String materialName = block.getKey().asString().split(":")[1]; //paper
                        String materialName = block.getKey().getKey();
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

        return new ArrayList<>();
    }

    private void showHelpMenu(CommandSender sender)
    {
        MessageHandler.getInstance().sendMessage(sender, "help-menu");
    }

    private void handleCreateSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.create"))
        {
            throw new InputMismatchException("no-perms");
        }

        if(args.length < 2)
        {
            throw new InputMismatchException("usage /mines create <name> (shape)");
        }

        WorldEditRegion region = getWERegion((Player) sender);
        Shape shape = args.length < 3 ? createShape(region,"cuboid") :
                                        createShape(region, args[2], Arrays.copyOfRange(args, 3, args.length));

        if(Mine.get(args[1]) != null)
        {
            MessageHandler.getInstance().sendMessage(sender, "pick-another-name",
                    new Placeholder("%mine%", args[1]));
            return;
        }

        Mine.createMine(shape, args[1]);

        MessageHandler.getInstance().sendMessage(sender, "mine-created-success",
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

        Mine mine = validateMine(args[1]);
        Material block = validateMaterial(args[2]);
        double percent = Double.parseDouble(args[3]) / 100;
        mine.getCompostion().addBlock(block, percent);

        MessageHandler.getInstance().sendMessage(sender, "block-add-success",
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
        Mine mine = validateMine(args[1]);

        //if its star we will remove every block
        if(args[2].equals("*"))
        {
            mine.getCompostion().removeAllBlocks();
            MessageHandler.getInstance().sendMessage(sender, "every-block-remove-success",
                    new Placeholder("%mine%", mine.getName()));
            return;
        }

        // otherwise we will try to remove the block specified
        Material block = validateMaterial(args[2]);
        if(mine.getCompostion().hasBlock(block))
        {
            mine.getCompostion().removeBlock(block);
            MessageHandler.getInstance().sendMessage(sender, "block-remove-success",
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
        Mine mine = validateMine(args[1]);

        mine.reset();
        MessageHandler.getInstance().sendMessage(sender, "mine-reset-success",
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
            throw new InputMismatchException("usage /mines resize <name> (shape)");
        }

        Mine mine = validateMine(args[1]);
        WorldEditRegion region = getWERegion((Player) sender);
        Shape shape = args.length < 3 ? createShape(region,mine.getShape().getName()) :
                createShape(region, args[2], Arrays.copyOfRange(args, 3, args.length));

        mine.setShape(shape);
        MessageHandler.getInstance().sendMessage(sender, "mine-resize-success",
                new Placeholder("%mine%", mine.getName()));
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

        Mine mine = validateMine(args[1]);

        for(String line : buildMineInfoText(mine))
        {
            sender.sendMessage(line);
        }
    }

    private List<String> buildMineInfoText(Mine mine)
    {
        MessageHandler messageHandler = MessageHandler.getInstance();
        ArrayList<String> infoLines = new ArrayList<>();
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
        MineComposition composition = mine.getCompostion();
        if (composition.getCount() == 0)
        {
            infoLines.add(messageHandler.get("mine-info.block-list-empty"
                    , new Placeholder("%mine%", mine.getName())));
        }
        else
        {
            for (Material block : composition.getBlocks())
            {
                String blockName = Util.materialToEnglish(block);
                String formattedPercent = Util.doubleToPercent(mine.getCompostion().getChance(block), 2);

                String text = messageHandler.get("mine-info.block-list",
                        new Placeholder("%block%", blockName),
                        new Placeholder("%percent%", formattedPercent));
                infoLines.add(text);
            }
        }

        return infoLines;
    }

    private void handleListSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.list"))
        {
            throw new InputMismatchException("no-perms");
        }

        MessageHandler messageHandler = MessageHandler.getInstance();

        ArrayList<TextComponent> infoLines = new ArrayList<>();
        TextComponent title = new TextComponent("Mines");
        title.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
        title.setBold(true);
        infoLines.add(title);

        for(Mine mine : Mine.getMines())
        {
            TextComponent tc = new TextComponent("  - " + mine.getName());
            tc.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND, "/mines info " + mine.getName()));
            tc.setHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT
                            ,new Text(messageHandler.get("mine-list.click-for-info")))
            );

            infoLines.add(tc);
        }

        for(TextComponent component : infoLines)
        {
            if(sender instanceof Player player)
            {
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

        Mine mine = validateMine(args[1]);

        String newName = args[2];
        if(Mine.get(newName) != null)
        {
            MessageHandler.getInstance().sendMessage(sender, "pick-another-name",
                    new Placeholder("%mine%", args[1]));
            return;
        }

        Yml yaml = new Yml(MinePlugin.getInstance(), "mines/" + mine.getName());
        yaml.rename(newName);
        yaml.set("name", newName);
        mine.setName(newName);

        MessageHandler.getInstance().sendMessage(sender, "mine-rename-success",
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

        Mine mine = validateMine(args[1]);

        if(args.length >= 3 && args[2].equals("confirm"))
        {
            Yml yaml = new Yml(MinePlugin.getInstance(), "mines/" + mine.getName());
            yaml.delete();
            Mine.getMines().remove(mine);
            MessageHandler.getInstance().sendMessage(sender, "mine-delete-success",
                    new Placeholder("%mine%", mine.getName()));
        }
        else
        {
            MessageHandler.getInstance().sendMessage(sender, "mine-delete-prompt",
                    new Placeholder("%mine%", mine.getName()));
        }
    }

    private void handleSettingsSubcommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("prisonmines.admin.settings"))
        {
            throw new InputMismatchException("no-perms");
        }

        if (args[2].equalsIgnoreCase("autoReset"))
        {
            handleAutoResetSetting(sender, args);
        }
        else if (args[2].equalsIgnoreCase("teleportLocation"))
        {
            handleTpLocSetting(sender, args);
        }
    }

    private void handleAutoResetSetting(CommandSender sender, String[] args)
    {
        Mine mine = validateMine(args[1]);

        if (args[3].equalsIgnoreCase("timed"))
        {
            if(args.length < 5)
            {
                throw new InputMismatchException("Usage /mines settings <name> autoReset timed <minutes>");
            }

            int resetInterval = Integer.parseInt(args[4]);
            mine.setResetInterval(resetInterval);

            if(resetInterval > 0)
            {
                MessageHandler.getInstance().sendMessage(sender, "mine-reset-interval-change-success",
                        new Placeholder("%mine%", args[1]),
                        new Placeholder("%time%", String.valueOf(resetInterval)));
                return;
            }

            MessageHandler.getInstance().sendMessage(sender, "mine-reset-interval-disable-success",
                    new Placeholder("%mine%", args[1]));
        }
        else if (args[3].equalsIgnoreCase("percent"))
        {
            if(args.length < 5)
            {
                throw new InputMismatchException("Usage /mines settings <name> autoReset percent <percent>");
            }

            int resetPercentage = Integer.parseInt(args[4]);

            if(resetPercentage < 0 || resetPercentage >= 100)
            {
                throw new InputMismatchException("mine-reset-percent-invalid-number");
            }

            mine.setResetPercentage(resetPercentage);
            MessageHandler.getInstance().sendMessage(sender, "mine-reset-percent-change-success",
                    new Placeholder("%mine%", args[1]),
                    new Placeholder("%percent%", Util.doubleToPercent(resetPercentage / 100D, 0)));
        }
        else if (args[3].equalsIgnoreCase("disable"))
        {
            mine.disableAutoReset();
            MessageHandler.getInstance().sendMessage(sender, "mine-reset-interval-disable-success",
                    new Placeholder("%mine%", args[1]));
        }
    }

    private void handleTpLocSetting(CommandSender sender, String[] args)
    {
        if(!(sender instanceof Player player))
        {
            throw new InputMismatchException("command-only-for-players");
        }

        Mine mine = validateMine(args[1]);

        Location loc = player.getLocation();

        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        Location teleportPosition = new Location(world, x, y, z, loc.getYaw(), loc.getPitch());

        mine.setTeleportPosition(teleportPosition);
        MessageHandler.getInstance().sendMessage(sender, "mine-tp-location-change-success",
                new Placeholder("%mine%", args[1]),
                new Placeholder("%x%", String.valueOf(x)),
                new Placeholder("%y%", String.valueOf(y)),
                new Placeholder("%z%", String.valueOf(z)),
                new Placeholder("%world%", world.getName()));
    }

    private Mine validateMine(String mineName)
    {
        Mine mine = Mine.get(mineName);
        if(mine == null)
        {
            String message = MessageHandler.getInstance().get("invalid-mine",
                    new Placeholder("%mine%", mineName));
            throw new InputMismatchException(message);
        }

        return mine;
    }

    private Material validateMaterial(String material)
    {
        if(!Util.getListOfBlocks().contains(material))
        {
            throw new InputMismatchException("invalid-block");
        }

        return Material.matchMaterial(material);
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

    private Shape createShape(WorldEditRegion region, String type, String... args)
    {
        try
        {
            Class<? extends Shape> clas = ShapeManager.getShapeClass(type);
            if (clas == null)
            {
                throw new InputMismatchException("invalid-shape");
            }

            return clas.getConstructor(Location.class, Location.class, String[].class)
                    .newInstance(region.getPos1(), region.getPos2(), args);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            throw new InputMismatchException(exception.getMessage());
        }
    }
}
