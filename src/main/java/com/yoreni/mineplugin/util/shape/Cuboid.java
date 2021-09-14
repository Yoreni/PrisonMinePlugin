package com.yoreni.mineplugin.util.shape;

import com.yoreni.mineplugin.util.Yml;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class Cuboid extends Shape
{
    Location pos1;
    Location pos2;

    public Cuboid(Location pos1, Location pos2)
    {
        if(!pos1.getWorld().equals(pos2.getWorld()))
        {
            throw new IllegalArgumentException("The 2 postions are not in the same world");
        }

        //making sure pos1 and pos2 are placed in a certain way so the toBlock method works
        int x1 = pos1.getBlockX();
        int y1 = pos1.getBlockY();
        int z1 = pos1.getBlockZ();
        int x2 = pos2.getBlockX();
        int y2 = pos2.getBlockY();
        int z2 = pos2.getBlockZ();

        int lowestX = Math.min(x1, x2);
        int lowestY = Math.min(y1, y2);
        int lowestZ = Math.min(z1, z2);
        int highestX = Math.max(x1, x2);
        int highestY = Math.max(y1, y2);
        int highestZ = Math.max(z1, z2);

        this.pos1 = new Location(pos1.getWorld(), lowestX, lowestY, lowestZ);
        this.pos2 = new Location(pos1.getWorld(), highestX, highestY, highestZ);
    }

    public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2)
    {
        this(new Location(world, x1, y1, z1), new Location(world,x2,y2,z2));
    }

    @Override
    public List<Location> getBlocks()
    {
        List<Location> blocks = new ArrayList<Location>();
        for(int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++)
        {
            for(int y = pos1.getBlockY(); y <= pos2.getBlockY(); y++)
            {
                for(int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++)
                {
                    blocks.add(new Location(getWorld(), x, y, z));
                }
            }
        }

        return blocks;
    }

    @Override
    public World getWorld()
    {
        return pos1.getWorld();
    }

    @Override
    public void writeToYaml(Yml file, String path)
    {
        //file.setPathPrefix(path);
        file.set(path + ".shape", "cuboid");
        file.set(path + ".world", getWorld().getName());

        file.set(path + ".pos1.x", pos1.getBlockX());
        file.set(path + ".pos1.y", pos1.getBlockY());
        file.set(path + ".pos1.z", pos1.getBlockZ());

        file.set(path + ".pos2.x", pos2.getBlockX());
        file.set(path + ".pos2.y", pos2.getBlockY());
        file.set(path + ".pos2.z", pos2.getBlockZ());
    }

    @Override
    public String getName() {
        return "cuboid";
    }

    @Override
    public boolean isInside(Location loc)
    {
       if(!loc.getWorld().equals(getWorld()))
       {
           return false;
       }

       if(loc.getBlockX() >= pos1.getBlockX() && loc.getBlockX() <= pos2.getBlockX())
       {
           if(loc.getBlockY() >= pos1.getBlockY() && loc.getBlockY() <= pos2.getBlockY())
           {
               if(loc.getBlockZ() >= pos1.getBlockZ() && loc.getBlockZ() <= pos2.getBlockZ())
               {
                    return true;
               }
           }
       }

       return false;
    }

    @Override
    public int getVolume()
    {
        final int width = Math.abs(pos1.getBlockX() - pos2.getBlockX());
        final int length = Math.abs(pos1.getBlockZ() - pos2.getBlockZ());
        final int height = Math.abs(pos1.getBlockY() - pos2.getBlockY());
        return width * length * height;
    }

    public static Shape readFromYaml(Yml file, String path)
    {
        //file.setPathPrefix(path);

        if(!file.getString(path + ".shape").equals("cuboid"))
        {
            return null;
        }
        World world = Bukkit.getWorld(file.getString(path + ".world"));
        if(world == null)
        {
            final String worldName = file.getString(path + ".world");
            throw new NullPointerException(String.format("The world (%s) defined in %s doesnt exist."
                    , worldName, file.getName()));
        }

        Location pos1 = new Location(world,
                file.getInt(path + ".pos1.x"), file.getInt(path + ".pos1.y"),
                file.getInt(path + ".pos1.z"));

        Location pos2 = new Location(world,
                file.getInt(path + ".pos2.x"), file.getInt(path + ".pos2.y"),
                file.getInt(path + ".pos2.z"));

        return new Cuboid(pos1, pos2);
    }

    public Location getPos1()
    {
        return pos1;
    }

    public Location getPos2()
    {
        return pos2;
    }
}
