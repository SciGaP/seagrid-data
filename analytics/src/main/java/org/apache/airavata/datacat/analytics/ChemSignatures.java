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
package org.apache.airavata.datacat.analytics;

import org.apache.airavata.datacat.analytics.input.chem.SGUtils;
import org.apache.airavata.datacat.analytics.input.chem.Sig2IDMapping;
import org.apache.airavata.datacat.analytics.input.chem.SignatureRecordDecision;
import org.apache.airavata.datacat.analytics.input.chem.SignatureRecordDecisionID;
import org.apache.airavata.datacat.analytics.util.AnalyticsConstants;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class ChemSignatures {
    private static String sparkMasterUrl = AnalyticsConstants.SPARK_MASTER_URL;
    private static String analyticsOutputDir = AnalyticsConstants.ANALYTICS_OUTPUT_DIR;
    private static String projectDir = AnalyticsConstants.PROJECT_DIR;

    public static void main(String[] args) throws InvalidSmilesException {
        SparkConf conf = new SparkConf()
                .setAppName("ChemSignature-Test")
                .setMaster(sparkMasterUrl);
        JavaSparkContext jsc = new JavaSparkContext(conf);
        jsc.addJar("file://" + projectDir + "/analytics/target/analytics-0.1-SNAPSHOT-jar-with-dependencies.jar");

        SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
        IAtomContainer mol1 = smilesParser.parseSmiles("C1=CC(=CC=C1NC(C)=O)O");
        IAtomContainer mol2 = smilesParser.parseSmiles("COc(c1)cccc1C#N");
        ArrayList<Tuple2<Double, IAtomContainer>> molsList = new ArrayList<>();
        molsList.add(new Tuple2(1.0, mol1));
        molsList.add(new Tuple2(0.0, mol2));
        JavaRDD<Tuple2<Double, IAtomContainer>> mols = jsc.parallelize(molsList);

        JavaRDD<SignatureRecordDecision> moleculesAfterSG  = mols.map(m -> SGUtils.atom2SigRecordDecision(m._2(), m._1(), 1, 3));
        Tuple2<JavaRDD<SignatureRecordDecisionID>, JavaRDD<Sig2IDMapping>> sig2ID = SGUtils.sig2ID(moleculesAfterSG);
        JavaRDD<LabeledPoint> resultAsLp = SGUtils.sig2LP(sig2ID._1());
        System.out.println(resultAsLp.count());
        // Use the labeledPoints to build a classifier of your own choice
        // ...

        // when you wish to test molecules, you will only care of the signatures that
        // is already in the "signature universe" of what you have previously seen
        // as you cannot gain anything by using signatures not used in the classifier
        IAtomContainer testMol1 = smilesParser.parseSmiles("Clc1ccc(cc1)C(c2ccccc2)N3CCN(CC3)CCOCC(=O)O");
        IAtomContainer testMol2 = smilesParser.parseSmiles("O=C(O)C(c1ccc(cc1)C(O)CCCN2CCC(CC2)C(O)(c3ccccc3)c4ccccc4)(C)C");
        JavaRDD<IAtomContainer> testMols = jsc.parallelize(Arrays.asList(testMol1,testMol2));
        JavaRDD<Vector> testLPs = SGUtils.atoms2Vectors(testMols, sig2ID._2(), 1, 3);
        System.out.println(testLPs.count());
        System.out.println("-------done------");
        // Use the previously created classifier to test these molecules.
        // ...
    }
}