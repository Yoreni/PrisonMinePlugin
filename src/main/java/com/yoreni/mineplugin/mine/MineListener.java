package com.yoreni.mineplugin.mine;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class MineListener implements Listener
{

    @EventHandler
    public void blockBreak(BlockBreakEvent event)
    {
        countBlock(event.getBlock().getLocation(), 1);
    }

    @EventHandler
    public void blockBreak(BlockExplodeEvent event)
    {
        for(Block block : event.blockList())
        {
            countBlock(block.getLocation() , 1);
        }
    }

    @EventHandler
    public void blockBreak(EntityExplodeEvent event)
    {
        countBlock(event.getLocation(), 1);

        for(Block block : event.blockList())
        {
            countBlock(block.getLocation(), 1);
        }
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event)
    {
        countBlock(event.getBlock().getLocation(), -1);
    }

    private void countBlock(Location location, int amount)
    {
        for(Mine mine : Mine.getMines())
        {
            if(mine.getShape().isInside(location))
            {
                mine.changeBlocksBroken(amount);
                break;
            }
        }
    }
}
