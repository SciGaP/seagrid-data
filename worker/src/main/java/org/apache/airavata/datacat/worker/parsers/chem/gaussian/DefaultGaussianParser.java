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
import org.apache.airavata.datacat.worker.parsers.chem.gaussian.old.*;
import org.apache.airavata.datacat.worker.util.ParserUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class DefaultGaussianParser implements IParser {

    private final static Logger logger = LoggerFactory.getLogger(DefaultGaussianParser.class);

    private final String outputFileName = "gaussian-output.json";

    public final static int randomNum = (int) (Math.random() * 100000000);


    //FIXME We Should Develop a Proper Parsing Library(Ideally Using Java)
    @SuppressWarnings("unchecked")
    public JSONObject parse(JSONObject finalObj, String dir, Map<String, Object> inputMetadata) throws Exception {
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
                logger.warn("Could not find the gaussian output file");
                return finalObj;
            }
            File outputFile = null;

            logger.info("Running the docker based parser to parse directory " + dir);
            String dockerCommand = "docker run -t --security-opt label:disable --rm=true --env LD_LIBRARY_PATH=/usr/local/lib -v " +
                    dir +":/datacat/working-dir scnakandala/datacat-chem python" +
                    " /datacat/gaussian.py /datacat/working-dir/"
                    + (new File(gaussianOutputFile)).getName() +" /datacat/working-dir/" + outputFileName;
                //FIXME Move the hardcoded script to some kind of configuration
            logger.info("Docker command : " + dockerCommand);
            Process proc = Runtime.getRuntime().exec(dockerCommand);
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String s;
            // read any errors from the attempted command
            String error = "";
            while ((s = stdError.readLine()) != null) {
                error += s;
            }
            if(error == null || !error.isEmpty()){
                logger.warn("Error running docker command "  + error);
            }

            outputFile = new File(dir + outputFileName);

            if(outputFile!=null && outputFile.exists()) {
                JSONObject temp = new JSONObject(new JSONTokener(new FileReader(dir + outputFileName)));

                inputMetadata.keySet().stream().forEach(key->{
                    temp.put(key, inputMetadata.get(key));
                });

                finalObj.put("Id", inputMetadata.get("Id"));
                finalObj.put("ExperimentName", inputMetadata.get("ExperimentName"));
                finalObj.put("ProjectName", inputMetadata.get("ProjectName"));
                finalObj.put("Username", ((String)inputMetadata.get("Username")).toLowerCase());
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
                if(temp.has("CalcType"))
                    temp2.put("CalcType", temp.get("CalcType"));
                if(temp.has("Methods"))
                    temp2.put("Methods", temp.get("Methods"));
                if(temp.has("Basis")) {
                    if(temp.get("Basis").toString().contains(";")){
                        temp2.put("Basis", temp.get("Basis").toString().split(";")[0]);
                    }else{
                        temp2.put("Basis", temp.get("Basis"));
                    }
                }
                if(temp.has("Nmo"))
                    temp2.put("NMO", temp.get("Nmo"));
                if(temp.has("Keywords"))
                    temp2.put("Keywords", temp.get("Keywords"));
                if(temp.has("NBasis"))
                    temp2.put("NBasis", temp.get("NBasis"));
                if(temp.has("JobStatus")) {
                    if(temp.get("JobStatus").toString().contains(";")){
                        temp2.put("JobStatus", temp.get("JobStatus").toString().split(";")[0]);
                    }else{
                        temp2.put("JobStatus", temp.get("JobStatus"));
                    }
                }
                if(temp.has("MoEnergies"))
                    temp2.put("MoEnergies", temp.get("MoEnergies"));
                finalObj.put("Calculation", temp2);

                temp2 = new JSONObject();
                if(temp.has("Formula"))
                    temp2.put("Formula", temp.get("Formula"));
                if(temp.has("NAtom"))
                    temp2.put("NAtom", temp.get("NAtom"));
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
                if(temp.has("Homos")) {
                    String homos = (String) temp.getJSONArray("Homos").get(0);
                    temp2.put("Homos", homos);

                    if (temp.has("MoEnergies")) {
                        int homosInt = Integer.parseInt(homos);
                        JSONArray moEnergiesAll = temp.getJSONArray("MoEnergies");
                        JSONArray moEnergies = moEnergiesAll.getJSONArray(0);
                        if (moEnergies.length() >= homosInt) {
                            temp2.put("Homo Eigenvalue", moEnergies.get(homosInt - 1));
                        }

                        if (homosInt > 9 && moEnergies.length() >= homosInt) {
                            String homoEigenvalues = "";
                            for (int i = 1; i <= 9; i++) {
                                homoEigenvalues = "Homo - " + i + " : " + moEnergies.getString(homosInt - 1 - i) + " ";
                            }
                            temp2.put("Homo Eigenvalue", homoEigenvalues);
                        }
                        if (homosInt + 9 < moEnergies.length()) {
                            String homoEigenvalues = "";
                            for (int i = 1; i <= 9; i++) {
                                homoEigenvalues = "Lumo + " + i + " : " + moEnergies.getString(homosInt - 1 + i) + " ";
                            }
                            temp2.put("Lumo Eigenvalue", homoEigenvalues);
                        }
                    }
                }
                if(temp.has("ZPE"))
                    temp2.put("Zero Point Energy", temp.get("ZPE"));
                if(temp.has("NImag"))
                    temp2.put("NImag", temp.get("NImag"));
                
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
                if(temp.has("CalcMachine")) {
                    if(temp.get("CalcMachine").toString().contains(";")){
                        String line = temp.get("CalcMachine").toString().split(";")[0];
                        String[] bits =  line.split("-");
                        if(bits.length == 4){
                            temp2.put("CalcMachine", bits[1]);
                        }else{
                            temp2.put("CalcMachine", line);
                        }
                    }else{
                        String[] bits =  temp.get("CalcMachine").toString().split("-");
                        if(bits.length == 4){
                            temp2.put("CalcMachine", bits[1]);
                        }else{
                            temp2.put("CalcMachine", temp.get("CalcMachine"));
                        }
                    }
                }
                if(temp.has("FinTime")) {
                    if(temp.get("FinTime").toString().contains(";")){
                        temp2.put("FinTime", temp.get("FinTime").toString().split(";")[0]);
                    }else{
                        temp2.put("FinTime", temp.get("FinTime"));
                    }
                    temp2.put("FinTimeStamp", ParserUtils.convertDateToTimestamp(temp2.getString("FinTime")));

                }
                if(temp.has("CalcBy")) {
                    if(temp.get("CalcBy").toString().contains(";")){
                        temp2.put("CalcBy", temp.get("CalcBy").toString().split(";")[0]);
                    }else{
                        temp2.put("CalcBy", temp.get("CalcBy"));
                    }
                }
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

                //Extracting some fields which cannot be extracted from existing parsers
                //    * Stoichiometry
                //    * Job cpu time
                //    * %mem
                //    * %nprocshare
                BufferedReader reader = new BufferedReader(new FileReader(gaussianOutputFile));
                String line = reader.readLine();
                while(line != null){
                    if(line.toLowerCase().startsWith(" stoichiometry")){
                        String formula = line.replaceAll("Stoichiometry", "").trim();
                        ((JSONObject)finalObj.get("Molecule")).put("Formula", formula);
                    }else if(line.toLowerCase().contains(" job cpu time")){
                        String time = line.split(":")[1].trim();
                        time = time.replaceAll("days","");
                        time = time.replaceAll("hours", "");
                        time = time.replaceAll("minutes", "");
                        time = time.replaceAll("seconds.","");
                        time = time.replaceAll(" +"," ");
                        String[] timeArr = time.split(" ");
                        double timeInSeconds  = 0.0;
                        timeInSeconds += Double.parseDouble(timeArr[0]) * 86400;
                        timeInSeconds += Double.parseDouble(timeArr[1]) * 3600;
                        timeInSeconds += Double.parseDouble(timeArr[2]) * 60;
                        timeInSeconds += Double.parseDouble(timeArr[3]);

                        if(((JSONObject)finalObj.get("ExecutionEnvironment")).has("JobCPURunTime")){
                            timeInSeconds += (Double)((JSONObject)finalObj.get("ExecutionEnvironment")).get("JobCPURunTime");
                            ((JSONObject)finalObj.get("ExecutionEnvironment")).put("JobCPURunTime", timeInSeconds);
                        }else{
                            ((JSONObject)finalObj.get("ExecutionEnvironment")).put("JobCPURunTime", timeInSeconds);
                        }
                    }else if(line.toLowerCase().startsWith(" %mem")){
                        String memory = line.split("=")[1].trim().toLowerCase();
                        //default memory is 256 MB
                        int memoryInt = 256;
                        if (memory.endsWith("gw")) {
                            memoryInt = Integer.parseInt(memory.substring(0, memory.length() - 2)) * 1000 * 8;
                        } else if (memory.endsWith("gb")) {
                            memoryInt = Integer.parseInt(memory.substring(0, memory.length() - 2)) * 1000;
                        } else if (memory.endsWith("mw")) {
                            memoryInt = Integer.parseInt(memory.substring(0, memory.length() - 2)) * 8;
                        } else if (memory.endsWith("mb")) {
                            memoryInt = Integer.parseInt(memory.substring(0, memory.length() - 2));
                        }
                        ((JSONObject)finalObj.get("ExecutionEnvironment")).put("Memory", memoryInt);
                    }else if(line.toLowerCase().startsWith(" %nproc")){
                        String nproc = line.split("=")[1].trim();
                        ((JSONObject)finalObj.get("ExecutionEnvironment")).put("NProcShared", nproc);
                    }

                    line = reader.readLine();
                }

                return finalObj;
            }

            throw new Exception("Could not parse data");
        }catch (Exception ex){
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
                + File.separator + DefaultGaussianParser.randomNum +"runtype1");
        InputStream mylist2a = new FileInputStream(System.getProperty("java.io.tmpdir")
                + File.separator + DefaultGaussianParser.randomNum +"runtype2");
        SequenceInputStream str4a = new SequenceInputStream(mylist1a, mylist2a);
        PrintStream temp4a = new PrintStream(new FileOutputStream(System.getProperty("java.io.tmpdir")
                + File.separator + DefaultGaussianParser.randomNum +"runtype"));

        int ccc1;
        while ((ccc1 = str4a.read()) != -1)
            temp4a.write(ccc1);

        // read the runtype file
        FileInputStream fis = new FileInputStream(System.getProperty("java.io.tmpdir")
                + File.separator + DefaultGaussianParser.randomNum +"runtype");
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
                    + DefaultGaussianParser.randomNum+"temporary2"));
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
                    + DefaultGaussianParser.randomNum+"temporary3"));
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
                    + DefaultGaussianParser.randomNum+"Energy_data"));
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
}
