package com.example.bankcards.security;

import com.example.bankcards.entity.BankUser;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) {

    BankUser bankUser = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    UserRole role = bankUser.getRole();
    List<GrantedAuthority> authorities = List.of(
        new SimpleGrantedAuthority("ROLE_" + role.getName()));

    return new User(bankUser.getUsername(), bankUser.getPassword(), authorities);
  }
}
