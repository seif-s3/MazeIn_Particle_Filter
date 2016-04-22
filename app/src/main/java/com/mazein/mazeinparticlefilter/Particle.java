package com.mazein.mazeinparticlefilter;

import android.util.Log;

import java.util.Random;

/**
 * Created by Seif3 on 3/30/2016.
 */
public class Particle
{
    static Random randomizer = new Random();
    final double N_DIMENSIONS = 3; // Dimensions of observation (measurements)
    State state;
    double weight;
    double stepLength = 0.75d;
    double MAX_X, MAX_Y;
    private State previousState; // Used in measurement model eq 9

    public Particle(double mx, double my, double currentHeading)
    {
        this.MAX_X = mx;
        this.MAX_Y = my;
        // Initialize Random Particles
        this.state = new State();
        state.x = randomizer.nextDouble() * this.MAX_X;
        state.y = randomizer.nextDouble() * this.MAX_Y;
        state.heading = currentHeading; // in degrees
        this.previousState = new State();
        weight = 1;
    }

    public Particle(State s, double w)
    {
        this.state = s;
        this.weight = w;
    }

    public double noise(double var, double mean)
    {
        // Add guassian noise to particle
        return new Random().nextGaussian() * Math.sqrt(var) + mean;
    }

    public void motionModelUpdate(double deltaHeading, double mean, double var)
    {
        //heading = heading + (Change in heading from newVals) + noise()
        if (this.previousState == null)  //initial state
        {
            Random randomizer = new Random();
            State previousState = new State();
            previousState.heading = randomizer.nextDouble() * 360;
            previousState.x = this.state.x;
            previousState.y = this.state.y;
        }
        else
        {
            previousState.heading = this.state.heading;
            previousState.x = this.state.x;
            previousState.y = this.state.y;
        }
        // TODO TODO TODO
        this.state.heading = (this.state.heading +  //new heading in degrees
                deltaHeading
                + noise(var, mean) * 0) % (180); //changed to 180
        // Get Step Length from newVals
        // x = x + cos(newHeading) * (stepLen + noise())
        // y = y + sin(newHeading) * (stepLen + noise())
        this.state.x = this.state.x + Math.cos(Math.toRadians(this.state.heading))
                * (stepLength + noise(1.0d, 0.0d));  //TODO check noise value

        this.state.y = this.state.y + Math.sin(Math.toRadians(this.state.heading))
                * (stepLength + noise(1.0d, 0.0d));
    }

    public void updateWeight2(double[][] MEASUREMENT_CHANGE)
    {
        //difference between this state and previous one observations
        MeasurementVector particleMeasureUpdate = FingerprintStore.obv(this.state).minus(FingerprintStore.obv(previousState));
        //difference between the difference in observation and difference in measurements which indicates the error
        double[][] errorVector = MatrixOps.minus(particleMeasureUpdate.toMatrix(), MEASUREMENT_CHANGE);
        //Exponential of the error indicates the weight

        this.weight = Math.pow(Math.E, -MatrixOps.vectorNorm(errorVector));

        return;
    }

    public void updateWeight3(double[][] CURRENT_MEASUREMENT)
    {
        // Updates weights without checking previous state.
        double[][] errorVector = MatrixOps.minus(CURRENT_MEASUREMENT,
                FingerprintStore.obv(this.state).toMatrix());

        if (this.state.x > MAX_X || this.state.x < 0 || this.state.y > MAX_Y || this.state.y < 0)
        {
            this.weight = 0;
        }
        else
        {
            this.weight = Math.pow(Math.E, -MatrixOps.vectorNorm(errorVector));
        }

        return;
    }



    public void updateWeight(double[][] MEASUREMENT_CHANGE)
    {
        // Measurement Model update (eq 9 Maloc)
        // Database queries go here
        // Covariance???
        final double FIRST_TERM = (1/(Math.pow(2* Math.PI, (N_DIMENSIONS/2))
                * Math.sqrt(FingerprintStore.covMagnitude)));

        // MEASURMENT_CHANGE => z(t+1) - z(t)
        // OBS_CHANGE       =>  obv(s(t+1)) - obv(s(t))

        //double[][] MEASUREMENT_CHANGE = (current.minus(prev).toMatrix());
        double[][] OBS_CHANGE = (FingerprintStore.obv(this.state).minus(FingerprintStore.obv(previousState))).toMatrix();

        //double[][] inverseCov = MatrixOps.invert(FingerprintStore.covarianceMat);

        double[][] first_mult = MatrixOps.trasposeMatrix(MatrixOps.minus(MEASUREMENT_CHANGE, OBS_CHANGE));
//        Log.d("first_mult", first_mult.toString());

//        Log.d("invCov", FingerprintStore.invCov.toString());
        // third_mult is the error value (vector of 3 elements)

        double[][] third_mult = MatrixOps.minus(MEASUREMENT_CHANGE, OBS_CHANGE);
//        double error_mag =    Math.sqrt(third_mult[0][0] * third_mult[0][0]
//                            + third_mult[1][0] * third_mult[1][0]
//                            + third_mult[2][0] * third_mult[2][0]);
//
//        this.weight = Math.pow(Math.E, -16.0 * error_mag);

//        Log.d("third_mult", third_mult.toString());
        double[][] EXP = MatrixOps.multiply(
                MatrixOps.multiply(first_mult, FingerprintStore.invCov)
                , third_mult);
        Log.d("EXP", String.valueOf(EXP[0][0]));

        this.weight = FIRST_TERM * Math.pow(Math.E, -0.5 * EXP[0][0]);


        return;
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
