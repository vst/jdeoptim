package com.vsthost.rnd.jdeoptim.stop;

/**
 * This can be used to stop iteration earlier.
 * 
 * @author subes
 *
 */
public interface StopIteration {
    
    boolean stopIteration(double bestScore);

}
