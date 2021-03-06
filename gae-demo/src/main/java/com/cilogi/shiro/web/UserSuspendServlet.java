// Copyright (c) 2011 Tim Niblett All Rights Reserved.
//
// File:        UserSuspendServlet.java  (12-Nov-2011)
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

import com.cilogi.shiro.gae.GaeUser;
import com.cilogi.shiro.gae.UserDAO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Singleton
public class UserSuspendServlet extends BaseServlet {
    static final Logger LOG = Logger.getLogger(UserSuspendServlet.class.getName());

    private static final String DELETE = "delete";
    private static final String SUSPEND = "suspend";
    private static final String USERNAME = "username";
    private static final String CODE = "code";


    @Inject
    UserSuspendServlet() {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String userName = request.getParameter(USERNAME);
            UserDAO dao = new UserDAO();
            GaeUser user = dao.get(userName);
            if (user != null) {
                boolean isSuspend = Boolean.parseBoolean(request.getParameter(SUSPEND));
                boolean isDelete = Boolean.parseBoolean(request.getParameter(DELETE));
                if (isDelete) {
                    if (isCurrentUserAdmin()) {
                        dao.delete(user);
                        issueJson(response, HTTP_STATUS_OK,
                                MESSAGE, "User " + userName + " is deleted");
                    } else {
                        issueJson(response, HTTP_STATUS_OK,
                                MESSAGE, "Only admins can delete users", CODE, "404");
                    }
                } else {
                    if (isCurrentUserAdmin()) {
                        user.setSuspended(isSuspend);
                        dao.put(user);
                        issueJson(response, HTTP_STATUS_OK,
                                MESSAGE, isSuspend
                                        ? "User " + userName + " is suspended"
                                        : "User " + userName + " is not suspended");
                    } else {
                        issueJson(response, HTTP_STATUS_OK,
                                MESSAGE, "Only admins can suspend users", CODE, "404");

                    }
                }
            } else {
                LOG.warning("Can't find user " + userName);
                issue(MIME_TEXT_PLAIN, HTTP_STATUS_NOT_FOUND,
                      "Can't find user " + userName, response);
            }
        } catch (Exception e) {
            LOG.severe("Suspend failure: " + e.getMessage());
            issue(MIME_TEXT_PLAIN, HTTP_STATUS_INTERNAL_SERVER_ERROR,
                  "Error generating JSON: " + e.getMessage(), response);
        }
    }

    private boolean isCurrentUserAdmin() {
        Subject subject = SecurityUtils.getSubject();
        return subject.hasRole("admin");
    }
}
