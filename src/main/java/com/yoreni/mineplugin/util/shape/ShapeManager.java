package com.yoreni.mineplugin.util.shape;

import com.yoreni.mineplugin.util.Yml;

import java.util.ArrayList;
import java.util.List;

public class ShapeManager
{
    private static final List<Class<? extends Shape>> shapes = new ArrayList<>();

    private ShapeManager()
    {

    }

    public static void registerShape(Class<? extends Shape> shape)
    {
        if (shape.getSuperclass() != Shape.class)
            return;

        if (shapes.contains(shape))
            return;

        shapes.add(shape);
    }

    /**
     * reads the shape data from a yml file and makes a shape object out of it.
     *
     * @param file
     * @param path
     * @return
     */
    public static Shape readFromYaml(Yml file, String path)
    {
        try
        {
            Class<? extends Shape> shapeClass = getShapeClass((String) file.get(path + ".shape"));
            Object shape = shapeClass.getMethod("readFromYaml", Yml.class, String.class)
                    .invoke(null, file, path);
            return (Shape) shape;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * @return a list of the names of all shapes registered
     */
    public static List<String> getShapeNames()
    {
        try
        {
            List<String> out = new ArrayList<>();

            for (Class<? extends Shape> clas : shapes)
            {
                out.add(getShapeName(clas));
            }

            return out;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static Class<? extends Shape> getShapeClass(String shapeName)
    {
        try
        {
            for (Class<? extends Shape> clas : shapes)
            {
                if (getShapeName(clas).equals(shapeName))
                {
                    return clas;
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

    private static String getShapeName(Class<? extends Shape> shape)
    {
        try
        {
            return (String) shape.getMethod("getSName").invoke(null);
        }
        catch (Exception exception)
        {
            return "could not get shape name";
        }
    }
}
