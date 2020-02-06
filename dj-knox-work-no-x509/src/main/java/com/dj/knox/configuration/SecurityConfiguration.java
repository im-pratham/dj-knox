package com.dj.knox.configuration;

import java.security.cert.X509Certificate;

import com.flowable.core.spring.security.web.authentication.AjaxAuthenticationFailureHandler;
import com.flowable.core.spring.security.web.authentication.AjaxAuthenticationSuccessHandler;
import com.flowable.platform.common.security.SecurityConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@Order(10)
@EnableWebSecurity
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
        http.csrf().disable().headers().frameOptions().sameOrigin()

                .and().rememberMe().key(key).rememberMeServices(rememberMeServicesObjectProvider.getIfAvailable()).and()
                .logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()).logoutUrl("/auth/logout")
                .and().exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        AnyRequestMatcher.INSTANCE)
                .and().formLogin().loginProcessingUrl("/auth/login")
                .successHandler(new AjaxAuthenticationSuccessHandler())
                .failureHandler(new AjaxAuthenticationFailureHandler()).and().authorizeRequests()
                .antMatchers("/analytics-api/**").hasAuthority(SecurityConstants.ACCESS_REPORTS_METRICS)
                .antMatchers("/template-api/**").hasAuthority(SecurityConstants.ACCESS_TEMPLATE_MANAGEMENT)
                .antMatchers("/work-object-api/**").hasAuthority(SecurityConstants.ACCESS_WORKOBJECT_API)
                .antMatchers("/").permitAll()
                .antMatchers("/**/*.svg", "/**/*.ico", "/**/*.png", "/**/*.woff2", "/**/*.css", "/**/*.woff",
                        "/**/*.html", "/**/*.js", "/**/index.html")
                .permitAll().anyRequest().authenticated().and().httpBasic();
    }
}
