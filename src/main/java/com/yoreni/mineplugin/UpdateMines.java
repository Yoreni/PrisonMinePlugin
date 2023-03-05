/**
 *  This class is used to reset mines once when thier intervals come
 */

package com.yoreni.mineplugin;

import com.yoreni.mineplugin.mine.Mine;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateMines extends BukkitRunnable
{
    @Override
    public void run()
    {
        for(Mine mine : Mine.getMines())
        {
            if (mine.isResetDue())
            {
                mine.reset();
            }
        }
    }
}
