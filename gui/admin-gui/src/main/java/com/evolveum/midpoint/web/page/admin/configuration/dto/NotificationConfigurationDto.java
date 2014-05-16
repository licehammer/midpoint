/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.web.page.admin.configuration.dto;

import com.evolveum.midpoint.web.util.WebMiscUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MailConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MailServerConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MailTransportSecurityType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.NotificationConfigurationType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;

import java.io.Serializable;

/**
 *  @author shood
 *
 *  TODO - contains only fields for e-mail configuration
 * */
public class NotificationConfigurationDto implements Serializable{

//    public static final String F_DEFAULT_FROM = "defaultFrom";
//    public static final String F_DEBUG = "debug";
//    public static final String F_REDIRECT_TO_FILE = "redirectToFile";
//    public static final String F_HOST = "host";
//    public static final String F_PORT = "port";
//    public static final String F_USERNAME = "username";
//    public static final String F_PASSWORD = "password";
//    public static final String F_MAILTST = "mailTransportSecurityType";

    private String defaultFrom;
    private boolean debug;
    private String redirectToFile;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private MailTransportSecurityType mailTransportSecurityType;

    public NotificationConfigurationDto(NotificationConfigurationType config){

        if(config.getMail() != null){
            MailConfigurationType mailConfig = config.getMail();

            defaultFrom = mailConfig.getDefaultFrom();
            debug = mailConfig.isDebug();
            redirectToFile = mailConfig.getRedirectToFile();

            if(!mailConfig.getServer().isEmpty() && mailConfig.getServer().get(0) != null){
                MailServerConfigurationType serverConfig = mailConfig.getServer().get(0);

                host = serverConfig.getHost();
                port = serverConfig.getPort();
                username = serverConfig.getUsername();
                password = serverConfig.getPassword().getClearValue();
                mailTransportSecurityType = serverConfig.getTransportSecurity();
            }
        }
    }

    public String getDefaultFrom() {
        return defaultFrom;
    }

    public void setDefaultFrom(String defaultFrom) {
        this.defaultFrom = defaultFrom;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getRedirectToFile() {
        return redirectToFile;
    }

    public void setRedirectToFile(String redirectToFile) {
        this.redirectToFile = redirectToFile;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MailTransportSecurityType getMailTransportSecurityType() {
        return mailTransportSecurityType;
    }

    public void setMailTransportSecurityType(MailTransportSecurityType mailTransportSecurityType) {
        this.mailTransportSecurityType = mailTransportSecurityType;
    }
}
