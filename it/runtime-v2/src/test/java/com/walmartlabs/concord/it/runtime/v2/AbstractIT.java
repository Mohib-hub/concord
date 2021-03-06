package com.walmartlabs.concord.it.runtime.v2;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2020 Walmart Inc.
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

import ca.ibodrov.concord.testcontainers.Concord;
import com.walmartlabs.concord.it.common.ITUtils;
import org.junit.Rule;
import org.testcontainers.images.PullPolicy;

public abstract class AbstractIT {

    protected static final long DEFAULT_TEST_TIMEOUT = 120000;

    @Rule
    public Concord concord = configure();

    protected String randomString() {
        return ITUtils.randomString();
    }

    protected String randomPwd() {
        return ITUtils.randomPwd();
    }

    private static Concord configure() {
        Concord concord = new Concord()
                .pathToRunnerV1(null)
                .pathToRunnerV2("target/runner-v2.jar")
                .serverImage(System.getProperty("server.image", "walmartlabs/concord-server"))
                .agentImage(System.getProperty("agent.image", "walmartlabs/concord-agent")).pullPolicy(PullPolicy.defaultPolicy())
                .streamServerLogs(true)
                .streamAgentLogs(true)
                .useLocalMavenRepository(true);

        boolean localMode = Boolean.parseBoolean(System.getProperty("it.local.mode"));
        if (localMode) {
            concord.mode(Concord.Mode.LOCAL);
        } else {
            boolean remoteMode = Boolean.parseBoolean(System.getProperty("it.remote.mode"));
            if (remoteMode) {
                concord.mode(Concord.Mode.REMOTE);
                concord.apiToken(System.getProperty("it.remote.token"));
            }
        }

        return concord;
    }
}
