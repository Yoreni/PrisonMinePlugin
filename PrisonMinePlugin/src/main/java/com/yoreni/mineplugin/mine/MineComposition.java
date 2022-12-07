package com.yoreni.mineplugin.mine;

import com.yoreni.mineplugin.util.Yml;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MineComposition
{
    /**
     * this is how the Block compostion wil be stored.
     * The Material is the block that it in
     * The double is the chance the block will appear in the nextBlock() function
     * the doube ranges from 0<x<=1
     */
    private Map<Material, Double> compostion;

    public MineComposition()
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
    public static MineComposition readFromYaml(Yml file, String path)
    {
        //file.setPathPrefix(path);
        MineComposition mineComostion = new MineComposition();

        @NotNull Map<String, Object> map = file.getValues(path);
        for(String key : map.keySet())
        {
            Material block = Material.getMaterial(key);
            if(block != null)
            {
                double chance = Double.parseDouble(map.get(key).toString());
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
    public void writeToYaml(Yml file, String path)
    {
        //file.setPathPrefix(path);

        if(file.isSet(path))
        {
            //this removes the blocks in the mine of the config file that isnt in the compistion list
            //this is to stop the mines messing up when a block has been removed
            @NotNull Map<String, Object> existingBlocksInFile = file.getValues(path);
            for (String key : existingBlocksInFile.keySet()) {
                Material block = Material.getMaterial(key);
                if (block != null) {
                    if (!hasBlock(block)) {
                        file.set(block.toString(), 0D);
                    }
                }
            }
        }

        //add new blocks or changing them if there are any
        for(Material block : compostion.keySet())
        {
            double chance = compostion.get(block);
            file.set(path + "." + block.toString(), chance);
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