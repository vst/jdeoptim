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

import java.util.Arrays;
import java.util.Random;

/**
 * Defines a differential evolution algorithm class.
 *
 * @author Vehbi Sinan Tunalioglu
 */
public class DE {
    /**
     * Defines the objective function to be used.
     */
    private ObjectiveFunction function;

    /**
     * Defines the dimension of the problem.
     */
    private int dimension;

    /**
     * Defines the lower limits of the values to be optimized.
     */
    private double[] lowerLimit;

    /**
     * Defines the upper limits of the values to be optimized.
     */
    private double[] upperLimit;

    /**
     * Defines the size of the population.
     */
    private int populationSize;

    /**
     * Defines the population.
     */
    private double[][] population;

    /**
     * Defines the number of iterations:
     */
    private int iterations;

    /**
     * Defines the crossover probability.
     */
    private double crossoverProbability;

    /**
     * Defines the weight.
     */
    private double weight;

    /**
     * Defines the best candidate.
     */
    private double[] bestCandidate;

    /**
     * Defines the best candidate score.
     */
    private double bestScore = Double.POSITIVE_INFINITY;

    /**
     * Defines an array of scores matching the active population.
     */
    private double[] scores;

    /**
     * Indicates if we are logging iteration diagnostics.
     */
    private boolean logging = true;

    /**
     * Defines the iteration log format.
     */
    private String iterationLogFormat;

    /**
     * Defines the start timestamp of the run.
     */
    private long timeStarted;

    /**
     * Defines the finish timestamp of the run.
     */
    private long timeFinished;

    /**
     * Constructor for the differential evolution algorithm runner.
     *
     * @param function The objective function
     * @param lowerLimit Lower limits
     * @param upperLimit Upper limits
     * @param iterations The number of iterations
     * @param populationSize The size of the population
     * @param crossoverProbability The crossover probability
     * @param weight The weight for deviations
     * @param logging Flag indivating if we are logging or not
     */
    public DE(ObjectiveFunction function, double[] lowerLimit, double[] upperLimit, int iterations, int populationSize, double crossoverProbability, double weight, boolean logging) {
        // Set the function:
        this.function = function;

        // Check upper/lower limit lengths:
        if (lowerLimit.length == 0 || lowerLimit.length != upperLimit.length) {
            throw new RuntimeException("Limit lengths should match each other and must be bigger than 0.");
        }

        // Check if lower limits are less than or equal to upper limits:
        for (int i = 0; i < lowerLimit.length; i++) {
            if (lowerLimit[i] > upperLimit[i]) {
                throw new RuntimeException("Lower limit cannot be greater than the corresponding upper limit.");
            }
        }

        // Set limits:
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;

        // Set the dimension:
        this.dimension = this.lowerLimit.length;

        // Set the population size:
        this.populationSize = populationSize;

        // Set the number of iterations:
        this.iterations = iterations;

        // Set the crossover probability:
        this.crossoverProbability = crossoverProbability;

        // Set the weight:
        this.weight = weight;

        // Set the logging flag:
        this.logging = logging;

        // Initialize:
        this.initialize();
    }

    /**
     * Initializes the evolution algorithm.
     */
    private void initialize () {
        // Initialize the iteration log format:
        this.iterationLogFormat = "Iteration: %0" + String.valueOf(this.iterations).length() + "d  [%.6f] %s";
    }

    /**
     * Initializes the population.
     */
    private void initializePopulation () {
        // Initialize the population:
        this.population = new double[this.populationSize][dimension];

        // Iterate over population size and populate candidate:
        for (int c = 0; c < this.populationSize; c++) {
            // Iterate over dimensions and populate candidate elements:
            for (int d = 0; d < this.dimension; d++) {
                // Get the minimum possible value:
                final double min = this.lowerLimit[d];

                // Get the maximum possible value:
                final double max = this.upperLimit[d];

                // Create a random value within the range:
                // TODO: Use a better random number generator?
                this.population[c][d] = min + ((max - min) * Math.random());
            }
        }
    }

    /**
     * Prints a line of information for debugging and narrative purposes.
     *
     * @param iteration The iteration number.
     */
    public void log(int iteration) {
        // Are we logging?
        if (!this.logging) {
            // Nope, return immediately:
            return;
        }

        // Print the line:
        System.out.println(String.format(this.iterationLogFormat, iteration, this.bestScore, DE.candidateToString(this.getBestCandidate())));
    }

    /**
     * Computes and returns a new population.
     *
     * @return A new population.
     */
    private void generatePopulation() {
        // Declare and initialize the new population:
        double[][] newPopulation = new double[this.populationSize][];

        // Declare and initialize the new population scores:
        double[] newScores = new double[this.populationSize];

        // Iterate over the current population:
        for (int c = 0; c < this.populationSize; c++) {
            // Get the candidate as the base of the next candidate (a.k.a. trial):
            double[] trial = Arrays.copyOf(this.population[c], this.dimension);

            // TODO: Implement all strategies.

            // Pick a random index from the dimension to start with:
            int index = new Random().nextInt(this.dimension);

            // Get 2 elements from the population which are distinct:
            int r1 = -1, r2 = -1;
            while (c == r1 || c == r2 || r1 == r2) {
                r1 = new Random().nextInt(this.populationSize);
                r2 = new Random().nextInt(this.populationSize);
            }

            // Iterate over dimensions and do stuff:
            for (int _i = 0; _i < this.dimension; _i++) {
                // To crossover or not to crossover:
                if (Math.random() < this.crossoverProbability) {
                    // Using strategy "DE/best/1/bin with jitter" as in R's DEoptim package (strategy==3):
                    trial[index] = this.bestCandidate[index] + this.weight * (Math.random() + 0.0001) * (this.population[r1][index] - this.population[r2][index]);

                    // Using strategy "DE/local-to-best/1/bin" as in R's DEoptim package (strategy==2):
                    // trial[index] += this.weight *  (this.population[r1][index] - this.population[r2][index]) + this.weight * (this.bestCandidate[index] - trial[index]);
                }

                // Update index:
                index = (index + 1) % dimension;
            }

            // Apply limits:
            for (int i = 0; i < this.lowerLimit.length; i++) {
                // Check lower limit:
                if (trial[i] < this.lowerLimit[i]) {
                    trial[i] = this.lowerLimit[i];
                }
                // Check upper limit:
                else if (trial[i] > this.upperLimit[i]) {
                    trial[i] = this.upperLimit[i];
                }
            }

            // Compute the score of the trial:
            final double score = this.function.apply(trial);

            // Check the score of this candidate:
            if (score < this.scores[c]) {
                newPopulation[c] = trial;
                newScores[c] = score;
            }
            else {
                newPopulation[c] = this.population[c];
                newScores[c] = this.scores[c];
            }
        }

        // Now, reset the population and scores:
        this.population = newPopulation;
        this.scores = newScores;
    }

    /**
     * Runs the evolutionary algorithm and returns the best candidate.
     *
     * @return The best candidate as an array of double values.
     */
    public void run() {
        // Mark the start time:
        this.timeStarted = System.currentTimeMillis();

        // Initialize the first population:
        this.initializePopulation();

        // Initialize the array of scores to be used at each iteration with the current population:
        this.scores = new double[this.populationSize];
        for (int i = 0; i < this.populationSize; i++) {
            this.scores[i] = this.function.apply(this.population[i]);
        }

        // Get the best score index and update best candidate and its score:
        int bestIndex = DE.minIndex(scores);
        this.bestCandidate = Arrays.copyOf(this.population[bestIndex], this.dimension);
        this.bestScore = this.scores[bestIndex];

        // Iterate over number of iterations:
        for (int iteration = 1; iteration < this.iterations; iteration++) {
            // Generate the new population, update scores etc.:
            this.generatePopulation();

            // Compute the index of the best candidate:
            bestIndex = DE.minIndex(this.scores);

            // Choose if the score is better:
            if (this.scores[bestIndex] < this.bestScore) {
                this.bestCandidate = Arrays.copyOf(this.population[bestIndex], this.dimension);
                this.bestScore = this.scores[bestIndex];
            }

            // Narrate:
            this.log(iteration);
        }

        // Mark the finish timestamp:
        this.timeFinished = System.currentTimeMillis();
    }

    /**
     * Returns the best candidate evolved.
     *
     * @return The best candidate.
     */
    public double[] getBestCandidate() {
        return this.bestCandidate;
    }

    /**
     * Prints a diagnosis to the standard output.
     */
    public void diagnose() {
        System.out.println("Best Candidate : " + DE.candidateToString(this.getBestCandidate()));
        System.out.println("Best Score     : " + this.function.apply(this.getBestCandidate()));
        System.out.println("Total Runtime  : " + (this.timeFinished - this.timeStarted) + " msecs.");
    }

    /**
     * Returns the candidate as a string.
     *
     * @param candidate The candidate to be formatted.
     * @return The string representation of the candidate.
     */
    private static String candidateToString (double[] candidate) {
        return Arrays.toString(candidate);
    }

    /**
     * Returns the index of the minimum element in the double array provided.
     *
     * @param array The array to be search for the index of the minimum element.
     * @return The index of the minimum element in the array.
     */
    private static int minIndex (double[] array) {
        // Declare and initialize the return value:
        int index = -1;

        // Declare and initialize the minimum value:
        double min = Double.POSITIVE_INFINITY;

        // Find the best score and update values:
        for (int i = 0; i < array.length; i++) {
            if (array[i] <= min) {
                min = array[i];
                index = i;
            }
        }

        // Done, return:
        return index;
    }
}