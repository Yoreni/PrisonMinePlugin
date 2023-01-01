package com.yoreni.mineplugin.util.shape;

import com.yoreni.mineplugin.util.Yml;

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
     * I would also make a static version of this method
     * and this method called the static version
     * @return returns the name of the shape
     */
    public abstract String getName();
}
