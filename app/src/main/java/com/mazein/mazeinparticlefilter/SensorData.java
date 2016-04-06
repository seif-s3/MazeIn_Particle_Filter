package com.mazein.mazeinparticlefilter;

/**
 * Created by Seif3 on 3/30/2016.
 */
public class SensorData
{
        /**
         * Accelerometer sensor data
         */
        public float[] acc = new float[3];

        /**
         * Magnetic sensor data
         */
        public float[] mag = new float[3];

        /**
         * Gyro sensor data
         */
        public float[] gyr = new float[3];

        /**
         * Orientation sensor data
         */
        public float[] ori = new float[3];

        /**
         * Constructor
         */
        public SensorData() {
            ori[0] = 0;
            ori[1] = 0;
            ori[2] = 0;
            gyr[0] = 0;
            gyr[1] = 0;
            gyr[2] = 0;
            acc[0] = 0;
            acc[1] = 0;
            acc[2] = 0;
            mag[0] = 0;
            mag[1] = 0;
            mag[2] = 0;
        }

        /**
         * Clone this data set
         *
         * @return a new object with the same user data
         */
        public SensorData clone() {
            SensorData data = new SensorData();
            data.ori[0] = ori[0];
            data.ori[1] = ori[1];
            data.ori[2] = ori[2];
            data.gyr[0] = gyr[0];
            data.gyr[1] = gyr[1];
            data.gyr[2] = gyr[2];
            data.acc[0] = acc[0];
            data.acc[1] = acc[1];
            data.acc[2] = acc[2];
            data.mag[0] = mag[0];
            data.mag[1] = mag[1];
            data.mag[2] = mag[2];
            return data;
        }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Accelerometer:\n");
        sb.append("[" + acc[0] + ", " + acc[1] + ", " + acc[2] + "]\n" );
        sb.append("Gyro:\n");
        sb.append("[" + gyr[0] + ", " + gyr[1] + ", " + gyr[2] + "]\n" );
        sb.append("Magnetometer:\n");
        sb.append("[" + mag[0] + ", " + mag[1] + ", " + mag[2] + "]\n" );
        sb.append("Orientation:\n");
        sb.append("[" + ori[0] + ", " + ori[1] + ", " + ori[2] + "]\n" );

        return  sb.toString();

    }
}
