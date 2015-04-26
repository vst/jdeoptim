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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a context class which is accessible by the strategy throughout
 * the entire evolution.
 *
 * <p>The context is not a standard concept in the DEoptim literature. The 6 well-documented
 * strategies do not need such a concept. However, if the DEoptim evolution is to be
 * guided by a custom, domain-specific strategy which may require auxiliary data,
 * then the context becomes relevant.</p>
 *
 * <p>The context is provided before the DEoptim run starts. Then, the context
 * is fed to the strategy at each iteration.</p>
 */
public class Context {
    /**
     * Indicates the up-to-date generation count.
     */
    private int generation = -1;

    /**
     * Defines a database for named properties.
     *
     * Note that we don't necessarily need a syncronized implementation as this
     * map is going to be populated, updated and read in a single thread.
     */
    final private Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Provides a default, no-argument constructor.
     */
    public Context() {}

    /**
     * Provides a convenience constructor to create a context with a property.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     */
    public Context(String name, Object value) {
        // Set the property with the given name and value:
        this.setProperty(name, value);
    }

    /**
     * Returns the current generation count.
     *
     * @return The current generation count.
     */
    public int getGeneration() {
        return this.generation;
    }

    /**
     * Sets the current generation count.
     *
     * @param generation The current generation count.
     */
    protected void setGeneration(int generation) {
        this.generation = generation;
    }

    /**
     * Increments the current generation count.
     */
    protected void incrementGeneration() {
        this.generation += 1;
    }

    /**
     * Sets the value of a named property.
     *
     * @param name The name of the property.
     * @param value The value of the property.
     */
    public void setProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    /**
     * Returns the value of the named property.
     *
     * @param name The name of the property.
     * @return The value of the named property.
     */
    public Object getProperty(String name) {
        return this.properties.get(name);
    }
}
