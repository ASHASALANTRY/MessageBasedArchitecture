package com.example.servicebus_receiver.config;

//import io.lettuce.authx.TokenBasedRedisCredentialsProvider;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import redis.clients.authentication.entraid.EntraIDTokenAuthConfigBuilder;
//
//import java.util.Collections;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {
    @Value("${spring.profiles.active}")
    private String profile;
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.scope}")
    private String scope;

    @Bean
    RedisClient connectionFactory(RedisProperties props) {

        ClientOptions opts = ClientOptions.builder()
                // Lettuce should re-auth when new token is available
                .reauthenticateBehavior(ClientOptions.ReauthenticateBehavior.ON_NEW_CREDENTIALS)
                .build();
        RedisURI uri;
        RedisStandaloneConfiguration cfg =
                new RedisStandaloneConfiguration(props.getHost(), props.getPort());
        if (props.getUsername() != null) cfg.setUsername(props.getUsername()); // "default" for ACL/access key
        if (props.getPassword() != null) cfg.setPassword(RedisPassword.of(props.getPassword()));

        uri = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withSsl(true)
                .withAuthentication(props.getUsername(), props.getPassword()) // ← key line
                .build();
        RedisClient redisClient = RedisClient.create(uri);
        redisClient.setOptions(opts);
        return redisClient;
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> redisConnection(RedisClient client) {
        return client.connect(StringCodec.UTF8);
    }


}

