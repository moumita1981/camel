/**
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
package org.apache.camel.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class RestSwaggerReaderTest extends CamelTestSupport {

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("dummy-rest", new DummyRestConsumerFactory());
        return jndi;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                rest("/hello")
                        .get("/hi").to("log:hi")
                        .get("/bye").to("log:bye")
                        .post("/bye").to("log:bye");
            }
        };
    }

    @Test
    public void testReaderRead() throws Exception {
        RestDefinition rest = context.getRestDefinitions().get(0);
        assertNotNull(rest);

        BeanConfig config = new BeanConfig();
        config.setBasePath("http://localhost:8080/api");
        RestSwaggerReader reader = new RestSwaggerReader();

        Swagger swagger = reader.read(rest);
        assertNotNull(swagger);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(swagger);

        log.info(json);

        assertTrue(json.contains("\"basePath\":\"http://localhost:8080/api\""));
        assertTrue(json.contains("\"resourcePath\":\"/hello\""));
        assertTrue(json.contains("\"method\":\"GET\""));
        assertTrue(json.contains("\"nickname\":\"getHelloHi\""));

        context.stop();
    }

}
