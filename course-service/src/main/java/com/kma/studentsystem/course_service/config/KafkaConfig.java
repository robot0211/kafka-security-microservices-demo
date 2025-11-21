package com.kma.studentsystem.course_service.config;

import com.kma.studentsystem.course_service.event.CourseEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:course-service-group}")
    private String groupId;

    @Value("${spring.kafka.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${spring.kafka.security.ssl.keystore-location:}")
    private String keystoreLocation;

    @Value("${spring.kafka.security.ssl.keystore-password:}")
    private String keystorePassword;

    @Value("${spring.kafka.security.ssl.truststore-location:}")
    private String truststoreLocation;

    @Value("${spring.kafka.security.ssl.truststore-password:}")
    private String truststorePassword;

    @Value("${spring.kafka.security.sasl.mechanism:SCRAM-SHA-512}")
    private String saslMechanism;

    @Value("${spring.kafka.security.sasl.jaas-config:}")
    private String saslJaasConfig;

    @Bean
    public ProducerFactory<String, CourseEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        if (securityEnabled) {
            configureSecurityProperties(configProps);
        }
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, CourseEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        if (securityEnabled) {
            configureSecurityProperties(props);
        }
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }

    private void configureSecurityProperties(Map<String, Object> props) {
        props.put("security.protocol", "SASL_SSL");
        if (!keystoreLocation.isEmpty()) {
            props.put("ssl.keystore.location", keystoreLocation);
            props.put("ssl.keystore.password", keystorePassword);
        }
        if (!truststoreLocation.isEmpty()) {
            props.put("ssl.truststore.location", truststoreLocation);
            props.put("ssl.truststore.password", truststorePassword);
        }
        props.put("ssl.endpoint.identification.algorithm", "");
        props.put("sasl.mechanism", saslMechanism);
        if (!saslJaasConfig.isEmpty()) {
            props.put("sasl.jaas.config", saslJaasConfig);
        }
    }
}
