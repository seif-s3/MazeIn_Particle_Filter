package com.mazein.mazeinparticlefilter;

import android.content.Context;
import android.util.Log;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Seif3 on 4/3/2016.
 */
public class FingerprintStore
{
    public static double[][] covarianceMat = new double[3][3];
    public static double covMagnitude;
    public static double[][] invCov;
    private static HashMap<State, MeasurementVector> fingerprints;

    public static void loadFingerprints_4Wifi(Context ctx)
    {
        Log.i("FingerprintsStore", "Start Loading Fingerprints");
        CSVReader reader = null;
        HashMap<State, MeasurementVector> tempMap = new HashMap<>();
        try
        {
            reader = new CSVReader(
                    new InputStreamReader(ctx.getAssets().open("fingerprints.csv"))
            );
            String [] v;
            double R1_mean = 0.0d;
            double R2_mean = 0.0d;
            double R3_mean = 0.0d;
            double R4_mean = 0.0d;
            int count = 0;

            while ((v = reader.readNext()) != null) {
                // v[] is an array of values from the line
                double R1       = Double.parseDouble(v[0]);
                double R2       = Double.parseDouble(v[1]);
                double R3       = Double.parseDouble(v[2]);
                double R4 = Double.parseDouble(v[3]);
                double xcoord = Double.parseDouble(v[4]);
                double ycoord = Double.parseDouble(v[5]);

                R1_mean += R1;
                R2_mean += R2;
                R3_mean += R3;
                R4_mean += R4;
                count++;
                tempMap.put(new State(xcoord, ycoord),
                        new MeasurementVector(R1, R2, R3, R4));
            }
            R1_mean/=count;
            R2_mean/=count;
            R3_mean/=count;
            R4_mean /= count;


//            covarianceMat[0][0]=0.0d;
//            covarianceMat[1][1]=0.0d;
//            covarianceMat[2][2]=0.0d;
//            for(State k: tempMap.keySet())
//            {
//                covarianceMat[0][0] += Math.pow(R1_mean - tempMap.get(k).get()[0], 2);
//                covarianceMat[1][1] += Math.pow(R2_mean - tempMap.get(k).get()[1], 2);
//                covarianceMat[2][2] += Math.pow(R3_mean - tempMap.get(k).get()[2], 2);
//            }
//            covarianceMat[0][0]/=count;
//            covarianceMat[1][1]/=count;
//            covarianceMat[2][2]/=count;
            covarianceMat = new double[][]{
                    {15.20634921, -1.746031746, -6.952380952, -2.927381886},
                    {-1.746031746, 33.07029478, -8.969765684, -35.53978525},
                    {-8.969765684, -35.53978525, 156.3013353, -1.746031746},
                    {-1.746031746, -6.952380952, -2.927381886, 123.8167907}
            }; // Meininger
//                    {36.35288141,20.50065322,-19.63390913},
//                            {20.50065322,40.6142038,-17.32443025},
//                            {-19.63390913,-17.32443025,33.3033822}
//            };
//          calcCovariance();
            covMagnitude = getCovMagnitude();
            Log.d("Covariance Magnitude",Double.toString(covMagnitude));
            invCov = MatrixOps.invert(FingerprintStore.covarianceMat);
            Log.d("FP Count", "Loaded " + count + "Fingerprints");

        } catch (FileNotFoundException e)
        {
            Log.e("FingerprintStore", "Fingerprint File not found");
            e.printStackTrace();
        } catch (IOException e)
        {
            Log.e("FingerprintStore", "IOException");
            e.printStackTrace();
        }

        Log.i("FingerprintsStore","Fingerprints Loaded..");
        fingerprints = (HashMap<State, MeasurementVector>) tempMap.clone();
    }

    public static void loadFingerprints_3Wifi_mag(Context ctx)
    {
        Log.i("FingerprintsStore", "Start Loading Fingerprints");
        CSVReader reader = null;
        HashMap<State, MeasurementVector> tempMap = new HashMap<>();
        try
        {
            reader = new CSVReader(
                    new InputStreamReader(ctx.getAssets().open("fingerprints.csv"))
            );
            String[] v;
            double R1_mean = 0.0d;
            double R2_mean = 0.0d;
            double R3_mean = 0.0d;
            double mag_mean = 0.0d;
            int count = 0;

            while ((v = reader.readNext()) != null)
            {
                // v[] is an array of values from the line
                double R1 = Double.parseDouble(v[0]);
                double R2 = Double.parseDouble(v[1]);
                double R3 = Double.parseDouble(v[2]);
                double mag = Double.parseDouble(v[3]);
                double xcoord = Double.parseDouble(v[4]);
                double ycoord = Double.parseDouble(v[5]);

                R1_mean += R1;
                R2_mean += R2;
                R3_mean += R3;
                mag_mean += mag;
                count++;
                tempMap.put(new State(xcoord, ycoord),
                        new MeasurementVector(R1, R2, R3, mag));
            }
            R1_mean /= count;
            R2_mean /= count;
            R3_mean /= count;
            mag_mean /= count;

//            covarianceMat[0][0]=0.0d;
//            covarianceMat[1][1]=0.0d;
//            covarianceMat[2][2]=0.0d;
//            for(State k: tempMap.keySet())
//            {
//                covarianceMat[0][0] += Math.pow(R1_mean - tempMap.get(k).get()[0], 2);
//                covarianceMat[1][1] += Math.pow(R2_mean - tempMap.get(k).get()[1], 2);
//                covarianceMat[2][2] += Math.pow(R3_mean - tempMap.get(k).get()[2], 2);
//            }
//            covarianceMat[0][0]/=count;
//            covarianceMat[1][1]/=count;
//            covarianceMat[2][2]/=count;
            covarianceMat = new double[][]{

                    {15.20634921, -1.746031746, -6.952380952, -2.927381886},
                    {-1.746031746, 33.07029478, -8.969765684, -35.53978525},
                    {-8.969765684, -35.53978525, 156.3013353, -1.746031746},
                    {-1.746031746, -6.952380952, -2.927381886, 123.8167907}
            }; // Meininger
//                    {36.35288141,20.50065322,-19.63390913},
//                            {20.50065322,40.6142038,-17.32443025},
//                            {-19.63390913,-17.32443025,33.3033822}
//            };
//          calcCovariance();
            covMagnitude = getCovMagnitude();
            Log.d("Covariance Magnitude", Double.toString(covMagnitude));
            invCov = MatrixOps.invert(FingerprintStore.covarianceMat);

        } catch (FileNotFoundException e)
        {
            Log.e("FingerprintStore", "Fingerprint File not found");
            e.printStackTrace();
        } catch (IOException e)
        {
            Log.e("FingerprintStore", "IOException");
            e.printStackTrace();
        }

        Log.i("FingerprintsStore", "Fingerprints Loaded..");
        fingerprints = (HashMap<State, MeasurementVector>) tempMap.clone();
    }

    public static MeasurementVector obv(State s)
    {
        double minDist = Double.MAX_VALUE;
        double tempDist;
        MeasurementVector ret = null;

        for(State k : fingerprints.keySet())
        {
            tempDist = k.getEuclideanDistance(s);
            if(tempDist < minDist)
            {
                minDist = tempDist;
                ret = fingerprints.get(k);
            }
        }
        return ret;
    }

    private static void calcCovariance()
    {
        for(int i=0; i<3; i++)
        {
            for(int j=0; j<3 ; j++)
            {
                if(i!=j)
                {
                    covarianceMat[i][j] = Math.sqrt(covarianceMat[i][i]) * Math.sqrt(covarianceMat[j][j]);
                }
            }
        }
        Log.i("FingerprintsStore","Covariance Matrix Calculated..");
    }

    private static double getCovMagnitude() {
        // TODO: Modify when adding magnetic to generic form of determinant
        double determinant = 0.0d;											 //value to be returned
        for (int i = 0; i < 4; i++)
        {
            int mult = (i % 2 == 0) ? 1 : -1; 											 //(We want to alternate between adding / subtracting the subdeterminants)
            determinant += (mult * covarianceMat[0][i] * determinant(subMatrix(0, i))); //Add/subtract the determinant of the submatrix.
        }
        return determinant;
    }

    private static double determinant2(double[][] mat)
    {
        return (mat[0][0] * mat[1][1]) - (mat[0][1] * mat[1][0]);
    }

    public static double determinant(double[][] result)
    {
        int rows = result.length;
        int cols = result[0].length;
        if (rows == 2)
        {
            return result[0][0] * result[1][1] - result[0][1] * result[1][0];
        }

        int determinant1 = 0, determinant2 = 0;
        for (int i = 0; i < rows; i++)
        {
            int temp = 1, temp2 = 1;
            for (int j = 0; j < cols; j++)
            {
                temp *= result[(i + j) % cols][j];
                temp2 *= result[(i + j) % cols][rows - 1 - j];
            }

            determinant1 += temp;
            determinant2 += temp2;
        }

        return determinant1 - determinant2;
    }

    private static double[][] subMatrix(int row, int col)
    {
        double[][] result = new double[3][3];    //Object to store result in.
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                //If we are not on the excluded row or column, add result to the submatrix
                if(row != i && col != j)
                    result[i < row ? i : i-1][j < col ? j : j-1] = covarianceMat[i][j];
        return result;
    }
}
