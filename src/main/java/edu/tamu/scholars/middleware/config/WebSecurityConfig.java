package edu.tamu.scholars.middleware.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.Customizer.withDefaults;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestRepository;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import edu.tamu.scholars.middleware.auth.config.AuthConfig;
import edu.tamu.scholars.middleware.auth.config.Saml2Config;
import edu.tamu.scholars.middleware.auth.config.TokenConfig;
import edu.tamu.scholars.middleware.auth.details.CustomUserDetails;
import edu.tamu.scholars.middleware.auth.handler.CustomAccessDeniedExceptionHandler;
import edu.tamu.scholars.middleware.auth.handler.CustomAuthenticationEntryPoint;
import edu.tamu.scholars.middleware.auth.handler.CustomAuthenticationFailureHandler;
import edu.tamu.scholars.middleware.auth.handler.CustomAuthenticationSuccessHandler;
import edu.tamu.scholars.middleware.auth.handler.CustomLogoutSuccessHandler;
import edu.tamu.scholars.middleware.auth.handler.CustomSaml2AuthenticationSuccessHandler;
import edu.tamu.scholars.middleware.auth.model.Role;
import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.auth.model.repo.UserRepo;
import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;

/**
 * 
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    @Value("${spring.profiles.active:default}")
    private String profile;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Value("${server.servlet.session.cookie.domain:localhost}")
    private String domainName;

    @Value("${ui.url:http://localhost:4200}")
    protected String uiUrl;

    @Autowired
    private MiddlewareConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SecurityExpressionHandler<FilterInvocation> securityExpressionHandler;

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
        embedConfig.addAllowedMethod("OPTION");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/displayViews/search/findByName", embedConfig);
        source.registerCorsConfiguration("/individual/{id}", embedConfig);
        source.registerCorsConfiguration("/individual/search/findByIdIn", embedConfig);

        CorsConfiguration samlConfig = new CorsConfiguration();
        samlConfig.setAllowCredentials(false);
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
        serializer.setCookiePath("/");
        serializer.setCookieName("SESSION");
        serializer.setDomainName(domainName);
        return serializer;
    }

    // NoOp persistence of SAML request/response
    @Bean
    Saml2AuthenticationRequestRepository<AbstractSaml2AuthenticationRequest> authenticationRequestRepository() {
       return new Saml2AuthenticationRequestRepository() {
        @Override
        public AbstractSaml2AuthenticationRequest loadAuthenticationRequest(HttpServletRequest request) {
            return null;
        }
        @Override
        public void saveAuthenticationRequest(AbstractSaml2AuthenticationRequest authenticationRequest,
                HttpServletRequest request, HttpServletResponse response) {

        }
        @Override
        public AbstractSaml2AuthenticationRequest removeAuthenticationRequest(HttpServletRequest request,
                HttpServletResponse response) {
            return null;
        }
       };
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

        AuthConfig authConfig = config.getAuth();
        Saml2Config saml2Config = authConfig.getSaml2();

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
                Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();

                Map<String, String> attributeMap = saml2Config.getAttributeMap();

                Map<String, List<Object>> attributes = principal.getAttributes();

                final String FIRST_NAME = "firstName";
                final String LAST_NAME = "lastName";
                final String USERNAME = "username";

                String firstNameKey = attributeMap.containsKey(FIRST_NAME) ? attributeMap.get(FIRST_NAME) : FIRST_NAME;
                String lastNameKey = attributeMap.containsKey(LAST_NAME) ? attributeMap.get(LAST_NAME) : LAST_NAME;
                String usernameKey = attributeMap.containsKey(USERNAME) ? attributeMap.get(USERNAME) : USERNAME;

                List<Object> firstNames = attributes.containsKey(firstNameKey) ? attributes.get(firstNameKey) : Arrays.asList();
                if (firstNames.isEmpty() || StringUtils.isBlank(firstNames.get(0).toString())) {
                    throw new InsufficientAuthenticationException("SAML2 authentication response is missing required `" + firstNameKey + "` attribute");
                }
                List<Object> lastNames = attributes.containsKey(lastNameKey) ? attributes.get(lastNameKey) : Arrays.asList();
                if (lastNames.isEmpty() || StringUtils.isBlank(lastNames.get(0).toString())) {
                    throw new InsufficientAuthenticationException("SAML2 authentication response is missing required `" + lastNameKey + "` attribute");
                }
                List<Object> usernames = attributes.containsKey(usernameKey) ? attributes.get(usernameKey) : Arrays.asList();
                if (usernames.isEmpty() || StringUtils.isBlank(usernames.get(0).toString())) {
                    throw new InsufficientAuthenticationException("SAML2 authentication response is missing required `" + usernameKey + "` attribute");
                }

                User user = new User(
                    firstNames.get(0).toString(),
                    lastNames.get(0).toString(),
                    usernames.get(0).toString()
                );

                user.setActive(true);
                user.setConfirmed(true);
                user.setEnabled(true);
                user.setRole(Role.ROLE_USER);
                userDetails = new CustomUserDetails(userRepo.save(user));
            }

            return new Saml2Authentication((AuthenticatedPrincipal) userDetails, responseToken.getToken().getSaml2Response(), userDetails.getAuthorities());
        });

        if (enableH2Console()) {
            // NOTE: permit all access to h2console
            http
                .headers()
                    .frameOptions()
                        .sameOrigin();
        }
        http
            .authorizeRequests()
                .antMatchers("/login/saml2/**", "/saml2/**")
                    .permitAll()

                .expressionHandler(securityExpressionHandler)

                .antMatchers(PATCH,
                    "/dataAndAnalyticsViews/{id}",
                    "/directoryViews/{id}",
                    "/discoveryViews/{id}",
                    "/displayViews/{id}",
                    "/themes/{id}"
                    )
                    .hasRole("ADMIN")

                .antMatchers(PATCH,
                    "/users/{id}"
                ).hasRole("SUPER_ADMIN")

                .antMatchers(POST,
                    "/registration")
                    .permitAll()

                .antMatchers(POST,
                    "/dataAndAnalyticsViews/{id}",
                    "/directoryViews/{id}",
                    "/discoveryViews/{id}",
                    "/displayViews/{id}",
                    "/themes/{id}")
                    .hasRole("ADMIN")

                .antMatchers(POST, "/users/{id}")
                    .denyAll()

                .antMatchers(PUT, "/registration")
                    .permitAll()

                .antMatchers(PUT,
                    "/dataAndAnalyticsViews/{id}",
                    "/directoryViews/{id}",
                    "/discoveryViews/{id}",
                    "/displayViews/{id}",
                    "/themes/{id}")
                    .hasRole("ADMIN")

                .antMatchers(PUT, "/users/{id}")
                    .denyAll()

                .antMatchers(GET, "/user")
                    .hasRole("USER")

                .antMatchers(GET,
                    "/users",
                    "/users/{id}",
                    "/themes",
                    "/themes/{id}")
                    .hasRole("ADMIN")

                .antMatchers(DELETE,
                    "/dataAndAnalyticsViews/{id}",
                    "/directoryViews/{id}",
                    "/discoveryViews/{id}",
                    "/displayViews/{id}",
                    "/themes/{id}")
                    .hasRole("ADMIN")

                .antMatchers(DELETE, "/users/{id}")
                    .hasRole("SUPER_ADMIN")

                .anyRequest()
                    .permitAll()

            .and()
                .saml2Login(saml2 -> saml2
                    .authenticationManager(new ProviderManager(authenticationProvider))
                    .successHandler(new CustomSaml2AuthenticationSuccessHandler(uiUrl))
                    .failureHandler(authenticationFailureHandler()))
                .formLogin()
                    .successHandler(authenticationSuccessHandler())
                    .failureHandler(authenticationFailureHandler())
                        .permitAll()
            .and()
                .saml2Logout(withDefaults())
                .logout()
                    .deleteCookies("SESSION")
                    .invalidateHttpSession(true)
                    .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll()
            .and()
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
            .and()
                .requestCache()
                    .requestCache(nullRequestCache())
            .and()
                .cors()
            .and()
                .csrf()
                    .disable();

        http.sessionManagement()
            .sessionFixation()
                .migrateSession()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

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
