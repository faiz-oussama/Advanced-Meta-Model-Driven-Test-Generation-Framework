package ${basePackage}.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/**").permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        List<UserDetails> users = new ArrayList<>();
        
<#if hasSecurityRules && allSecurityRoles?has_content>
        <#-- Create users based on actual roles from security rules -->
        <#list allSecurityRoles as role>
        UserDetails ${role?lower_case}User = User.builder()
                .username("${role?lower_case}")
                .password(passwordEncoder().encode("${role?lower_case}123"))
                .roles("${role}")
                .build();
        users.add(${role?lower_case}User);
        
        </#list>
        <#-- Create a super admin with all roles for comprehensive testing -->
        UserDetails superAdmin = User.builder()
                .username("superadmin")
                .password(passwordEncoder().encode("superadmin123"))
                .roles(<#list allSecurityRoles as role>"${role}"<#if role_has_next>, </#if></#list>)
                .build();
        users.add(superAdmin);
<#else>
        <#-- Fallback users when no security rules exist -->
        UserDetails adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER")
                .build();
        users.add(adminUser);

        UserDetails regularUser = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user123"))
                .roles("USER")
                .build();
        users.add(regularUser);
</#if>

        return new InMemoryUserDetailsManager(users);
    }
}
