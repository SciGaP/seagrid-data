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
package org.apache.airavata.metcat.commons;

import java.net.URI;
import java.util.HashMap;

public class ParseMetadataRequest {
    private HashMap<String, Object> ingestMetadata;
    private URI fileUri;
    private String mimeType;

    public HashMap<String, Object> getIngestMetadata() {
        return ingestMetadata;
    }

    public void setIngestMetadata(HashMap<String, Object> ingestMetadata) {
        this.ingestMetadata = ingestMetadata;
    }

    public URI getFileUri() {
        return fileUri;
    }

    public void setFileUri(URI fileUri) {
        this.fileUri = fileUri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}