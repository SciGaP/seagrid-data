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
package org.apache.airavata.datacat.analytics.input.chem;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.AtomSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;

public class SGUtils {
    private final static Logger logger = LoggerFactory.getLogger(SGUtils.class);

    /**
     * atoms2Vectors take an RDD of molecules (without any outcome class/regression value) and
     * turns each molecule into a Vector, using the given signatureUniverse.
     */
    public static JavaRDD<Vector> atoms2Vectors(JavaRDD<IAtomContainer> molecules, JavaRDD<Sig2IDMapping> signatureUniverse,
                                         Integer hStart, Integer hStop){
        return atoms2VectorsCarryData(molecules.mapToPair(mol->new Tuple2<>(null, mol)), signatureUniverse, hStart, hStop)
                .map(r -> r._2());
    }

    /**
     * atoms2VectorsCarryData allows the user to send data together with each molecule
     */
    public static <T> JavaRDD<Tuple2<T,Vector>> atoms2VectorsCarryData(JavaPairRDD<T,
            IAtomContainer> molecules,JavaRDD<Sig2IDMapping> signatureUniverse , final Integer hStart, final Integer hStop){
        //sigRecordsFakeDecision: RDD[(T, (Double, SignatureRecord))] = null
        JavaPairRDD<T, Tuple2<Double,SignatureRecord>> sigRecordsFakeDecision = molecules.mapToPair(
                mol -> new Tuple2<>(mol._1(), new Tuple2(0.0, atom2SigRecord(mol._2(), hStart, hStop)))
        );

        JavaPairRDD<T, SignatureRecordID> rddWithFutureVectors = getFeatureVectorsCarryData(sigRecordsFakeDecision.mapToPair(r -> new Tuple2<>(r._1(),
                new SignatureRecordDecision(r._2()._1(),r._2()._2()))), signatureUniverse).mapToPair(r -> new Tuple2<>(r._1(), r._2().getSignatureRecordID()));

        //Find the biggest d (dimension)
        Long d=0L;
        try{
            d = signatureUniverse.map(s->s.getValue()).max((o1,o2)->(int)(o1 - o2)) + 1;
        }catch (Exception ex){
            logger.warn("Sign-mapping was empty! (just use 0)");
        }
        return sig2VectorsCarryData(rddWithFutureVectors, d);
    }

    /**
     * atom2SigRecordDecision computes signatures for a molecule, for usecase where you wish to store the class/regression-value
     * together with the molecule itself.
     * @param molecule      The molecule stored as an IAtomContainer
     * @param decisionClass The decision-class or regression value of this molecule
     * @param hStart       The starting height of signature generation
     * @param hStop        The stopping height of signature generation
     * @return              SignatureRecordDecision = (Decision-class, Map[Signature, Num occurrences])
     */
    public static SignatureRecordDecision atom2SigRecordDecision(IAtomContainer molecule, Double decisionClass, Integer hStart,
                                                          Integer hStop) throws SignatureGenException {
        return new SignatureRecordDecision(decisionClass, atom2SigRecord(molecule, hStart, hStop));
    }

    /**
     * atom2SigRecord computes signatures for a molecule, used when class/regression-info doesn't need to be sent with the molecule.
     * @param molecule      The molecule stored as an IAtomContainer
     * @param hStart       The starting height of signature generation
     * @param hStop        The stopping height of signature generation
     * @return              SignatureRecord = Map[Signature, Num occurrences]
     */
    public static SignatureRecord atom2SigRecord(IAtomContainer molecule, Integer hStart, Integer hStop) throws SignatureGenException {
        try{
            SignatureRecord signatureRecord = new SignatureRecord();
            Integer hStopNew = Math.min(molecule.getAtomCount() -1, hStop);
            for(IAtom atom : molecule.atoms()){
                for(int height = hStart; height <= hStopNew; height++){
                    AtomSignature atomSign = new AtomSignature(molecule.getAtomNumber(atom), height, molecule);
                    String canonicalSign = atomSign.toCanonicalString();
                    if(signatureRecord.get(canonicalSign) == null){
                        signatureRecord.put(canonicalSign, 1);
                    }else{
                        signatureRecord.put(canonicalSign, 1 + signatureRecord.get(canonicalSign));
                    }
                }
            }
            return signatureRecord;
        }catch (Exception ex){
            throw new SignatureGenException("Unknown exception occured (in 'atom2SigRecord'), exception was: " + ex);
        }
    }

    /**
     * Take the rdd_input (your data-records) and the unique mapping of Signature->ID
     * Transform data-records to use the Signature->ID mapping. For molecules that has no
     * signatures in the signatureUniverise will be removed from the output!
     * @param rdd              The RDD containing the data using Signatures
     * @param uniqueSignatures The unique signatures (mapping Signature->ID)
     * @return    RDD[SignatureRecordDecision_ID] (Value/Class, Map[Signature_ID, #occurrences])
     */
    public  static <T> JavaPairRDD<T, SignatureRecordDecisionID> getFeatureVectorsCarryData(JavaPairRDD<T,
            SignatureRecordDecision> rdd, JavaRDD<Sig2IDMapping> uniqueSignatures){
        // Add a unique ID to each record:
        // So here we store (T, (Double, Map[String, Int])) -> ID
        JavaPairRDD<Tuple2<T, SignatureRecordDecision>, Long> recWithId = rdd.zipWithUniqueId();

        // Sig2ID_Mapping = (String, Long)
        //((Double, Map[String, Int]), Long) = (SignatureRecordDecision, Long)
        // transform to: (String, (Double, Int, Long)) format for joining them!

        // Expand the Maps
        // result: (signature, (#of occurrences of this signature, MoleculeID))
        JavaPairRDD<String, Tuple2<Integer, Long>> expandedRdd = recWithId.flatMapToPair(rec -> {
            ArrayList<Tuple2<String, Tuple2<Integer, Long>>> list = new ArrayList<>();
            rec._1()._2().getSignatureRecord().entrySet().stream().forEach(sig -> list.add(new Tuple2<>(sig.getKey(),
                    new Tuple2<>(sig.getValue(), rec._2()))));
            return list;
        });

        // combine the unique_ID with expanded mapping
        // ((signature, ((#occurrences, MoleculeID), Signature_ID))
        JavaPairRDD<String, Tuple2<Tuple2<Integer, Long>, Long>> joined = expandedRdd.join(uniqueSignatures
                .mapToPair(uniqSig -> new Tuple2<>(uniqSig.getKey(), uniqSig.getValue())));

        // (MoleculeID, (Signature_ID, #occurrences))
        JavaPairRDD<Long, Tuple2<Long, Integer>> resRdd
                = joined.mapToPair(tuple -> new Tuple2<>(tuple._2()._1()._2(), new Tuple2<>(tuple._2()._2(), tuple._2()._1()._1())));

        //(MoleculeID, SignatureRecordId = Map(Signature_ID, #occurrences))
        JavaPairRDD<Long, SignatureRecordID> result = resRdd.aggregateByKey(new SignatureRecordID(), (signatureRecordID, longIntegerTuple2) -> {
            signatureRecordID.put(longIntegerTuple2._1(), longIntegerTuple2._2());
            return signatureRecordID;
        }, (signatureRecordID, signatureRecordID2) -> {
            signatureRecordID.putAll(signatureRecordID2);
            return signatureRecordID;
        });

        //(Double, Map[Long, Int]);
        // add the carry and decision/regression value
        return recWithId.mapToPair(r->new Tuple2<>(r._2(), new Tuple2<>(r._1()._1(), r._1()._2().getDecision()))).join(result)
                .mapToPair(r-> new Tuple2<>(r._2()._1()._1(), new SignatureRecordDecisionID(r._2()._1()._2(),r._2()._2())));
    }

    /**
     * sig2Vectors - including carryData
     */
    public static <T> JavaRDD<Tuple2<T,Vector>> sig2VectorsCarryData(JavaPairRDD<T, SignatureRecordID> in, Long dim){
        SparkContext sc = in.context();
        JavaSparkContext jsc = new JavaSparkContext(sc);
        if(in.isEmpty()) {
            return jsc.emptyRDD();
        }
        JavaPairRDD<T,Tuple2<Long[],Integer[]>> sparseVectors = in.mapToPair(r -> {
            int arrLength = r._2().size();
            Long[] keyArr = new Long[arrLength];
            Integer[] valueArr = new Integer[arrLength];

            // construct arrays for the signatures
            SortedMap<Long, Integer> sortedMap = new TreeMap<>();
            sortedMap.putAll(r._2());
            Map.Entry<Long, Integer>[] entries = sortedMap.entrySet().toArray(new Map.Entry[sortedMap.size()]);
            for (int i = 0; i < entries.length; i++) {
                keyArr[i] = entries[i].getKey();
                valueArr[i] = entries[i].getValue();
            }
            return new Tuple2<>(r._1(), new Tuple2<>(keyArr, valueArr));
        });
        // Determine number of features.
        long d = 0;
        if(dim>0){
            d = dim;
        }else{
            d = sparseVectors.map(r->{
                if(r._2()._1().length==0){
                    return 0;
                }else{
                    Optional<Long> result = Arrays.stream(r._2()._1()).max((o1, o2) -> (int)(o1-o2));
                    return result.get();
                }
            }).reduce((number1, number2) -> Math.max(number1.longValue(), number2.longValue())).longValue() + 1;
        }
        final int dFinal = (int)d;
        return sparseVectors.map(r -> {
            return new Tuple2<>(r._1(), new SparseVector(dFinal,
                    Arrays.stream(r._2()._1()).mapToInt(l -> (int) l.longValue()).toArray(),
                    Arrays.stream(r._2()._2()).mapToDouble(value -> (double) value).toArray()));
        });
    }

    /**
     * sig2ID is for transforming an RDD with SignatureRecordDecision-records into the universe of signatures
     * mapped into corresponding ID's and the records transformed into using the ID's instead. The result is the tuple:
     * (RDD[(T, SignatureRecordDecision_ID)], RDD[Sig2ID_Mapping]), where the first element is the SignatureRecordDecision now
     * encoded with ID's instead of signatures. The second element is the universe of signatures used.
     *
     * @param rdd     The RDD of SignatureRecordDecision-records
     * @return        The tuple of (records using ID, ID_mapping)
     */
    public static Tuple2<JavaRDD<SignatureRecordDecisionID>, JavaRDD<Sig2IDMapping>> sig2ID(JavaRDD<SignatureRecordDecision> rdd){
        // Get the unique Signature -> ID mapping
        JavaRDD<Sig2IDMapping> sig2IDMapping = rdd.flatMap(signatureRecordDecision -> signatureRecordDecision.getSignatureRecord()
                .keySet()).zipWithUniqueId().map(r->new Sig2IDMapping(r._1(),r._2()));
        return new Tuple2<>(getFeatureVectors(rdd,sig2IDMapping), sig2IDMapping);
    }

    /**
     * Take the rdd_input (your data-records) and the unique mapping of Signature->ID
     * Transform data-records to use the Signature->ID mapping
     * @param rdd              The RDD containing the data using Signatures
     * @param uniqueSignatures The unique signatures (mapping Signature->ID)
     * @return    RDD[SignatureRecordDecision_ID] (Value/Class, Map[Signature_ID, #occurrences]
     */
    public static JavaRDD<SignatureRecordDecisionID> getFeatureVectors(JavaRDD<SignatureRecordDecision> rdd,
                                                                       JavaRDD<Sig2IDMapping> uniqueSignatures){
        return getFeatureVectorsCarryData(rdd.mapToPair(r->new Tuple2<>(null,r)), uniqueSignatures).map(r->r._2());
    }

    /**
     * Transfers records with IDs into LabeledPoint-objects to allow machine learning used on them
     *
     * @param in     The RDD[SignatureRecordDecision_ID] with data
     * @param dim
     * @return      RDD[LabeledPoint]
     */
    public static <T> JavaRDD<Tuple2<T,LabeledPoint>> sig2LPCarryData(JavaRDD<Tuple2<T,SignatureRecordDecisionID>> in, Long dim){
        SparkContext sc = in.context();
        JavaSparkContext jsc = new JavaSparkContext(sc);
        if(in.isEmpty()) {
            return jsc.emptyRDD();
        }

        JavaPairRDD<T,Tuple2<Double,Tuple2<Long[],Integer[]>>> parsedData = in.mapToPair(r -> {
            double label = r._2().getDecision();
            int arrLength = r._2().getSignatureRecordID().size();
            Long[] keyArr = new Long[arrLength];
            Integer[] valueArr = new Integer[arrLength];

            // construct arrays for the signatures
            SortedMap<Long, Integer> sortedMap = new TreeMap<>();
            sortedMap.putAll(r._2().getSignatureRecordID());
            Map.Entry<Long, Integer>[] entries = sortedMap.entrySet().toArray(new Map.Entry[sortedMap.size()]);
            for (int i = 0; i < entries.length; i++) {
                keyArr[i] = entries[i].getKey();
                valueArr[i] = entries[i].getValue();
            }
            return new Tuple2<>(r._1(), new Tuple2<>(label,new Tuple2<>(keyArr, valueArr)));
        });

        // Determine number of features.
        long d = 0;
        if(dim>0){
            d = dim;
        }else{
            d = parsedData.map(r->{
                if(r._2()._2()._1().length==0){
                    return 0;
                }else{
                    Optional<Long> result = Arrays.stream(r._2()._2()._1()).max((o1, o2) -> (int) (o1 - o2));
                    return result.get();
                }
            }).reduce((number1, number2) -> Math.max(number1.longValue(), number2.longValue())).longValue() + 1;
        }
        final int dFinal = (int)d;
        return parsedData.map(r -> {
            return new Tuple2<>(r._1(), new LabeledPoint(r._2()._1(), new SparseVector(dFinal,
                    Arrays.stream(r._2()._2()._1()).mapToInt(l -> (int) l.longValue()).toArray(),
                    Arrays.stream(r._2()._2()._2()).mapToDouble(value -> (double) value).toArray())));
        });
    }

    /**
     * Transfers records with IDs into LabeledPoint-objects to allow machine learning on them
     * @param in    The RDD[SignatureRecordDecision_ID] with data
     * @param dim
     * @return   RDD[LabeledPoint]
     */
    public static JavaRDD<LabeledPoint> sig2LP(JavaRDD<SignatureRecordDecisionID> in, Long dim){
        return sig2LPCarryData(in.map(r->new Tuple2<>(Void.class, r)), dim).map(r->r._2());
    }

    /**
     * Transfers records with IDs into LabeledPoint-objects to allow machine learning on them
     * @param in    The RDD[SignatureRecordDecision_ID] with data
     * @return   RDD[LabeledPoint]
     */
    public static JavaRDD<LabeledPoint> sig2LP(JavaRDD<SignatureRecordDecisionID> in){
        return sig2LP(in, 0L);
    }
}