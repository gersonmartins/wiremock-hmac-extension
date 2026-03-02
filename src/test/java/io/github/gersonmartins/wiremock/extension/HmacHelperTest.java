package io.github.gersonmartins.wiremock.extension;

import com.github.jknack.handlebars.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacHelperTest {

    private HmacHelper helper;

    @BeforeEach
    void setUp() {
        helper = new HmacHelper();
    }

    @Test
    void shouldCalculateHmac256Successfully() throws IOException {
        String payload = "my-payload";
        String secret = "my-secret-key";

        // Expected HMAC generated using echo -n "my-payload" | openssl dgst -sha256
        // -hmac "my-secret-key"
        String expectedHmac = "f3a215046687511b81203f1096f626d4cc39504c858b375c849ffcf5613678d1";

        Options options = new Options.Builder(null, null, null, null, null)
                .setParams(new Object[] { secret })
                .build();

        Object result = helper.apply(payload, options);

        assertThat(result).isEqualTo(expectedHmac);
    }

    @Test
    void shouldCalculateHmac1Successfully() throws IOException {
        String payload = "my-payload";
        String secret = "my-secret-key";
        String algorithm = "HmacSHA1";

        // Expected HMAC generated using echo -n "my-payload" | openssl dgst -sha1 -hmac
        // "my-secret-key"
        String expectedHmac = "bc45abd1dd18806fef2cfa02940b865199b67371";

        Options options = new Options.Builder(null, null, null, null, null)
                .setParams(new Object[] { secret, algorithm })
                .build();

        Object result = helper.apply(payload, options);

        assertThat(result).isEqualTo(expectedHmac);
    }

    @Test
    void shouldReturnEmptyStringIfContextIsNull() throws IOException {
        Options options = new Options.Builder(null, null, null, null, null)
                .setParams(new Object[] { "secret" })
                .build();

        Object result = helper.apply(null, options);

        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldThrowExceptionIfSecretIsMissing() {
        Options options = new Options.Builder(null, null, null, null, null)
                .setParams(new Object[] {}) // Empty params
                .build();

        assertThatThrownBy(() -> helper.apply("payload", options))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HMAC Helper requires at least 1 parameter: the secret key");
    }
}
