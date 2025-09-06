package taro.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taro.domain.User;
import taro.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 먼저 loginId로 검색 (일반 로그인)
        User user = userRepository.findByLoginId(username)
                .orElseGet(() -> 
                    // loginId로 못 찾으면 email로 검색 (소셜 로그인)
                    userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                );
        
        return new CustomUserDetails(user);
    }
}
