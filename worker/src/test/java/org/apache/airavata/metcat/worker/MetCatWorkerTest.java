/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.metcat.worker;

import org.apache.airavata.metcat.commons.MimeTypes;
import org.apache.airavata.metcat.commons.ParseMetadataRequest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class MetCatWorkerTest {
    private final static Logger logger = LoggerFactory.getLogger(MetCatWorkerTest.class);

    @Test
    public void testMetCatWorker() throws URISyntaxException {
        MetCatWorker metCatWorker = new MetCatWorker();
        ParseMetadataRequest parseMetadataRequest = new ParseMetadataRequest();
        HashMap<String, Object> ingestMetadata = new HashMap<>();
        ingestMetadata.put("experimentId", "test-000000-0000000-000000000-00000000000");
        parseMetadataRequest.setIngestMetadata(ingestMetadata);
        parseMetadataRequest.setMimeType(MimeTypes.APPLICATION_GAUSSIAN);
        parseMetadataRequest.setFileUri(new URI("file://"+MetCatWorkerTest.class.getResource("/Gaussian.log").getPath().toString()));
        metCatWorker.handle(parseMetadataRequest);
    }
}