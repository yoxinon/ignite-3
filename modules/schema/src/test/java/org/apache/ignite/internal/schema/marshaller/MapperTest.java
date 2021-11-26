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

package org.apache.ignite.internal.schema.marshaller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.function.Function;
import org.apache.ignite.internal.schema.testobjects.TestOuterObject;
import org.apache.ignite.internal.schema.testobjects.TestOuterObject.NestedObject;
import org.apache.ignite.table.mapper.Mapper;
import org.apache.ignite.table.mapper.MapperBuilder;
import org.junit.jupiter.api.Test;

/**
 * Columns mappers test.
 */
public class MapperTest {

    @Test
    public void misleadingMapperUsage() {
        // Empty mapping.
        assertThrows(IllegalStateException.class, () -> Mapper.buildFrom(TestObject.class).build());

        // Many fields to one column.
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(TestObject.class)
                .map("id", "key")
                .map("longCol", "key"));

        // One field to many columns.
        assertThrows(IllegalStateException.class, () -> Mapper.buildFrom(TestObject.class)
                .map("id", "key")
                .map("id", "val1")
                .map("stringCol", "val2")
                .build());

        // Mapper builder reuse fails.
        assertThrows(IllegalStateException.class, () -> {
            MapperBuilder<TestObject> builder = Mapper.buildFrom(TestObject.class)
                    .map("id", "key");

            builder.build();

            builder.map("stringCol", "val2");
        });
    }

    @Test
    public void supportedClassKinds() {
        class LocalClass {
            long id;
        }

        Function anonymous = (i) -> i;

        Mapper.buildFrom(TestOuterObject.class);
        Mapper.buildFrom(NestedObject.class);

        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(Long.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(TestOuterObject.InnerObject.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(AbstractTestObject.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(LocalClass.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(anonymous.getClass()));
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(int[].class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(Object[].class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(TestInterface.class)); // Interface
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(TestAnnotation.class)); // annotation
        assertThrows(IllegalArgumentException.class, () -> Mapper.buildFrom(EnumTestObject.class)); // enum

        Mapper.of(Long.class);
        Mapper.of(TestOuterObject.class);
        Mapper.of(NestedObject.class);
        Mapper.of(ArrayList.class);

        assertThrows(IllegalArgumentException.class, () -> Mapper.of(TestOuterObject.InnerObject.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of(LocalClass.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of(AbstractTestObject.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of(anonymous.getClass()));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of(int[].class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of(Object[].class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of(TestInterface.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of(TestAnnotation.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of(EnumTestObject.class));

        Mapper.of("column", Long.class);
        Mapper.of("column", TestOuterObject.class);
        Mapper.of("column", NestedObject.class);
        Mapper.of("column", AbstractTestObject.class);
        Mapper.of("column", int[].class);
        Mapper.of("column", Object.class);
        Mapper.of("column", ArrayList.class);
        Mapper.of("column", TestInterface.class);

        assertThrows(IllegalArgumentException.class, () -> Mapper.of("column", TestOuterObject.InnerObject.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of("column", LocalClass.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of("column", anonymous.getClass()));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of("column", TestAnnotation.class));
        assertThrows(IllegalArgumentException.class, () -> Mapper.of("column", EnumTestObject.class));

    }

    @Test
    public void identityMapping() {
        Mapper<TestObject> mapper = Mapper.of(TestObject.class);

        assertNull(mapper.mappedColumn());
        assertEquals("id", mapper.mappedField("id"));
        assertNull(mapper.mappedField("val"));
    }

    @Test
    public void basicMapping() {
        Mapper<TestObject> mapper = Mapper.of(TestObject.class);

        assertNull(mapper.mappedColumn());
        assertEquals("id", mapper.mappedField("id"));
        assertNull(mapper.mappedField("val"));
    }

    /**
     * Test object.
     */
    @SuppressWarnings({"InstanceVariableMayNotBeInitialized", "unused"})
    static class TestObject {
        private long id;

        private long longCol;

        private String stringCol;
    }

    /**
     * Test object.
     */
    @SuppressWarnings({"InstanceVariableMayNotBeInitialized", "unused"})
    static abstract class AbstractTestObject {
        private long id;
    }

    /**
     * Test object.
     */
    enum EnumTestObject {
        ONE,
        TWO
    }

    /**
     * Test object.
     */
    @interface TestAnnotation {
        long id = 0L;
    }

    /**
     * Test object.
     */
    interface TestInterface {
        int id = 0;
    }
}
