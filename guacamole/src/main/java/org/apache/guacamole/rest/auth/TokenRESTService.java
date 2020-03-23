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

package org.apache.guacamole.rest.auth;

import com.google.common.io.BaseEncoding;
import com.google.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleResourceNotFoundException;
import org.apache.guacamole.api.dockeride.UserSessionService;
import org.apache.guacamole.net.auth.AuthenticatedUser;
import org.apache.guacamole.net.auth.Credentials;
import org.apache.guacamole.net.auth.UserContext;
import org.apache.guacamole.GuacamoleSession;
import org.apache.guacamole.rest.APIRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service for managing auth tokens via the Guacamole REST API.
 */
@Path("/tokens")
@Produces(MediaType.APPLICATION_JSON)
public class TokenRESTService {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(TokenRESTService.class);

    /**
     * Service for authenticating users and managing their Guacamole sessions.
     */
    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserSessionService userSessionService;

    /**
     * Returns the credentials associated with the given request, using the
     * provided username and password.
     *
     * @param request
     *     The request to use to derive the credentials.
     *
     * @param accessToken
     *     The accessToken to associate with credentials, ot null if the
     *     accessToken should be derived from the request
     *
     * @param username
     *     The username to associate with the credentials, or null if the
     *     username should be derived from the request.
     *
     * @return
     *     A new Credentials object whose contents have been derived from the
     *     given request, along with the provided username and password.
     */
    private Credentials getCredentials(HttpServletRequest request, String username, String accessToken) {

        // TODO
        // If no username/password given, try Authorization header
        if (accessToken == null) {

            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Basic ")) {

                try {
                    // Decode base64 authorization
                    String basicBase64 = authorization.substring(6);
                    String basicCredentials = new String(
                            BaseEncoding.base64().decode(basicBase64), "UTF-8");

                    // Pull username/password from auth data
                    int colon = basicCredentials.indexOf(':');
                    if (colon != -1) {
                        username = basicCredentials.substring(0, colon);
                        accessToken = basicCredentials.substring(colon + 1);
                    }
                    else
                        logger.debug("Invalid HTTP Basic \"Authorization\" header received.");

                }

                // UTF-8 support is required by the Java specification
                catch (UnsupportedEncodingException e) {
                    throw new UnsupportedOperationException("Unexpected lack of UTF-8 support.", e);
                }

            }

        } // end Authorization header fallback

        // Build credentials
        return new Credentials(username, accessToken, request);

    }

    /**
     * Authenticates a user, generates an auth token, associates that auth token
     * with the user's UserContext for use by further requests. If an existing
     * token is provided, the authentication procedure will attempt to update
     * or reuse the provided token.
     *
     * @param accessToken
     *     A token given by 3d party services to a user who is to be authenticated.
     *
     * @param token
     *     An optional existing auth token for the user who is to be
     *     authenticated.
     *
     * @param consumedRequest
     *     The HttpServletRequest associated with the login attempt. The
     *     parameters of this request may not be accessible, as the request may
     *     have been fully consumed by JAX-RS.
     *
     * @param parameters
     *     A MultivaluedMap containing all parameters from the given HTTP
     *     request. All request parameters must be made available through this
     *     map, even if those parameters are no longer accessible within the
     *     now-fully-consumed HTTP request.
     *
     * @return
     *     An authentication response object containing the possible-new auth
     *     token, as well as other related data.
     *
     * @throws GuacamoleException
     *     If an error prevents successful authentication.
     */
    @POST
    public APIAuthenticationResult createToken(
            @FormParam("username") String username,
            @FormParam("accessToken") String accessToken,
            @FormParam("token") String token,
            @Context HttpServletRequest consumedRequest,
            MultivaluedMap<String, String> parameters)
            throws GuacamoleException {

        Boolean isLoginProcess = accessToken != null && token == null;

        // Reconstitute the HTTP request with the map of parameters
        HttpServletRequest request = new APIRequest(consumedRequest, parameters);

        // Build credentials from request
        Credentials credentials = getCredentials(request, username, accessToken);

        // Create/update session producing possibly-new token
        token = authenticationService.authenticate(credentials, token);

        // Pull corresponding session
        GuacamoleSession session = authenticationService.getGuacamoleSession(token);
        if (session == null)
            throw new GuacamoleResourceNotFoundException("No such token.");

        // Build list of all available auth providers
        List<DecoratedUserContext> userContexts = session.getUserContexts();
        List<String> authProviderIdentifiers = new ArrayList<String>(userContexts.size());
        for (UserContext userContext : userContexts)
            authProviderIdentifiers.add(userContext.getAuthenticationProvider().getIdentifier());

        // Return possibly-new auth token
        AuthenticatedUser authenticatedUser = session.getAuthenticatedUser();

        // User logins at the application, start session
        if (isLoginProcess) {
            logger.info("Start user session for accessToken " + accessToken);

            userSessionService.startSession(accessToken);
        }

        return new APIAuthenticationResult(
            token,
            authenticatedUser.getIdentifier(),
            authenticatedUser.getAuthenticationProvider().getIdentifier(),
            authProviderIdentifiers
        );

    }

    /**
     * Invalidates a specific auth token, effectively logging out the associated
     * user.
     * 
     * @param authToken
     *     The token being invalidated.
     *
     * @throws GuacamoleException
     *     If the specified token does not exist.
     */
    @DELETE
    @Path("/{token}")
    public void invalidateToken(@PathParam("token") String authToken)
            throws GuacamoleException {

        // Invalidate session, if it exists
        if (!authenticationService.destroyGuacamoleSession(authToken))
            throw new GuacamoleResourceNotFoundException("No such token.");

    }

}
