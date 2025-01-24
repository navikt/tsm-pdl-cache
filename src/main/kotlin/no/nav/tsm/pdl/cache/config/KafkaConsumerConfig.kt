package no.nav.tsm.pdl.cache.config

import org.apache.kafka.common.metrics.stats.Max
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff
import org.springframework.util.backoff.FixedBackOff.UNLIMITED_ATTEMPTS

@Configuration
class KafkaConsumerConfig {

    companion object {
        private const val BACKOFF_INTERVAL = 60_000L
    }

    @Bean
    fun errorHandler(): DefaultErrorHandler {
        val fixedBackOff = FixedBackOff(BACKOFF_INTERVAL, UNLIMITED_ATTEMPTS)
        return DefaultErrorHandler(fixedBackOff)
    }
}
