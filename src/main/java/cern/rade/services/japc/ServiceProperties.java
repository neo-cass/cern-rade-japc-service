package cern.rade.services.japc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * a POJO configuration class. Holds the properties from application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "service-properties")
public class ServiceProperties {
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration pingToClientFrequency;
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration waitTimeForNewValueFromSub;
    @DurationUnit(ChronoUnit.MILLIS)
    private Duration socketIdleTimeout;
    private int subQueueSize;

    public long getPingToClientFrequency() {
        return pingToClientFrequency.getSeconds();
    }
    public long getWaitTimeForNewValueFromSub() {
        return waitTimeForNewValueFromSub.getSeconds();
    }
    public long getSocketIdleTimeout() {
        return socketIdleTimeout.getSeconds();
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
