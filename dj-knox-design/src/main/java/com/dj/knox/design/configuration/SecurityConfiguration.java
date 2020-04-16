package com.dj.knox.design.configuration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.flowable.design.domain.editor.ModelerUser;
import com.flowable.design.repository.editor.ModelerUserRepository;
import com.flowable.design.security.ModelerUserDetailsService;

import org.assertj.core.util.Strings;
import org.flowable.ui.common.security.FlowableAppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

import lombok.RequiredArgsConstructor;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    final protected ModelerUserRepository userRepository;
    final protected UserDetailsService modelerUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().headers().frameOptions().sameOrigin().and().exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        AnyRequestMatcher.INSTANCE)
                .and().x509().subjectPrincipalRegex("CN=(.*?)(?:,|$)").userDetailsService(modelerUserDetailsService);
    }

}
