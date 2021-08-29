/**
 *  easy to know a players world edit region with out having a buch of annoying meessy code
 */

package com.yoreni.mineplugin.util;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.yoreni.mineplugin.MinePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WorldEditRegion
{
    Location pos1;
    Location pos2;

    public WorldEditRegion(Player player)
    {
        if (MinePlugin.WORLD_EDIT != null && MinePlugin.WORLD_EDIT.getSession(player) != null)
        {
            WorldEditPlugin worldEdit = MinePlugin.WORLD_EDIT;
            LocalSession session = worldEdit.getSession(player);
            com.sk89q.worldedit.world.World weWorld = session.getSelectionWorld();
            try
            {
                Region selection = session.getSelection(weWorld);
                if (selection instanceof CuboidRegion)
                {
                    CuboidRegion cubeSelection = (CuboidRegion) selection;
                    Vector p1 = blockVector3toVector(cubeSelection.getPos1());
                    Vector p2 = blockVector3toVector(cubeSelection.getPos2());

                    //The world edit api has its own world class
                    World world = Bukkit.getWorld(cubeSelection.getWorld().getName());

                    pos1 = new Location(world, p1.getBlockX(), p1.getBlockY(), p1.getBlockZ());
                    pos2 = new Location(world, p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());
                }
            }
            catch (IncompleteRegionException e)
            {
                // no region has been selected
            }
        }
    }

    /**
     * gets the players first position of thier selction
     * @return
     */
    public Location getPos1()
    {
        return pos1;
    }

    /**
     * gets the players second postion of thier selection
     * @return
     */
    public Location getPos2()
    {
        return pos2;
    }

    /**
     * gets if the region is complete or not.
     * @return
     */
    public boolean hasValidRegion()
    {
        return pos1 != null && pos2 != null;
    }

    private Vector blockVector3toVector(BlockVector3 bv3)
    {
        return new Vector(bv3.getBlockX(), bv3.getBlockY(), bv3.getBlockZ());
    }
}
