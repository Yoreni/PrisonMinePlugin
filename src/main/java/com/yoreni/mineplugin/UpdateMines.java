/**
 *  This class is used to reset mines once when thier intervals come
 */

package com.yoreni.mineplugin;

import org.bukkit.scheduler.BukkitRunnable;

public class UpdateMines extends BukkitRunnable
{
    public UpdateMines()
    {

    }

    @Override
    public void run()
    {
        for(Mine mine : Mine.getMines())
        {
            if (mine.getTimeUntillNextReset() < 0)
            {
                mine.reset();
            }
        }
    }
}
