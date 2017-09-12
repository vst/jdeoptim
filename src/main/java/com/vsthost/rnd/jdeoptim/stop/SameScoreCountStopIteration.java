package com.vsthost.rnd.jdeoptim.stop;

public class SameScoreCountStopIteration implements StopIteration {

    private int sameScoreCount = 0;
    private long prevRoundedBestScore = 0;
    private final int maxSameScoreCount;
    private final long precisionMultiplier;

    public SameScoreCountStopIteration(final int maxSameScoreCount, final int precision) {
        this.maxSameScoreCount = maxSameScoreCount;
        this.precisionMultiplier = (long) Math.pow(10, precision);
    }

    @Override
    public boolean stopIteration(final double bestScore) {
    	//positive infinity can be an invalid result, we don't want to count these
        if (bestScore < Double.POSITIVE_INFINITY) {
            final long roundedBestScore = (long) (bestScore * precisionMultiplier);
            if (roundedBestScore == prevRoundedBestScore) {
                sameScoreCount++;
                if (sameScoreCount > maxSameScoreCount) {
                    //we were not able to improve for a while, thus we can stop now
                    return true;
                }
            } else {
                prevRoundedBestScore = roundedBestScore;
                sameScoreCount = 0;
            }
        }
        return false;
    }

}
