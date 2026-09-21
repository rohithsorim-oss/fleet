package com.sorim.fleetmanagement.config;

import com.sorim.fleetmanagement.security.CustomUserDetailsService;
import com.sorim.fleetmanagement.security.JwtAuthenticationFilter;
import com.sorim.fleetmanagement.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtFilterConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider, 
                                                            CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }
}
