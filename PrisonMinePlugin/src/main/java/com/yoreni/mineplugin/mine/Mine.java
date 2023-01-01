package com.yoreni.mineplugin.mine;

import com.yoreni.mineplugin.MinePlugin;
import com.yoreni.mineplugin.util.Util;
import com.yoreni.mineplugin.util.Yml;
import com.yoreni.mineplugin.util.shape.Cuboid;
import com.yoreni.mineplugin.util.shape.Cylinder;
import com.yoreni.mineplugin.util.shape.Shape;
import com.yoreni.mineplugin.util.shape.ShapeManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class Mine
{
    private static List<Mine> mines = new ArrayList<Mine>();

    protected int blocksBroken = 0;
    private String name;
    private Shape shape;
    private MineComposition compostion;
    private MineResetCondition resetCondition = MineResetCondition.NONE;
    private int resetValue = 0;
    private Location teleportPosition;
    private long lastReset = System.currentTimeMillis();

    private Mine(Shape shape, @NotNull String name)
    {
        this.shape = shape;
        this.name = name;

        this.compostion = new MineComposition();
    }

    /**
     * creates a new mine
     *
     * @param shape the size and shape of the mine
     * @param name the name of the mine
     * @return the newly created mine objected
     */
    public static Mine createMine(Shape shape, @NotNull String name)
    {
        Mine mine = new Mine(shape, name);
        mines.add(mine);
        return mine;
    }

    /**
     * gets an instance of an already existing mine based on its name
     * @param name the name of that mine
     * @return the instance of that mine if it exists
     */
    public static Mine get(String name)
    {
        Mine mine = null;
        for(Mine m : mines)
        {
            if(m.getName().equals(name))
            {
                mine = m;
            }
        }

        return mine;
    }

    /**
     * @return list of all mines
     */
    public static List<Mine> getMines()
    {
        return mines;
    }

    /**
     * reads all the mines and put them in to Mine objects
     */
    public static void initMineList()
    {
        List<Mine> mines = new ArrayList<Mine>();
        String minesFilePath = "plugins/" + MinePlugin.getInstance().getName() + "/mines";
        File minesDir = new File(minesFilePath);
        minesDir.mkdir();

        for (File file : minesDir.listFiles())
        {
            Yml yaml = new Yml(MinePlugin.getInstance(), file);
            Mine mine = Mine.readFromYaml(yaml);
            mines.add(mine);
        }

        Mine.mines = mines;
    }

    public static void saveMines()
    {
        for(Mine mine : mines)
        {
            mine.writeToYaml();
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public MineComposition getCompostion()
    {
        return compostion;
    }

    /**
     * resets the mine
     */
    public void reset()
    {
        //teleporting the players outside the mine if they are in the mine
        for(Player player : Bukkit.getOnlinePlayers())
        {
            if(shape.isInside(player.getLocation()))
            {
                player.teleport(getTeleportPosition());
            }
        }

        final int BLOCKS_PER_TICK =
                Integer.parseInt(MinePlugin.getConfigFile().getOrSetDefault("mineResetBlocksPerTick", 500));

        /*
            Some blocks of the mine will be reset every tick untill all of the blocks are reset
            cos if someone wants to make a large mine it will lag the server alot cos its
            place alot of blocks at once where as if we place them x blocks per tick then it will
            handle the load better.
         */
        new BukkitRunnable()
        {
            int tick = 0;
            final List<Location> blocks = shape.getBlocks();

            @Override
            public void run()
            {
                //these calculate which section of the blocks list we will set in the world.
                int startIndex = tick * BLOCKS_PER_TICK;
                int endIndex = Math.min(((tick + 1) * BLOCKS_PER_TICK), blocks.size());

                for (Location location : blocks.subList(startIndex, endIndex))
                {
                    Material blockType = compostion.getNextBlock();
                    shape.getWorld().getBlockAt(location).setType(blockType);
                }

                tick++;
                if(endIndex == blocks.size())
                {
                    this.cancel();
                }
            }
        }.runTaskTimer(MinePlugin.getInstance(), 1L, 1L);

        lastReset = System.currentTimeMillis();
        blocksBroken = 0;
    }

    /**
     * gets the time remaining untill the next reset
     * returns Long.MAX_VALUE if the mine does not have a resetInterval
     *
     * @return
     */
    public long getTimeUntillNextReset()
    {
        if(resetValue <= 0 || resetCondition != MineResetCondition.TIMED_INTERVAL)
        {
            return Long.MAX_VALUE;
        }
        else
        {
            long nextResetTime = lastReset + (resetValue * 60000L);
            return nextResetTime - System.currentTimeMillis();
        }
    }

    public void setShape(Shape shape)
    {
        this.shape = shape;
    }

    public int getResetInterval()
    {
        return resetCondition == MineResetCondition.TIMED_INTERVAL ? resetValue : 0;
    }

    /**
     * @return the amount of the mine that needs to be mined for it to be reset
     */
    public double getResetPercentage()
    {
        if(resetCondition == MineResetCondition.PERCENT_EMPTY) {
            return resetValue / 100D;
        } else {
            return 1;
        }
    }

    public boolean isResetDue()
    {
        switch (resetCondition) {
            case NONE:
                return false;
            case TIMED_INTERVAL:
                return getTimeUntillNextReset() < 0;
            case PERCENT_EMPTY:
                return 1 - getPercentMined() < getResetPercentage();
        }

        return false;
    }

    public double getPercentMined()
    {
        return blocksBroken / (double) shape.getVolume();
    }

    public void setResetInterval(int resetInterval)
    {
        resetCondition = MineResetCondition.TIMED_INTERVAL;
        resetValue = resetInterval;
    }

    public void setResetPercentage(int resetPercentage)
    {
        resetCondition = MineResetCondition.PERCENT_EMPTY;
        resetValue = resetPercentage;
    }

    public void disableAutoReset()
    {
        resetCondition = MineResetCondition.NONE;
        resetValue = 0;
    }

    public MineResetCondition getResetCondition()
    {
        return resetCondition;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    protected void changeBlocksBroken(int blocksBroken) {
        this.blocksBroken += blocksBroken;
        if(blocksBroken > shape.getVolume())
        {
            blocksBroken = shape.getVolume();
        }
    }

    public Shape getShape()
    {
        return shape;
    }

    /**
     * gets the teleport position where players will be teleported to if
     * they are in the mine when it resets
     *
     * @return the teleport position. however if ones not defined one will be calculated based on the mid points of the mine
     */
    public Location getTeleportPosition()
    {
        if(teleportPosition != null)
        {
            return teleportPosition;
        }
        else
        {
            //TODO change this. maybe add a getDefaultTpLocation to the shape and call that
            if(shape instanceof Cuboid cuboid)
            {
                int x = (cuboid.getPos1().getBlockX() + cuboid.getPos2().getBlockX()) / 2;
                int y = cuboid.getPos2().getBlockY() + 1;
                int z = (cuboid.getPos1().getBlockZ() + cuboid.getPos2().getBlockZ()) / 2;

                return new Location(cuboid.getWorld(), x, y, z);
            }
            else if(shape instanceof Cylinder cylinder)
            {
                int y = cylinder.getCenter().getBlockY() + cylinder.getHeight() + 1;

                return new Location(cylinder.getWorld()
                        , cylinder.getCenter().getBlockX()
                        , y
                        , cylinder.getCenter().getBlockZ());
            }
            else
            {
                return null;
            }
        }
    }

    public void setTeleportPosition(Location teleportPosition)
    {
        this.teleportPosition = teleportPosition;
    }

    private static Mine readFromYaml(Yml file)
    {
        String name = file.getString("name");

        Shape shape = ShapeManager.readFromYaml(file, "shape");
        Mine mine = new Mine(shape, name);
        mine.compostion = MineComposition.readFromYaml(file, "blocks");
        mine.resetCondition = MineResetCondition.valueOf(file.getOrDefault("settings.resetCondition", "NONE"));
        mine.resetValue = Integer.parseInt(file.getOrDefault("settings.resetValue", 0));

        mine.blocksBroken = Integer.parseInt(file.getOrDefault("data.minedblocks", 0));

        if(file.isSet("settings.teleportPosition"))
        {
            World world = Bukkit.getWorld(file.getString("settings.teleportLocation.world"));
            int x = file.getInt("settings.teleportLocation.x");
            int y = file.getInt("settings.teleportLocation.y");
            int z = file.getInt("settings.teleportLocation.z");
            float yaw = file.getFloat("settings.teleportLocation.yaw");
            float pitch = file.getFloat("settings.teleportLocation.pitch");

            if(world != null)
            {
                mine.teleportPosition = new Location(world, x, y, z, yaw, pitch);
            }
        }

        return mine;
    }

    private void writeToYaml()
    {
        Yml yaml = new Yml(MinePlugin.getInstance(),
                "mines/" + this.getName());
        yaml.set("name", getName());
        yaml.set("settings.resetCondition", resetCondition.toString());
        yaml.set("settings.resetValue", resetValue);

        yaml.set("data.minedblocks", blocksBroken);

        shape.writeToYaml(yaml, "shape");
        compostion.writeToYaml(yaml, "blocks");

        // saving mine settings
        if(teleportPosition != null)
        {
            yaml.set("settings.teleportLocation.world", teleportPosition.getWorld().getName());
            yaml.set("settings.teleportLocation.x", teleportPosition.getBlockX());
            yaml.set("settings.teleportLocation.y", teleportPosition.getBlockY());
            yaml.set("settings.teleportLocation.z", teleportPosition.getBlockZ());
            yaml.set("settings.teleportLocation.yaw", teleportPosition.getYaw());
            yaml.set("settings.teleportLocation.pitch", teleportPosition.getPitch());
        }
    }
}

