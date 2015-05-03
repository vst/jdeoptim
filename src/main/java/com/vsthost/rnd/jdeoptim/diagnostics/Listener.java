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

/**
 * Defines an evolution observer.
 */
public interface Listener {
    /**
     * Defines a slot to be invoked when evolution starts.
     */
    public void evolutionStarted();

    /**
     * Defines a slot when an iteration starts.
     *
     * @param iteration The iteration count.
     */
    public void iterationStarted(int iteration);

    /**
     * Defines a slot when an iteration finishes.
     *
     * @param iteration The iteration count.
     * @param population The population after the re-generation.
     */
    public void iterationFinished(int iteration, Population population);

    /**
     * Defines a slot when an evolution finishes.
     *
     * @param population The final population after the evolution.
     */
    public void evolutionFinished(Population population);
}
