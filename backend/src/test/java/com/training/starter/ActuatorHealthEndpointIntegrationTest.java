package com.training.starter;

import com.training.starter.repository.AppointmentRepository;
import com.training.starter.repository.PatientRepository;
import com.training.starter.repository.UserRepository;
import com.training.starter.service.AccessTokenBlacklistStore;
import com.training.starter.service.RefreshTokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "management.health.redis.enabled=false",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration"
        })
class ActuatorHealthEndpointIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpoint_anonymousRequest_returnsActuatorPayload() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("status");
        assertThat(response.getBody()).doesNotContainKeys("success", "message", "data");
    }

    @Test
    void healthEndpoint_withAuthorizationHeader_skipsJwtFilterAndReturnsActuatorPayload() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("not-a-real-token");

        ResponseEntity<Map> response = restTemplate.exchange(
                "/actuator/health",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("status");
        assertThat(response.getBody()).doesNotContainKeys("success", "message", "data");
    }

    @Test
    void unknownAuthRoute_requiresAuthentication() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/auth/does-not-exist", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        UserRepository userRepository() {
            return repositoryProxy(UserRepository.class);
        }

        @Bean
        PatientRepository patientRepository() {
            return repositoryProxy(PatientRepository.class);
        }

        @Bean
        AppointmentRepository appointmentRepository() {
            return repositoryProxy(AppointmentRepository.class);
        }

        private static <T> T repositoryProxy(Class<T> repositoryType) {
            Object repository = Proxy.newProxyInstance(
                    repositoryType.getClassLoader(),
                    new Class<?>[]{repositoryType},
                    (proxyInstance, method, args) -> {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethodValue(proxyInstance, method);
                        }
                        return defaultValue(method.getReturnType());
                    });
            return repositoryType.cast(repository);
        }

        private static Object objectMethodValue(Object proxy, java.lang.reflect.Method method) {
            return switch (method.getName()) {
                case "toString" -> proxy.getClass().getInterfaces()[0].getSimpleName() + " test proxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> false;
                default -> null;
            };
        }

        private static Object defaultValue(Class<?> returnType) {
            if (returnType.equals(boolean.class)) {
                return false;
            }
            if (returnType.equals(int.class)) {
                return 0;
            }
            if (returnType.equals(long.class)) {
                return 0L;
            }
            if (returnType.equals(Optional.class)) {
                return Optional.empty();
            }
            if (returnType.equals(List.class)) {
                return List.of();
            }
            return null;
        }

        @Bean
        AccessTokenBlacklistStore accessTokenBlacklistStore() {
            return new AccessTokenBlacklistStore(new RedisTemplate<>());
        }

        @Bean
        RefreshTokenStore refreshTokenStore() {
            return new RefreshTokenStore(new RedisTemplate<>());
        }

        @Bean
        JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            Object proxy = Proxy.newProxyInstance(
                    RedisConnectionFactory.class.getClassLoader(),
                    new Class<?>[]{RedisConnectionFactory.class},
                    (proxyInstance, method, args) -> {
                        if (method.getDeclaringClass().equals(Object.class)) {
                            return objectMethodValue(proxyInstance, method);
                        }
                        return defaultValue(method.getReturnType());
                    });
            return RedisConnectionFactory.class.cast(proxy);
        }
    }
}
