--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

-- Create default user "guacadmin" with access_token "guacadmin"
INSERT INTO guacamole_entity (name, type) VALUES ('guacadmin', 'USER');
INSERT INTO guacamole_user (entity_id, access_token)
SELECT
    entity_id,
    'guacadmin'
FROM guacamole_entity WHERE name = 'guacadmin' AND guacamole_entity.type = 'USER';

-- Grant this user all system permissions
INSERT INTO guacamole_system_permission (entity_id, permission)
SELECT entity_id, permission::guacamole_system_permission_type
FROM (
    VALUES
        ('guacadmin', 'CREATE_CONNECTION'),
        ('guacadmin', 'CREATE_CONNECTION_GROUP'),
        ('guacadmin', 'CREATE_SHARING_PROFILE'),
        ('guacadmin', 'CREATE_USER'),
        ('guacadmin', 'CREATE_USER_GROUP'),
        ('guacadmin', 'ADMINISTER')
) permissions (username, permission)
JOIN guacamole_entity ON permissions.username = guacamole_entity.name AND guacamole_entity.type = 'USER';

-- Grant admin permission to read/update/administer self
INSERT INTO guacamole_user_permission (entity_id, affected_user_id, permission)
SELECT guacamole_entity.entity_id, guacamole_user.user_id, permission::guacamole_object_permission_type
FROM (
    VALUES
        ('guacadmin', 'guacadmin', 'READ'),
        ('guacadmin', 'guacadmin', 'UPDATE'),
        ('guacadmin', 'guacadmin', 'ADMINISTER')
) permissions (username, affected_username, permission)
JOIN guacamole_entity          ON permissions.username = guacamole_entity.name AND guacamole_entity.type = 'USER'
JOIN guacamole_entity affected ON permissions.affected_username = affected.name AND guacamole_entity.type = 'USER'
JOIN guacamole_user            ON guacamole_user.entity_id = affected.entity_id;



-- INSERT INTO guacamole_connection (connection_name, protocol) VALUES ('RDP Connection', 'rdp');
--
-- INSERT INTO guacamole_connection_parameter (connection_id, parameter_name, parameter_value)
-- SELECT connection_id, 'security', 'any'
-- FROM guacamole_connection WHERE connection_name = 'RDP Connection';
--
-- INSERT INTO guacamole_connection_parameter (connection_id, parameter_name, parameter_value)
-- SELECT connection_id, 'hostname', 'host.docker.internal'
-- FROM guacamole_connection WHERE connection_name = 'RDP Connection';
--
-- INSERT INTO guacamole_connection_parameter (connection_id, parameter_name, parameter_value)
-- SELECT connection_id, 'password', 'ubuntu'
-- FROM guacamole_connection WHERE connection_name = 'RDP Connection';
--
-- INSERT INTO guacamole_connection_parameter (connection_id, parameter_name, parameter_value)
-- SELECT connection_id, 'username', 'ubuntu'
-- FROM guacamole_connection WHERE connection_name = 'RDP Connection';
--
-- INSERT INTO guacamole_connection_parameter (connection_id, parameter_name, parameter_value)
-- SELECT connection_id, 'port', '3389'
-- FROM guacamole_connection WHERE connection_name = 'RDP Connection';
--
-- INSERT INTO guacamole_connection_parameter (connection_id, parameter_name, parameter_value)
-- SELECT connection_id, 'ignore-cert', 'true'
-- FROM guacamole_connection WHERE connection_name = 'RDP Connection';
