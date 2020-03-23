/*
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
 */


package org.apache.guacamole.api.dockeride;

import com.google.inject.Inject;
import org.apache.guacamole.GuacamoleException;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UserSessionService {

  /**
   * Logger for this class.
   */
  private static final Logger logger = LoggerFactory.getLogger(UserSessionService.class);

  /**
   * The Docker Ide Service Environment
   */
  @Inject
  private DockerIDEEnvironment environment;

  public void startSession(String accessToken) throws DockerIDEUserSessionError, GuacamoleException {
    HttpClient httpClient = HttpClients.createDefault();
    ResponseHandler<String> handler = new BasicResponseHandler();

    HttpPost httpPostRequest = new HttpPost(environment.getDockerIdeAdminUrl() + "/api/v1/sessions");
    httpPostRequest.setHeader("Content-Type", "application/json");

    String json = "{\"access_token\":\"" + accessToken + "\"}";

    try {
      StringEntity requestEntity = new StringEntity(json);
      httpPostRequest.setEntity(requestEntity);

      HttpResponse response = httpClient.execute(httpPostRequest);
      HttpEntity httpEntity = response.getEntity();

      String responseJson = EntityUtils.toString(httpEntity);
      int statusCode = response.getStatusLine().getStatusCode();

      logger.info("POST /api/v1/sessions; " + "body: " + responseJson + "; status: " + statusCode +";");

      if (statusCode != 200) {
        throw new DockerIDEUserSessionError("Unable to start user session");
      }
    }
    catch (HttpResponseException httpException) {
      httpException.printStackTrace();
      throw new DockerIDEUserSessionError("Unable to start user session");
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new DockerIDEUserSessionError("Unable to start user session");
    }
  }

}
