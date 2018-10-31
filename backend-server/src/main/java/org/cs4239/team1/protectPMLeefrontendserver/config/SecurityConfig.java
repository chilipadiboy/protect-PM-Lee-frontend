package org.cs4239.team1.protectPMLeefrontendserver.config;

import org.cs4239.team1.protectPMLeefrontendserver.security.JwtAuthenticationEntryPoint;
import org.cs4239.team1.protectPMLeefrontendserver.security.JwtAuthenticationFilter;
import org.cs4239.team1.protectPMLeefrontendserver.security.UserAuthentication;
import org.cs4239.team1.protectPMLeefrontendserver.security.UserAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserAuthentication userAuthentication;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .authenticationProvider(authProvider());
    }

    @Bean
    public AuthenticationProvider authProvider() {
        return new UserAuthenticationProvider(userAuthentication);
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                    .disable()
                .exceptionHandling()
                    .authenticationEntryPoint(unauthorizedHandler)
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                    .antMatchers("/api/auth/**")
                        .permitAll()
                    .antMatchers("/api/admin/**")
                        .hasRole("ADMINISTRATOR")
                    .antMatchers("/api/records/therapist/patient/**")
                        .hasRole("THERAPIST")
                    .antMatchers("/api/records/create/**")
                        .hasRole("THERAPIST")
                    .antMatchers("/api/permissions/permit/**")
                        .hasRole("PATIENT")
                    .antMatchers("/api/permissions/revoke/**")
                        .hasRole("PATIENT")
                    .antMatchers("/api/permissions/therapist/allowed/")
                        .hasRole("THERAPIST")
                    .antMatchers("/api/permissions/patient/given/")
                        .hasRole("PATIENT")
                    .antMatchers("/api/treatments/start/**")
                        .hasRole("ADMINISTRATOR")
                    .antMatchers("/api/treatments/stop/**")
                        .hasRole("ADMINISTRATOR")
                    .antMatchers("/api/treatments/getAll/")
                        .hasRole("ADMINISTRATOR")
                    .antMatchers("/api/treatments/getPatients/", "/api/treatments/getUserSummary/*")
                        .hasRole("THERAPIST")
                    .antMatchers("/api/treatments/getTherapists/")
                        .hasRole("PATIENT")
                    .antMatchers("/api/notes/create/**")
                        .hasAnyRole("THERAPIST","PATIENT")
                    .antMatchers("/api/notes/update/")
                        .hasAnyRole("THERAPIST","PATIENT")
                    .antMatchers("/api/notes/notePermission/")
                        .hasRole("THERAPIST")
                    .antMatchers("/api/notes/checkNoteIdConsent/**/")
                        .hasRole("THERAPIST")
                    .antMatchers("/api/notes/getPatient/")
                        .hasRole("THERAPIST")
                    .antMatchers("/api/notes/getOwn/")
                        .hasRole("PATIENT")
                    .antMatchers("/api/notes/getPermitted/")
                        .hasRole("PATIENT")
                    .antMatchers("/api/external/upload/**")
                        .hasRole("EXTERNAL_PARTNER")
                    .anyRequest()
                        .authenticated();

        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    }
}