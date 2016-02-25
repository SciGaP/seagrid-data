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
package org.apache.airavata.datacat.worker.parsers.chem.gaussian;

import org.apache.airavata.datacat.worker.parsers.IParser;
import org.apache.airavata.datacat.worker.parsers.ParserException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class MainGaussianParser implements IParser {

    private final static Logger logger = LoggerFactory.getLogger(MainGaussianParser.class);

    private final String outputFileName = "gaussian-output.json";

    public final static int randomNum = (int) (Math.random() * 100000000);

    @SuppressWarnings("unchecked")
    public JSONObject parse(String dir, Map<String, Object> inputMetadata) throws Exception {
        try{
            if(!dir.endsWith(File.separator)){
                dir += File.separator;
            }
            String gaussianOutputFile = null;
            for(File file : (new File(dir).listFiles())){
                if(file.getName().endsWith(".out") || file.getName().endsWith(".log")){
                    gaussianOutputFile = file.getAbsolutePath();
                }
            }
            if(gaussianOutputFile == null){
                throw new Exception("Could not find the gaussian output file");
            }

            //FIXME Move the hardcoded script to some kind of configuration
            Process proc = Runtime.getRuntime().exec(
                    "docker run -t --env LD_LIBRARY_PATH=/usr/local/lib -v " +
                            dir +":/datacat/working-dir scnakandala/datacat-chem python" +
                            " /datacat/gaussian.py /datacat/working-dir/"
                    + (new File(gaussianOutputFile)).getName() +" /datacat/working-dir/" + outputFileName);


            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String s;
            // read any errors from the attempted command
            String error = "";
            while ((s = stdError.readLine()) != null) {
                error += s;
            }
            if(error == null || !error.isEmpty()){
                logger.warn(error);
            }

            File outputFile = new File(dir + outputFileName);
            if(outputFile.exists()){
                JSONObject temp = new JSONObject(new JSONTokener(new FileReader(dir + outputFileName)));

                inputMetadata.keySet().stream().forEach(key->{
                    temp.put(key, inputMetadata.get(key));
                });

                JSONObject finalObj = new JSONObject();
                finalObj.put("Id", inputMetadata.get("Id"));
                finalObj.put("ExperimentName", inputMetadata.get("ExperimentName"));
                finalObj.put("ProjectName", inputMetadata.get("ProjectName"));
                finalObj.put("Username", inputMetadata.get("Username"));
                finalObj.put("IndexedTime", System.currentTimeMillis());

                JSONObject temp2 = new JSONObject();
                if(temp.has("InChI"))
                    temp2.put("InChI", temp.get("InChI"));
                if(temp.has("InChIKey"))
                    temp2.put("InChIKey", temp.get("InChIKey"));
                if(temp.has("SMILES"))
                    temp2.put("SMILES", temp.get("SMILES"));
                if(temp.has("CanonicalSMILES"))
                    temp2.put("CanonicalSMILES", temp.get("CanonicalSMILES"));
                finalObj.put("Identifiers", temp2);

                temp2 = new JSONObject();
                if(temp.has("CodeVersion"))
                    temp2.put("Package", temp.get("CodeVersion"));
                if(temp.has("Method"))
                    temp2.put("Method", temp.get("Method"));
                if(temp.has("Keywords"))
                    temp2.put("Keywords", temp.get("Keywords"));
                if(temp.has("Basis"))
                    temp2.put("Basis", temp.get("Basis"));
                if(temp.has("CalcType"))
                    temp2.put("CalcType", temp.get("CalcType"));
                if(temp.has("NBasis"))
                    temp2.put("NBasis", temp.get("NBasis"));
                if(temp.has("JobStatus"))
                    temp2.put("JobStatus", temp.get("JobStatus"));
                finalObj.put("Calculation", temp2);

                temp2 = new JSONObject();
                if(temp.has("Formula"))
                    temp2.put("Formula", temp.get("Formula"));
                if(temp.has("NAtom"))
                    temp2.put("NAtom", temp.get("NAtom"));
                if(temp.has("Nmo"))
                    temp2.put("NMo", temp.get("Nmo"));
                if(temp.has("OrbSym"))
                    temp2.put("OrbSym", temp.get("OrbSym"));
                if(temp.has("NAtom"))
                    temp2.put("NAtom", temp.get("NAtom"));
                if(temp.has("Multiplicity"))
                    temp2.put("Multiplicity", temp.get("Multiplicity"));
                if(temp.has("Charge"))
                    temp2.put("Charge", temp.get("Charge"));
                if(temp.has("ElecSym"))
                    temp2.put("ElecSym", temp.get("ElecSym"));
                finalObj.put("Molecule", temp2);

                temp2 = new JSONObject();
                if(temp.has("Energy"))
                    temp2.put("Energy", temp.get("Energy"));
                if(temp.has("Dipole"))
                    temp2.put("Dipole", temp.get("Dipole"));
                if(temp.has("HF"))
                    temp2.put("HF", temp.get("HF"));
                if(temp.has("Homos"))
                    temp2.put("Homos", temp.get("Homos"));
                try{
                    Double[][] gradientValues = getDistributionValues(gaussianOutputFile);
                    if(gradientValues != null){
                        if(gradientValues[0] != null && gradientValues[0].length > 0)
                            temp2.put("Iterations", gradientValues[0]);
                        if(gradientValues[1] != null && gradientValues[1].length > 0)
                            temp2.put("MaximumGradientDistribution", gradientValues[1]);
                        if(gradientValues[2] != null && gradientValues[2].length > 0)
                            temp2.put("RMSGradientDistribution", gradientValues[2]);
                        if(gradientValues[3] != null && gradientValues[3].length > 0)
                            temp2.put("EnergyDistribution", gradientValues[3]);
                    }
                }catch (Exception ex){
                    logger.warn("Failed calculating Gradient Data :" + ex.getMessage());
                }
                finalObj.put("CalculatedProperties", temp2);

                temp2 = new JSONObject();
                if(temp.has("CalcMachine"))
                    temp2.put("CalcMachine", temp.get("CalcMachine"));
                if(temp.has("FinTime"))
                    temp2.put("FinTime", temp.get("FinTime"));
                if(temp.has("CalcBy"))
                    temp2.put("CalcBy", temp.get("CalcBy"));
                finalObj.put("ExecutionEnvironment", temp2);

                temp2 = new JSONObject();
                if(temp.has("SDF"))
                    temp2.put("SDF", temp.get("SDF"));
                if(temp.has("PDB"))
                    temp2.put("PDB", temp.get("PDB"));
                finalObj.put("FinalMoleculeStructuralFormats", temp2);

                temp2 = new JSONObject();
                File baseDir = new File(dir);
                for(File f : baseDir.listFiles()){
                    if(f.getName().endsWith(".out") || f.getName().endsWith(".log")){
                        temp2.put("GaussianOutputFile", f.getAbsolutePath());
                    }else if(f.getName().endsWith(".com") || f.getName().endsWith(".in")){
                        temp2.put("GaussianInputFile", f.getAbsolutePath());
                    }else if(f.getName().endsWith(".chk")){
                        temp2.put("GaussianCheckpointFile", f.getAbsolutePath());
                    }else if(f.getName().endsWith(".fchk")){
                        temp2.put("GaussianFCheckpointFile", f.getAbsolutePath());
                    }
                }
                if(temp.has("SDF")){
                    BufferedWriter writer = new BufferedWriter(new FileWriter(dir+File.separator+"structure.sdf"));
                    writer.write(temp.get("SDF").toString());
                    writer.close();
                    temp2.put("SDFStructureFile", dir+File.separator+"structure.sdf");
                }
                if(temp.has("PDB")){
                    BufferedWriter writer = new BufferedWriter(new FileWriter(dir+File.separator+"structure.pdb"));
                    writer.write(temp.get("PDB").toString());
                    writer.close();
                    temp2.put("PDBStructureFile", dir+File.separator+"structure.pdb");
                }
                if(temp.has("InChI")){
                    BufferedWriter writer = new BufferedWriter(new FileWriter(dir+File.separator+"inchi.txt"));
                    writer.write(temp.get("InChI").toString());
                    writer.close();
                    temp2.put("InChIFile", dir+File.separator+"inchi.txt");
                }
                if(temp.has("SMILES")){
                    BufferedWriter writer = new BufferedWriter(new FileWriter(dir+File.separator+"smiles.txt"));
                    writer.write(temp.get("SMILES").toString());
                    writer.close();
                    temp2.put("SMILESFile", dir+File.separator+"smiles.txt");
                }

                finalObj.put("Files", temp2);

                return finalObj;
            }

            throw new Exception("Could not parse data");
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new ParserException(ex);
        }
    }

    private Double[][] getDistributionValues(String gaussianOutputFile) throws Exception{
        //First find the method
        MethodParser pp = new MethodParser(new MethodLexer(new FileReader(gaussianOutputFile)));
        pp.parse();

        // then find the wavefunction
        WavefunctionParser pp11 = new WavefunctionParser(new WavefunctionLexer(new FileReader(gaussianOutputFile)));
        pp11.parse();

        // concatenate runtyp1  and runtype2 into runtype
        InputStream mylist1a = new FileInputStream(System.getProperty("java.io.tmpdir")
                + File.separator + MainGaussianParser.randomNum +"runtype1");
        InputStream mylist2a = new FileInputStream(System.getProperty("java.io.tmpdir")
                + File.separator + MainGaussianParser.randomNum +"runtype2");
        SequenceInputStream str4a = new SequenceInputStream(mylist1a, mylist2a);
        PrintStream temp4a = new PrintStream(new FileOutputStream(System.getProperty("java.io.tmpdir")
                + File.separator + MainGaussianParser.randomNum +"runtype"));

        int ccc1;
        while ((ccc1 = str4a.read()) != -1)
            temp4a.write(ccc1);

        // read the runtype file
        FileInputStream fis = new FileInputStream(System.getProperty("java.io.tmpdir")
                + File.separator + MainGaussianParser.randomNum +"runtype");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
        String record = dis.readLine();
        String record1 = String.valueOf(new char[]{'o', 'p', 't', 'R', 'H', 'F'});
        String record1a = String.valueOf(new char[]{'o', 'p', 't', 'B', '3', 'L', 'Y', 'P'});
        String record1b = String.valueOf(new char[]{'o', 'p', 't', 'c', 'a', 's','s', 'c', 'f'});
        String record1c = String.valueOf(new char[]{'o', 'p', 't', 'c', 'c', 's','d'});
        String record1d = String.valueOf(new char[]{'s', 'c', 'f', 'R', 'H', 'F'});
        String record1e = String.valueOf(new char[]{'o', 'p', 't', 'B', '3', 'P', 'W', '9', '1'});
        String record1f = String.valueOf(new char[]{'o', 'p', 't', 'B', '1', 'B', '9', '5'});
        String record3 = String.valueOf(new char[]{'h', 'f', 'o', 'p', 't'});
        String record2 = String.valueOf(new char[]{'o', 'p', 't', 'M', 'P', '2'});
        String record4 = String.valueOf(new char[]{'G', '1', 'g', 'e', 'o', 'm'});

        String record5 = String.valueOf(new char[]{'C', 'B', 'S', '-', 'Q', 'g', 'e', 'o', 'm'});

        //this is for SCF, B3LYP, B3PW91(?), MP2, CASSCF Optimization
        if (record1.equals(record) || record1a.equals(record) || record1b.equals(record)
                || record1c.equals(record) || record1e.equals(record) || record1f.equals(record)) {
            GOPTLexer scanner = new GOPTLexer(new java.io.FileReader(gaussianOutputFile));
            GOPTParser goptParser = new GOPTParser(scanner);
            goptParser.init_actions();
            goptParser.parse();

            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir") + File.separator
                    + MainGaussianParser.randomNum+"temporary2"));
            String temp = reader.readLine();
            while(!temp.startsWith("DataSet:")){
                temp = reader.readLine();
            }
            ArrayList<Double> values = new ArrayList();
            temp = reader.readLine();
            while(temp != null && !temp.isEmpty()){
                values.add(Double.parseDouble(temp.split(",")[1].trim()));
                temp = reader.readLine();
            }

            Double[][] returnArr = new Double[4][];
            returnArr[0] = new Double[values.size()];
            for(double d=1; d<=values.size();d++){
                returnArr[0][(int)d-1] = d;
            }
            returnArr[1] = values.toArray(new Double[values.size()]);

            reader = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir") + File.separator
                    + MainGaussianParser.randomNum+"temporary3"));
            temp = reader.readLine();
            while(!temp.startsWith("DataSet:")){
                temp = reader.readLine();
            }
            values = new ArrayList();
            temp = reader.readLine();
            while(temp != null && !temp.isEmpty()){
                values.add(Double.parseDouble(temp.split(",")[1].trim()));
                temp = reader.readLine();
            }
            returnArr[2] = values.toArray(new Double[values.size()]);

            reader = new BufferedReader(new FileReader(System.getProperty("java.io.tmpdir") + File.separator
                    + MainGaussianParser.randomNum+"Energy_data"));
            temp = reader.readLine();
            while(!temp.startsWith("DataSet:")){
                temp = reader.readLine();
            }
            values = new ArrayList();
            temp = reader.readLine();
            while(temp != null && !temp.isEmpty()){
                if(temp.split(",").length == 2) {
                    try{
                        values.add(Double.parseDouble(temp.split(",")[1].trim()));
                    }catch (NumberFormatException ex){
                        logger.warn(ex.getMessage());
                    }
                }
                temp = reader.readLine();
            }
            returnArr[3] = values.toArray(new Double[values.size()]);

            return returnArr;
        }

        return null;
    }


    public static void main(String[] args) throws Exception {
        MainGaussianParser mainGaussianParser = new MainGaussianParser();
        mainGaussianParser.getDistributionValues("/Users/supun/Downloads/default_job.out");
//        mainGaussianParser.getDistributionValues("/Users/supun/ns1.out");
    }
}