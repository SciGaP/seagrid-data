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
package org.apache.airavata.datacat.analytics.spark.chem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * SignatureRecordDecision = (Decision/Regression-value, Map[Signature, #occurrences]
 */
public class SignatureRecordDecision implements Serializable{
    private final static Logger logger = LoggerFactory.getLogger(SignatureRecordDecision.class);
    private Double decision;
    private SignatureRecord signatureRecord;

    public Double getDecision() {
        return decision;
    }

    public void setDecision(Double decision) {
        this.decision = decision;
    }

    public SignatureRecord getSignatureRecord() {
        return signatureRecord;
    }

    public void setSignatureRecord(SignatureRecord signatureRecord) {
        this.signatureRecord = signatureRecord;
    }

    public SignatureRecordDecision(Double decision, SignatureRecord signatureRecord) {

        this.decision = decision;
        this.signatureRecord = signatureRecord;
    }
}