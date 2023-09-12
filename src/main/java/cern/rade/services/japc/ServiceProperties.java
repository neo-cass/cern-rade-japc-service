package cern.rade.services.japc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import net.logstash.logback.appender.LogstashUdpSocketAppender;
@Configuration
@ConfigurationProperties(prefix = "radeservicesjapc")
public class ServiceProperties {
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration pingToClientFrequency;
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration waitTimeForNewValueFromSub;
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration socketIdleTimeout;
    private int subQueueSize;

    public Duration getPingToClientFrequency() {
        return pingToClientFrequency;
    }

    public Duration getWaitTimeForNewValueFromSub() {
        return waitTimeForNewValueFromSub;
    }
    public Duration getSocketIdleTimeout() {
        return socketIdleTimeout;
    }

    public int getSubQueueSize() {
        return subQueueSize;
    }

    public void setPingToClientFrequency(Duration pingToClientFrequency) {
        this.pingToClientFrequency = pingToClientFrequency;
    }

    public void setSocketIdleTimeout(Duration socketIdleTimeout) {
        this.socketIdleTimeout = socketIdleTimeout;
    }

    public void setSubQueueSize(int subQueueSize) {
        this.subQueueSize = subQueueSize;
    }

    public void setWaitTimeForNewValueFromSub(Duration waitTimeForNewValueFromSub) {
        this.waitTimeForNewValueFromSub = waitTimeForNewValueFromSub;
    }
}
