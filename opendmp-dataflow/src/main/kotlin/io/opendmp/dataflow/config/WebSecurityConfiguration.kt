package io.opendmp.dataflow.config

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfiguration{
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity) : SecurityWebFilterChain {
        return http
                .authorizeExchange()
                .pathMatchers("/**")
                .permitAll()
                .and()
                .csrf().disable()
                .build()
    }
}