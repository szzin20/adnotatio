package com.exacc.adnotatio.user.service;

import com.exacc.adnotatio.user.entity.User;
import com.exacc.adnotatio.user.repository.UserRepository;
import com.exacc.adnotatio.user.repository.UserRoleRepository;
import com.exacc.adnotatio.user.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        var role = userRoleRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        return new UserPrincipal(user, role.getRole());
    }
}
