package com.rh360.rh360.config;

import com.rh360.rh360.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private Environment environment;

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthenticationFilter);
        registration.addUrlPatterns("/api/*");
        // Ordem 1 para executar após o CORS (que tem ordem padrão 0)
        registration.setOrder(1);
        return registration;
    }
    
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsFilter corsFilter) {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(corsFilter);
        registration.addUrlPatterns("/*");
        // Ordem 0 para executar primeiro, antes do JWT filter
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Obter origens permitidas da configuração, padrão "*"
        String allowedOrigins = environment.getProperty("cors.allowed-origins", "*");
        
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        // Para desenvolvimento: usar allowedOriginPatterns com "*" 
        // Isso permite qualquer origem dinamicamente
        if ("*".equals(allowedOrigins)) {
            // Permitir todas as origens usando pattern
            // Isso permite que qualquer origem seja aceita dinamicamente
            corsConfiguration.setAllowedOriginPatterns(List.of("*"));
            // Permitir credentials mesmo com pattern (funciona com allowedOriginPatterns)
            corsConfiguration.setAllowCredentials(true);
        } else {
            // Permitir origens específicas (permite credentials)
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            corsConfiguration.setAllowedOrigins(origins.stream().map(String::trim).toList());
            corsConfiguration.setAllowCredentials(true);
        }
        
        // Métodos HTTP permitidos
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Headers permitidos (incluindo Authorization)
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        
        // Headers expostos para o cliente
        corsConfiguration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // Tempo de cache para preflight requests
        corsConfiguration.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowedOrigins = environment.getProperty("cors.allowed-origins", "*");
        
        if ("*".equals(allowedOrigins)) {
            registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .exposedHeaders("Authorization", "Content-Type")
                    .allowCredentials(true)
                    .maxAge(3600);
        } else {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            registry.addMapping("/**")
                    .allowedOrigins(origins.stream().map(String::trim).toArray(String[]::new))
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .exposedHeaders("Authorization", "Content-Type")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }
}
