package com.vsthost.rnd.jdeoptim.stop;

import java.util.ArrayList;
import java.util.List;

public abstract class SteadyResultAvgOptimizationLoop {

    private final long sameResultPrecisionMultiplier = (long) Math.pow(10, newSameResultPrecision());
    private final long sameResultTolerance = newSameResultTolerance();
    private final int sameResultCount = newSameResultCount();
    private final int maxTries = newMaxTries();

    protected int newSameResultCount() {
        return 2;
    }

    protected int newSameResultPrecision() {
        return 3;
    }

    protected int newSameResultTolerance() {
        return 10;
    }

    private int newMaxTries() {
        return 10;
    }

    public double[] loop() {
        final List<double[]> validResults = new ArrayList<double[]>();
        final List<double[]> sameResults = new ArrayList<double[]>();
        double[] prevResult = null;
        int tries = 0;
        while (true) {
            final double[] result = optimize();
            if (isValidResult(result)) {
                validResults.add(result);
            } else {
                continue;
            }
            if (prevResult == null) {
                sameResults.add(result);
            } else if (isSameResult(prevResult, result)) {
                sameResults.add(result);
                if (sameResults.size() >= sameResultCount) {
                    final double[] avgResult = avgResult(sameResults);
                    return avgResult;
                }
            } else {
                tries++;
                if (tries >= maxTries) {
                    return newTriesExceededResult(validResults);
                }
                sameResults.clear();
            }
            prevResult = result;
        }
    }

    protected abstract double[] newTriesExceededResult(List<double[]> validResults);

    protected abstract double[] optimize();

    protected double[] avgResult(final List<double[]> results) {
        final double[] avgs = new double[results.get(0).length];
        for (final double[] result : results) {
            for (int i = 0; i < result.length; i++) {
                avgs[i] += result[i];
            }
        }
        final int sameResultsCount = results.size();
        for (int i = 0; i < avgs.length; i++) {
            avgs[i] = avgs[i] / sameResultsCount;
        }
        return avgs;
    }

    protected double[] minResult(final List<double[]> results) {
        final double minSum = Double.MAX_VALUE;
        double[] minResult = null;
        for (final double[] result : results) {
            double sum = 0;
            for (int i = 0; i < result.length; i++) {
                sum += result[i];
            }
            if (sum < minSum) {
                minResult = result;
            }
        }
        return minResult;
    }

    private boolean isSameResult(final double[] prevResult, final double[] result) {
        if (prevResult == null) {
            return false;
        }
        if(prevResult.length != result.length) {
        	throw new IllegalArgumentException("prevResult.length ["+prevResult.length+"] should be equal to result.length ["+result.length+"]");
        }
        for (int i = 0; i < prevResult.length; i++) {
            final long prevF = (long) (prevResult[i] * sameResultPrecisionMultiplier);
            final long f = (long) (result[i] * sameResultPrecisionMultiplier);
            if (Math.abs(prevF - f) > sameResultTolerance) {
                return false;
            }
        }
        return true;
    }

    protected boolean isValidResult(final double[] result) {
        for (int i = 0; i < result.length; i++) {
            if (result[i] != 0D) {
                return true;
            }
        }
        return false;
    }

}
