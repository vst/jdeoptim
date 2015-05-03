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

package com.vsthost.rnd.jdeoptim.diagnostics;

import com.vsthost.rnd.jdeoptim.evolution.Population;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides a diagnostics class.
 */
public class Diagnostics {
    /**
     * Defines the iteration log format.
     */
    final private String logFormat;

    /**
     * Indicates if logging is enabled or not.
     */
    final private boolean logging;

    /**
     * Indicates if statistics are enabled or not.
     */
    final private boolean statistics;

    /**
     * Initializes an evolution book for keeping records.
     */
    final private List<Diagnostics.Entry> entries = new ArrayList();

    /**
     * Defines a list of observers.
     */
    final private List<Listener> observers = new ArrayList<Listener>();

    /**
     * Defines the start timestamp of the run.
     */
    private long timeStarted;

    /**
     * Defines the finish timestamp of the run.
     */
    private long timeFinished;

    /**
     * Defines the best member.
     */
    private double[] bestMember;

    /**
     * Defines the score of the best member.
     */
    private double bestScore;

    /**
     * Constructs a new diagnostics instance.
     *
     * @param logging Indicates if logging is enabled or not.
     * @param statistics Indicates if detailed statistics are to be computed or not.
     */
    public Diagnostics(final boolean logging, final boolean statistics) {
        // Define the log format:
        this.logFormat = "%02d [%.6f] %s";

        // Are we logging?
        this.logging = logging;

        // Are we collecting statistics?
        this.statistics = statistics;

        // Set default observers:
        this.addObserver(new Listener() {
            @Override
            public void evolutionStarted() {
                // Mark the started datetime.
                timeStarted = System.currentTimeMillis();
            }

            @Override
            public void iterationStarted(int iteration) {
                // Nothing to be done...
            }

            @Override
            public void iterationFinished(int iteration, Population population) {
                // Are we logging? If yes log it:
                if (logging) {
                    System.err.println(String.format(logFormat, iteration, population.getBestScore(), Arrays.toString(population.getBestMember())));
                }

                if (statistics) {
                    // Calculate the statistics:
                    DescriptiveStatistics stats = new DescriptiveStatistics(population.getScores());

                    // Add the log entry to the evolution book:
                    entries.add(new Diagnostics.Entry(population.getBestScore(), population.getBestMember(), stats));
                }
                else {
                    // Add the log entry to the evolution book:
                    entries.add(new Diagnostics.Entry(population.getBestScore(), population.getBestMember(), null));
                }
            }

            @Override
            public void evolutionFinished(Population population) {
                // Mark the end datetime:
                timeFinished = System.currentTimeMillis();

                // Set the best member:
                bestMember = population.getBestMember();

                // Set the best score:
                bestScore = population.getBestScore();
            }
        });
    }

    /**
     * Returns the book entries of the evolution.
     *
     * @return Returns the book entries of the evolution.
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Returns the time evolution started.
     *
     * @return The time evolution started.
     */
    public long getTimeStarted() {
        return timeStarted;
    }

    /**
     * Returns the time evolution finished.
     *
     * @return The time evolution finished.
     */
    public long getTimeFinished() {
        return timeFinished;
    }

    /**
     * Returns the best member.
     *
     * @return The best member.
     */
    public double[] getBestMember() {
        return bestMember;
    }

    /**
     * Returns the score of the best member.
     *
     * @return The score of the best member.
     */
    public double getBestScore() {
        return bestScore;
    }

    /**
     * Indicates if the evolution has started.
     *
     * @return True if the evolution has started, false otherwise.
     */
    public boolean hasStarted () {
        return this.timeStarted > 0;
    }

    /**
     * Indicates if the evolution has finished.
     *
     * @return True if the evolution has finished, false otherwise.
     */
    public boolean hasFinished () {
        return this.timeFinished > 0;
    }

    /**
     * Adds a new observer to the observer list.
     *
     * @param observer A new observer to be added.
     */
    public void addObserver(Listener observer) {
        this.observers.add(observer);
    }

    /**
     * Defines a signal to be emitted when an evolution has started.
     */
    public void evolutionStarted () {
        this.observers.forEach(new Consumer<Listener>() {
            @Override
            public void accept(Listener observer) {
                observer.evolutionStarted();
            }
        });
    }

    /**
     * Defines a signal to be emitted when an iteration has started.
     *
     * @param iteration The iteration number.
     */
    public void iterationStarted (final int iteration) {
        this.observers.forEach(new Consumer<Listener>() {
            @Override
            public void accept(Listener observer) {
                observer.iterationStarted(iteration);
            }
        });
    }

    /**
     * Defines a signal to be emitted when an iteration has finished.
     *
     * @param iteration The iteration number.
     * @param population The population as of the iteration has finished.
     */
    public void iterationFinished (final int iteration, final Population population) {
        this.observers.forEach(new Consumer<Listener>() {
            @Override
            public void accept(Listener observer) {
                observer.iterationFinished(iteration, population);
            }
        });
    }

    /**
     * Defines a signal to be emitted when an evolution has finished.
     *
     * @param population The population as of the evolution has finished.
     */
    public void evolutionFinished (final Population population) {
        this.observers.forEach(new Consumer<Listener>() {
            @Override
            public void accept(Listener observer) {
                observer.evolutionFinished(population);
            }
        });
    }

    /**
     * Defines a diagnostics entry class for evolutionary bookkeeping purposes.
     */
    public class Entry {
        /**
         * Defines the score of the best member.
         */
        final public double score;

        /**
         * Defines the best member.
         */
        final public double[] member;

        /**
         * Defines the population score statistics.
         */
        final public DescriptiveStatistics statistics;

        /**
         * Defines the only constructor for the entry class.
         *
         * @param score The score of the best member.
         * @param member The best member.
         * @param statistics The population score statistics.
         */
        public Entry(double score, double[] member, DescriptiveStatistics statistics) {
            this.score = score;
            this.member = member;
            this.statistics = statistics;
        }
    }
}
