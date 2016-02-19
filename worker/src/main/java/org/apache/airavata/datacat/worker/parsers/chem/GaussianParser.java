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
package org.apache.airavata.datacat.worker.parsers.chem;

import org.apache.airavata.datacat.worker.parsers.IParser;
import org.apache.airavata.datacat.worker.parsers.ParserException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Map;

public class GaussianParser implements IParser {

    private final static Logger logger = LoggerFactory.getLogger(GaussianParser.class);

    private final String outputFileName = "gaussian-output.json";

    @SuppressWarnings("unchecked")
    public JSONObject parse(String dir, Map<String, Object> inputMetadata) throws Exception {
        try{
            if(!dir.endsWith(File.separator)){
                dir += File.separator;
            }
            String gaussianOutputFile = null;
            for(File file : (new File(dir).listFiles())){
                if(file.getName().endsWith(".out")){
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

                JSONObject temp2 = new JSONObject();
                if(temp.get("InChI") != null)
                    temp2.put("InChI", temp.get("InChI"));
                if(temp.get("InChIKey") != null)
                    temp2.put("InChIKey", temp.get("InChIKey"));
                if(temp.get("SMILES") != null)
                    temp2.put("SMILES", temp.get("SMILES"));
                if(temp.get("CanonicalSMILES") != null)
                    temp2.put("CanonicalSMILES", temp.get("CanonicalSMILES"));
                finalObj.put("Identifiers", temp2);

                temp2 = new JSONObject();
                if(temp.get("CodeVersion") != null)
                    temp2.put("Package", temp.get("CodeVersion"));
                if(temp.get("Method") != null)
                    temp2.put("Method", temp.get("Method"));
                if(temp.get("Keywords") != null)
                    temp2.put("Keywords", temp.get("Keywords"));
                if(temp.get("Basis") != null)
                    temp2.put("Basis", temp.get("Basis"));
                if(temp.get("CalcType") != null)
                    temp2.put("CalcType", temp.get("CalcType"));
                if(temp.get("NBasis") != null)
                    temp2.put("NBasis", temp.get("NBasis"));
                if(temp.get("JobStatus") != null)
                    temp2.put("JobStatus", temp.get("JobStatus"));
                finalObj.put("Calculation", temp2);

                temp2 = new JSONObject();
                if(temp.get("Formula") != null)
                    temp2.put("Formula", temp.get("Formula"));
                if(temp.get("NAtom") != null)
                    temp2.put("NAtom", temp.get("NAtom"));
                if(temp.get("Nmo") != null)
                    temp2.put("NMo", temp.get("Nmo"));
                if(temp.get("OrbSym") != null)
                    temp2.put("OrbSym", temp.get("OrbSym"));
                if(temp.get("NAtom") != null)
                    temp2.put("NAtom", temp.get("NAtom"));
                if(temp.get("Multiplicity") != null)
                    temp2.put("Multiplicity", temp.get("Multiplicity"));
                if(temp.get("Charge") != null)
                    temp2.put("Charge", temp.get("Charge"));
                if(temp.get("ElecSym") != null)
                    temp2.put("ElecSym", temp.get("ElecSym"));
                finalObj.put("Molecule", temp2);

                temp2 = new JSONObject();
                if(temp.get("Energy") != null)
                    temp2.put("Energy", temp.get("Energy"));
                if(temp.get("Dipole") != null)
                    temp2.put("Dipole", temp.get("Dipole"));
                if(temp.get("HF") != null)
                    temp2.put("HF", temp.get("HF"));
                if(temp.get("Homos") != null)
                    temp2.put("Homos", temp.get("Homos"));
                finalObj.put("CalculatedProperties", temp2);

                temp2 = new JSONObject();
                if(temp.get("CalcMachine") != null)
                    temp2.put("CalcMachine", temp.get("CalcMachine"));
                if(temp.get("FinTime") != null)
                    temp2.put("FinTime", temp.get("FinTime"));
                if(temp.get("CalcBy") != null)
                    temp2.put("CalcBy", temp.get("CalcBy"));
                finalObj.put("ExecutionEnvironment", temp2);

                temp2 = new JSONObject();
                if(temp.get("SDF") != null)
                    temp2.put("SDF", temp.get("SDF"));
                if(temp.get("PDB") != null)
                    temp2.put("PDB", temp.get("PDB"));
                finalObj.put("FinalMoleculeStructuralFormats", temp2);

                temp2 = new JSONObject();
                File baseDir = new File(dir);
                for(File f : baseDir.listFiles()){
                    if(f.getName().endsWith(".out")){
                        temp2.put("GaussianOutputFile", f.getAbsolutePath());
                    }else if(f.getName().endsWith(".com") || f.getName().endsWith(".in")){
                        temp2.put("GaussianInputFile", f.getAbsolutePath());
                    }else if(f.getName().endsWith(".chk")){
                        temp2.put("GaussianCheckpointFile", f.getAbsolutePath());
                    }else if(f.getName().endsWith(".fchk")){
                        temp2.put("GaussianFCheckpointFile", f.getAbsolutePath());
                    }
                }
                finalObj.put("Files", temp2);

                return finalObj;
            }

            throw new Exception("Could not parse data");
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new ParserException(ex);
        }finally {
            File outputFile = new File(dir + outputFileName);
            if(outputFile.exists()){
                outputFile.delete();
            }
        }
    }
}