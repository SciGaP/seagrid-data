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
package org.apache.airavata.datacat.agent.gridchem.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GridChemUITSScanner {
    private final static Logger logger = LoggerFactory.getLogger(GridChemUITSScanner.class);

    public static void main(String[] args) {
        logger.info("Starting File System Scanning");
        String dataRootPath = "/home/ccguser/mss/internal";
        logger.info("Entering " + dataRootPath);
        File dataRoot = new File(dataRootPath);
        for(File userDir : dataRoot.listFiles()){
            if(userDir.getName().equals("..") || userDir.getName().equals(".") || !userDir.isDirectory())
                continue;
            logger.info("Entering user dir" + userDir.getName());
            for(File expDir : userDir.listFiles()){
                if(expDir.getName().equals("..") || expDir.getName().equals(".") || !expDir.isDirectory())
                    continue;
                String experimentName = expDir.getName();
                logger.info("Entering experiment dir" + expDir.getName());
            }
        }

    }

}