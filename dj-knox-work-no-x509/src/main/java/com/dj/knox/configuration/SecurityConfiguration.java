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
        http
            // If we're going to enable it, better to allow token to be pushed through cookies.
            // That way we could retrieve it using simple HEAD requests to login endpoint.
            // .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
            .csrf().disable()

            // Frontend would eventually load browser's PDF viewer using object/embed
            // To avoid problems in some scenarios, we need to do set X-Frame-Options sameorigin;
            .headers().frameOptions().sameOrigin()

            // In case you want to disable JSESSIONID cookies.
            // Flowable's Authentication class expects AuthenticationSuccessEvent to be fired
            // every time, and sending the cookie makes it fail from time to time. Check class
            // SecurityAutoConfiguration for insights on how is this configured.
            // Two things to notice:
            // 1. You have to add basic auth to /frontend/engage/api/auth.ts
            //    Axios.defaults.auth = { username, password };
            // 2. WebSockets+SocksJS, https://github.com/sockjs/sockjs-client/issues/196
            //.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            //.and()
            .and()
            .rememberMe()
            .key(key)
            .rememberMeServices(rememberMeServicesObjectProvider.getIfAvailable())
            .and()
            .logout()
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
            .logoutUrl("/auth/logout")
            .and()
            // Non authenticated exception handling. The formLogin and httpBasic configure the exceptionHandling
            // We have to initialize the exception handling with a default authentication entry point in order to return 401 each time and not have a
            // forward due to the formLogin or the http basic popup due to the httpBasic
            .exceptionHandling()
            .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), AnyRequestMatcher.INSTANCE)
            .and()
            .formLogin()
            .loginProcessingUrl("/auth/login")
            .successHandler(new AjaxAuthenticationSuccessHandler())
            .failureHandler(new AjaxAuthenticationFailureHandler())
            .and()
            .authorizeRequests()
            .antMatchers("/analytics-api/**").hasAuthority(SecurityConstants.ACCESS_REPORTS_METRICS)
            .antMatchers("/template-api/**").hasAuthority(SecurityConstants.ACCESS_TEMPLATE_MANAGEMENT)
            .antMatchers("/work-object-api/**").hasAuthority(SecurityConstants.ACCESS_WORKOBJECT_API)
            // allow context root for all (it triggers the loading of the initial page)
            .antMatchers("/").permitAll()
            .antMatchers(
                    "/**/*.svg", "/**/*.ico", "/**/*.png", "/**/*.woff2", "/**/*.css",
                    "/**/*.woff", "/**/*.html", "/**/*.js",
                    "/**/index.html").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
