#!/usr/bin/env bash

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

#
# helper
#

docker exec -d e2ff31a11cd7 rm /usr/local/tomcat/webapps/guacamole.war
docker exec -d e2ff31a11cd7 rm /etc/guacamole/extensions/guacamole-auth-jdbc-postgresql-1.1.0.jar

docker exec -d e2ff31a11cd7 rm /etc/guacamole/schema/001-create-schema.sql
docker exec -d e2ff31a11cd7 rm /etc/guacamole/schema/002-create-admin-user.sql

docker cp guacamole/target/guacamole-1.1.0.war e2ff31a11cd7:/usr/local/tomcat/webapps/guacamole.war
docker cp extensions/guacamole-auth-jdbc/modules/guacamole-auth-jdbc-postgresql/target/guacamole-auth-jdbc-postgresql-1.1.0.jar e2ff31a11cd7:/etc/guacamole/extensions/guacamole-auth-jdbc-postgresql-1.1.0.jar

docker cp extensions/guacamole-auth-jdbc/modules/guacamole-auth-jdbc-postgresql/schema/001-create-schema.sql e2ff31a11cd7:/etc/guacamole/schema/001-create-schema.sql
docker cp extensions/guacamole-auth-jdbc/modules/guacamole-auth-jdbc-postgresql/schema/002-create-admin-user.sql e2ff31a11cd7:/etc/guacamole/schema/002-create-admin-user.sql