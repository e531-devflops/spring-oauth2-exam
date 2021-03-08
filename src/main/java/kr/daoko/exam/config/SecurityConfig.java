package kr.daoko.exam.config;

import kr.daoko.exam.provider.ThirdOAuth2Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static kr.daoko.exam.model.SocialType.FACEBOOK;
import static kr.daoko.exam.model.SocialType.GOOGLE;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/oauth2/**", "/login/**", "/css/**", "/images/**", "/js/**", "/console/**", "/favicon.ico/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().defaultSuccessUrl("/success").failureUrl("/failure")
                .and()
                .headers().frameOptions().disable()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .and()
                .formLogin().successForwardUrl("/success")
                .and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/").deleteCookies("JSESSIONID").invalidateHttpSession(true)
                .and()
                .csrf().disable();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            OAuth2ClientProperties properties,
            @Value("${custom.oauth2.kakao.client-id}") String kakaoClientId,
            @Value("${custom.oauth2.kakao.client-secret}") String kakaoClientSecret,
            @Value("${custom.oauth2.naver.client-id}") String naverClientId,
            @Value("${custom.oauth2.naver.client-secret}") String naverClientSecret) {
        List<ClientRegistration> registrations = properties.getRegistration().keySet().stream()
                .map(client -> getRegistration(properties, client))
                .filter(Objects::nonNull).collect(Collectors.toList());

        registrations.add(
                ThirdOAuth2Provider.KAKAO.getBuilder("kakao")
                        .clientId(kakaoClientId)
                        .clientSecret(kakaoClientSecret)
                        .jwkSetUri("tmp")
                        .build()
        );

        registrations.add(
                ThirdOAuth2Provider.NAVER.getBuilder("naver")
                        .clientId(naverClientId)
                        .clientSecret(naverClientSecret)
                        .jwkSetUri("tmp")
                        .build()
        );

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration getRegistration(OAuth2ClientProperties properties, String socialType) {
        if(socialType.equals(GOOGLE.getValue())) {
            OAuth2ClientProperties.Registration registration = properties.getRegistration().get(GOOGLE.getValue());

            return CommonOAuth2Provider.GOOGLE.getBuilder(socialType)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .scope("email", "profile")
                    .build();
        } else if(socialType.equals(FACEBOOK.getValue())) {
            OAuth2ClientProperties.Registration registration = properties.getRegistration().get(FACEBOOK.getValue());

            return CommonOAuth2Provider.FACEBOOK.getBuilder(socialType)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,link")
                    .scope("email")
                    .build();
        }

        return null;
    }
}
