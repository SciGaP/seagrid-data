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
package org.apache.airavata.datacat.query.api.services;

import org.apache.airavata.datacat.registry.IRegistry;
import org.apache.airavata.datacat.registry.RegistryFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/query-api")
public class QueryAPIService {

    private final static Logger logger = LoggerFactory.getLogger(QueryAPIService.class);
    public static final String QUERY_SERVER_VERSION = "0.1-SNAPSHOT";

    private final IRegistry registry;

    public QueryAPIService(){
        registry = RegistryFactory.getRegistryImpl();
    }

    @GET
    @Path("/getAPIVersion")
    @Produces("application/json")
    public Response getAPIVersion(){
        return Response.status(200).entity(QUERY_SERVER_VERSION).build();
    }

    @GET
    @Path("/select")
    @Produces("application/json")
    public Response select(@QueryParam("username") String username, @QueryParam("q") String queryString,
                           @QueryParam("limit") int limit, @QueryParam("offset") int offset){
        try {
            if(limit == 0)
                limit = 10;
            List<JSONObject> result = registry.select(username, queryString, offset, limit);
            JSONArray jsonArray = new JSONArray(result);
            return Response.status(200).entity(jsonArray.toString()).build();
        } catch (Exception e) {
            logger.error(e.toString());
            return Response.status(503).entity(e.toString()).build();
        }
    }

    @GET
    @Path("/make-public")
    @Produces("application/json")
    public Response makePublic(@QueryParam("username") String username, @QueryParam("id") String id){
        try {
            JSONObject result = registry.get(id);
            if(result.get("Username").toString().equals(username)){
                result.put("Shared", true);
                registry.update(result);
                return Response.status(200).entity(result.toString()).build();
            }else{
                return Response.status(401).build();
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return Response.status(503).entity(e.toString()).build();
        }
    }

    @GET
    @Path("/make-private")
    @Produces("application/json")
    public Response makePrivate(@QueryParam("username") String username, @QueryParam("id") String id){
        try {
            JSONObject result = registry.get(id);
            if(result.get("Username").toString().equals(username)){
                result.put("Shared", false);
                registry.update(result);
                return Response.status(200).entity(result.toString()).build();
            }else{
                return Response.status(401).build();
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return Response.status(503).entity(e.toString()).build();
        }
    }

    @GET
    @Path("/get")
    @Produces("application/json")
    public Response get(@QueryParam("username") String username, @QueryParam("id") String id){
        try {
            JSONObject result = registry.get(id);
            if(result.get("Username").toString().equals(username) || (result.has("Shared") && (boolean)result.get("Shared") == true)){
                return Response.status(200).entity(result.toString()).build();
            }else{
                return Response.status(401).build();
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return Response.status(503).entity(e.toString()).build();
        }
    }

}