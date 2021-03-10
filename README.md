# spring-oauth2-exam
Spring Boot를 이용한 OAuth 2.0 인증 예제

<br />

## 개발 환경
- macOS 11.2.3
- Spring Boot 2.4.3
- OpenJDK 11

## 프로젝트 설정
### 1. API 설정
각 서비스 별 애플리케이션을 먼저 생성 후 API 사용 설정을 하셔야합니다.<br />
이후 발급된 `client-id`와 `client-secret`을 이용하여 OAuth 2.0 인증을 시도합니다.
- [구글](htttps://console.cloud.google.com/)
- [페이스북](https://developers.facebook.com/)
- [카카오](https://developers.kakao.com/)
- [네이버](https://developers.naver.com/)

어플리케이션 생성 및 API 사용 설정에 대한 자세한 설명은 생략합니다.

### 2. `application.yml` 설정
`spring-oauth2-exam/src/main/resources/application.yml`에 주석 처리 된 `client-id`와 `client-secret`을 앞서 발급 받은 키를 이용하여 작성합니다.

### 3. 실행
프로젝트 실행 후 `localhost:32768`에 접속하여 OAuth 2.0을 이용한 로그인을 테스트 할 수 있습니다.