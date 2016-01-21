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
package org.apache.airavata.metcat.worker.util.sshutils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

public class SCPFileDownloader {
    private final static Logger logger = LoggerFactory.getLogger(SCPFileDownloader.class);

    private JSch jSch;
    private Session session;

    public SCPFileDownloader(String hostName, String pubKeyFile, String privateKeyFile, String loginUsername, String passPhrase, Integer port)
            throws JSchException, IOException {
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        jSch = new JSch();
        jSch.addIdentity(UUID.randomUUID().toString(), Files.readAllBytes(Paths.get(privateKeyFile)),
                Files.readAllBytes(Paths.get(pubKeyFile)), passPhrase.getBytes());
        session = jSch.getSession(loginUsername, hostName, port);
        session.setConfig(config);
        session.connect();
    }

    public void downloadFile(String remotePath, String localPath) throws JSchException,
            IOException, SSHApiException {
        if(!session.isConnected()){
            session.connect();
        }
        SSHUtils.scpFrom(remotePath, localPath, session);
    }
}