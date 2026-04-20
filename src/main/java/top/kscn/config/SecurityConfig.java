package top.kscn.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.kscn.filter.JwtAuthenticationFilter;
import top.kscn.handler.SecurityAccessDeniedHandler;
import top.kscn.handler.SecurityAuthEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityAuthEntryPoint securityAuthEntryPoint;
    private final SecurityAccessDeniedHandler securityAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 禁用CSRF
                .csrf(csrf -> csrf.disable())

                // 启用CORS
                .cors(cors -> {})

                // 禁用Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 认证
                .authorizeHttpRequests(auth -> auth
                        // 管理员专属
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 其余在接口中处理
                        .anyRequest().permitAll()
                )

                // 异常处理
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(securityAuthEntryPoint)
                        .accessDeniedHandler(securityAccessDeniedHandler)
                )

                // JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
