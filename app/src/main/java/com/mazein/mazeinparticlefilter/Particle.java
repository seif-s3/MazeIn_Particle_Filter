package com.mazein.mazeinparticlefilter;

import java.util.Random;

/**
 * Created by Seif3 on 3/30/2016.
 */
public class Particle
{
    State state;
    double weight;
    double stepLength =0.5d;
    final double N_DIMENSIONS = 3; // Dimensions of observation (measurements)
    static Random randomizer = new Random();

    private Particle previous; // Used in measurement model eq 9

    public Particle(float MAX_X, float MAX_Y)
    {
        // Initialize Random Particles
        this.state = new State();
        state.x = randomizer.nextGaussian() * MAX_X;
        state.y = randomizer.nextGaussian() * MAX_Y;
        state.heading = randomizer.nextDouble() % (180.0 / Math.PI);
        weight = 1;
    }

    public Particle(State s, double w)
    {
        this.state = s;
        this.weight = w;
    }

    public Particle(State s, double w, State prev, double prev_w)
    {
        this.state = s;
        this.weight = w;
    }

    public double noise(double var, double mean)
    {
        // Add guassian noise to particle
        return new Random().nextGaussian() * Math.sqrt(var) + mean;
    }

    public void motionModelUpdate(SensorData newVals, double mean, double var)
    {
        //heading = heading + (Change in heading from newVals) + noise()
        if(this.previous == null)
        {
            Random randomizer = new Random();
            this.state.heading = (this.state.heading +
                Math.abs(newVals.ori[0] - (randomizer.nextDouble() % (180/Math.PI)) )
                + noise(var, mean)) % (float)(180.0 / Math.PI);

            this.state.x = this.state.x + Math.cos(this.state.heading)
                    * (stepLength + noise(1.0d, 0.0d));

            this.state.y = this.state.y + Math.sin(this.state.heading)
                    * (stepLength + noise(1.0d, 0.0d));
            return;
        }

        this.state.heading = (this.state.heading +
                Math.abs(newVals.ori[0] - previous.state.heading)
                + noise(var, mean)) % (float)(180.0 / Math.PI);
        // Get Step Length from newVals
        // x = x + cos(newHeading) * (stepLen + noise())
        // y = y + sin(newHeading) * (stepLen + noise())
        this.state.x = this.state.x + Math.cos(this.state.heading)
                * (stepLength + noise(1.0d, 0.0d));

        this.state.y = this.state.y + Math.sin(this.state.heading)
                * (stepLength + noise(1.0d, 0.0d));
    }

    public void updateWeight(double[][] MEASUREMENT_CHANGE)
    {
        previous = this.clone();
        // Measurement Model update (eq 9 Maloc)
        // Database queries go here
        // Covariance???
        final double FIRST_TERM = (1/(Math.pow(2* Math.PI, (N_DIMENSIONS/2))
                * Math.sqrt(FingerprintStore.covMagnitude)));

        // MEASURMENT_CHANGE => z(t+1) - z(t)
        // OBS_CHANGE       =>  obv(s(t+1)) - obv(s(t))

        //double[][] MEASUREMENT_CHANGE = (current.minus(prev).toMatrix());
        double[][] OBS_CHANGE = (FingerprintStore.obv(this.state).minus(FingerprintStore.obv(previous.state))).toMatrix();
        // TODO: Implement transpose, inverse, matrix multiplication
        //double[][] inverseCov = MatrixOps.invert(FingerprintStore.covarianceMat);

        double[][] first_mult = MatrixOps.trasposeMatrix(MatrixOps.minus(MEASUREMENT_CHANGE, OBS_CHANGE));
//        Log.d("first_mult", first_mult.toString());

//        Log.d("sec_mult", FingerprintStore.sec_mult.toString());
        double[][] third_mult = MatrixOps.minus(MEASUREMENT_CHANGE, OBS_CHANGE);
//        Log.d("third_mult", third_mult.toString());
        double[][] EXP = MatrixOps.multiply(MatrixOps.multiply(first_mult, FingerprintStore.sec_mult)
                , third_mult);
//        Log.d("EXP", EXP.toString());

        this.weight = FIRST_TERM * Math.pow(Math.E, -0.5 * EXP[0][0]);
    }


    @Override
    protected Particle clone()
    {
        return new Particle(this.state, this.weight);
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.state.x) + ","
                + String.valueOf(this.state.y) + ","
                + String.valueOf(this.state.heading) + ","
                + String.valueOf(this.weight) + "\n";
    }
}
