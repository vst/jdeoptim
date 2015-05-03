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

package com.vsthost.rnd.jdeoptim.evolution;

/**
 * Defines a problem to be solved by the differential evolution algorithm.
 */
public class Problem {
    /**
     * Defines lower limits of a possible solution.
     */
    final private double[] lower;

    /**
     * Defines upper limits of a possible solution.
     */
    final private double[] upper;

    /**
     * Defines the problem dimension as the number of
     * elements of the desired solution.
     */
    final private double dimension;

    /**
     * Constructs a problem with the given lower and upper bounds of values
     * which a candidate element can take.
     *
     * @param lower Lower limits of a possible solution.
     * @param upper Upper limits of a possible solution.
     */
    public Problem(double[] lower, double[] upper) {
        // Check arguments:
        if (lower == null || upper == null) {
            throw new IllegalArgumentException("Lower and upper limits of a DE problem can not be null.");
        }
        else if (lower.length == 0 || lower.length != upper.length) {
            throw new IllegalArgumentException("Limit lengths must match each other and be bigger than 0.");
        }

        // Check if lower limits are less than or equal to upper limits:
        for (int i = 0; i < lower.length; i++) {
            if (lower[i] > upper[i]) {
                throw new IllegalArgumentException("Lower limit can not be greater than the corresponding upper limit.");
            }
        }

        // Save lower and upper bounds;
        this.lower = lower;
        this.upper = upper;

        // Save the dimension:
        this.dimension = this.lower.length;
    }

    /**
     * Returns the lower limits of a possible solution.
     *
     * @return Lower limits of a possible solution.
     */
    public double[] getLower() {
        return this.lower;
    }

    /**
     * Returns the upper limits of a possible solution.
     *
     * @return Upper limits of a possible solution.
     */
    public double[] getUpper() {
        return this.upper;
    }

    /**
     * Returns the dimension of the problem, ie. the solution vector size.
     *
     * @return The dimension of the problem.
     */
    public double getDimension() {
        return this.dimension;
    }

    /**
     * Checks if a candidate is valid or not.
     *
     * @param candidate The candidate to be checked.
     * @return True if the candidate is valid, false otherwise.
     */
    public boolean isValid(double[] candidate) {
        // Iterate over elements and check for the lower and upper bounds:
        for (int i = 0; i < this.dimension; i++) {
            if (candidate[i] < this.lower[i] || candidate[i] > this.upper[i]) {
                // Violating, return false:
                return false;
            }
        }

        // Candidate is valid. Return true:
        return true;
    }
}
