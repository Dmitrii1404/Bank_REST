package com.example.bankcards.security;

import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceCustom implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsCustom loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).map(UserDetailsCustom::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
