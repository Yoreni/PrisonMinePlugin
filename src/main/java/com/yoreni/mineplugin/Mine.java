package com.yoreni.mineplugin;

import com.yoreni.mineplugin.util.shape.Cuboid;
import com.yoreni.mineplugin.util.shape.Cylinder;
import com.yoreni.mineplugin.util.shape.Shape;
import de.leonhard.storage.Yaml;
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

    private String name = null;
    private Shape shape;
    private MineComostion compostion;
    private int resetInterval = 0;
    private Location teleportPosition;
    private long lastReset = System.currentTimeMillis();

    private Mine(Shape shape, @NotNull String name)
    {
        this.shape = shape;
        this.name = name;

        this.compostion = new MineComostion();
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
     * @param name
     * @return
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
     * gets a list of all mines
     * @return
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
        String minesFilePath = MinePlugin.getInstance().pluginFolder + "/mines";
        File minesDir = new File(minesFilePath);
        minesDir.mkdir();

        for (File file : minesDir.listFiles())
        {
            Yaml yaml = new Yaml(file);
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

    public MineComostion getCompostion()
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
                MinePlugin.getInstance().config.getOrSetDefault("mineResetBlocksPerTick", 500);

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
                int startIndex = tick * BLOCKS_PER_TICK;           //  (end of the list)
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
    }

    /**
     * gets the time remaining untill the next reset
     * returns -1 if the mine does not have a resetInterval
     *
     * @return
     */
    public long getTimeUntillNextReset()
    {
        if(resetInterval <= 0)
        {
            return Long.MAX_VALUE;
        }
        else
        {
            long nextResetTime = lastReset + (resetInterval * 60000);
            return nextResetTime - System.currentTimeMillis();
        }
    }

    public void setShape(Shape shape)
    {
        this.shape = shape;
    }

    public int getResetInterval()
    {
        return resetInterval;
    }

    public void setResetInterval(int resetInterval)
    {
        this.resetInterval = resetInterval;
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
            if(shape instanceof Cuboid)
            {
                Cuboid cuboid = (Cuboid) shape;

                int x = (cuboid.getPos1().getBlockX() + cuboid.getPos2().getBlockX()) / 2;
                int y = cuboid.getPos2().getBlockY() + 1;
                int z = (cuboid.getPos1().getBlockZ() + cuboid.getPos2().getBlockZ()) / 2;

                return new Location(cuboid.getWorld(), x, y, z);
            }
            else if(shape instanceof Cylinder)
            {
                Cylinder cylinder = (Cylinder) shape;

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

    private static Mine readFromYaml(Yaml file)
    {
        String name = file.getString("name");
        int resetInterval = file.getInt("settings.resetInterval");

        Shape shape = Shape.readFromYaml(file, "shape");
        Mine mine = new Mine(shape, name);
        mine.compostion = MineComostion.readFromYaml(file, "blocks");

        file.setPathPrefix("settings");
        mine.resetInterval = resetInterval;
        if(file.getMapParameterized("").containsKey("teleportPosition"))
        {
            World world = Bukkit.getWorld(file.getString("teleportLocation.world"));
            int x = file.getInt("teleportLocation.x");
            int y = file.getInt("teleportLocation.y");
            int z = file.getInt("teleportLocation.z");
            float yaw = file.getFloat("teleportLocation.yaw");
            float pitch = file.getFloat("teleportLocation.pitch");

            if(world != null)
            {
                mine.teleportPosition = new Location(world, x, y, z, yaw, pitch);
            }
        }

        return mine;
    }

    private void writeToYaml()
    {
        Yaml yaml = new Yaml(this.getName(), MinePlugin.getInstance().pluginFolder + "/mines");
        yaml.set("name", getName());
        yaml.set("settings.resetInterval", resetInterval);

        shape.writeToYaml(yaml, "shape");
        compostion.writeToYaml(yaml, "blocks");

        // saving mine settings
        yaml.setPathPrefix("settings");
        if(teleportPosition != null)
        {
            yaml.set("teleportLocation.world", teleportPosition.getWorld().getName());
            yaml.set("teleportLocation.x", teleportPosition.getBlockX());
            yaml.set("teleportLocation.y", teleportPosition.getBlockY());
            yaml.set("teleportLocation.z", teleportPosition.getBlockZ());
            yaml.set("teleportLocation.yaw", teleportPosition.getYaw());
            yaml.set("teleportLocation.pitch", teleportPosition.getPitch());
        }
    }
}

class MineComostion
{
    /**
     * this is how the Block compostion wil be stored.
     * The Material is the block that it in
     * The double is the chance the block will appear in the nextBlock() function
     * the doube ranges from 0<x<=1
     */
    private Map<Material, Double> compostion;

    public MineComostion()
    {
        compostion = new HashMap<Material, Double>();
    }

    /**
     * gets data from a yml and tries to create a MineCompostion
     * from it.
     *
     * @param file the file you want it to look at
     * @param path the path of where you want it to be
     * @return the object it created
     */
    public static MineComostion readFromYaml(Yaml file, String path)
    {
        file.setPathPrefix(path);
        MineComostion mineComostion = new MineComostion();

        Map<String, Double> map = file.getMapParameterized("");
        for(String key : map.keySet())
        {
            Material block = Material.getMaterial(key);
            if(block != null)
            {
                double chance = Double.parseDouble(((Object) map.get(key)).toString());
                if(chance > 0)
                {
                    mineComostion.addBlock(block, chance);
                }
            }
        }

        return mineComostion;
    }

    /**
     * writes this object to a yml file
     *
     * @param file the file you want to write it to
     * @param path the path of where in the file you want to write it
     */
    public void writeToYaml(Yaml file, String path)
    {
        file.setPathPrefix(path);

        //this removes the blocks in the mine of the config file that isnt in the compistion list
        //this is to stop the mines messing up when a block has been removed
        Map<String, Double> existingBlocksInFile = file.getMapParameterized("");
        for(String key : existingBlocksInFile.keySet())
        {
            Material block = Material.getMaterial(key);
            if (block != null)
            {
                if(!hasBlock(block))
                {
                    file.set(block.toString(), 0D);
                }
            }
        }

        //add new blocks or changing them if there are any
        for(Material block : compostion.keySet())
        {
            double chance = compostion.get(block);
            file.set(block.toString(), chance);
        }
    }

    /**
     * adds a block to the mine
     *
     * @param block the block you want to add
     * @param chance the % of that block it will appear
     */
    public void addBlock(Material block, double chance)
    {
        if(!block.isBlock())
        {
            throw new IllegalArgumentException("Items can not be added to mines");
        }

        //the chance gets rounded to 10dp to stop any wiredness
        chance = Math.round(chance * 1e10);
        chance /= 1e10;

        compostion.put(block, chance);
    }

    /**
     * removes a block from the compostion
     *
     * @param block the block you want to remove
     */
    public void removeBlock(Material block)
    {
        if(hasBlock(block))
        {
            compostion.remove(block);
        }
    }

    /**
     * this removes everything from the compsotion
     */
    public void removeAllBlocks()
    {
        compostion = new HashMap<Material, Double>();
    }

    /**
     * returns true or false based on whether the block is in the compostion
     * or not
     *
     * @param block
     * @return
     */
    public boolean hasBlock(Material block)
    {
        return compostion.containsKey(block);
    }

    public Set<Material> getBlocks()
    {
        return compostion.keySet();
    }

    public double getChance(Material block)
    {
        if(hasBlock(block))
        {
            return compostion.get(block);
        }
        else
        {
            return 0;
        }
    }

    /**
     * gets the total percent of blocks in the composition
     *
     * @return
     */
    public double getTotalComostion()
    {
        Collection<Double> chances = compostion.values();

        double sum = 0;
        for(double number : chances)
        {
            sum += number;
        }

        return sum;
    }

    /**
     * randomly picks the next block based of the compition
     *
     * @return
     */
    public Material getNextBlock()
    {
        double random = Math.random();
        double accChance = 0;

        for(Material block : compostion.keySet())
        {
            accChance += compostion.get(block);
            if(accChance >= random)
            {
                return block;
            }
        }

        return Material.AIR;
    }
}