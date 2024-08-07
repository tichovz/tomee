/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.security.http;

import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.Set;

public interface LoginToContinueMechanism {
    int MAX_SAVE_POST_SIZE = 4 * 1024;

    String ORIGINAL_REQUEST = "org.apache.tomee.security.request.original";
    String AUTHENTICATION = "org.apache.tomee.security.request.authentication";
    String CALLER_AUTHENICATION = "org.apache.tomee.security.request.caller.authentication";

    LoginToContinue getLoginToContinue();

    static void saveRequest(final HttpServletRequest request) throws IOException {
        // Stash the SavedRequest in our session for later use
        request.getSession().setAttribute(ORIGINAL_REQUEST, SavedRequest.fromRequest(request));
    }

    static boolean matchRequest(final HttpServletRequest request) {
        // Has a session been created?
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        // Is there a saved request?
        SavedRequest originalRequest = (SavedRequest) request.getSession().getAttribute(ORIGINAL_REQUEST);
        if (originalRequest == null) {
            return false;
        }

        // Is there a saved principal?
        /*
        if (session.getNote(Constants.FORM_PRINCIPAL_NOTE) == null) {
            return false;
        }
        */

        // Does the request URI match?
        String requestURI = request.getRequestURI();
        return requestURI != null && requestURI.equals(originalRequest.getUrlWithQueryString());
    }

    static boolean hasRequest(final HttpServletRequest request) {
        return request.getSession().getAttribute(ORIGINAL_REQUEST) != null;
    }

    static SavedRequest getRequest(final HttpServletRequest request) {
        return (SavedRequest) request.getSession().getAttribute(ORIGINAL_REQUEST);
    }

    static void saveAuthentication(final HttpServletRequest request,
                                   final Principal principal,
                                   final Set<String> groups) {
        SavedAuthentication savedAuthentication = new SavedAuthentication(principal, groups);
        request.getSession().setAttribute(AUTHENTICATION, savedAuthentication);
    }

    static boolean hasAuthentication(final HttpServletRequest request) {
        return request.getSession().getAttribute(AUTHENTICATION) != null;
    }

    static SavedAuthentication getAuthentication(final HttpServletRequest request) {
        return (SavedAuthentication) request.getSession().getAttribute(AUTHENTICATION);
    }

    static void clearRequestAndAuthentication(final HttpServletRequest request) {
        request.getSession().removeAttribute(ORIGINAL_REQUEST);
        request.getSession().removeAttribute(AUTHENTICATION);
    }
}
