package com.yoreni.mineplugin.util.shape;

import com.yoreni.mineplugin.util.Yml;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cylinder extends Shape
{
    private Location center;
    private double xRadius;
    private double zRadius;
    private int height;

    private int volume = 0;

    public Cylinder(Location pos1, Location pos2)
    {
        if(!pos1.getWorld().equals(pos2.getWorld()))
        {
            throw new IllegalArgumentException("The 2 postions are not in the same world");
        }

        World world = pos1.getWorld();
        double midX = (pos1.getBlockX() + pos2.getBlockX()) / 2;
        double lowY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        double midZ = (pos1.getBlockZ() + pos2.getBlockZ()) / 2;
        center = new Location(world, midX, lowY, midZ);

        xRadius = (Math.abs(pos1.getBlockX() - pos2.getBlockX()) / 2) + 0.5;
        zRadius = (Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) / 2) + 0.5;
        height = Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1;
    }

    public Cylinder(Location center, double xRadius, double zRadius, int height)
    {
        this.center = center;
        this.xRadius = xRadius;
        this.zRadius = zRadius;
        this.height = height;
    }

    @Override
    public List<Location> getBlocks()
    {
        // yes i basicly just stole the world edit code.
        List<Location> blocks = new ArrayList<Location>();

        final double invRadiusX = 1 / xRadius;
        final double invRadiusZ = 1 / zRadius;

        final int ceilRadiusX = (int) Math.ceil(xRadius);
        final int ceilRadiusZ = (int) Math.ceil(zRadius);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x)
        {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ: for (int z = 0; z <= ceilRadiusZ; ++z)
            {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = (xn * xn) + (zn * zn);
                if (distanceSq > 1)
                {
                    if (z == 0)
                    {
                        break forX;
                    }
                    break forZ;
                }

                for (int y = 0; y < height; ++y)
                {
                    blocks.add(new Location(getWorld(),
                            center.getBlockX() + x,
                            Math.ceil(center.getY() + y),
                            center.getBlockZ() + z));

                    blocks.add(new Location(getWorld(),
                            center.getBlockX() - x,
                            Math.ceil(center.getY() + y),
                            center.getBlockZ() + z));

                    blocks.add(new Location(getWorld(),
                            center.getBlockX() + x,
                            Math.ceil(center.getY() + y),
                            center.getBlockZ() - z));

                    blocks.add(new Location(getWorld(),
                            center.getBlockX() - x,
                            Math.ceil(center.getY() + y),
                            center.getBlockZ() - z));
                }
            }
        }

        return blocks;
    }

    @Override
    public World getWorld()
    {
        return center.getWorld();
    }

    @Override
    public void writeToYaml(Yml file, String path)
    {
        //file.setPathPrefix(path);

        file.set(path + ".shape", "cylinder");
        file.set(path + ".world", getWorld().getName());
        file.set(path + ".xRadius", xRadius);
        file.set(path + ".zRadius", zRadius);
        file.set(path + ".height", height);

        file.set(path + ".center.x", center.getX());
        file.set(path + ".center.y", center.getY());
        file.set(path + ".center.z", center.getZ());
    }

    @Override
    public String getName() {
        return "cylinder";
    }

    @Override
    public boolean isInside(Location loc)
    {
        if(!loc.getWorld().equals(getWorld()))
        {
            return false;
        }

        if(loc.getBlockY() >= center.getBlockY() && loc.getBlockY() <= center.getBlockY() + height)
        {
            final int x = Math.abs(loc.getBlockX()) - Math.abs(center.getBlockX());
            final int z = Math.abs(loc.getBlockZ()) - Math.abs(center.getBlockZ());

            final double invRadiusX = 1 / xRadius;
            final double invRadiusZ = 1 / zRadius;

            double xn = x * invRadiusX;
            double zn = z * invRadiusZ;

            double distanceSq = (xn * xn) + (zn * zn);

            if(distanceSq > 1)
            {
                return false;
            }
            else
            {
                return true;
            }
        }

        return false;
    }

    //TODO this could be opimised
    @Override
    public int getVolume()
    {
        //if it has already been calculated we will return that
        if(volume != 0)
        {
            return volume;
        }

        Set<Location> blocks = new HashSet<Location>();

        final double invRadiusX = 1 / xRadius;
        final double invRadiusZ = 1 / zRadius;

        final int ceilRadiusX = (int) Math.ceil(xRadius);
        final int ceilRadiusZ = (int) Math.ceil(zRadius);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x)
        {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ:
            for (int z = 0; z <= ceilRadiusZ; ++z)
            {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = (xn * xn) + (zn * zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break forZ;
                }

                blocks.add(new Location(getWorld(),
                        center.getBlockX() + x,
                        0,
                        center.getBlockZ() + z));

                blocks.add(new Location(getWorld(),
                        center.getBlockX() - x,
                        0,
                        center.getBlockZ() + z));

                blocks.add(new Location(getWorld(),
                        center.getBlockX() + x,
                        0,
                        center.getBlockZ() - z));

                blocks.add(new Location(getWorld(),
                        center.getBlockX() - x,
                        0,
                        center.getBlockZ() - z));
            }
        }
        volume = blocks.size() * height;
        return volume;
    }

    public static Shape readFromYaml(Yml file, String path)
    {
        //file.setPathPrefix(path);
        if(!file.getString(path + ".shape").equals("cylinder"))
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

        Location center = new Location(world,
                file.getDouble(path + ".center.x"), file.getDouble(path + ".center.y"),
                file.getDouble(path + ".center.z"));
        double xRadius = file.getDouble(path + ".xRadius");
        double zRadius = file.getDouble(path + ".zRadius");
        int height = file.getInt(path + ".height");

        return new Cylinder(center,xRadius, zRadius, height);
    }

    public Location getCenter()
    {
        return center;
    }

    public double getxRadius()
    {
        return xRadius;
    }

    public double getzRadius()
    {
        return zRadius;
    }

    public int getHeight()
    {
        return height;
    }
}
