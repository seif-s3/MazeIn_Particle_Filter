package com.mazein.mazeinparticlefilter;

import java.util.HashMap;

/**
 * Created by Seif3 on 4/5/2016.
 */
public class AccessPointMacs
{
    // Hofburg WiFI Macs
    public static String AP1_MAC = "68:86:a7:31:c5:5a";
    public static String AP2_MAC = "00:1c:0e:d7:03:1a";
    public static String AP3_MAC = "68:86:a7:31:c5:55";
    public static String AP4_MAC = "00:1c:0e:d7:03:15"; // Location Competition

    public static HashMap<String, Integer> keys = new HashMap<>();
    public static HashMap<Integer, String> vals = new HashMap<>();
    public static double[] normalization = new double[4];

    static
    {
        keys.put(AP1_MAC, 0);
        keys.put(AP2_MAC, 1);
        keys.put(AP3_MAC, 2);
        keys.put(AP4_MAC, 3);

        vals.put(0, AP1_MAC);
        vals.put(1, AP2_MAC);
        vals.put(2, AP3_MAC);
        vals.put(3, AP4_MAC);

        normalization[0] = -37.0;
        normalization[1] = -52.0;
        normalization[2] = -38.0;
        normalization[3] = -51.0;
    }

}
