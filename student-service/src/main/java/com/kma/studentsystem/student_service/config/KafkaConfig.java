package com.kma.studentsystem.student_service.config;

import com.kma.studentsystem.student_service.event.StudentEvent;
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
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    
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
    public ProducerFactory<String, StudentEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        
        // Security configurations
        if (securityEnabled) {
            configureSecurityProperties(configProps);
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, StudentEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public ConsumerFactory<String, StudentEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "student-service-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        // JSON Deserializer specific configurations
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, StudentEvent.class.getName());
        
        // Security configurations
        if (securityEnabled) {
            configureSecurityProperties(configProps);
        }
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StudentEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StudentEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
    
    private void configureSecurityProperties(Map<String, Object> configProps) {
        // SSL/TLS Configuration
        if (!keystoreLocation.isEmpty()) {
            configProps.put("security.protocol", "SASL_SSL");
            configProps.put("ssl.keystore.location", keystoreLocation);
            configProps.put("ssl.keystore.password", keystorePassword);
            configProps.put("ssl.key.password", keystorePassword);
            configProps.put("ssl.truststore.location", truststoreLocation);
            configProps.put("ssl.truststore.password", truststorePassword);
            configProps.put("ssl.endpoint.identification.algorithm", "");
        } else {
            configProps.put("security.protocol", "SASL_PLAINTEXT");
        }
        
        // SASL Configuration
        configProps.put("sasl.mechanism", saslMechanism);
        if (!saslJaasConfig.isEmpty()) {
            configProps.put("sasl.jaas.config", saslJaasConfig);
        }
    }
}
