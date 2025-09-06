package taro.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taro.domain.SocialProvider;
import taro.domain.User;
import taro.domain.UserRole;
import taro.repository.UserRepository;
import taro.security.CustomUserDetails;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        
        if (oAuth2UserInfo.getEmail() == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        User user = saveOrUpdate(oAuth2UserInfo, SocialProvider.valueOf(registrationId.toUpperCase()));
        
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
    
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return new KakaoOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
    }
    
    private User saveOrUpdate(OAuth2UserInfo oAuth2UserInfo, SocialProvider socialProvider) {
        User user = userRepository.findBySocialProviderAndSocialId(socialProvider, oAuth2UserInfo.getId())
                .orElse(null);
        
        if (user == null) {
            user = User.builder()
                    .email(oAuth2UserInfo.getEmail())
                    .nickname(oAuth2UserInfo.getName())
                    .profileImageUrl(oAuth2UserInfo.getImageUrl())
                    .socialProvider(socialProvider)
                    .socialId(oAuth2UserInfo.getId())
                    .role(UserRole.ROLE_USER)
                    .build();
        } else {
            user.setNickname(oAuth2UserInfo.getName());
            user.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
        }
        
        return userRepository.save(user);
    }
}
