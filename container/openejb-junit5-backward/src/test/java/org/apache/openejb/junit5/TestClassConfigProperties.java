/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.junit5;

import org.apache.openejb.junit.ContextConfig;
import org.apache.openejb.junit.Property;
import org.apache.openejb.junit.TestResource;
import org.apache.openejb.junit.TestResourceTypes;
import org.junit.jupiter.api.Test;

import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWithOpenEjb
@ContextConfig(
    properties = {
        @Property("java.naming.factory.initial=org.apache.openejb.core.LocalInitialContextFactory"),
        @Property("junit.test-property=Test String from Properties"),
        @Property("junit.test-empty-property="),
        @Property("junit.test-null-property"),
        @Property(" junit.test-trim-empty-property-key = "),
        @Property(" junit.test-trim-null-property-key "),
        @Property(" junit.test-trim-property-key-and-value = trimmed value ")
    }
)
public class TestClassConfigProperties {
    @TestResource(TestResourceTypes.CONTEXT_CONFIG)
    private Hashtable<String, String> contextConfig;

    public TestClassConfigProperties() {
    }

    @Test
    public void testConfig() {
        assertNotNull(contextConfig);

        checkProperty("junit.test-property", "Test String from Properties");
        checkProperty("junit.test-empty-property", "");
        checkProperty("junit.test-null-property", "");
        checkProperty("junit.test-trim-empty-property-key", "");
        checkProperty("junit.test-trim-null-property-key", "");
        checkProperty("junit.test-trim-property-key-and-value", "trimmed value");
    }

    private void checkProperty(final String key, final String expected) {
        final String value = contextConfig.get(key);
        assertEquals(expected, value);
    }
}
