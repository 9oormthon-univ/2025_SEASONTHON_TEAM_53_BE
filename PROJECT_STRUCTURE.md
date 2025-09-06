# Taro 프로젝트 구조 및 JWT 소셜 로그인 아키텍처

## 구현 완료 기능
- JWT 토큰 기반 인증 (Access Token + Refresh Token)
- 카카오 소셜 로그인
- 토큰 갱신 기능
- 로그아웃 (Refresh Token 제거)
- 회원 탈퇴

## 프로젝트 구조

```
src/main/java/taro/
├── config/
│   ├── JpaConfig.java           # JPA Auditing 설정
│   ├── SecurityConfig.java      # Spring Security + OAuth2 설정
│   └── SwaggerConfig.java       # Swagger UI 설정
├── domain/
│   ├── auth/
│   │   ├── controller/
│   │   │   └── AuthController.java    # 인증 관련 API
│   │   ├── dto/
│   │   │   ├── TokenResponse.java     # 토큰 응답 DTO
│   │   │   └── TokenRefreshRequest.java # 토큰 갱신 요청 DTO
│   │   └── service/
│   │       └── AuthService.java       # 인증 비즈니스 로직
│   ├── user/
│   │   ├── controller/
│   │   │   └── UserController.java    # 사용자 관련 API
│   │   ├── dto/
│   │   │   └── UserResponse.java      # 사용자 정보 응답 DTO
│   │   └── service/
│   │       └── UserService.java       # 사용자 비즈니스 로직
│   ├── User.java                # User 엔티티
│   ├── UserRole.java            # 사용자 권한 Enum
│   ├── SocialProvider.java      # 소셜 로그인 제공자 Enum
│   ├── Card.java                # 카드 엔티티 (규호님)
│   └── CardDrawHistory.java     # 카드 뽑기 이력 (규호님)
├── repository/
│   ├── UserRepository.java      # User JPA Repository
│   ├── CardRepository.java      # Card Repository (규호님)
│   └── CardDrawHistoryRepository.java # (기존)
├── security/
│   ├── jwt/
│   │   ├── JwtTokenProvider.java      # JWT 토큰 생성/검증
│   │   └── JwtAuthenticationFilter.java # JWT 인증 필터
│   ├── oauth2/
│   │   ├── CustomOAuth2UserService.java # OAuth2 사용자 서비스
│   │   ├── OAuth2AuthenticationSuccessHandler.java # 로그인 성공 핸들러
│   │   ├── OAuth2AuthenticationFailureHandler.java # 로그인 실패 핸들러
│   │   ├── OAuth2UserInfo.java        # OAuth2 사용자 정보 추상 클래스
│   │   └── KakaoOAuth2UserInfo.java   # 카카오 사용자 정보
│   ├── CustomUserDetails.java          # Spring Security UserDetails 구현
│   └── CustomUserDetailsService.java   # UserDetailsService 구현
├── service/
│   └── CardService.java         # 카드 서비스 (규호님)
└── TaroApplication.java         # Spring Boot 메인 클래스
```

## 인증 플로우

### 1. 카카오 로그인
```
사용자 → /oauth2/authorization/kakao → 카카오 인증 서버
     ↓
카카오 인증 서버 → /oauth2/callback/kakao → CustomOAuth2UserService
     ↓
OAuth2AuthenticationSuccessHandler → JWT 토큰 발급
     ↓
리다이렉트 (with tokens in URL params)
```

### 2. API 요청
```
클라이언트 → Request (with Authorization: Bearer <token>)
     ↓
JwtAuthenticationFilter → Token 검증
     ↓
SecurityContext 설정 → Controller → Service → Repository
```

### 3. 토큰 갱신
```
클라이언트 → POST /api/auth/refresh (with refresh token)
     ↓
AuthService → 새로운 Access/Refresh Token 발급
```

## 주요 설정

### JWT 설정
- Access Token 유효기간: 1시간 (3600초)
- Refresh Token 유효기간: 7일 (604800초)
- Secret Key: Base64 인코딩된 256bit 이상의 키

### 카카오 OAuth2 설정
- Authorization URI: https://kauth.kakao.com/oauth/authorize
- Token URI: https://kauth.kakao.com/oauth/token
- User Info URI: https://kapi.kakao.com/v2/user/me
- Scope: profile_nickname, profile_image, account_email

## API 엔드포인트

### 인증 관련
- `GET /oauth2/authorization/kakao` - 카카오 로그인 시작
- `POST /api/auth/refresh` - 토큰 갱신
- `POST /api/auth/logout` - 로그아웃
- `DELETE /api/auth/withdraw` - 회원 탈퇴

### 사용자 관련
- `GET /api/users/me` - 내 정보 조회 (인증 필요)

### 공개 엔드포인트
- `GET /` - 메인 페이지
- `GET /swagger-ui.html` - Swagger UI
- `GET /h2-console` - H2 Database Console (개발 환경)

## 주의사항

1. **환경 변수 설정 필요**
   - `KAKAO_CLIENT_ID`: 카카오 앱 REST API 키
   - `KAKAO_CLIENT_SECRET`: 카카오 앱 Client Secret
   - `JWT_SECRET`: JWT 서명용 비밀키

2. **프로덕션 환경 설정**
   - `application-prod.properties` 파일에 실제 DB 정보 설정
   - JWT Secret Key를 안전하게 관리
   - HTTPS 사용 필수

3. **테스트 환경**
   - H2 인메모리 데이터베이스 사용
   - 개발 환경에서는 `spring.profiles.active=dev`로 실행

## 테스트 결과
- JWT 토큰 생성 및 검증 성공
- 카카오 사용자 저장 및 조회 성공
- 로그아웃 시 Refresh Token 제거 성공
- 전체 인증 플로우 통합 테스트 성공
- Refresh Token으로 Access Token 갱신 테스트 성공
