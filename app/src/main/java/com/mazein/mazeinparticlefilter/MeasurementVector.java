package com.mazein.mazeinparticlefilter;

/**
 * Created by Seif3 on 4/3/2016.
 */
public class MeasurementVector
{
    private double[] readings;

    public MeasurementVector(double... params)
    {
        // TODO: Include Magnetic measurement in vector
        readings = new double[params.length];
        for (int i=0;i<params.length; i++)
        {
            readings[i] = params[i];
        }
    }

    public double[] get()
    {
        return readings;
    }

    public MeasurementVector minus(MeasurementVector param)
    {
        double[] ret = new double[readings.length];
        double[] that = param.get();
        for (int i = 0; i < readings.length; i++)
        {
            ret[i] = readings[i] - that[i];
        }
        return new MeasurementVector(ret);
    }

    public void set(double[] arr)
    {
        this.readings = arr.clone();
    }

    public double[][] toMatrix()
    {
        double[][] ret = new double[readings.length][1];
        for(int i=0;i<readings.length; i++)
        {
            ret[i][0] = readings[i];
        }
        return ret;
    }
}
