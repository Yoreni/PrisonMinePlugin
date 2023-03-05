package com.yoreni.mineplugin.util;

import com.yoreni.mineplugin.MinePlugin;
import net.md_5.bungee.chat.TranslationRegistry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util
{
    private static final boolean DEBUG = true;
    private static List<String> listOfBlocksNames = new ArrayList<>();

    public static void debug(String string)
    {
        if (DEBUG)
        {
            Bukkit.getLogger().info(string);
        }
    }

    public static List<String> getListOfBlocks()
    {
        if(listOfBlocksNames.size() == 0)
        {
            initListOfBlocks();
        }

        return listOfBlocksNames;
    }

    /**
     * takes a number and shows it as a percent
     *
     * @param number the number. if 0.2 is put in it will show 20% and if 1 is put in it will show 100% for example
     * @param dp the number of decimal places to round it to
     * @return
     */
    public static String doubleToPercent(double number,int dp)
    {
        //this stops the wired output of "-0%" when you put a negative number thats close to 0
        if(Math.abs(number) < 1e-14)
        {
            number = 0;
        }

        number *= 100;

        // rounding the number
        number *= Math.pow(10, dp);
        number = Math.round(number);
        number /= Math.pow(10, dp);
        String percent = toCommas(number);

        //some langauges write percentages in differnt ways this is so it acounts for that
        percent = MessageHandler.getInstance().get("number.percent-format",
                new Placeholder("number", percent));

        return percent;
    }

    /**
     * converts a material to a human readable (english) name
     *
     * @param material the material you want to convert
     * @return the human frendly name
     */
    public static String materialToEnglish(Material material)
    {
        return TranslationRegistry.INSTANCE.translate(material.name());
    }

    public static String formatTime(long milliseconds)
    {
        int s = (int) (milliseconds / 1_000) % 60;
        int m = (int) (milliseconds / 60_000) % 60;
        int h = (int) (milliseconds / 3_600_000) % 24;
        int d = (int) (milliseconds / 86_400_000);

        Placeholder[] placeholders = {
                new Placeholder("%s%", s + ""),
                new Placeholder("%ss%", StringUtils.leftPad(s + "", 2, '0')),
                new Placeholder("%m%", m + ""),
                new Placeholder("%mm%", StringUtils.leftPad(m + "", 2,'0')),
                new Placeholder("%h%", h + ""),
                new Placeholder("%hh%", StringUtils.leftPad(h + "", 2, '0')),
                new Placeholder("%d%", d + ""),
                new Placeholder("%dd%", StringUtils.leftPad(d + "", 2, '0'))
        };

        if(milliseconds < 60_000)
        {
            return MessageHandler.getInstance().get("number.time.seconds",
                    Arrays.copyOfRange(placeholders, 0, 2)
            );
        }
        else if(milliseconds < 3_600_000)
        {
            return MessageHandler.getInstance().get("number.time.minutes",
                    Arrays.copyOfRange(placeholders, 0, 4)
            );
        }
        else if(milliseconds < 86_400_000)
        {
            return MessageHandler.getInstance().get("number.time.hours",
                    Arrays.copyOfRange(placeholders, 0, 6)
            );
        }
        else
        {
            return MessageHandler.getInstance().get("number.time.days",
                    placeholders
            );
        }
    }

    /**
     * sperates a number with commas to make it more readible eg. 23432345 = 23,432,345
     *
     * @param number
     * @return a String
     */
    public static String toCommas(double number)
    {
        return toCommas(number, "#,###.###############");
    }

    public static String toCommas(double number, String format)
    {
        String commaNumber = new DecimalFormat(format).format(number);

        /*
            other languages use different symbols for decimal points and thousand separators
            so tis accounts for this
         */
        final String thousandsSeperator = MessageHandler.getInstance().get("number.thousand-separator");
        final String decimalPoint = MessageHandler.getInstance().get("number.decimal-point");

        commaNumber = commaNumber.replace(",", "a").replace(".", "b");
        commaNumber = commaNumber.replace("a", thousandsSeperator).replace("b", decimalPoint);

        return commaNumber;
    }

    private static void initListOfBlocks()
    {
        if(listOfBlocksNames.size() == 0)
        {
            //getting all the materials in the game and only getting the blocks
            Material[] blocks = Arrays.stream(Material.values())
                    .filter((material) ->
                            (material.isBlock() && material.isSolid())
                     || material == Material.WATER || material == Material.LAVA)
                    .toArray(Material[]::new);

            //turning all the block materials into strings
            String[] blockNames = Arrays.stream(blocks)
                    //.map((block) -> block.getKey().asString().split(":")[1]) paper
                    .map((block) -> block.getKey().getKey())
                    .toArray(String[]::new);

            listOfBlocksNames = Arrays.asList(blockNames);
        }
    }
}
