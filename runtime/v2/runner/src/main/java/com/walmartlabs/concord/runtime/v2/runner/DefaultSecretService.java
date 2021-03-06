package com.walmartlabs.concord.runtime.v2.runner;

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

import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.client.SecretClient;
import com.walmartlabs.concord.client.SecretEntry;
import com.walmartlabs.concord.common.secret.BinaryDataSecret;
import com.walmartlabs.concord.runtime.common.cfg.RunnerConfiguration;
import com.walmartlabs.concord.runtime.common.injector.InstanceId;
import com.walmartlabs.concord.runtime.v2.sdk.FileService;
import com.walmartlabs.concord.runtime.v2.sdk.SecretService;
import com.walmartlabs.concord.sdk.Secret;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultSecretService implements SecretService {

    private final SecretClient secretClient;
    private final FileService fileService;
    private final InstanceId instanceId;

    @Inject
    public DefaultSecretService(RunnerConfiguration cfg, ApiClient apiClient, FileService fileService, InstanceId instanceId) {
        this.secretClient = new SecretClient(apiClient, cfg.api().retryCount(), cfg.api().retryInterval());
        this.fileService = fileService;
        this.instanceId = instanceId;
    }

    @Override
    public String exportAsString(String orgName, String name, String password) throws Exception {
        BinaryDataSecret s = get(orgName, name, password, SecretEntry.TypeEnum.DATA);
        return new String(s.getData());
    }

    @Override
    public KeyPair exportKeyAsFile(String orgName, String name, String password) throws Exception {
        com.walmartlabs.concord.common.secret.KeyPair kp = get(orgName, name, password, SecretEntry.TypeEnum.KEY_PAIR);

        Path tmpDir = fileService.createTempDirectory("secret-service");

        Path privateKey = Files.createTempFile(tmpDir, "private", ".key");
        Files.write(privateKey, kp.getPrivateKey());

        Path publicKey = Files.createTempFile(tmpDir, "public", ".key");
        Files.write(publicKey, kp.getPublicKey());

        return KeyPair.builder()
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
    }

    @Override
    public UsernamePassword exportCredentials(String orgName, String name, String password) throws Exception {
        com.walmartlabs.concord.common.secret.UsernamePassword up = get(orgName, name, password, SecretEntry.TypeEnum.USERNAME_PASSWORD);
        return UsernamePassword.of(up.getUsername(), new String(up.getPassword()));
    }

    @Override
    public Path exportAsFile(String orgName, String name, String password) throws Exception {
        BinaryDataSecret bds = get(orgName, name, password, SecretEntry.TypeEnum.DATA);

        Path p = fileService.createTempFile("secret-service-file", ".bin");
        Files.write(p, bds.getData());

        return p;
    }

    @Override
    public String decryptString(String s) throws Exception {
        byte[] input;

        try {
            input = DatatypeConverter.parseBase64Binary(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid encrypted string value, please verify that it was specified/copied correctly: " + e.getMessage());
        }

        return new String(secretClient.decryptString(instanceId.getValue(), input));
    }

    @Override
    public String encryptString(String orgName, String projectName, String value) throws Exception {
        return secretClient.encryptString(instanceId.getValue(), orgName, projectName, value);
    }

    private <T extends Secret> T get(String orgName, String secretName, String password, SecretEntry.TypeEnum type) throws Exception {
        return secretClient.getData(orgName, secretName, password, type);
    }
}
