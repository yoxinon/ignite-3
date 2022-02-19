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

package org.apache.ignite.internal.causality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

/**
 * Tests of causality token implementation based on versioned value.
 * {@link VersionedValue}
 */
public class VersionedValueTest {
    /** Test value. */
    public static final int TEST_VALUE = 1;

    /** The test revision register is used to move the revision forward. */
    public static final TestRevisionRegister REGISTER = new TestRevisionRegister();

    /**
     * The test gets a value for {@link VersionedValue} before the value is calculated.
     *
     * @throws OutdatedTokenException If failed.
     */
    @Test
    public void testGetValueBeforeReady() throws OutdatedTokenException {
        VersionedValue<Integer> longVersionedValue = new VersionedValue<>(
                (integerVersionedValue, token) -> {
                    integerVersionedValue.set(token, TEST_VALUE);
                },
                REGISTER,
                2
        );

        CompletableFuture<Integer> fut = longVersionedValue.get(0);

        assertFalse(fut.isDone());

        REGISTER.moveRevision.accept(0L);

        assertTrue(fut.isDone());

        assertEquals(TEST_VALUE, fut.join());

        assertSame(fut, longVersionedValue.get(0));
    }

    /**
     * The test explicitly sets a value to {@link VersionedValue} without waiting for the revision updaste.
     *
     * @throws OutdatedTokenException If failed.
     */
    @Test
    public void testExplicitlySetValue() throws OutdatedTokenException {
        VersionedValue<Integer> longVersionedValue = new VersionedValue<>(REGISTER);

        CompletableFuture<Integer> fut = longVersionedValue.get(0);

        assertFalse(fut.isDone());

        longVersionedValue.set(0, TEST_VALUE);

        assertTrue(fut.isDone());

        assertEquals(TEST_VALUE, fut.join());

        assertSame(fut, longVersionedValue.get(0));
    }

    /**
     * The test reads a value with the specific token in which the value should not be updated.
     * The read happenes before the revision updated.
     *
     * @throws OutdatedTokenException If failed.
     */
    @Test
    public void testMissValueUpdateBeforeReady() throws OutdatedTokenException {
        VersionedValue<Integer> longVersionedValue = new VersionedValue<>(REGISTER);

        longVersionedValue.set(0, TEST_VALUE);

        REGISTER.moveRevision.accept(0L);

        CompletableFuture<Integer> fut = longVersionedValue.get(1);

        assertFalse(fut.isDone());

        REGISTER.moveRevision.accept(1L);

        assertTrue(fut.isDone());

        assertEquals(TEST_VALUE, fut.join());

        assertSame(fut.join(), longVersionedValue.get(0).join());
    }

    /**
     * The test reads a value with the specific token in which the value should not be updated.
     * The read happens after the revision updated.
     *
     * @throws OutdatedTokenException If failed.
     */
    @Test
    public void testMissValueUpdate() throws OutdatedTokenException {
        VersionedValue<Integer> longVersionedValue = new VersionedValue<>(REGISTER);

        longVersionedValue.set(0, TEST_VALUE);

        REGISTER.moveRevision.accept(0L);
        REGISTER.moveRevision.accept(1L);

        CompletableFuture<Integer> fut = longVersionedValue.get(1);

        assertTrue(fut.isDone());

        assertEquals(TEST_VALUE, fut.join());

        assertSame(fut, longVersionedValue.get(0));
    }

    /**
     * Test checks token history size.
     */
    @Test
    public void testObsoleteToken() {
        VersionedValue<Integer> longVersionedValue = new VersionedValue<>(REGISTER);

        longVersionedValue.set(0, TEST_VALUE);

        REGISTER.moveRevision.accept(0L);

        longVersionedValue.set(1, TEST_VALUE);

        REGISTER.moveRevision.accept(1L);
        REGISTER.moveRevision.accept(2L);

        assertThrowsExactly(OutdatedTokenException.class, () -> longVersionedValue.get(0));
    }

    /**
     * Checks that future will be completed automatically when the related token becomes actual.
     */
    @Test
    public void testAutocompleteFuture() throws OutdatedTokenException {
        VersionedValue<Integer> longVersionedValue = new VersionedValue<>((b, r) -> {
        }, REGISTER);

        longVersionedValue.set(0, TEST_VALUE);

        REGISTER.moveRevision.accept(0L);

        CompletableFuture<Integer> fut = longVersionedValue.get(1);

        assertFalse(fut.isDone());

        REGISTER.moveRevision.accept(1L);
        REGISTER.moveRevision.accept(2L);

        assertTrue(fut.isDone());
        assertTrue(longVersionedValue.get(2).isDone());
    }

    /**
     * Checks that the update method work as expected when the previous value is known.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testUpdate() throws Exception {
        VersionedValue<Integer> longVersionedValue = new VersionedValue<>((b, r) -> {
        }, REGISTER);

        longVersionedValue.set(0, TEST_VALUE);

        REGISTER.moveRevision.accept(0L);

        CompletableFuture<Integer> fut = longVersionedValue.get(1);

        assertFalse(fut.isDone());

        longVersionedValue.update(1, previous -> ++previous, ex -> null);

        assertTrue(fut.isDone());

        assertEquals(TEST_VALUE + 1, fut.get());
    }

    /**
     * Checks that the update method work as expected when the previous value does not assign.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testUpdatePredefined() throws Exception {
        VersionedValue<Integer> longVersionedValue = new VersionedValue<>((b, r) -> {
        }, REGISTER);

        CompletableFuture<Integer> fut = longVersionedValue.get(0);

        assertFalse(fut.isDone());

        longVersionedValue.update(0, previous -> TEST_VALUE, ex -> null);

        assertTrue(fut.isDone());

        assertEquals(TEST_VALUE, fut.get());
    }

    /**
     * Test revision register.
     */
    private static class TestRevisionRegister implements Consumer<Consumer<Long>> {

        /** Revision consumer. */
        Consumer<Long> moveRevision;

        /** {@inheritDoc} */
        @Override
        public void accept(Consumer<Long> consumer) {
            moveRevision = consumer;
        }
    }
}