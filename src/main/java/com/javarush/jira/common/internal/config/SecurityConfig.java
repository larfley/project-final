package com.javarush.jira.common.internal.config;

import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.Role;
import com.javarush.jira.login.internal.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Slf4j
@AllArgsConstructor
//https://stackoverflow.com/questions/72493425/548473
public class SecurityConfig {
    public static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserRepository userRepository;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PASSWORD_ENCODER;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            log.debug("Authenticating '{}'", email);
            return new AuthUser(userRepository.getExistedByEmail(email));
        };
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**").authorizeHttpRequests()
                .requestMatchers("/api/admin/**").hasRole(Role.ADMIN.name())
                .requestMatchers("/api/mngr/**").hasAnyRole(Role.ADMIN.name(), Role.MANAGER.name())
                .requestMatchers(HttpMethod.POST, "/api/users").anonymous()
                .requestMatchers("/api/**").authenticated()
                .and().httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER) // support sessions Cookie for UI ajax
                .and().csrf().disable();
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers("/view/unauth/**", "/ui/register/**", "/ui/password/**").anonymous()
                .requestMatchers("/", "/doc", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/static/**").permitAll()
                .requestMatchers("/ui/admin/**", "/view/admin/**").hasRole(Role.ADMIN.name())
                .requestMatchers("/ui/mngr/**").hasAnyRole(Role.ADMIN.name(), Role.MANAGER.name())
                .anyRequest().authenticated()
                .and().formLogin().permitAll()
                .loginPage("/view/login")
                .defaultSuccessUrl("/", true)
                .loginPage("/view/login")
                .defaultSuccessUrl("/", true)
                .and().logout()
                .logoutUrl("/ui/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .and().csrf().disable();
        return http.build();
    }

}
