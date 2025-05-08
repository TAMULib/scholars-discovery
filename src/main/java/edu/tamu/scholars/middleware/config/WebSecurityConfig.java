package edu.tamu.scholars.middleware.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.lang.System.LoggerFinder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.servlet.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationRequestFilter;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import edu.tamu.scholars.middleware.auth.config.TokenConfig;
import edu.tamu.scholars.middleware.auth.handler.CustomAccessDeniedExceptionHandler;
import edu.tamu.scholars.middleware.auth.handler.CustomAuthenticationEntryPoint;
import edu.tamu.scholars.middleware.auth.handler.CustomAuthenticationFailureHandler;
import edu.tamu.scholars.middleware.auth.handler.CustomAuthenticationSuccessHandler;
import edu.tamu.scholars.middleware.auth.handler.CustomLogoutSuccessHandler;
import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;

/**
 * 
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.profiles.active:default}")
    private String profile;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Value("${server.servlet.session.cookie.domain:library.tamu.edu}")
    private String domainName;

    @Autowired
    private MiddlewareConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SecurityExpressionHandler<FilterInvocation> securityExpressionHandler;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        logger.info("\n\n\n WebSecurityConfig configureGlobal \n\n\n");
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenService tokenService() throws NoSuchAlgorithmException {
        KeyBasedPersistenceTokenService tokenService = new KeyBasedPersistenceTokenService();
        TokenConfig tokenConfig = config.getAuth().getToken();
        tokenService.setServerInteger(tokenConfig.getServerInteger());
        tokenService.setServerSecret(tokenConfig.getServerSecret());
        tokenService.setPseudoRandomNumberBytes(tokenConfig.getPseudoRandomNumberBytes());
        tokenService.setSecureRandom(SecureRandom.getInstanceStrong());
        return tokenService;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration embedConfig = new CorsConfiguration();
        embedConfig.setAllowCredentials(true);
        embedConfig.setAllowedOriginPatterns(Arrays.asList("*"));
        embedConfig.addAllowedHeader("Origin");
        embedConfig.addAllowedHeader("Content-Type");
        embedConfig.addAllowedMethod("GET");
        embedConfig.addAllowedMethod("OPTION");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/displayViews/search/findByName", embedConfig);
        source.registerCorsConfiguration("/individual/{id}", embedConfig);
        source.registerCorsConfiguration("/individual/search/findByIdIn", embedConfig);

        CorsConfiguration primaryConfig = new CorsConfiguration();
        primaryConfig.setAllowCredentials(true);
        primaryConfig.setAllowedOrigins(config.getAllowedOrigins());
        primaryConfig.setAllowedMethods(Arrays.asList(
            "GET",
            "DELETE",
            "PUT",
            "POST",
            "PATCH",
            "OPTIONS"
        ));
        primaryConfig.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Origin",
            "Content-Type",
            "Content-Disposition"
        ));
        primaryConfig.setExposedHeaders(Arrays.asList("Content-Disposition"));

        // NOTE: most general path must be last
        source.registerCorsConfiguration("/**", primaryConfig);
        return new CorsFilter(source);
    }

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setUseHttpOnlyCookie(false);
        serializer.setUseSecureCookie(false);
        serializer.setCookiePath("/");
        serializer.setCookieName("SESSION");
        serializer.setDomainName(domainName);
        return serializer;
    }


    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        logger.info("\n\n\n SecurityFilterChain setup started: \n");

        http.getSharedObjects().forEach((key, value) ->
            logger.info("\n\n\n Shared Object: {} = {}", key.getSimpleName(), value)
        );
        logger.info("\n\n\n-----\n\n\n");
        if (enableH2Console()) {
            // NOTE: permit all access to h2console
            http
                .headers()
                    .frameOptions()
                        .sameOrigin();
        }

    http
        .authorizeRequests()
            .expressionHandler(securityExpressionHandler)
            .antMatchers("/login/saml2/**", "/saml2/**")
                .permitAll()
            .antMatchers(PATCH, "/dataAndAnalyticsViews/{id}", "/directoryViews/{id}",
                "/discoveryViews/{id}", "/displayViews/{id}", "/themes/{id}")
                .hasRole("ADMIN")
            .antMatchers(PATCH, "/users/{id}")
                .hasRole("SUPER_ADMIN")
            .antMatchers(POST, "/registration")
                .permitAll()
            .antMatchers(POST, "/dataAndAnalyticsViews/{id}", "/directoryViews/{id}",
                "/discoveryViews/{id}", "/displayViews/{id}", "/themes/{id}")
                .hasRole("ADMIN")
            .antMatchers(POST, "/users/{id}")
                .denyAll()
            .antMatchers(PUT, "/registration")
                .permitAll()
            .antMatchers(PUT, "/dataAndAnalyticsViews/{id}", "/directoryViews/{id}",
                "/discoveryViews/{id}", "/displayViews/{id}", "/themes/{id}")
                .hasRole("ADMIN")
            .antMatchers(PUT, "/users/{id}")
                .denyAll()
            .antMatchers(GET, "/user")
                .hasRole("USER")
            .antMatchers(GET, "/users", "/users/{id}", "/themes", "/themes/{id}")
                .hasRole("ADMIN")
            .antMatchers(DELETE, "/dataAndAnalyticsViews/{id}", "/directoryViews/{id}",
                "/discoveryViews/{id}", "/displayViews/{id}", "/themes/{id}")
                .hasRole("ADMIN")
            .antMatchers(DELETE, "/users/{id}")
                .hasRole("SUPER_ADMIN")
            .anyRequest()
                .permitAll();

    http
        .requestCache(requestCache -> requestCache
            .requestCache(new NullRequestCache()));
    http
    .csrf(csrf -> csrf.disable())
    .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    )
    .saml2Login(saml2 -> saml2
        .loginPage("/saml2/authenticate/my-idp")
        .successHandler(authenticationSuccessHandler())
        .failureHandler(authenticationFailureHandler())
        .permitAll()
    );
//         .authenticationRequestRepository(new StatelessSaml2AuthenticationRequestRepository())

    http
        .logout(logout -> logout
            .deleteCookies("SESSION")
            .invalidateHttpSession(true)
            .logoutSuccessHandler(logoutSuccessHandler())
            .permitAll());

    http
        .exceptionHandling(handling -> handling
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler()));

    http
        .csrf(csrf -> csrf
            .disable());

    http
        .sessionManagement(management -> management
            .sessionFixation()
            .migrateSession()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

    return http.build();
    }

    /*

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        RelyingPartyRegistration registration = RelyingPartyRegistrations
            .fromMetadataLocation("https://login.microsoftonline.com/{tenant-id}/federationmetadata/2007-06/federationmetadata.xml")
            .registrationId("entra-id")
            .build();
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    */

    private CustomAuthenticationSuccessHandler authenticationSuccessHandler() {

        return new CustomAuthenticationSuccessHandler(objectMapper);
    }

    private CustomAuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    private CustomLogoutSuccessHandler logoutSuccessHandler() {
        return new CustomLogoutSuccessHandler(messageSource);
    }

    private CustomAuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    private CustomAccessDeniedExceptionHandler accessDeniedHandler() {
        return new CustomAccessDeniedExceptionHandler();
    }

    private NullRequestCache nullRequestCache() {
        return new NullRequestCache();
    }

    private boolean enableH2Console() {
        return h2ConsoleEnabled && !profile.equals("production");
    }

}
