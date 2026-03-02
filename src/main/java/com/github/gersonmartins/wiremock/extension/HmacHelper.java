package com.github.gersonmartins.wiremock.extension;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacHelper implements Helper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        if (context == null) {
            return "";
        }

        String payload = context.toString();

        if (options.params.length < 1) {
            throw new IllegalArgumentException("HMAC Helper requires at least 1 parameter: the secret key");
        }

        String secret = options.param(0).toString();

        String algorithm = "HmacSHA256";
        if (options.params.length >= 2) {
            algorithm = options.param(1).toString();
        }

        try {
            return calculateHmac(algorithm, payload, secret);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            String errorMsg = String.format("[ERROR: Failed to calculate HMAC: %s]", e.getMessage());
            System.err.println(errorMsg);
            e.printStackTrace();
            return errorMsg;
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
