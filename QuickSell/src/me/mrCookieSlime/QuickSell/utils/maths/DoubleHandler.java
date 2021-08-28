package me.mrCookieSlime.QuickSell.utils.maths;

import java.text.DecimalFormat;

public final class DoubleHandler
{
    private static final String[] NUMBER_SUFFIXES = {"","k"," mil"," bil"," tril"," quad"," quint"," sext"," sept"," oct"," non"," dec"," und"," dud"," tred"};
    private static final DecimalFormat FORMAT = new DecimalFormat("#,###.###############");

    private DoubleHandler() {}

    /**
     * formats a number into scientific notation
     *
     * @param number
     * @return a String in scientifific notation
     */
    public static String toScientificNotation(double number)
    {
        if(number < 0)
        {
            number = Math.abs(number);
            return "-" + toScientificNotation(number);
        }
        else if (number == 0)
        {
            return "0";
        }
        else
        {
            int exponent = (int) Math.floor(Math.log10(number));
            double mentessia = number / Math.pow(10,exponent);
            return round(mentessia,"2 DP") + "e" + exponent;
        }
    }

    public static String getFancyDouble(double number)
    {
        int exponent = (int) Math.log10(number);
        int powerOf1000 = exponent / 3;

        if(number < 0)
        {
            number = Math.abs(number);
            return "-" + getFancyDouble(number);
        }
        else if(number < 1000)
        {
            //we cast it to an int cos people will only want the intager portion cos so there wont be any
            //redundent .0's
            return ((int) number) + "";
        }
        else if (powerOf1000 < NUMBER_SUFFIXES.length)
        {
            number = number / Math.pow(10, powerOf1000 * 3);
            String roundedNumber = round(number, "3 SF");
            return roundedNumber + NUMBER_SUFFIXES[powerOf1000];
        }
        //if the number is so big that we dont have a suffix for it then we will display it in scienfic notation
        else
        {
            return toScientificNotation(number);
        }

    }

    public static double fixDouble(double amount, int digits) {
        if (digits == 0) return (int) amount;
        StringBuilder format = new StringBuilder("##");
        for (int i = 0; i < digits; i++) {
            if (i == 0) format.append(".");
            format.append("#");
        }
        return Double.valueOf(new DecimalFormat(format.toString()).format(amount).replace(",", "."));
    }

    /**
     * rounds a number with a rounding rule telling how it should be one
     *
     *  Syntax of Rounding Rule
     *  DP means decimal places and SF means sifnificant figures
     *  eg.
     *  2 DP means round the number to 2 decimal places
     *  5 SF means round the number t0 5 significant figures
     *
     * @param number
     * @param roundingRule
     * @return the rounded number
     */
    public static String round(double number, String roundingRule)
    {
        //if they provided an invalid rule we will default to 2 decimal places
        if(!roundingRule.split(" ")[1].equals("DP") && !roundingRule.split(" ")[1].equals("SF"))
        {
            roundingRule = "2 DP";
        }

        //gets how to round the number
        double factor = 0;
        if(roundingRule.split(" ")[1].equals("DP"))
        {
            factor = Math.pow(10,Double.parseDouble(roundingRule.split(" ")[0]));
        }
        if(roundingRule.split(" ")[1].equals("SF"))
        {
            factor = Math.floor(Math.log10(number)) - (Double.parseDouble(roundingRule.split(" ")[0]) - 1);
            factor = Math.pow(10,factor * -1);
        }

        //rounds the number
        number = Math.round(number * factor);
        number = number / factor;

        //removes the reduntent ".0" if there is one
        String output = FORMAT.format(number);

        return output;
    }

    public static double fixDouble(double amount) {
        return fixDouble(amount, 2);
    }

}
