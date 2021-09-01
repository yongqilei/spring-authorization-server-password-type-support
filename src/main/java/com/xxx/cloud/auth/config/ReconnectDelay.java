package com.xxx.cloud.auth.config;

import io.lettuce.core.resource.Delay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ReconnectDelay extends Delay {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReconnectDelay.class);

    static final String DEFAULT_DURATION_LIST_STR = "1ms,2ms,4ms,8ms,16ms,32ms,64ms,128ms,256ms," +
            "1s,1s,1s,1s,500ms,500ms,500ms,500ms,1s,1s,1s,1s,1s,5s,5s,5s,30s";

    private final List<Duration> durationList;

    public ReconnectDelay(List<Duration> durationList) {
        if (CollectionUtils.isEmpty(durationList)) {
            throw new IllegalArgumentException("'redis.reconnect.delay' should not be empty.");
        }
        this.durationList = durationList;
    }

    @Override
    public Duration createDelay(long attempt) {
        int attemptInt = (int) attempt;
        if (attemptInt < 1) {
            attemptInt = 1;
        } else if (attemptInt > durationList.size()) {
            attemptInt = durationList.size();
        }
        LOGGER.info("Reconnect attempt: {}", attempt);
        return durationList.get(attemptInt - 1);
    }

    public static List<Duration> getReconnectDelay(String reconnectDelayStr) {
        String[] reconnectDelays = reconnectDelayStr.split(",");
        ApplicationConversionService conversionService = new ApplicationConversionService();
        List<Duration> reconnectDelayList = new ArrayList<>();
        for (String delay: reconnectDelays) {
            reconnectDelayList.add(conversionService.convert(delay, Duration.class));
        }
        return reconnectDelayList;
    }
}
