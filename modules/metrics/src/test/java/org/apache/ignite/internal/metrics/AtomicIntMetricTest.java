/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.metrics;

/**
 * Test for {@link AtomicIntMetric}.
 */
public class AtomicIntMetricTest extends AbstractIntMetricTest {
    /** {@inheritDoc} */
    @Override protected AtomicIntMetric createMetric(String name, String description) {
        return new AtomicIntMetric(name, description);
    }

    /** {@inheritDoc} */
    @Override protected void increment0(IntMetric metric) {
        ((AtomicIntMetric) metric).increment();
    }

    /** {@inheritDoc} */
    @Override protected void decrement0(IntMetric metric) {
        ((AtomicIntMetric) metric).decrement();
    }

    /** {@inheritDoc} */
    @Override protected void add0(IntMetric metric, int value) {
        ((AtomicIntMetric) metric).add(value);
    }

    /** {@inheritDoc} */
    @Override protected void setValue0(IntMetric metric, int value) {
        ((AtomicIntMetric) metric).value(value);
    }
}
