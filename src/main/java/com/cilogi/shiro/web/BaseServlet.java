// Copyright (c) 2011 Tim Niblett All Rights Reserved.
//
// File:        BaseServlet.java  (31-Oct-2011)
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

import com.cilogi.shiro.guice.IniAdmins;
import com.cilogi.shiro.gae.UserDAO;
import com.cilogi.util.doc.CreateDoc;
import com.google.common.base.Preconditions;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;


class BaseServlet extends HttpServlet {
    static final Logger LOG = Logger.getLogger(BaseServlet.class.getName());

    protected static final String MESSAGE = "message";

    protected final String CODE = "code";

    // these 4 are from jQuery.dataTables
    protected final String DATATABLE_ECHO = "sEcho";
    protected final String DATATABLE_START = "iDisplayStart";
    protected final String DATATABLE_LENGTH = "iDisplayLength";
    protected final String DATATABLE_SEARCH = "sSearch";

    protected final String DELETE = "delete";
    protected final String TOKEN = "password";
    protected final String REMEMBER_ME = "rememberMe";
    protected final String SUSPEND = "suspend";
    protected final String USERNAME = "username";

    protected final String MIME_TEXT_PLAIN = "text/plain";
    protected final String MIME_TEXT_HTML = "text/html";
    protected final String MIME_APPLICATION_JSON = "application/json";

    protected final int HTTP_STATUS_OK = 200;
    protected final int HTTP_STATUS_NOT_FOUND = 404;
    protected final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;

    private CreateDoc create;
    private final Provider<UserDAO> userDAOProvider;
    @Inject private IniAdmins iniAdmins;

    BaseServlet(Provider<UserDAO> userDAOProvider) {
        this.userDAOProvider = userDAOProvider;
    }

    protected UserDAO getDAO() {
        return userDAOProvider.get();
    }

    @Inject
    protected void setCreate(CreateDoc create) {
        this.create = create;
    }

    protected void issue(String mimeType, int returnCode, String output, HttpServletResponse response) throws IOException {
        response.setContentType(mimeType);
        response.setStatus(returnCode);
        response.getWriter().println(output);
    }

    protected void issueJson(HttpServletResponse response, int status, String... args) throws IOException {
        Preconditions.checkArgument(args.length % 2 == 0, "There must be an even number of strings");
            try {
            JSONObject obj = new JSONObject();
            for (int i = 0; i < args.length; i += 2) {
                obj.put(args[i], args[i+1]);
            }
            issueJson(response, status, obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected void issueJson(HttpServletResponse response, int status, JSONObject obj) throws IOException {
        issue(MIME_APPLICATION_JSON, status, obj.toString(), response);
    }

    protected void showView(HttpServletResponse response, String templateName, Object... args) throws IOException {
        String html =  create.createDocumentString(templateName, CreateDoc.map(args));
        issue(MIME_TEXT_HTML, HTTP_STATUS_OK, html, response);
    }

    protected String view(String templateName, Object... args) {
        return create.createDocumentString(templateName, CreateDoc.map(args));
    }

    protected int intParameter(String name, HttpServletRequest request, int deflt) {
        String s = request.getParameter(name);
        return (s == null) ? deflt : Integer.parseInt(s);
    }

    protected boolean booleanParameter(String name, HttpServletRequest request, boolean deflt) {
        String s = request.getParameter(name);
        return (s == null) ? deflt : Boolean.parseBoolean(s);
    }

    protected boolean isCurrentUserAdmin() {
        Subject subject = SecurityUtils.getSubject();
        return subject.hasRole("admin") || isAdmin((String)subject.getPrincipal());
    }

    protected  boolean isAdmin(String name) {
        return iniAdmins.isAdmin(name);
    }
}
