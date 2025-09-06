package taro;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import taro.auth.CustomUserDetails;
import taro.auth.TokenDto;
import taro.auth.UserDetailsImpl;
import taro.repository.UserRepository;
import taro.user.User;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    public static final String ROLES_CLAIM_KEY = "roles";
    private static final String BEARER_TYPE = "Bearer";

    private final Key key;

    private final long ACCESS_TOKEN_VALIDITY;   // 15분(밀리초)
    private final long REFRESH_TOKEN_VALIDITY;  // 7일(밀리초)
    private final UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKeyBase64,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity,
            UserRepository userRepository) {
        // 1) .env → JWT_SECRET(Base64) → decodedKey(바이트 배열)
        byte[] decodedKey = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKeyBase64);
        // 2) HS512 알고리즘용 Key 객체 생성
        this.key = Keys.hmacShaKeyFor(decodedKey);

        // 3) 만료 시간 주입
        this.ACCESS_TOKEN_VALIDITY = accessTokenValidity;
        this.REFRESH_TOKEN_VALIDITY = refreshTokenValidity;
        this.userRepository = userRepository;
    }

    public TokenDto generateTokenDto(Authentication authentication){
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = new Date().getTime();

        //AccessToken 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_VALIDITY);
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())         // payload "sub": "email or kakaoId"
                .claim(ROLES_CLAIM_KEY, authorities)          // payload "roles: "common"
                .setExpiration(accessTokenExpiresIn)          // payload "exp": 151621022 (ex)
                .signWith(key, SignatureAlgorithm.HS512)      // header  "alg": "HS512"
                .compact();

        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }


    public Authentication getAuthentication(String accessToken){
//        //토큰 복호화
//        Claims claims = parseClaims(accessToken);
//
//        if(claims.get(ROLES_CLAIM_KEY) == null){
//            throw new RuntimeException("권한 정보가 없는 토큰입니다");
//        }
//
//        //클레임에서 권한 정보 가져오기
//        Collection<? extends GrantedAuthority> authorities =
//                Arrays.stream(claims.get(ROLES_CLAIM_KEY).toString().split(","))
//                        .map(SimpleGrantedAuthority::new)
//                        .collect(Collectors.toList());
//
//        UserDetails principal = new User(claims.getSubject(), "", authorities);
//
//        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
        Claims claims = parseClaims(accessToken);

        String email = claims.getSubject();

        User user = userRepository.findByKakaoEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        CustomUserDetails principal = new CustomUserDetails(user);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();    // 유효한 경우: 정상적으로 claims 반환
        } catch (ExpiredJwtException e) {
            return e.getClaims();  // 만료된 경우: 예외 안 터트리고 claims만 꺼내기
        }
    }

    public long getRemainingTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return expiration.getTime() - System.currentTimeMillis(); // 밀리초(ms)
        } catch (ExpiredJwtException e) {
            // 이미 만료된 토큰인 경우 남은 시간은 0
            return 0;
        }
    }

}
