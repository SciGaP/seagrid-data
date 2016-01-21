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
package org.apache.airavata.metcat.worker.util;

import org.apache.airavata.metcat.worker.util.sshutils.SCPFileDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.UUID;

public class FileHelper {private final static Logger logger = LoggerFactory.getLogger(FileHelper.class);
    public static final String PUBLIC_KEY_FILE = "public.key.file";
    public static final String PRIVATE_KEY_FILE = "private.key.file";
    public static final String SSH_LOGIN_USERNAME = "ssh.login.username";
    public static final String PASS_PHRASE = "pass.phrase";

    public String createLocalCopyOfFile(URI uri) throws Exception {
        if(uri.getScheme().equals("scp")){
            String pubKeyFile = WorkerProperties.getInstance().getProperty(PUBLIC_KEY_FILE, "");
            String privateKeyFile = WorkerProperties.getInstance().getProperty(PRIVATE_KEY_FILE, "");
            String loginUsername = WorkerProperties.getInstance().getProperty(SSH_LOGIN_USERNAME, "");
            String passPhrase = WorkerProperties.getInstance().getProperty(PASS_PHRASE, "");

            SCPFileDownloader scpFileDownloader = new SCPFileDownloader(uri.getHost(), pubKeyFile,
                    privateKeyFile, loginUsername, passPhrase, 22);
            String workingDir = WorkerProperties.getInstance().getProperty(WorkerConstants.WORKING_DIR, "/tmp");
            if(!workingDir.endsWith(File.separator)){
                workingDir += File.separator;
            }
            String randomString = workingDir + UUID.randomUUID().toString();
            scpFileDownloader.downloadFile(uri.getPath(), randomString);
            File file = new File(randomString);
            if(!file.exists()){
                throw new Exception("File download failed for " + uri.toString());
            }
            return randomString;
        }else if(uri.getScheme().equals("file")){
            return uri.getPath();
        }else{
            throw new Exception("Unsupported file protocol");
        }
    }
}