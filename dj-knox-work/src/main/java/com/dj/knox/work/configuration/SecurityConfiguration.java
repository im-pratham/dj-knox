package com.dj.knox.work.configuration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@Order(10)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    protected ObjectProvider<RememberMeServices> rememberMeServicesObjectProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        RememberMeServices rememberMeServices = rememberMeServicesObjectProvider.getIfAvailable();
        String key = null;
        if (rememberMeServices instanceof AbstractRememberMeServices) {
            key = ((AbstractRememberMeServices) rememberMeServices).getKey();
        }
        http.csrf().disable().headers().frameOptions().sameOrigin().and().rememberMe().key(key)
                .rememberMeServices(rememberMeServicesObjectProvider.getIfAvailable()).and().exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        AnyRequestMatcher.INSTANCE)
                .and().x509().subjectPrincipalRegex("CN=(.*?)(?:,|$)").userDetailsService(userDetailsService());
    }

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                String grantedAuthoritiesStringList = "GROUP_flowableAdministrator,GROUP_flowableUser,GROUP_knoxAdministrator,"
                        + "GROUP_knoxUser,USER_DEFINITION_KEY_admin-flowable,access-actuators,access-cases,access-changeContactPassword,"
                        + "access-changeOwnPassword,access-contacts,access-createUser,access-createWork,access-editContactAvatar,"
                        + "access-processes,access-reports,access-reportsMetrics,access-tasks,access-templateManagement,access-user-mgmt,access-work";
                List<GrantedAuthority> grantedAuthorities = Stream.of(grantedAuthoritiesStringList.split(","))
                        .collect(Collectors.toList()).stream().map((authority) -> {
                            return new SimpleGrantedAuthority(authority);
                        }).collect(Collectors.toList());

                if (username.equals("admin")) {
                    return new User(username, "", grantedAuthorities);
                } else {
                    throw new UsernameNotFoundException("The user '" + username + "' is not registered.");
                }
            }
        };
    }

}
