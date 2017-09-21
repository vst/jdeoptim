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

import com.vsthost.rnd.commons.math.ext.linear.DMatrixUtils;
import com.vsthost.rnd.jdeoptim.DEoptim;
import com.vsthost.rnd.jdeoptim.evolution.Objective;
import com.vsthost.rnd.jdeoptim.evolution.Population;
import com.vsthost.rnd.jdeoptim.evolution.Problem;
import com.vsthost.rnd.jdeoptim.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Defines a simple strategy that runs the population objective in parallel for
 * each iteration. Useful for long running objective functions. Though has the
 * drawback of trials in one iteration not affecting one another during mutation
 * since each trial is executed at the same time.
 */
public class ParallelStrategy3 implements Strategy {
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
	private final double c;

	/**
	 * Defines the jitter factor.
	 */
	private final double jitterFactor;

	/**
	 * Defines if we are bouncing back.
	 */
	private final boolean bounceBack;

	/**
	 * Defines the mean crossover probability for the case of
	 * <code>c &gt; 0</code>.
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

	/**
	 * Defines the precision.
	 */
	private double precision;

	/**
	 * Reuse array to reduce effort on garbage collector
	 */
	final private int[] randomCandidates = new int[2];

	/**
	 * The given executor for parallel runs.
	 */
	final protected ExecutorService executor;

	/**
	 * Implements the Strategy 3.
	 *
	 * @param cr
	 *            Crossover probability
	 * @param f
	 *            Weighting factor.
	 * @param c
	 *            Speed of CR adaptation.
	 * @param jitterFactor
	 *            The jitter factor
	 * @param bounceBack
	 *            Indicates if we are bouncing back or setting to limits in case
	 *            of violations.
	 * @param randomGenerator
	 *            The random generator.
	 */
	public ParallelStrategy3(double cr, double f, double c, double jitterFactor, boolean bounceBack, double precision,
			RandomGenerator randomGenerator, ExecutorService executor) {
		// Save the random number generator:
		this.randomGenerator = randomGenerator;

		// Save the CR, F and c parameters:
		this.cr = cr;
		this.f = f;
		this.c = c;

		// Save the jitter factor:
		this.jitterFactor = jitterFactor;

		// Save the bounce back param:
		this.bounceBack = bounceBack;

		// Set the meanCR initially to the crossover:
		this.meanCR = this.cr;

		// Set the meanF initially to the crossover:
		this.meanF = this.f;

		// Save precision:
		this.precision = precision;

		// Setup the distribution for random sampling of members:
		this.probability = new UniformRealDistribution(randomGenerator, 0, 1);

		this.executor = executor;
	}

	@Override
	public void regenerate(Population population, Problem problem, Objective objective) {
		// Setup a uniform integer distribution for candidate element selection:
		UniformIntegerDistribution elementSampling = new UniformIntegerDistribution(this.randomGenerator, 0,
				population.getDimension() - 1);

		// Get the best member of the population:
		final double[] bestMember = population.getBestMember();

		// Define the new population data and scores as we don't want to
		// override old one within the loop:
		final double[][] newPopData = new double[population.getSize()][];
		final double[] newPopScores = new double[population.getSize()];
		final boolean[] newPopFlags = new boolean[population.getSize()];

		// Iterate over the current population:
		List<double[]> trials = new ArrayList<double[]>(population.getSize());
		List<Integer> trialIndexes = new ArrayList<Integer>(population.getSize());
		for (int c = 0; c < population.getSize(); c++) {
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
				} while (this.f <= 0.0);
			}

			// Get the candidate as the base of the next candidate (a.k.a.
			// trial):
			final double[] trial = population.getMemberCopy(c);

			// Get two random candidate indices:
			Utils.fastPickTwoRandomMembers(randomCandidates, population.getSize(), c, randomGenerator);

			// Get the index of element of candidate to start with:
			int j = elementSampling.sample();

			// Set the counter for preventing overflowing dimension:
			int k = 0;

			// Iterate and set elements:
			boolean changed = false;
			do {
				// Get the jitter:
				final double jitter = (probability.sample() * this.jitterFactor) + this.f;

				// Get the respective element of the best candidate:
				final double bestest = bestMember[j];

				// Get the random candidate elements:
				final double random1 = population.getMember(randomCandidates[0])[j];
				final double random2 = population.getMember(randomCandidates[1])[j];

				// Override trial:
				double newValue = bestest + jitter * (random1 - random2);
				// Apply limits in case that we have violated:
				if (bounceBack) {
					newValue = Utils.applyLimits(problem, j, newValue);
				} else {
					newValue = applyLimitsBounceback(problem, j, newValue);
				}
				// We have an interim trial. We will now truncate:
				if (precision != 0) {
					// OK, truncate:
					newValue = DMatrixUtils.roundDoubleToClosest(trial[j], this.precision);
				}
				changed = changed || newValue != trial[j];
				trial[j] = newValue;

				// Move to the next element:
				j = (j + 1) % population.getDimension();

				// Increment k:
				k++;
			} while (probability.sample() < this.cr && k < population.getDimension());

			if (changed || population.getScore(c) == DEoptim.INVALID_SCORE) {
				trials.add(trial);
				trialIndexes.add(c);
			}
		}

		if (!trials.isEmpty()) {
			final double[] newScores = submitTrials(trials, objective);

			for (int i = 0; i < trials.size(); i++) {
				int c = trialIndexes.get(i);
				// We will now check if we have a
				// better candidate. If yes, we will replace the old member with
				// the
				// trial,
				// if not we will just skip. Compute the score:
				final double newScore = newScores[i];

				// Get the score of the candidate:
				final double oldScore = population.getScore(c);

				// Check the new score against the old one and act accordingly:
				if (newScore < oldScore) {
					double[] trial = trials.get(i);
					// Yes, our trial is a better candidate. Replace:
					newPopData[c] = trial;
					newPopScores[c] = newScore;
					newPopFlags[c] = true;

					// We will now re-adjust for CR and F.
					this.goodCR += this.cr / ++this.goodNPCount;
					this.goodF += this.f;
					this.goodF2 += Math.pow(this.f, 2);
				} else {
					newPopFlags[c] = false;
				}
			}

			// Re-compute mean CR and F if required:
			if (this.c > 0 && this.goodF != 0) {
				this.meanCR = (1 - this.c) * this.meanCR + this.c * this.goodCR;
				this.meanF = (1 - this.c) * this.meanF + this.c * this.goodF2 / this.goodF;
			}

			// Now, override the population:
			for (int i = 0; i < population.getSize(); i++) {
				if (newPopFlags[i]) {
					population.setMember(i, newPopData[i], newPopScores[i]);
				}
			}
		}
	}

	private double applyLimitsBounceback(final Problem problem, final int i, final double newValue) {
		// Check lower limit:
		final double lower = problem.getLower()[i];
		if (newValue < lower) {
			return lower + probability.sample() * (problem.getUpper()[i] - problem.getLower()[i]);
		}
		// Check upper limit:
		final double upper = problem.getUpper()[i];
		if (newValue > upper) {
			return upper - probability.sample() * (problem.getUpper()[i] - problem.getLower()[i]);
		}
		return newValue;
	}

	/**
	 * Can be overridden to maybe chunk tasks manually.
	 */
	protected double[] submitTrials(List<double[]> trials, final Objective objective) {
		@SuppressWarnings("unchecked")
		final Future<Double>[] futures = new Future[trials.size()];
		for (int i = 0; i < trials.size(); i++) {
			double[] trial = trials.get(i);
			futures[i] = executor.submit(new Callable<Double>() {
				@Override
				public Double call() throws Exception {
					final double newScore = objective.apply(trial);
					return newScore;
				}
			});
		}
		double[] newScores = new double[futures.length];
		for (int i = 0; i < futures.length; i++) {
			Future<Double> future = futures[i];
			double newScore = Utils.getFutureScore(future);
			newScores[i] = newScore;
		}
		return newScores;
	}

}
