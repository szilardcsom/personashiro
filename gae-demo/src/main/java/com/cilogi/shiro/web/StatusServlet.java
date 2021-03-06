// Copyright (c) 2011 Tim Niblett All Rights Reserved.
//
// File:        StatusServlet.java  (02-Nov-2011)
// Author:      tim

//
// Copyright in the whole and every part of this source file belongs to
// Tim Niblett (the Author) and may not be used,
// sold, licenced, transferred, copied or reproduced in whole or in
// part in any manner or form or in or on any media to any person
// other than in accordance with the terms of The Author's agreement
// or otherwise without the prior written consent of The Author.  All
// information contained in this source file is confidential information
// belonging to The Author and as such may not be disclosed other
// than in accordance with the terms of The Author's agreement, or
// otherwise, without the prior written consent of The Author.  As
// confidential information this source file must be kept fully and
// effectively secure at all times.
//


package com.cilogi.shiro.web;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Get the current session status via Ajax.  Gets information about whether the
 * user is known (remembered) and authenticated.  You are authenticated only if
 * you logged in during this session.
 * <p> Note that any client-side record of status can't be definitive -- checks are
 * always run server-side to avoid scams.  However this information can be used to
 * set up display options.
 */
@Singleton
public class StatusServlet extends BaseServlet {
    static final Logger LOG = Logger.getLogger(StatusServlet.class.getName());

    @Inject
    StatusServlet() {}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Subject subject = SecurityUtils.getSubject();
            String email = getPrincipal(subject);
            boolean isAuthenticated = (subject != null) && subject.isAuthenticated();
            boolean isAdmin = subject != null && hasRole(subject, "admin");

            issueJson(response, HTTP_STATUS_OK,
                    "email", email,
                    "isAuthenticated", isAuthenticated,
                    "isAdmin", isAdmin);
        } catch (Exception e) {
            issue(MIME_TEXT_PLAIN, HTTP_STATUS_INTERNAL_SERVER_ERROR,
                  "Internal error getting status: " + e.getMessage(), response);
        }
    }

    private static String getPrincipal(Subject subject) {
        if (subject == null) {
            return null;
        } else {
            boolean isKnown = subject.isAuthenticated() || subject.isRemembered();
            Object principal = subject.getPrincipal();
            return (principal == null || !isKnown) ? "" : principal.toString();
        }
    }

}
