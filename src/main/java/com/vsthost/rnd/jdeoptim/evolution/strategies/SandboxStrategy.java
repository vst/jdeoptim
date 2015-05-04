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

package com.vsthost.rnd.jdeoptim.evolution.strategies;

import com.vsthost.rnd.jdeoptim.evolution.Objective;
import com.vsthost.rnd.jdeoptim.evolution.Population;
import com.vsthost.rnd.jdeoptim.evolution.Problem;
import com.vsthost.rnd.jdeoptim.utils.Utils;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Defines a simple strategy.
 */
public class SandboxStrategy implements Strategy {
    /**
     * Defines a uniform, real probability distribution.
     */
    final private UniformRealDistribution probability;

    /**
     * Defines the random number generator for the strategy.
     */
    final private RandomGenerator randomGenerator;

    /**
     * Defines the crossover probability.
     */
    private double cr;

    /**
     * Defines the weighting factor of differentials.
     */
    private double f;

    /**
     * Defines the weighting factor of differentials.
     */
    final private double c;

    /**
     * Defines the mean crossover probability for the case of <code>c &gt; 0</code>.
     */
    private double meanCR;

    /**
     * Defines the mean weighting factor for the case of <code>c &gt; 0</code>;
     */
    private double meanF;

    /**
     * Defines the number of successful trials.
     */
    private int goodNPCount;

    /**
     * Defines the marginal part of CR adoption.
     */
    private double goodCR;

    /**
     * Defines the marginal part of F adoption.
     */
    private double goodF;

    /**
     * Defines the rate of marginal part of F adoption.
     */
    private double goodF2;

    public SandboxStrategy(double cr, double f, double c, RandomGenerator randomGenerator) {
        // Save the random number generator:
        this.randomGenerator = randomGenerator;

        // Save the CR, F and c parameters:
        this.cr = cr;
        this.f = f;
        this.c = c;

        // Set the meanCR initially to the crossover:
        this.meanCR = this.cr;

        // Set the meanF initially to the crossover:
        this.meanF = this.f;

        // Setup the distribution for random sampling of members:
        this.probability = new UniformRealDistribution(randomGenerator, 0, 1);
    }

    @Override
    public void regenerate(Population population, Problem problem, Objective objective) {
        // Get the best member of the population:
        final double[] bestMember = population.getBestMember();

        // Are we going to adjust CR and F?
        if (this.c > 0) {
            // Yes. We will not adjust the CR first:
            this.cr = new NormalDistribution(this.randomGenerator, this.meanCR, 0.1).sample();

            // Check and reset CR:
            this.cr = this.cr > 1 ? 1 : (this.cr < 0 ? 0 : this.cr);

            // OK, now we will adjust F:
            do {
                // Get the new F:
                this.f = new CauchyDistribution(this.randomGenerator, this.meanF, 0.1).sample();

                // Check and reset F if required:
                this.f = this.f > 1 ? 1 : this.f;
            } while (this.f <= 0);
//            System.err.print(this.meanF);
//            System.err.print(" ");
//            System.err.print(this.cr);
//            System.err.print(" ");
//            System.err.println(this.f);
        }

        // Iterate over the current population:
        for (int c = 0; c < population.getSize(); c++) {
            // Get the candidate as the base of the next candidate (a.k.a. trial):
            final double[] trial = population.getMemberCopy(c);

            // Get the score of the candidate:
            final double oldScore = population.getScore(c);

            // Get 2 random member indices from the population which are distinct:
            int[] randomMembers = Utils.pickRandom(Utils.sequence(population.getSize()), 2, new int[]{c}, this.randomGenerator);

            // Get the random members:
            final double[] randomMember1 = population.getMember(randomMembers[0]);
            final double[] randomMember2 = population.getMember(randomMembers[1]);

            // Iterate over all member elements and do the trick:
            for (int i = 0; i < population.getDimension(); i++) {
                // Any manipulation?
                if (probability.sample() < this.cr) {
                    // Yes, we will proceed with a change:
                    trial[i] = bestMember[i] + this.f * (probability.sample() + 0.0001) * (randomMember1[i] - randomMember2[i]);
                }
            }

            // Apply limits in case that we have violated:
            for (int i = 0; i < trial.length; i++) {
                // Check lower limit:
                if (trial[i] < problem.getLower()[i]) {
                    trial[i] = problem.getLower()[i];
                }
                // Check upper limit:
                else if (trial[i] > problem.getUpper()[i]) {
                    trial[i] = problem.getUpper()[i];
                }
            }

            // OK, we are done with the trial. We will now check if we have a
            // better candidate. If yes, we will replace the old member with the trial,
            // if not we will just skip. Compute the score:
            final double newScore = objective.apply(trial);

            // Check the new score against the old one and act accordingly:
            if (newScore < oldScore) {
                // Yes, our trial is a better candidate. Replace:
                population.setMember(c, trial, newScore);

                // We will now re-adjust for CR and F.
                this.goodCR += this.cr / ++this.goodNPCount;
                this.goodF += this.f;
                this.goodF2 += Math.pow(this.f, 2);
            }

        }

        // Re-compute mean CR and F if required:
        if (this.c > 0 && this.goodF != 0) {
            this.meanCR = (1 - this.c) * this.meanCR + this.c * this.goodCR;
            this.meanF = (1 - this.c) * this.meanF + this.c * this.goodF2 / this.goodF;
        }
    }
}
