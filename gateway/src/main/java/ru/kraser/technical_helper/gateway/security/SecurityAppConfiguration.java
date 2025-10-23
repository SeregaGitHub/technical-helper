package ru.kraser.technical_helper.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kraser.technical_helper.common_module.model.JwtUserDetails;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.gateway.client.UserClient;
//import ru.kraser.technical_helper.main_server.repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityAppConfiguration {
    //private final UserRepository userRepository;
    //private final AuthenticationClient authenticationClient;
    private final UserClient userClient;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                /*User user = userRepository.findUserByUsernameAndEnabledTrue(username).orElseThrow(
                        () -> new NotFoundException("Пользователь с логином - " + username + ", не был найден.")
                );*/
                User user = userClient.getUserByName(username);
                return new JwtUserDetails(user);
            }
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
