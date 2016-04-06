package com.mazein.mazeinparticlefilter;

/**
 * Created by Seif3 on 3/30/2016.
 */
public class State
{
    public double x;
    public double y;
    public double heading;
    public State(double x, double y, double heading)
    {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }
    public State()
    {

    }
    public State(double x, double y)
    {
        this.x = x;
        this.y = y;
        this.heading = 0.0d;
    }

    public double getEuclideanDistance(State s)
    {
        return Math.sqrt(
                (this.x - s.x)*(this.x - s.x)
            + (this.y - s.y)*(this.y - s.y)
        );
    }
}
