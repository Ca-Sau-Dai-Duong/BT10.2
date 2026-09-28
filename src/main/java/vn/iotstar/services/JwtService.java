package vn.iotstar.services;

import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import vn.iotstar.exceptions.ExpiredJwtTokenException;
import vn.iotstar.exceptions.InvalidJwtException;

/** JwtService dùng thư viện Nimbus JOSE + JWT (thay cho JJWT). Thuật toán: HS256 */
@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        final JWTClaimsSet claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    // ===== TẠO TOKEN: header + payload(claims) -> ký HMAC-SHA256 -> serialize "xxx.yyy.zzz" =====
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        try {
            long now = System.currentTimeMillis();

            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(new Date(now))
                    .expirationTime(new Date(now + expiration));
            extraClaims.forEach(claims::claim);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims.build());
            signedJWT.sign(new MACSigner(getSignInKey()));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot sign JWT", e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, JWTClaimsSet::getExpirationTime);
    }

    // ===== ĐỌC TOKEN: parse -> verify chữ ký -> kiểm tra hạn =====
    // (Nimbus không tự kiểm tra exp nên phải tự làm)
    private JWTClaimsSet extractAllClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // chỉ chấp nhận đúng HS256 (chặn kiểu tấn công đổi thuật toán)
            if (!JWSAlgorithm.HS256.equals(signedJWT.getHeader().getAlgorithm())) {
                throw new InvalidJwtException("Unsupported JWT algorithm");
            }
            if (!signedJWT.verify(new MACVerifier(getSignInKey()))) {
                throw new InvalidJwtException("JWT signature does not match locally computed signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw new ExpiredJwtTokenException("JWT expired at " + exp);
            }
            return claims;
        } catch (ParseException e) {
            throw new InvalidJwtException("Invalid compact JWT string: " + e.getMessage(), e);
        } catch (JOSEException e) {
            throw new InvalidJwtException("Cannot verify JWT: " + e.getMessage(), e);
        }
    }

    private byte[] getSignInKey() {
        return Base64.getDecoder().decode(secretKey); // >= 32 byte cho HS256
    }
}
