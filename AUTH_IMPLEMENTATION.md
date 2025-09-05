# 인증 시스템 구현 가이드

## 구현된 기능

### 1. 일반 로그인 (LOCAL)
- **회원가입**: `POST /api/auth/register`
  - 아이디 중복 체크
  - 비밀번호 2회 일치 확인
  - BCrypt로 비밀번호 암호화

- **로그인**: `POST /api/auth/login`
  - JWT Access Token + Refresh Token 발급
  - Refresh Token은 DB에 저장

### 2. 카카오 로그인 (KAKAO)
- **로그인 시작**: `GET /api/auth/kakao/login`
  - 카카오 인증 페이지로 리다이렉트
  
- **콜백 처리**: OAuth2 자동 처리
  - 최초 로그인 시 자동 회원가입
  - JWT 토큰 발급

### 3. 공통 기능
- **토큰 재발급**: `POST /api/auth/refresh`
- **로그아웃**: `POST /api/auth/logout`
- **회원 탈퇴**: `DELETE /api/auth/withdraw`

## 데이터베이스 스키마

### User 테이블
```sql
- id (PK)
- loginId (UNIQUE) - 일반 로그인용
- passwordHash - BCrypt 암호화된 비밀번호
- email (UNIQUE) - 소셜 로그인용
- nickname
- profileImageUrl
- socialProvider (ENUM: LOCAL, KAKAO)
- socialId (UNIQUE) - 소셜 제공자 고유 ID
- role (ENUM: USER, ADMIN)
- refreshToken
- createdAt
- updatedAt
```

## API 명세

### 1. 회원가입
```http
POST /api/auth/register
Content-Type: application/json

{
  "loginId": "yeeeun123",
  "password": "abcd1234!",
  "passwordConfirm": "abcd1234!",
  "nickname": "예은"
}
```

**응답**
- 201 Created: 성공
- 409 Conflict: 아이디 중복
- 400 Bad Request: 비밀번호 불일치 또는 유효성 검증 실패

### 2. 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
  "loginId": "yeeeun123",
  "password": "abcd1234!"
}
```

**응답**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

### 3. 토큰 재발급
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJ..."
}
```

### 4. 보호된 API 호출
```http
GET /api/users/me
Authorization: Bearer {accessToken}
```

## 보안 설정

### JWT 설정
- Access Token: 1시간 (개발), 15-30분 (운영 권장)
- Refresh Token: 7일 (개발), 7-14일 (운영 권장)

### 비밀번호 정책
- 최소 8자 이상
- BCrypt로 암호화 저장

### CORS 설정
- 프론트엔드 도메인 허용 필요
- 카카오 콜백 URL 등록 필요

## 테스트 방법

### Postman 테스트
1. 회원가입: `POST /api/auth/register`
2. 로그인: `POST /api/auth/login` → 토큰 받기
3. 보호된 API: Authorization 헤더에 토큰 추가
4. 카카오 로그인: 브라우저에서 `/api/auth/kakao/login` 접속

### Swagger UI
- URL: http://localhost:8080/swagger-ui.html
- 인증이 필요한 API는 토큰 입력 필요

## 환경 변수 설정

`.env` 파일 또는 환경변수 설정:
```env
# JWT
JWT_SECRET=your-secret-key-base64-encoded
JWT_TOKEN_VALIDITY=3600
JWT_REFRESH_TOKEN_VALIDITY=604800

# Kakao OAuth
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret

# OAuth2 Redirect
OAUTH2_REDIRECT_URI=http://localhost:3000/oauth2/redirect
```

## 주의사항

1. **카카오 개발자 콘솔 설정**
   - Redirect URI를 정확히 등록
   - 필요한 동의 항목 설정

2. **프로덕션 배포 시**
   - JWT Secret 키 안전하게 관리
   - HTTPS 사용 필수
   - Refresh Token Rotation 고려

3. **에러 처리**
   - 401: 인증 실패
   - 403: 권한 부족
   - 409: 중복 (아이디 등)
   - 400: 잘못된 요청
