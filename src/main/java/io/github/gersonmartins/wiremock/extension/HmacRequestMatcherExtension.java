package io.github.gersonmartins.wiremock.extension;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacRequestMatcherExtension extends RequestMatcherExtension {

    @Override
    public String getName() {
        return "hmac-matcher";
    }

    @Override
    public MatchResult match(Request request, Parameters parameters) {
        String secret = parameters.getString("secret");
        if (secret == null || secret.isEmpty()) {
            System.err.println("HMAC Matcher requires a 'secret' parameter");
            return MatchResult.noMatch();
        }

        String headerName = parameters.containsKey("header") ? parameters.getString("header") : "X-Hmac-Signature";
        String algorithm = parameters.containsKey("algorithm") ? parameters.getString("algorithm") : "HmacSHA256";
        String prefix = parameters.containsKey("prefix") ? parameters.getString("prefix") : null;

        if (!request.containsHeader(headerName)) {
            return MatchResult.noMatch();
        }

        String actualSignature = request.header(headerName).firstValue();

        if (prefix != null && !prefix.isEmpty()) {
            if (actualSignature.startsWith(prefix)) {
                actualSignature = actualSignature.substring(prefix.length());
            } else {
                return MatchResult.noMatch(); // Prefix was expected but not found
            }
        }

        String payload = request.getBodyAsString();

        try {
            String expectedSignature = calculateHmac(algorithm, payload, secret);
            return MatchResult.of(expectedSignature.equals(actualSignature));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.err.println(String.format("Failed to calculate HMAC: %s", e.getMessage()));
            return MatchResult.noMatch();
        }
    }

    private String calculateHmac(String algorithm, String payload, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac hmac = Mac.getInstance(algorithm);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
        hmac.init(secretKey);

        byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
