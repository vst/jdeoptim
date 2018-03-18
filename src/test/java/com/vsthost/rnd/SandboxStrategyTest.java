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

package com.vsthost.rnd;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

import com.vsthost.rnd.jdeoptim.DEoptim;
import com.vsthost.rnd.jdeoptim.diagnostics.Diagnostics;
import com.vsthost.rnd.jdeoptim.evolution.Objective;
import com.vsthost.rnd.jdeoptim.evolution.Population;
import com.vsthost.rnd.jdeoptim.evolution.Problem;
import com.vsthost.rnd.jdeoptim.evolution.strategies.SandboxStrategy;
import com.vsthost.rnd.jdeoptim.evolution.strategies.Strategy;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class SandboxStrategyTest {

    /**
     * Defines a silly formula to be used in the objectives.
     *
     * @param x X
     * @param y Y
     * @return Some silly result.
     */
    public static double SillyFormula (double x, double y) {
        final double z = Math.sqrt(x * y == 0 ? 1 : Math.abs(x * y));
        final double t = Math.pow(x <= 0 ? 1 : x, y);
        return x * x * x * + 3 * x * x + 2 * x * y + 3 *  y * y + y * y * y + 5 * x * y + z + t;
    }

    @Test
    public void isItEvolving() {
        // Create an instance of a problem:
        Problem problem = new Problem(new double[] {-1, -1}, new double[]{1, 1});

        // Define an objective:
        Objective objective = candidate -> Math.abs(SillyFormula(candidate[0], candidate[1]));

        // Define a strategy:
        Strategy strategy = new SandboxStrategy(0.75, 0.8, 0.1, new MersenneTwister());

        // Initialize a population:
        Population population = new Population(20, 2, new double[] {-1, -1}, new double[]{1, 1}, new UniformRealDistribution());

        // Define the diagnostics:
        Diagnostics diagnostics = new Diagnostics(true, true);

        // Define the DE instance:
        DEoptim DEoptim = new DEoptim(50, problem, objective, strategy, population, diagnostics);

        // Run it:
        DEoptim.evolve();

        // Compare the first score to the best:
        assertTrue(diagnostics.getEntries().get(0).score >= diagnostics.getBestScore());
    }
}
