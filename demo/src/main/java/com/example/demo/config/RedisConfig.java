package com.example.demo.config;

        import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

        import org.springframework.data.redis.connection.RedisPassword;
        import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
        import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
        import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

        import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
/*    @Bean
    LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }*/
@Bean
LettuceConnectionFactory connectionFactory(RedisProperties props) {
    RedisStandaloneConfiguration cfg =
            new RedisStandaloneConfiguration(props.getHost(), props.getPort());
    if (props.getUsername() != null) cfg.setUsername(props.getUsername()); // "default" for ACL/access key
    if (props.getPassword() != null) cfg.setPassword(RedisPassword.of(props.getPassword()));

    LettuceClientConfiguration clientCfg = LettuceClientConfiguration.builder()
            .useSsl() // Azure Cache requires SSL on 6380
            .build();

    return new LettuceConnectionFactory(cfg, clientCfg);
}

    @Bean
    StringRedisTemplate redisTemplate(LettuceConnectionFactory cf) {
        return new StringRedisTemplate(cf);
        // or RedisTemplate<String,String> with serializers if you need custom types
    }

}
