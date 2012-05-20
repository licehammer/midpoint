/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.web.security;

import com.evolveum.midpoint.common.crypto.EncryptionException;
import com.evolveum.midpoint.common.crypto.Protector;
import com.evolveum.midpoint.model.security.api.Credentials;
import com.evolveum.midpoint.model.security.api.PrincipalUser;
import com.evolveum.midpoint.model.security.api.UserDetailsService;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_1.CredentialsType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ProtectedStringType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.UserType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author lazyman
 */
public class MidPointAuthenticationProvider implements AuthenticationProvider {

    private static final Trace LOGGER = TraceManager.getTrace(MidPointAuthenticationProvider.class);
    @Autowired(required = true)
    private transient UserDetailsService userManagerService;
    @Autowired(required = true)
    private transient Protector protector;
    private int loginTimeout;
    private int maxFailedLogins;

    public void setLoginTimeout(int loginTimeout) {
        if (loginTimeout < 0) {
            loginTimeout = 0;
        }
        this.loginTimeout = loginTimeout;
    }

    public void setMaxFailedLogins(int maxFailedLogins) {
        if (maxFailedLogins < 0) {
            maxFailedLogins = 0;
        }
        this.maxFailedLogins = maxFailedLogins;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (StringUtils.isBlank((String) authentication.getPrincipal())
                || StringUtils.isBlank((String) authentication.getCredentials())) {
            throw new BadCredentialsException("web.security.provider.invalid");
        }
        //throw new BadCredentialsException("web.security.provider.illegal");
        PrincipalUser user = null;
        List<GrantedAuthority> grantedAuthorities = null;
        try {
            user = userManagerService.getUser((String) authentication.getPrincipal());
            authenticateUser(user, (String) authentication.getCredentials());
        } catch (BadCredentialsException ex) {
            if (user != null) {
                Credentials credentials = user.getCredentials();
                credentials.addFailedLogin();
                credentials.setLastFailedLoginAttempt(System.currentTimeMillis());

                userManagerService.updateUser(user);
            }

            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Can't get user with username '{}'. Unknown error occured, reason {}.",
                    new Object[]{authentication.getPrincipal(), ex.getMessage()});
            LOGGER.debug("Can't authenticate user '{}'.", new Object[]{authentication.getPrincipal()}, ex);
            throw new AuthenticationServiceException("web.security.provider.unavailable");
        }

        if (user != null) {
            grantedAuthorities = new ArrayList<GrantedAuthority>();
            UserType userType = user.getUser();
            CredentialsType credentials = userType.getCredentials();

            if (credentials == null) {
                credentials = new CredentialsType();
                userType.setCredentials(credentials);
            }

            boolean isAdminGuiAccess = credentials.isAllowedIdmAdminGuiAccess() != null
                    ? credentials.isAllowedIdmAdminGuiAccess() : false;
            if (isAdminGuiAccess) {
                grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
            } else {
                grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
            }

            /*
                * List<Role> roles = new ArrayList<Role>(0);
                * //user.getAssociatedRoles(); for (Role role : roles) {
                * GrantedAuthority authority = new
                * SimpleGrantedAuthority(role.getRoleName());
                * grantedAuthorities.add(authority); }
                */
        } else {
            throw new BadCredentialsException("web.security.provider.invalid");
        }
        return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(),
                grantedAuthorities);
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        if (UsernamePasswordAuthenticationToken.class.equals(authentication)) {
            return true;
        }

        return false;
    }

    private void authenticateUser(PrincipalUser user, String password) throws BadCredentialsException {
        if (user == null) {
            throw new BadCredentialsException("web.security.provider.invalid");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException("web.security.provider.disabled");
        }

        Credentials credentials = user.getCredentials();
        if (maxFailedLogins > 0 && credentials.getFailedLogins() >= maxFailedLogins) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(credentials.getLastFailedLoginAttempt());
            calendar.add(Calendar.MINUTE, loginTimeout);
            long lockedTill = calendar.getTimeInMillis();

            if (lockedTill > System.currentTimeMillis()) {
                long time = (lockedTill - System.currentTimeMillis()) / 60000L;
                throw new BadCredentialsException("web.security.provider.locked", new Object[]{time});
            }
        }

        ProtectedStringType protectedString = credentials.getPassword();
        if (protectedString == null) {
            throw new BadCredentialsException("web.security.provider.password.bad");
        }

        if (StringUtils.isEmpty(password)) {
            throw new BadCredentialsException("web.security.provider.password.encoding");
        }

        try {
            String decoded;
            if (protectedString.getEncryptedData() != null) {
                decoded = protector.decryptString(protectedString);
            } else {
                LOGGER.warn("Authenticating user based on clear value. Please check objects, " +
                        "this should not happen. Protected string should be encrypted.");
                decoded = protectedString.getClearValue();
            }
            if (password.equals(decoded)) {
                if (credentials.getFailedLogins() > 0) {
                    credentials.clearFailedLogin();
                    userManagerService.updateUser(user);
                }
                return;
            }
        } catch (EncryptionException ex) {
            throw new AuthenticationServiceException("web.security.provider.unavailable", ex);
        }

        throw new BadCredentialsException("web.security.provider.invalid");
    }
}
