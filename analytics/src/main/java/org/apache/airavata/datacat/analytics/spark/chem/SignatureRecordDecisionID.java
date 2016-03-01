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

import java.io.Serializable;

/**
 * SignatureRecordDecision_ID = (Decision/Regression-value, Map[SignatureID, #occurrences])
 * This corresponds to a molecule after Signature generation and make sense only in combination with
 * the Sig2ID_Mapping that give the translation SignatureID->Signature
 */
public class SignatureRecordDecisionID implements Serializable{
    private Double decision;
    private SignatureRecordID signatureRecordID;

    public SignatureRecordDecisionID(Double decision, SignatureRecordID signatureRecordID) {
        this.decision = decision;
        this.signatureRecordID = signatureRecordID;
    }

    public Double getDecision() {
        return decision;
    }

    public void setDecision(Double decision) {
        this.decision = decision;
    }

    public SignatureRecordID getSignatureRecordID() {
        return signatureRecordID;
    }

    public void setSignatureRecordID(SignatureRecordID signatureRecordID) {
        this.signatureRecordID = signatureRecordID;
    }
}