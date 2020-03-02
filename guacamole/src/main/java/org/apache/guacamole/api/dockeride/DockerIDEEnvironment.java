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

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.environment.LocalEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerIDEEnvironment extends LocalEnvironment {

  /**
   * Logger for this class.
   */
  private static final Logger logger = LoggerFactory.getLogger(DockerIDEEnvironment.class);

  /**
   * @throws GuacamoleException
   */
  public DockerIDEEnvironment() throws GuacamoleException {

    // Init underlying Guacamole environment
    super();

  }

  /**
   * Returns REST API Endpoint for Docker IDE Service
   *
   * @return
   *     The api endpoint for Docker IDE Service
   *
   * @throws GuacamoleException
   *     If an error occurs while retrieving the property value, or if the
   *     value was not set, as this property is required.
   */
  public String getDockerIdeAdminUrl() throws GuacamoleException {
    return getRequiredProperty(DockerIDEGuacamoleProperties.ADMIN_API_URL);
  }

}
