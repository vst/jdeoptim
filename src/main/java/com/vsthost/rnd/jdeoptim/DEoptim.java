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

package com.vsthost.rnd.jdeoptim;

import com.vsthost.rnd.jdeoptim.diagnostics.Diagnostics;
import com.vsthost.rnd.jdeoptim.evolution.Objective;
import com.vsthost.rnd.jdeoptim.evolution.Population;
import com.vsthost.rnd.jdeoptim.evolution.Problem;
import com.vsthost.rnd.jdeoptim.evolution.strategies.Strategy;
import com.vsthost.rnd.jdeoptim.stop.StopIteration;

/**
 * Provides a differential evolution runner class.
 */
public class DEoptim {
	/**
	 * Defines the number of iterations.
	 */
	final protected int iterations;

	/**
	 * Defines the problem to be solved.
	 */
	final protected Problem problem;

	/**
	 * Defines the objective objective to calculate fitness scores.
	 */
	final protected Objective objective;

	/**
	 * Defines the strategy for the evolution of population.
	 */
	final protected Strategy strategy;

	/**
	 * Defines the population.
	 */
	final protected Population population;

	/**
	 * Defines the diagnostics.
	 */
	final protected Diagnostics diagnostics;

	/**
	 * Defines the stopIteration.
	 */
	final protected StopIteration stopIteration;

	/**
	 * Provides the only constructor for the differential evolution runner
	 * class.
	 *
	 * <p>
	 * This constructor is the only one available for the class and will be kept
	 * as the only one as static builders are going to be used for convenience
	 * reasons. All the arguments to this method will then safely be final as
	 * there will be no setters for these.
	 * </p>
	 *
	 * @param iterations
	 *            The number of iterations.
	 * @param problem
	 *            The problem to be solved.
	 * @param objective
	 *            The objective as a fitness score calculator.
	 * @param strategy
	 *            The population evolution strategy.
	 * @param population
	 *            The initial population to start with.
	 * @param diagnostics
	 *            The diagnostics instance.
	 */
	public DEoptim(int iterations, Problem problem, Objective objective, Strategy strategy, Population population,
			Diagnostics diagnostics) {
		this(iterations, problem, objective, strategy, population, diagnostics, null);
	}

	/**
	 * Provides the only constructor for the differential evolution runner
	 * class.
	 *
	 * <p>
	 * This constructor is the only one available for the class and will be kept
	 * as the only one as static builders are going to be used for convenience
	 * reasons. All the arguments to this method will then safely be final as
	 * there will be no setters for these.
	 * </p>
	 *
	 * @param iterations
	 *            The number of iterations.
	 * @param problem
	 *            The problem to be solved.
	 * @param objective
	 *            The objective as a fitness score calculator.
	 * @param strategy
	 *            The population evolution strategy.
	 * @param population
	 *            The initial population to start with.
	 * @param diagnostics
	 *            The diagnostics instance.
	 * @param stopIteration
	 *            The stopIteration instance to preliminary stop the iteration.
	 */
	public DEoptim(int iterations, Problem problem, Objective objective, Strategy strategy, Population population,
			Diagnostics diagnostics, final StopIteration stopIteration) {
		// Save object fields:
		this.iterations = iterations;
		this.problem = problem;
		this.objective = objective;
		this.strategy = strategy;
		this.population = population;
		this.diagnostics = diagnostics;
		if (stopIteration == null) {
			//never stop iteration prematurely
			this.stopIteration = new StopIteration() {
				@Override
				public boolean stopIteration(double bestScore) {
					return false;
				}
			};
		} else {
			this.stopIteration = stopIteration;
		}
	}

	/**
	 * Runs the DE algorithm, evolves a population and populates diagnostics.
	 */
	public void evolve() {
		// Check if the evolution is running for the second time.
		if (this.diagnostics.hasStarted()) {
			// TODO: Do we really want to halt? Just return? Or manage a
			// stateless evolution for multiple runs?
			throw new RuntimeException("The DE instance can run only once. Use convenience tools for multiple runs.");
		}

		// Tell diagnostics that we are starting:
		this.diagnostics.evolutionStarted();

		// First, compute the score of the initial population:
		initialize();

		// OK, fun starts. We will iterate, evolve and update observers:
		iterate();

		// Done, let the diagnostics know that we are finished with the
		// evolution:
		this.diagnostics.evolutionFinished(this.population);
	}

	protected void initialize() {
		for (int i = 0; i < this.population.getSize(); i++) {
			// Get the population member first:
			final double[] member = this.population.getMember(i);

			// We are not sure if the member is a valid member. The likelihood
			// increases
			// when the population is provided by a routine which does not
			// ensure validity.
			// Thus, check for validity:
			if (!this.problem.isValid(member)) {
				// Oops, the member is not valid. Set the score to worst
				// possible. The evolution
				// is going to take care of it afterwards:
				this.population.setScore(i, Double.POSITIVE_INFINITY);
			} else {
				// Good to go! Calculate and set the score:
				this.population.setScore(i, this.objective.apply(member));
			}
		}
	}

	protected void iterate() {
		for (int iteration = 0; iteration < this.iterations; iteration++) {
			// Tell diagnostics that we are starting a new iteration:
			this.diagnostics.iterationStarted(iteration);

			// Re-generate the population using the strategy:
			this.strategy.regenerate(this.population, this.problem, this.objective);

			// Tell diagnostics that we are finished with this iteration:
			this.diagnostics.iterationFinished(iteration, this.population);

			final double bestScore = population.getBestScore();
			if (bestScore <= Double.NEGATIVE_INFINITY) {
				break;
			}
			if (stopIteration.stopIteration(bestScore)) {
				break;
			}
		}
	}

	/**
	 * Returns the number of iterations.
	 *
	 * @return The number of iterations.
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * Returns the problem specification.
	 *
	 * @return The problem specification.
	 */
	public Problem getProblem() {
		return problem;
	}

	/**
	 * Returns the objective.
	 *
	 * @return The objective.
	 */
	public Objective getObjective() {
		return objective;
	}

	/**
	 * Returns the strategy.
	 *
	 * @return The strategy.
	 */
	public Strategy getStrategy() {
		return strategy;
	}

	/**
	 * Returns the last available population.
	 *
	 * @return The last available population.
	 */
	public Population getPopulation() {
		return population;
	}

	/**
	 * Returns the diagnostics.
	 *
	 * @return The diagnostics.
	 */
	public Diagnostics getDiagnostics() {
		return diagnostics;
	}
	
	/**
	 * Returns the stopIteration.
	 *
	 * @return The stopIteration.
	 */
	public StopIteration getStopIteration() {
		return stopIteration;
	}
}
