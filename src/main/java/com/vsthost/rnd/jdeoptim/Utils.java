/*
 * Copyright (c) 2015 Vehbi Sinan Tunalioglu <vst@vsthost.com>.
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

package com.vsthost.rnd.jdeoptim;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * Provides some utility functions used throughout the library.
 */
public class Utils {
    /**
     * Creates a random population.
     *
     * @param size The size of the population to be created.
     * @param dimension The dimension of the candidate (length of each candidate) to be created.
     * @param lower The lower boundary.
     * @param upper The upper boundary.
     * @param distribution The distribution to be sampled from.
     * @return A random population.
     */
    private double[][] randomPopulation (int size, int dimension, double[] lower, double[] upper, RealDistribution distribution) {
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
                // TODO: For better performance, memoize (max - min).
                population[c][d] = min + ((max - min) * distribution.sample());
            }
        }

        // Done, return the population:
        return population;
    }
}
