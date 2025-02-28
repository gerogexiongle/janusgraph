// Copyright 2024 JanusGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.janusgraph.graphdb.inmemory;

import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.diskstorage.configuration.WriteConfiguration;
import org.janusgraph.graphdb.configuration.builder.GraphDatabaseConfigurationBuilder;
import org.janusgraph.graphdb.database.LazyLoadGraphTest;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class InMemoryLazyLoadGraphTest extends InMemoryGraphTest {

    @Override
    public void open(WriteConfiguration config) {
        graph = new LazyLoadGraphTest(new GraphDatabaseConfigurationBuilder().build(config));
        features = graph.getConfiguration().getStoreFeatures();
        tx = graph.buildTransaction().start();
        mgmt = graph.openManagement();
    }

    @Override @Test
    public void testPropertyIdAccessInDifferentTransaction() {
        JanusGraphVertex v1 = graph.addVertex();
        Object expectedId = v1.property("name", "foo").id();
        graph.tx().commit();

        VertexProperty p = getOnlyElement(v1.properties("name"));

        // access property id in new transaction
        graph.tx().commit();
        Exception exception = assertThrows(IllegalStateException.class, p::id);
        assertEquals(exception.getMessage(), "Any lazy load operation is not supported when transaction is already closed.");
    }

}
