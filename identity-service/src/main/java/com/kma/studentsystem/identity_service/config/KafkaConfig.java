package com.kma.studentsystem.identity_service.config;

import com.kma.studentsystem.identity_service.event.IdentityEvent;
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
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
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
    public ProducerFactory<String, IdentityEvent> identityEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        // Security configuration
        if (securityEnabled) {
            configProps.put("security.protocol", "SASL_SSL");
            configProps.put("ssl.keystore.location", keystoreLocation);
            configProps.put("ssl.keystore.password", keystorePassword);
            configProps.put("ssl.truststore.location", truststoreLocation);
            configProps.put("ssl.truststore.password", truststorePassword);
            configProps.put("sasl.mechanism", saslMechanism);
            configProps.put("sasl.jaas.config", saslJaasConfig);
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, IdentityEvent> identityEventKafkaTemplate() {
        return new KafkaTemplate<>(identityEventProducerFactory());
    }
    
    @Bean
    public ConsumerFactory<String, IdentityEvent> identityEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        
        // Security configuration
        if (securityEnabled) {
            props.put("security.protocol", "SASL_SSL");
            props.put("ssl.keystore.location", keystoreLocation);
            props.put("ssl.keystore.password", keystorePassword);
            props.put("ssl.truststore.location", truststoreLocation);
            props.put("ssl.truststore.password", truststorePassword);
            props.put("sasl.mechanism", saslMechanism);
            props.put("sasl.jaas.config", saslJaasConfig);
        }
        
        JsonDeserializer<IdentityEvent> deserializer = new JsonDeserializer<>(IdentityEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);
        
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, IdentityEvent> identityEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, IdentityEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(identityEventConsumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
}
