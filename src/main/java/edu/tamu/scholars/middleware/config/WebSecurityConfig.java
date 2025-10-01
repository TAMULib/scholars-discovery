package edu.tamu.scholars.middleware.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.Customizer.withDefaults;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
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
import edu.tamu.scholars.middleware.auth.handler.CustomSaml2AuthenticationSuccessHandler;
import edu.tamu.scholars.middleware.auth.service.ExternalAuthUserDetailsService;
import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;

/**
 * Spring Web Security autoconfiguration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    private final MiddlewareConfig config;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final ExternalAuthUserDetailsService<Saml2Authentication> userDetailsService;

    @Value("${spring.profiles.active:default}")
    private String profile;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Value("${server.servlet.session.cookie.domain:localhost}")
    private String domainName;

    @Value("${ui.url:http://localhost:4200}")
    protected String uiUrl;

    public WebSecurityConfig(
        MiddlewareConfig config,
        ObjectMapper objectMapper,
        MessageSource messageSource,
        ExternalAuthUserDetailsService<Saml2Authentication> userDetailsService
    ) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    TokenService tokenService() throws NoSuchAlgorithmException {
        KeyBasedPersistenceTokenService tokenService = new KeyBasedPersistenceTokenService();
        TokenConfig tokenConfig = config.getAuth().getToken();
        tokenService.setServerInteger(tokenConfig.getServerInteger());
        tokenService.setServerSecret(tokenConfig.getServerSecret());
        tokenService.setPseudoRandomNumberBytes(tokenConfig.getPseudoRandomNumberBytes());
        tokenService.setSecureRandom(SecureRandom.getInstanceStrong());
        return tokenService;
    }

    @Bean
    CorsFilter corsFilter() {
        CorsConfiguration embedConfig = new CorsConfiguration();
        embedConfig.setAllowCredentials(true);
        embedConfig.setAllowedOriginPatterns(Arrays.asList("*"));
        embedConfig.addAllowedHeader("Origin");
        embedConfig.addAllowedHeader("Content-Type");
        embedConfig.addAllowedMethod("GET");
        embedConfig.addAllowedMethod("POST");
        embedConfig.addAllowedMethod("OPTIONS");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/displayViews/search/findByName", embedConfig);
        source.registerCorsConfiguration("/individual/{id}", embedConfig);
        source.registerCorsConfiguration("/individual/search/findByIdIn", embedConfig);
        source.registerCorsConfiguration("/individual/*/export", embedConfig);
        source.registerCorsConfiguration("/individual/{id}", embedConfig);
        source.registerCorsConfiguration("/individual/export", embedConfig);
        source.registerCorsConfiguration("/individual/**", embedConfig);

        CorsConfiguration samlConfig = new CorsConfiguration();
        samlConfig.setAllowCredentials(true);
        samlConfig.setAllowedOriginPatterns(Arrays.asList("*"));
        samlConfig.addAllowedHeader("*");
        samlConfig.addAllowedMethod("POST");
        samlConfig.addAllowedMethod("GET");
        samlConfig.addAllowedMethod("OPTIONS");
        samlConfig.setMaxAge(3600L);

        source.registerCorsConfiguration("/login/saml2/**", samlConfig);
        source.registerCorsConfiguration("/saml2/**", samlConfig);

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
    LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    @Bean
    CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(true);
        serializer.setSameSite("None"); // Add this line
        serializer.setCookiePath("/");
        serializer.setCookieName("SESSION");
        serializer.setDomainName(domainName);
        return serializer;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        OpenSaml4AuthenticationProvider authenticationProvider = new OpenSaml4AuthenticationProvider();

        Converter<ResponseToken, Saml2Authentication> delegate =
            OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

        authenticationProvider.setResponseAuthenticationConverter(responseToken -> {

            final Saml2Authentication authentication = delegate.convert(responseToken);

            final String username = authentication.getName();
            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch(UsernameNotFoundException e) {
                userDetails = userDetailsService.loadUserFromExternalAuthentication(authentication);
            }

            return new Saml2Authentication((AuthenticatedPrincipal) userDetails, responseToken.getToken().getSaml2Response(), userDetails.getAuthorities());
        });

        if (enableH2Console()) {
            // NOTE: permit all access to h2console
            http
                .headers(headers -> headers
                    .frameOptions(FrameOptionsConfig::sameOrigin)
                );
        }

        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login/saml2/**", "/saml2/**")
                    .permitAll()

                .requestMatchers(POST, "/individual/export").permitAll()

                .requestMatchers(POST, "/individual/*/export").permitAll()
                
                .requestMatchers(PATCH,
                    "/dataAndAnalyticsViews/{id}",
                    "/directoryViews/{id}",
                    "/discoveryViews/{id}",
                    "/displayViews/{id}",
                    "/themes/{id}"
                    )
                    .hasRole("ADMIN")

                .requestMatchers(PATCH, "/users/{id}")
                    .hasRole("SUPER_ADMIN")

                .requestMatchers(POST, "/registration")
                    .permitAll()

                .requestMatchers(POST,
                    "/dataAndAnalyticsViews/{id}",
                    "/directoryViews/{id}",
                    "/discoveryViews/{id}",
                    "/displayViews/{id}",
                    "/themes/{id}")
                    .hasRole("ADMIN")

                .requestMatchers(POST, "/users/{id}")
                    .denyAll()

                .requestMatchers(PUT, "/registration")
                    .permitAll()

                .requestMatchers(PUT,
                    "/dataAndAnalyticsViews/{id}",
                    "/directoryViews/{id}",
                    "/discoveryViews/{id}",
                    "/displayViews/{id}",
                    "/themes/{id}")
                    .hasRole("ADMIN")

                .requestMatchers(PUT, "/users/{id}")
                    .denyAll()

                .requestMatchers(GET, "/user")
                    .hasRole("USER")

                .requestMatchers(GET,
                    "/users",
                    "/users/{id}",
                    "/themes",
                    "/themes/{id}")
                    .hasRole("ADMIN")

                .requestMatchers(DELETE,
                    "/dataAndAnalyticsViews/{id}",
                    "/directoryViews/{id}",
                    "/discoveryViews/{id}",
                    "/displayViews/{id}",
                    "/themes/{id}")
                    .hasRole("ADMIN")

                .requestMatchers(DELETE, "/users/{id}")
                    .hasRole("SUPER_ADMIN")

                .anyRequest()
                    .permitAll()
            )
            .saml2Login(saml2 -> saml2
                .authenticationManager(new ProviderManager(authenticationProvider))
                .successHandler(new CustomSaml2AuthenticationSuccessHandler(uiUrl))
                .failureHandler(authenticationFailureHandler()))
            .formLogin(form -> form
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll())
            .saml2Logout(withDefaults())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .deleteCookies("SESSION")
                .invalidateHttpSession(true)
                .logoutSuccessHandler(logoutSuccessHandler())
                .permitAll())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler()))
            .requestCache(cache -> cache
                .requestCache(nullRequestCache()))
            .cors(withDefaults())
            .csrf(csrf -> csrf.disable());

        http.sessionManagement(session -> session
            .sessionFixation().migrateSession()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }

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
