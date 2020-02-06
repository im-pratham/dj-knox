package com.dj.knox.design.configuration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.flowable.design.domain.editor.ModelerUser;

import org.flowable.ui.common.security.FlowableAppUser;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().headers().frameOptions().sameOrigin().and().exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        AnyRequestMatcher.INSTANCE)
                .and().x509().subjectPrincipalRegex("CN=(.*?)(?:,|$)").userDetailsService(userDetailsService());
    }

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                String grantedAuthoritiesStringList = "access-admin,access-control,admin";
                List<GrantedAuthority> grantedAuthorities = Stream.of(grantedAuthoritiesStringList.split(","))
                        .collect(Collectors.toList()).stream().map((authority) -> {
                            return new SimpleGrantedAuthority(authority);
                        }).collect(Collectors.toList());

                if (username.equals("admin")) {
                    ModelerUser user = new ModelerUser();
                    user.setUsername("admin");
                    user.setPassword("");
                    user.setFirstName("Admin");
                    user.setLastName("O'Admin");
                    user.setDisplayName("Admin");
                    user.setEmail("admin@flowable.com");
                    user.setAdmin(true);
                    return new FlowableAppUser(user, username, grantedAuthorities);
                } else {
                    throw new UsernameNotFoundException("The user '" + username + "' is not registered.");
                }
            }
        };
    }

}
