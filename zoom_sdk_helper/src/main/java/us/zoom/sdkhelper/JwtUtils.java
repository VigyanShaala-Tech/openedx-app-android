package us.zoom.sdkhelper;

import android.util.Log;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;

public class JwtUtils {
    private static final String TAG = "JwtUtils";

    public static String createJWTToken(String clientId, String clientSecret) {
        try {
            JWSSigner signer = new MACSigner(clientSecret);

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            Date expiry = new Date(nowMillis + 3600 * 1000 * 24); // 24 hours

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .claim("appKey", clientId)
                    .claim("sdkKey", clientId)
                    .claim("role", 0) // 0 for join, 1 for start
                    .claim("iat", now.getTime() / 1000)
                    .claim("exp", expiry.getTime() / 1000)
                    .claim("tokenExp", expiry.getTime() / 1000)
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create JWT token", e);
        }
        return "";
    }
}
