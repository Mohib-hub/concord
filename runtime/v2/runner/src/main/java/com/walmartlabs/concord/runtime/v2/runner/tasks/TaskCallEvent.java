package com.walmartlabs.concord.runtime.v2.runner.tasks;

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

import com.walmartlabs.concord.runtime.v2.model.ProcessDefinition;
import com.walmartlabs.concord.runtime.v2.model.Step;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.UUID;

@Value.Immutable
@Value.Style(jdkOnly = true)
public interface TaskCallEvent {

    Phase phase();

    ProcessDefinition processDefinition();

    @Nullable
    Step currentStep();

    String taskName();

    String methodName();

    @Nullable
    Object[] input();

    UUID correlationId();

    @Nullable
    Object out();

    @Nullable
    Long duration();

    @Nullable
    Serializable result();

    static ImmutableTaskCallEvent.Builder builder() {
        return ImmutableTaskCallEvent.builder();
    }

    enum Phase {

        PRE,
        POST
    }
}