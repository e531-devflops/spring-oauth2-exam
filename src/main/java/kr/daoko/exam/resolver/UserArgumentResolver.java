package kr.daoko.exam.resolver;

import kr.daoko.exam.annotation.SocialUser;
import kr.daoko.exam.model.SocialType;
import kr.daoko.exam.model.User;
import kr.daoko.exam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static kr.daoko.exam.model.SocialType.*;

@RequiredArgsConstructor
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(SocialUser.class) != null && parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpSession session = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        User user = (User)session.getAttribute("user");

        return getUser(user, session);
    }

    private User getUser(User user, HttpSession session) {
        if(user == null) {
            try {
                OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                Map<String, Object> map = token.getPrincipal().getAttributes();

                User convertUser = convertUser(token.getAuthorizedClientRegistrationId(), map);

                user = userRepository.findByEmail(convertUser.getEmail());
                if(user == null) {
                    user = userRepository.save(convertUser);
                }

                setRoleIfNotSame(user, token, map);
                session.setAttribute("user", user);
            } catch(ClassCastException e) {
                return user;
            }
        }

        return user;
    }

    private User convertUser(String authority, Map<String, Object> map) {
        if(GOOGLE.getValue().equals(authority)) {
            return getModernUser(GOOGLE, map);
        } else if(FACEBOOK.getValue().equals(authority)) {
            return getModernUser(FACEBOOK, map);
        } else if(KAKAO.getValue().equals(authority)) {
            return getKakaoUser(map);
        } else if(NAVER.getValue().equals(authority)) {
            return getNaverUser(map);
        }

        return null;
    }

    private User getModernUser(SocialType socialType, Map<String, Object> map) {
        return User.builder()
                .name(String.valueOf(map.get("name")))
                .email(String.valueOf(map.get("email")))
                .principal(String.valueOf(map.get("id")))
                .socialType(socialType)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    private User getKakaoUser(Map<String, Object> map) {
        HashMap<String, String> propertyMap = (HashMap<String, String>) map.get("properties");
        HashMap<String, String> accountMap = (HashMap<String, String>) map.get("kakao_account");

        return User.builder()
                .name(propertyMap.get("nickname"))
                .email(accountMap.get("email"))
                .principal(String.valueOf(map.get("id")))
                .socialType(KAKAO)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    private User getNaverUser(Map<String, Object> map) {
        HashMap<String, String> responseMap = (HashMap<String, String>) map.get("response");

        return User.builder()
                .name(responseMap.get("name"))
                .email(responseMap.get("email"))
                .principal(responseMap.get("id"))
                .socialType(NAVER)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
    }

    private void setRoleIfNotSame(User user, OAuth2AuthenticationToken token, Map<String, Object> map) {
        if(!token.getAuthorities().contains(new SimpleGrantedAuthority(user.getSocialType().getRoleType()))) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(map, "N/A", AuthorityUtils.createAuthorityList(user.getSocialType().getRoleType()))
            );
        }
    }
}
