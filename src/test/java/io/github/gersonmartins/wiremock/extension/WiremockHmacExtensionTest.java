package io.github.gersonmartins.wiremock.extension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class WiremockHmacExtensionTest {

        @RegisterExtension
        static WireMockExtension wm = WireMockExtension.newInstance()
                        .options(wireMockConfig()
                                        .dynamicPort()
                                        .extensions(new HmacTemplateHelperProviderExtension(),
                                                        new HmacRequestMatcherExtension()))
                        .build();

        @Test
        void shouldGenerateHmacSuccessfully() throws IOException, InterruptedException {
                String secret = "super-secret";

                wm.stubFor(post(urlEqualTo("/api/data"))
                                .willReturn(ok("{{hmac request.body '" + secret + "'}}")
                                                .withTransformers("response-template")));

                String payload = "{\"data\": \"test\"}";
                String validSignature = "686cae6e8db4f3929dba1a8608cc6a3cfae912f9895b2e92234da8d5404d3f1b";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(wm.baseUrl() + "/api/data"))
                                .POST(HttpRequest.BodyPublishers.ofString(payload))
                                .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.body()).isEqualTo(validSignature);
        }

        @Test
        void shouldMatchRequestWithValidHmacMatcher() throws IOException, InterruptedException {
                String secret = "super-secret";

                wm.stubFor(requestMatching("hmac-matcher",
                                com.github.tomakehurst.wiremock.extension.Parameters.one("secret", secret))
                                .willReturn(ok("Matcher signature valid")));

                String payload = "{\"data\": \"test\"}";
                String validSignature = "686cae6e8db4f3929dba1a8608cc6a3cfae912f9895b2e92234da8d5404d3f1b";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(wm.baseUrl() + "/api/data"))
                                .header("X-Hmac-Signature", validSignature) // default header
                                .POST(HttpRequest.BodyPublishers.ofString(payload))
                                .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.body()).isEqualTo("Matcher signature valid");
        }

        @Test
        void shouldMatchRequestWithValidHmacMatcherCustomHeaderAndPrefix() throws IOException, InterruptedException {
                String secret = "super-secret";

                com.github.tomakehurst.wiremock.extension.Parameters params = new com.github.tomakehurst.wiremock.extension.Parameters();
                params.put("secret", secret);
                params.put("header", "X-Custom-Signature");
                params.put("prefix", "sha256=");

                wm.stubFor(requestMatching("hmac-matcher", params)
                                .willReturn(ok("Matcher signature valid")));

                String payload = "{\"data\": \"test\"}";
                String validSignature = "686cae6e8db4f3929dba1a8608cc6a3cfae912f9895b2e92234da8d5404d3f1b";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(wm.baseUrl() + "/api/data"))
                                .header("X-Custom-Signature", "sha256=" + validSignature)
                                .POST(HttpRequest.BodyPublishers.ofString(payload))
                                .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.body()).isEqualTo("Matcher signature valid");
        }

        @Test
        void shouldNotMatchIfPrefixIsMissingButConfigured() throws IOException, InterruptedException {
                String secret = "super-secret";

                com.github.tomakehurst.wiremock.extension.Parameters params = new com.github.tomakehurst.wiremock.extension.Parameters();
                params.put("secret", secret);
                params.put("prefix", "sha256=");

                wm.stubFor(requestMatching("hmac-matcher", params)
                                .willReturn(ok("Matcher signature valid")));

                String payload = "{\"data\": \"test\"}";
                // Real signature but lacking the prefix
                String validSignatureOnly = "686cae6e8db4f3929dba1a8608cc6a3cfae912f9895b2e92234da8d5404d3f1b";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(wm.baseUrl() + "/api/data"))
                                .header("X-Hmac-Signature", validSignatureOnly)
                                .POST(HttpRequest.BodyPublishers.ofString(payload))
                                .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Should be 404 because no mapping successfully matched
                assertThat(response.statusCode()).isEqualTo(404);
        }
}
