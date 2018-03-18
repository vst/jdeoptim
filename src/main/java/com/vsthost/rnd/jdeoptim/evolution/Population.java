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

import org.apache.commons.math3.distribution.RealDistribution;

import com.vsthost.rnd.jdeoptim.utils.Utils;

import java.util.Arrays;

/**
 * Defines a population class.
 *
 * <p>Note that the indexing operations are not checked against
 * array limits as we don't want to waste time with it. Thus, the calling
 * procedure is responsible of making sure that there are no boundary violations.</p>
 */
public class Population {
    /**
     * Defines the population data.
     */
    final private double[][] data;

    /**
     * Defines an array of scores matching the population.
     */
    private double[] scores;

    /**
     * Defines the size of the population.
     */
    final private int size;

    /**
     * Defines the dimension of an individual member of the population.
     */
    final private int dimension;

    /**
     * Defines the best member and score index.
     */
    private int best = 0;

    /**
     * Instantiates a population with the data consumed.
     *
     * @param data The data which the population is going to be initialized with.
     */
    public Population(double[][] data) {
        // Save the data field:
        this.data = data;

        // Get the size and save it:
        this.size = this.data.length;

        // Get the dimension and save it:
        this.dimension = this.size > 0 ? this.data[0].length : 0;

        // Initialize the scores:
        this.setScores(Double.POSITIVE_INFINITY);
    }

    /**
     * Instantiates a population by generating a random population with the arguments provided.
     *
     * @param size The size of the population.
     * @param dimension The dimension of a population member.
     * @param lowerLimits Lower limits for the population.
     * @param upperLimits Upper limits for the population.
     * @param distribution The real-value distribution to be used.
     */
    public Population(int size, int dimension, double[] lowerLimits, double[] upperLimits, RealDistribution distribution) {
        // Generate a random population and save it:
        this.data = Utils.randomPopulation(size, dimension, lowerLimits, upperLimits, distribution);

        // Save the size and dimension fields:
        this.size = size;
        this.dimension = dimension;

        // Initialize the scores:
        this.setScores(Double.POSITIVE_INFINITY);
    }

    /**
     * Returns the population data.
     *
     * @return The population data.
     */
    public double[][] getData() {
        return this.data;
    }

    /**
     * Returns scores.
     *
     * @return Population member scores.
     */
    public double[] getScores() {
        return this.scores;
    }

    /**
     * Sets the scores of the population.
     *
     * @param scores New scores of the population.
     */
    public void setScores(double[] scores) {
        this.scores = scores;
    }

    /**
     * Sets all the scores of the population to the given value.
     *
     * @param score New score for the entire population member scores.
     */
    public void setScores(double score) {
        // Make sure that the score array is initialized:
        if (this.scores == null) {
            this.scores = new double[this.size];
        }

        // Set all the values:
        for (int i = 0; i < this.size; i++) {
            this.scores[i] = score;
        }
    }

    /**
     * Returns the score of the population member with the given index.
     *
     * @param index The index of the population member.
     * @return The score of the population member with the given index.
     */
    public double getScore(int index) {
        return this.scores[index];
    }

    /**
     * Sets the score of a population member with the given index to the given score.
     *
     * @param index The index of the population member.
     * @param score The new score of the population member with the given index.
     */
    public void setScore(int index, double score) {
        // Get the best score so far:
        final double bestScore = this.scores[this.best];

        // Save the score:
        this.scores[index] = score;

        // Check if the new score is better than the current one:
        if (score < bestScore) {
            // Yes, save the index:
            this.best = index;
        }
    }

    /**
     * Returns the population size.
     *
     * @return The populatio size.
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Returns the dimension of an individual member of the population.
     *
     * @return The dimension of an individual member of the population.
     */
    public int getDimension() {
        return this.dimension;
    }

    /**
     * Returns the population member with the given index.
     *
     * @param index The index of the population member.
     * @return The population member with the given index.
     */
    public double[] getMember(int index) {
        return this.data[index];
    }

    /**
     * Returns a copy of the member with the given index.
     *
     * <p>This method is required for operations which are not safe.</p>
     *
     * @param index The index of the population member.
     * @return The copy of the population member.
     */
    public double[] getMemberCopy(int index) {
        return Arrays.copyOf(this.data[index], this.dimension);
    }

    /**
     * Sets the population member with the given index and elements.
     *
     * @param index The index of the population member.
     * @param elements The new values for the elements of the member.
     */
    public void setMember(int index, double[] elements) {
        this.data[index] = elements;
    }

    /**
     * Sets the population member with the given index and elements along with its score.
     *
     * @param index The index of the population member.
     * @param elements The new values for the elements of the member.
     * @param score The score of the member.
     */
    public void setMember(int index, double[] elements, double score) {
        // First set the member:
        this.setMember(index, elements);

        // Now, set the corresponding score:
        this.setScore(index, score);
    }

    /**
     * Returns the order of the scores, ie. indices of population members sorted according to their scores.
     *
     * @return The indices of population members sorted according to their scores.
     */
    public int[] getOrder() {
        return Utils.order(this.scores);
    }

    /**
     * Returns the index of the best member of the population.
     *
     * @return The index of the best member of the population.
     */
    public int getBestIndex() {
        return this.best;
    }

    /**
     * Returns the score of the best member of the population.
     *
     * @return The score of the best member of the population.
     */
    public double getBestScore() {
        return this.scores[this.best];
    }

    /**
     * Returns the copy of the best member of the population.
     *
     * @return The copy of the best member of the population.
     */
    public double[] getBestMember() {
        return this.getMemberCopy(this.best);
    }
}
