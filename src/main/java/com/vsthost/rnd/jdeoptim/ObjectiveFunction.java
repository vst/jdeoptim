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

/**
 * Defines an interface which all DE objective functions should implement.
 *
 * @author Vehbi Sinan Tunalioglu
 */
public interface ObjectiveFunction {
    /**
     * Evaluates the candidate and returns the candidate score.
     *
     * @param candidate The candidate to be evaluated.
     * @return The score of the candidate.
     */
    public double apply(double[] candidate);
}
