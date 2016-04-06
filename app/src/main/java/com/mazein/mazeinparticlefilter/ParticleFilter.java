package com.mazein.mazeinparticlefilter;

import android.util.Log;

import java.util.Random;

/**
 * Created by Seif3 on 3/30/2016.
 */
public class ParticleFilter
{
    public Particle[] particles;

    public ParticleFilter(int particleCount, float MAX_X, float MAX_Y)
    {
        particles = new Particle[particleCount];
        for(int i=0; i<particleCount; i++)
        {
            particles[i] = new Particle(MAX_X, MAX_Y);
        }
    }

    public void resample()
    {
        Log.i("ParticleFilter", "Start Resampling");
        Random randomizer = new Random();
        double sumW = 0.0d;
        double[] weights = new double[particles.length];
        double[] cumSum = new double[particles.length];
        double[] T = new double[particles.length];

        int[] I = new int[particles.length];

        for(int i=0; i<particles.length; i++)
        {
            sumW += particles[i].weight;
            T[i] = randomizer.nextDouble();
        }

        for(int i=0;i<particles.length; i++)
        {
            particles[i].weight/=sumW;
            weights[i] = particles[i].weight;
        }

        cumSum[0] = weights[0];
        for(int i=1;i<particles.length; i++)
        {
            cumSum[i] = cumSum[i-1]+weights[i];
        }

        for(int i=0;i<particles.length; i++)
        {
            for(int j=0; j<particles.length-1; j++)
            {
                if(T[i] > cumSum[j] && T[i]<cumSum[j+1])
                {
                    I[i] = j+1;
                }
                else if(T[i] < cumSum[0])
                {
                    I[i] = 0;
                }
            }
        }

        for(int i=0;i<particles.length; i++)
        {
            particles[i].state.x = particles[I[i]].state.x;
            particles[i].state.y = particles[I[i]].state.y;
            particles[i].state.heading = particles[I[i]].state.heading;
            particles[i].weight = particles[I[i]].weight;
        }

    }


    public State getAvgLocation()
    {
        // Calculate Location based on weighted average of particles
        float x = 0;
        float y = 0;
        float heading = 0;

        for(Particle p : particles)
        {
            x += p.weight * p.state.x;
            y += p.weight * p.state.y;
            heading += p.weight * p.state.heading;
        }
        x/= particles.length;
        y/=particles.length;
        heading/=particles.length;
        // Reported Location
        return new State(x,y,heading);
    }

    public String visualize()
    {
        StringBuilder sb = new StringBuilder();
        for(Particle p : particles)
        {
            sb.append(p.toString());
        }
        return sb.toString();
    }
}
