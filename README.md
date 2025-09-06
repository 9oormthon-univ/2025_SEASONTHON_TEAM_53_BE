# Server
⚙️ Server repo

## 📋 튜토리얼 기능

### 개요
첫 로그인 시 1회만 노출되는 온보딩(튜토리얼)에서 사용자 정보를 수집합니다:
- **닉네임**: 2~16자 한글/영문/숫자/_/- (중복 불가, 금칙어 차단)
- **직업 분야**: DEVELOPER | DESIGNER
- **성향**: EXTROVERT | INTROVERT
- **임베딩 저장**: 직업/성향 정보를 임베딩 벡터로 변환하여 저장 (AI 기능용)

### API 엔드포인트

#### 인증 (Auth)
- `POST /api/auth/login` - 로그인 (needsTutorial 포함)
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/refresh` - 토큰 갱신
- `GET /api/auth/kakao/login` - 카카오 로그인 시작
- `GET /api/auth/kakao/callback` - 카카오 콜백 (JWT 발급, needsTutorial 포함)

#### 사용자 (User)
- `GET /api/users/me` - 내 프로필 조회 (JWT 필요)
- `PUT /api/users/profile` - 튜토리얼 정보 저장 (JWT 필요)

#### 메타데이터 (Meta)
- `GET /api/meta/job-options` - 직업 분야 옵션 조회
- `GET /api/meta/personality-options` - 성향 옵션 조회

#### 타로 (Tarot)
- `GET /api/tarot/draw?stage=BEFORE` - 타로 카드 뽑기 (튜토리얼 완료 필수)

### 실행 방법

#### Demo 모드 (H2 파일 데이터베이스)
```bash
# Windows
run-demo.bat

# Linux/Mac
./run-demo.sh

# 또는 직접 실행
./gradlew bootRun --args='--spring.profiles.active=demo'
```

Demo 모드 특징:
- H2 파일 모드로 데이터 영구 저장 (`./data/taro-demo.db`)
- 앱 재시작/로그아웃 후에도 데이터 유지
- H2 Console 접속: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/taro-demo;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE`
  - Username: `sa`
  - Password: (빈 값)

#### Development 모드
```bash
./gradlew bootRun
```

#### Production 모드
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### 클라이언트 연동 흐름

1. **로그인 성공** → 응답의 `needsTutorial: true/false` 확인
2. `true`면 튜토리얼 화면 표시 (닉네임/직업/성향 입력)
3. **PUT /api/users/profile** 로 저장 → `tutorialCompleted=true` 처리
4. 홈/메인 화면으로 이동
5. 재로그인 시 `needsTutorial=false` → 튜토리얼 미노출

## 🤝 Commit Convention

| 머릿말           | 설명                                                                      |
| ---------------- | ------------------------------------------------------------------------- |
| feat             | 새로운 기능 추가                                                          |
| fix              | 버그 수정                                                                 |
| design           | CSS 등 사용자 UI 디자인 변경                                              |
| !BREAKING CHANGE | 커다란 API 변경의 경우                                                    |
| !HOTFIX          | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우                     |
| refactor         | 프로덕션 코드 리팩토링업                                                  |
| comment          | 필요한 주석 추가 및 변경                                                  |
| docs             | 문서 수정                                                                 |
| test             | 테스트 추가, 테스트 리팩토링(프로덕션 코드 변경 X)                        |
| setting          | 패키지 설치, 개발 설정                                                    |
| chore            | 빌드 테스트 업데이트, 패키지 매니저를 설정하는 경우(프로덕션 코드 변경 X) |
| rename           | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우                        |
| remove           | 파일을 삭제하는 작업만 수행한 경우                                        |


### 🤝 Commit Convention Detail
<div markdown="1">

- `<타입>`: `<제목> (<이슈번호>)` 의 형식으로 제목을 아래 공백줄에 작성
- 제목은 50자 이내 / 변경사항이 "무엇"인지 명확히 작성 / 끝에 마침표 금지
- 예) Feat: 로그인 기능 구현 (#5)


</div>
