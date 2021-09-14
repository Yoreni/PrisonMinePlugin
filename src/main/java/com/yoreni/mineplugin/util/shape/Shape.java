package com.yoreni.mineplugin.util.shape;

import com.yoreni.mineplugin.util.Yml;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;

import java.util.List;

public abstract class Shape
{

    /**
     * gets a list of all postions thats part of the shape
     *
     * @return
     */
    public abstract List<Location> getBlocks();

    /**
     * gets the world of the shape
     * @return
     */
    public abstract World getWorld();

    /**
     * saves the shapes data to a yml file
     * @param file the file you want to save it to
     * @param path the path of the file
     */
    public abstract void writeToYaml(Yml file, String path);

    /**
     * gets the name of the shape
     * @return
     */
    public abstract String getName();

    /**
     * checks if a position is inside the shape
     * @param loc the postion you want to check
     * @return true if its inside otherwise false
     */
    public abstract boolean isInside(Location loc);

    /**
     * returns the voulume of the shape
     * @return
     */
    public abstract  int getVolume();

    /**
     * reads the shape data from a yml file and makes a shape object out of it.
     *
     * @param file
     * @param path
     * @return
     */
    public static Shape readFromYaml(Yml file, String path)
    {
        //file.setPathPrefix(path);
        if(file.get(path + ".shape").equals("cuboid"))
        {
            return Cuboid.readFromYaml(file, path);
        }
        else if(file.get(path + ".shape").equals("cylinder"))
        {
            return Cylinder.readFromYaml(file, path);
        }
        return null;
    }
}
