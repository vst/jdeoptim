/*
 * Copyright 2015 Vehbi Sinan Tunalioglu <vst@vsthost.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.vsthost.rnd.jdeoptim.evolution.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import com.vsthost.rnd.jdeoptim.DEoptim;
import com.vsthost.rnd.jdeoptim.evolution.Objective;
import com.vsthost.rnd.jdeoptim.evolution.Population;
import com.vsthost.rnd.jdeoptim.evolution.Problem;
import com.vsthost.rnd.jdeoptim.utils.Utils;

/**
 * Defines a simple strategy that runs the population objective in parallel for
 * each iteration. Useful for long running objective functions. Though has the
 * drawback of trials in one iteration not affecting one another during mutation
 * since each trial is executed at the same time.
 */
public class ParallelSimpleStrategy implements Strategy {
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
	final private double cr;

	/**
	 * Defines the weighting factor of differentials.
	 */
	final private double f;

	/**
	 * Reuse array to reduce effort on garbage collector
	 */
	final private int[] randomMembers = new int[2];

	/**
	 * The given executor for parallel runs.
	 */
	final protected ExecutorService executor;

	public ParallelSimpleStrategy(final double cr, final double f, final RandomGenerator randomGenerator,
			ExecutorService executor) {
		// Save the random number generator:
		this.randomGenerator = randomGenerator;

		// Save the CR and F parameters:
		this.cr = cr;
		this.f = f;

		// Setup the distribution for random sampling of members:
		this.probability = new UniformRealDistribution(randomGenerator, 0, 1);

		this.executor = executor;
	}

	@Override
	public void regenerate(final Population population, final Problem problem, final Objective objective) {
		// Get the best member of the population:
		final double[] bestMember = population.getBestMember();

		// Iterate over the current population:
		List<double[]> trials = new ArrayList<double[]>(population.getSize());
		List<Integer> trialIndexes = new ArrayList<Integer>(population.getSize());
		for (int c = 0; c < population.getSize(); c++) {
			// Get the candidate as the base of the next candidate (a.k.a.
			// trial):

			final double[] trial = population.getMemberCopy(c);

			// Get 2 random member indices from the population which are
			// distinct:
			Utils.fastPickTwoRandomMembers(randomMembers, population.getSize(), c, randomGenerator);

			// Get the random members:
			final double[] randomMember1 = population.getMember(randomMembers[0]);
			final double[] randomMember2 = population.getMember(randomMembers[1]);

			// Iterate over all member elements and do the trick:
			boolean changed = false;
			for (int i = 0; i < population.getDimension(); i++) {
				// Any manipulation?
				if (this.cr >= 1 || probability.sample() < this.cr) {
					// Yes, we will proceed with a change:
					double newValue = bestMember[i]
							+ this.f * (probability.sample() + 0.0001) * (randomMember1[i] - randomMember2[i]);
					// Apply limits in case that we have violated:
					newValue = Utils.applyLimits(problem, i, newValue);
					changed = changed || newValue != trial[i];
					trial[i] = newValue;
				}
			}

			if (changed || population.getScore(c) == DEoptim.INVALID_SCORE) {
				trials.add(trial);
				trialIndexes.add(c);
			}
		}

		if (!trials.isEmpty()) {
			final double[] newScores = submitTrials(trials, objective);

			// OK, we are done with the trial. We will now check if we
			// have a
			// better candidate. If yes, we will replace the old member
			// with the trial,
			// if not we will just skip. Compute the score:
			for (int i = 0; i < trials.size(); i++) {
				int c = trialIndexes.get(i);
				// Get the score of the candidate:
				final double oldScore = population.getScore(c);
				final double newScore = newScores[i];

				// Check the new score against the old one and act accordingly:
				if (newScore < oldScore) {
					// Yes, our trial is a better candidate. Replace:
					double[] trial = trials.get(i);
					population.setMember(c, trial, newScore);
				}
			}
		}
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
			double newScore = getFuture(future);
			newScores[i] = newScore;
		}
		return newScores;
	}

	protected double getFuture(Future<Double> future) {
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

}
