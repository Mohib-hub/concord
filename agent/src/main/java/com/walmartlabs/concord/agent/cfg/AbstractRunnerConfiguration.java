package com.walmartlabs.concord.agent.cfg;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2019 Walmart Inc.
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

import com.typesafe.config.Config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.walmartlabs.concord.agent.cfg.Utils.getDir;
import static com.walmartlabs.concord.agent.cfg.Utils.getStringOrDefault;

public abstract class AbstractRunnerConfiguration {

    private final Path path;
    private final Path cfgDir;
    private final String javaCmd;
    private final String mainClass;
    private final boolean securityManagerEnabled;

    public AbstractRunnerConfiguration(String prefix, Config cfg) {
        String path = getStringOrDefault(cfg, prefix + ".path", () -> {
            try {
                Properties props = new Properties();
                props.load(RunnerV2Configuration.class.getResourceAsStream(prefix + ".properties"));
                return props.getProperty("path");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        this.path = Paths.get(path);
        this.cfgDir = getDir(cfg, prefix + ".cfgDir");
        this.javaCmd = cfg.getString(prefix + ".javaCmd");
        this.mainClass = cfg.getString(prefix + ".mainClass");
        this.securityManagerEnabled = cfg.getBoolean(prefix + ".securityManagerEnabled");
    }

    public Path getPath() {
        return path;
    }

    public Path getCfgDir() {
        return cfgDir;
    }

    public String getJavaCmd() {
        return javaCmd;
    }

    public String getMainClass() {
        return mainClass;
    }

    public boolean isSecurityManagerEnabled() {
        return securityManagerEnabled;
    }

    public abstract String getRuntimeName();
}
