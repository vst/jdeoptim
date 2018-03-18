/*
 * Copyright 2015 Vehbi Sinan Tunalioglu <vst@vsthost.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vsthost.rnd.jdeoptim.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import com.vsthost.rnd.jdeoptim.evolution.Problem;

/**
 * Provides some utility functions used throughout the library.
 */
public class Utils {
	/**
	 * Creates a random population.
	 *
	 * @param size
	 *            The size of the population to be created.
	 * @param dimension
	 *            The dimension of the candidate (length of each candidate) to
	 *            be created.
	 * @param lower
	 *            The lower boundary.
	 * @param upper
	 *            The upper boundary.
	 * @param distribution
	 *            The distribution to be sampled from.
	 * @return A random population.
	 */
	public static double[][] randomPopulation(int size, int dimension, double[] lower, double[] upper,
			RealDistribution distribution) {
		// Create the population matrix:
		final double[][] population = new double[size][dimension];

		// Iterate over population and populate each candidate:
		for (int c = 0; c < size; c++) {
			// Iterate over dimensions and populate candidate elements:
			for (int d = 0; d < dimension; d++) {
				// Get the minimum possible value:
				final double min = lower[d];

				// Get the maximum possible value:
				final double max = upper[d];

				// Create a random value within the range:
				population[c][d] = min + ((max - min) * distribution.sample());
			}
		}

		// Done, return the population:
		return population;
	}

	/**
	 * Returns the order of the values, ie. ordered indices sorted by comparing
	 * the elements of the array.
	 *
	 * @param values
	 *            The vector of which the indices will be ordered.
	 * @return The indices of the vector ordered according to sorted elements.
	 */
	public static int[] order(final double[] values) {
		// Initialize the return vector:
		Integer[] vector = Utils.toObject(Utils.sequence(values.length));

		// Order:
		Arrays.sort(vector, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Double.compare(values[o1], values[o2]);
			}
		});

		// Done, return indices:
		return Utils.toPrimitive(vector);
	}

	/**
	 * Creates an Integer sequence starting with 0 and sized given length.
	 *
	 * @param length
	 *            The length of the sequence.
	 * @return A sequence starting with 0 and ending with
	 *         <code>length - 1</code>.
	 */
	public static int[] sequence(int length) {
		return IntStream.range(0, length).toArray();
	}

	/**
	 * Converts an int array into an Integer array.
	 *
	 * @param values
	 *            Primitive int array.
	 * @return Integer array.
	 */
	public static Integer[] toObject(int[] values) {
		// Initialize the return vector:
		Integer[] vector = new Integer[values.length];

		// Set values:
		for (int i = 0; i < vector.length; i++) {
			vector[i] = i;
		}

		// Done, return:
		return vector;
	}

	/**
	 * Converts an Integer array into a primitive int array.
	 *
	 * @param values
	 *            Integer array.
	 * @return Primitive int array.
	 */
	public static int[] toPrimitive(Integer[] values) {
		// Initialize the return vector:
		int[] vector = new int[values.length];

		// Set values:
		for (int i = 0; i < vector.length; i++) {
			vector[i] = i;
		}

		// Done, return:
		return vector;
	}

	/**
	 * Picks two random members very fast.
	 * 
	 * @param randomMembers an array of length two to store the random members
	 * @param populationSize what is member population
	 * @param exclude what member should be excluded
	 * @param randomGenerator
	 */
	public static void fastPickTwoRandomMembers(final int[] randomMembers, final int populationSize, final int exclude,
			RandomGenerator randomGenerator) {
		/*
		 * since we only pick two random members anyway, we can just remember
		 * the previous value for faster comparison
		 */
		int prevRandomMember = Integer.MIN_VALUE;
		for (int i = 0; i < 2; i++) {
			final int randomMember = randomGenerator.nextInt(populationSize);
			if (randomMember != exclude && randomMember != prevRandomMember) {
				randomMembers[i] = randomMember;
				prevRandomMember = randomMember;
			} else {
				i--;
			}
		}
	}

	/**
	 * Applies the limits to the new value.
	 * 
	 * @param problem
	 *            the problem with the limits
	 * @param i
	 *            the index of the new value
	 * @param newValue
	 *            to apply limits to
	 * @return the limited value
	 */
	public static double applyLimits(final Problem problem, final int i, final double newValue) {
		// Check lower limit:
		final double lower = problem.getLower()[i];
		if (newValue < lower) {
			return lower;
			// Check upper limit:
		} else {
			final double upper = problem.getUpper()[i];
			if (newValue > upper) {
				return upper;
			}
		}
		return newValue;
	}
	
	public static double getFutureScore(Future<Double> future) {
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
    /**
     * Returns the closest rounded value of the given value for the given steps.
     *
     * @param value The original value to be rounded.
     * @param steps The steps.
     * @return The closest rounded value of the given value for the given steps.
     */
    public static double roundDoubleToClosest (double value, double steps) {
        final double down = roundDoubleDownTo(value, steps);
        final double up = roundDoubleUpTo(value, steps);
        if (Math.abs(value - down) < Math.abs(value - up)) {
            return down;
        }
        return up;
    }

    private static double roundDoubleDownTo(double value, double steps) {
        if (steps == 0) {
            return value;
        }
        else {
            return Math.floor(value / steps) * steps;
        }
    }

    private static double roundDoubleUpTo(double value, double steps) {
        if (steps == 0) {
            return value;
        }
        else {
            return Math.ceil(value / steps) * steps;
		}
	}
}
