/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.authenticator;

import org.wso2.carbon.apimgt.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.authenticator.utils.bean.AuthResponseBean;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * This class provides access token during login from store app.
 *
 */
public class AuthenticatorAPI implements Microservice {

    /**
     * This method authenticate the user for store app.
     *
     */
    @POST
    @Path ("/token")
    @Produces ("application/json")
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    public Response authenticate(@Context Request request,
            @FormDataParam ("username") String userName, @FormDataParam ("password") String password,
            @FormDataParam ("scopes") String scopesList) {
        IntrospectService introspectService = new IntrospectService();
        AuthResponseBean authResponseBean = new AuthResponseBean();
        String accessToken = introspectService
                .getAccessToken(authResponseBean, userName, password, scopesList.split(" "));
        String part1 = accessToken.substring(0, accessToken.length() / 2);
        String part2 = accessToken.substring(accessToken.length() / 2);
        NewCookie cookie = new NewCookie(AuthenticatorConstants.TOKEN_1,
                part1 + "; path=" + AuthUtil.getAppContext(request));
        NewCookie cookie2 = new NewCookie(AuthenticatorConstants.TOKEN_2,
                part2 + "; path=" + AuthUtil.getAppContext(request) + "; " + AuthenticatorConstants.HTTP_ONLY_COOKIE);
        NewCookie backendCookie = new NewCookie(AuthenticatorConstants.MSF4J_TOKEN_1, part1 + "; path=/api/am");
        NewCookie backendCookie2 = new NewCookie(AuthenticatorConstants.MSF4J_TOKEN_2,
                part2 + "; path=/api/am; " + AuthenticatorConstants.HTTP_ONLY_COOKIE);
        return Response.ok(authResponseBean, MediaType.APPLICATION_JSON)
                .cookie(cookie, cookie2, backendCookie, backendCookie2).header(AuthenticatorConstants.REFERER_HEADER,
                        (request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null && request
                                .getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)
                                .equals(request.getHeader(AuthenticatorConstants.REFERER_HEADER))) ?
                                "" :
                                request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null ?
                                        request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) :
                                        "").build();

    }
}