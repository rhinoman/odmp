package io.opendmp.dataflow.config

import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler
import reactor.core.publisher.Mono
import java.util.stream.Collectors


@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfiguration {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity) : SecurityWebFilterChain {
        http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/dataflow_api/dataflow/**").hasAnyAuthority("user")
                .pathMatchers("/dataflow_api/processor/**").hasAnyAuthority("user")
                .pathMatchers("/").permitAll()
                .pathMatchers("/dataflow_api/doc/**").permitAll()
                .anyExchange().authenticated()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(grantedAuthoritiesExtractor())
        return http.build()
    }

    @Bean
    fun grantedAuthoritiesExtractor(): Converter<Jwt?, Mono<AbstractAuthenticationToken?>?>? {
        val extractor = GrantedAuthoritiesExtractor()
        return ReactiveJwtAuthenticationConverterAdapter(extractor)
    }

    internal class GrantedAuthoritiesExtractor : JwtAuthenticationConverter() {
        @Suppress("UNCHECKED_CAST")
        override fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
            var roles: List<String?>? = emptyList()
            val resource = jwt.getClaimAsMap("resource_access")
            if (resource.containsKey("opendmp-dataflow")) {
                roles = (resource["opendmp-dataflow"] as Map<String?, List<String?>?>?)!!["roles"]
            }
            return roles!!.stream().map { role: String? -> SimpleGrantedAuthority(role) }.collect(Collectors.toList())
        }
    }

}