package com.clinic.clinic.security;

import com.clinic.clinic.JpaRepo.UserJpaRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("user")
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserJpaRepo userJpaRepo;


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        return userJpaRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException(userEmail));
    }
}
