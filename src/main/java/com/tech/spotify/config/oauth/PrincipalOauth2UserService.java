package com.tech.spotify.config.oauth;

import com.tech.spotify.Repository.UserRepository;
import com.tech.spotify.config.auth.OAuthPrincipalDetails;
import com.tech.spotify.config.oauth.provider.GoogleUserInfo;
import com.tech.spotify.config.oauth.provider.NaverUserInfo;
import com.tech.spotify.config.oauth.provider.OAuth2UserInfo;
import com.tech.spotify.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("userRequest : " + userRequest);
        System.out.println("getClientRegistration : " + userRequest.getClientRegistration()); // registraionId로 어떤 OAuth로 로그인 했는지 확인 가능
        System.out.println("userRequest.getClientRegistration().getRegistrationId() = " + userRequest.getClientRegistration().getRegistrationId());
        OAuth2User oauth2User = super.loadUser(userRequest);
        // 구글 로그인 버튼 클릭 -> 구글 로그인 창 -> 로그인 완료 -> code 리턴(OAuth2-Client 라이브러리) -> AccessToken 요청
        System.out.println("getAccessToken : " + userRequest.getAccessToken());
        System.out.println("oAuth2User.getAttributes() : " + oauth2User.getAttributes());

        // 회원가입 강제로 진행
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map) oauth2User.getAttributes().get("response"));
        } else {
            System.out.println("우리는 구글,네이버 지원해요");
        }

        String provider = oAuth2UserInfo.getProvider();
        String providerID = oAuth2UserInfo.getProviderId();
        String username = provider + "_" + providerID;
        String password = bCryptPasswordEncoder.encode("겟인데어");
        String roles = "ROLE_USER";
        String email = oAuth2UserInfo.getEmail();

        User userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .roles(roles)
                    .email(email)
                    .provider(provider)
                    .providerID(providerID)
                    .build();
            userRepository.save(userEntity);
        }

        return new OAuthPrincipalDetails(userEntity, oauth2User.getAttributes());
    }
}
