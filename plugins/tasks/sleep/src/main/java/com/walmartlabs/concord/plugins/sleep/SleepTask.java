package com.walmartlabs.concord.plugins.sleep;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.walmartlabs.concord.ApiException;
import com.walmartlabs.concord.client.ApiClientConfiguration;
import com.walmartlabs.concord.client.ApiClientFactory;
import com.walmartlabs.concord.client.ClientUtils;
import com.walmartlabs.concord.client.ProcessApi;
import com.walmartlabs.concord.sdk.Context;
import com.walmartlabs.concord.sdk.ContextUtils;
import com.walmartlabs.concord.sdk.MapUtils;
import com.walmartlabs.concord.sdk.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Named("sleep")
public class SleepTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(SleepTask.class);

    private final ApiClientFactory apiClientFactory;

    @Inject
    public SleepTask(ApiClientFactory apiClientFactory) {
        this.apiClientFactory = apiClientFactory;
    }

    public void ms(long t) {
        SleepTaskUtils.sleep(t);
    }

    @Override
    public void execute(Context ctx) throws Exception {
        Map<String, Object> cfg = createCfg(ctx);
        Number duration = MapUtils.getNumber(cfg, Constants.DURATION_KEY, null);
        Instant until = getUntil(ctx);

        SleepTaskUtils.validateInputParams(duration, until);

        boolean suspend = MapUtils.getBoolean(cfg, Constants.SUSPEND_KEY, false);
        if (suspend) {
            Instant sleepUntil = SleepTaskUtils.toSleepUntil(duration, until);
            if (sleepUntil.isBefore(Instant.now())) {
                log.warn("Skipping the sleep, the specified datetime is in the past: {}", sleepUntil);
                return;
            }
            log.info("Sleeping until {}...", sleepUntil);
            suspend(sleepUntil, ctx);
        } else {
            long sleepTime = SleepTaskUtils.toSleepDuration(duration, until);
            if (sleepTime <= 0) {
                log.warn("Skipping the sleep, the specified datetime is either negative " +
                        "or is in the past: {}", sleepTime);
                return;
            }
            log.info("Sleeping for {}ms", sleepTime);
            SleepTaskUtils.sleep(sleepTime);
        }
    }

    private void suspend(Instant until, Context ctx) throws ApiException {
        ProcessApi api = new ProcessApi(apiClientFactory.create(ApiClientConfiguration.builder()
                .context(ctx)
                .build()));

        ClientUtils.withRetry(Constants.RETRY_COUNT, Constants.RETRY_INTERVAL, () -> {
            api.setWaitCondition(ContextUtils.getTxId(ctx), SleepTaskUtils.createCondition(until));
            return null;
        });

        ctx.suspend(Constants.RESUME_EVENT_NAME);
    }

    private static Map<String, Object> createCfg(Context ctx) {
        Map<String, Object> m = new HashMap<>();
        for (String k : Constants.ALL_IN_PARAMS) {
            Object v = ctx.getVariable(k);
            if (v != null) {
                m.put(k, v);
            }
        }
        return m;
    }

    private static Instant getUntil(Context ctx) {
        Object value = ctx.getVariable(Constants.UNTIL_KEY);
        if (value == null) {
            return null;
        }

        return SleepTaskUtils.getUntil(value);
    }
}
