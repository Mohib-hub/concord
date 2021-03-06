package com.walmartlabs.concord.runtime.v2.parser;

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

import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.ImmutableMap;
import com.walmartlabs.concord.runtime.v2.exception.UnsupportedException;
import com.walmartlabs.concord.runtime.v2.model.ImmutableTrigger;
import com.walmartlabs.concord.runtime.v2.model.Trigger;
import io.takari.parc.Parser;
import io.takari.parc.Seq;

import java.util.List;
import java.util.Map;

import static com.walmartlabs.concord.runtime.v2.parser.GrammarLookup.lookup;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarMisc.*;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarOptions.*;
import static com.walmartlabs.concord.runtime.v2.parser.GrammarV2.*;
import static io.takari.parc.Combinators.choice;
import static io.takari.parc.Combinators.many1;

public final class TriggersGrammar {

    private static final Parser<Atom, Map<String, Object>> githubTriggerConditionsV2 =
            betweenTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT,
                with(ImmutableMap::<String, Object>builder,
                        o -> options(
                                mandatory("type", stringVal.map(v -> o.put("type", v))),
                                optional("githubOrg", patternOrArrayVal.map(v -> o.put("githubOrg", v))),
                                optional("githubRepo", patternOrArrayVal.map(v -> o.put("githubRepo", v))),
                                optional("githubHost", regexpVal.map(v -> o.put("githubHost", v))),
                                optional("branch", regexpVal.map(v -> o.put("branch", v))),
                                optional("sender", regexpVal.map(v -> o.put("sender", v))),
                                optional("status", regexpVal.map(v -> o.put("status", v))),
                                optional("repositoryInfo", arrayOfValues.map(v -> o.put("repositoryInfo", v.getValue()))),
                                optional("payload", mapVal.map(v -> o.put("payload", v)))))
                        .map(ImmutableMap.Builder::build));

    private static final Parser<Atom, Map<String, Object>> githubTriggerConditionsValV2 =
            orError(githubTriggerConditionsV2, YamlValueType.GITHUB_TRIGGER_CONDITIONS);

    private static final Parser<Atom, Trigger> githubTriggerV1 = in -> {
        throw new UnsupportedException("Version 1 of github trigger not supported");
    };

    private static final Parser<Atom, Trigger> githubTriggerV2 =
            with(ImmutableTrigger::builder,
                    o -> options(
                            optional("useInitiator", booleanVal.map(v -> o.putConfiguration("useInitiator", v))),
                            mandatory("entryPoint", stringVal.map(v -> o.putConfiguration("entryPoint", v))),
                            optional("activeProfiles", stringArrayVal.map(o::activeProfiles)),
                            optional("useEventCommitId", booleanVal.map(v -> o.putConfiguration("useEventCommitId", v))),
                            optional("arguments", mapVal.map(o::arguments)),
                            optional("exclusive", stringVal.map(v -> o.putConfiguration("exclusive", v))),
                            mandatory("conditions", githubTriggerConditionsValV2.map(o::putAllConditions)),
                            mandatory("version", intVal.map(v -> o.putConditions("version", v)))))
                    .map(t -> t.name("github"))
                    .map(ImmutableTrigger.Builder::build);

    private static final Parser<Atom, Trigger> githubTrigger =
            betweenTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT,
                    lookup("version", YamlValueType.INT, 2, githubTriggerV2, githubTriggerV1));

    private static final Parser<Atom, Trigger> githubTriggerVal =
            orError(githubTrigger, YamlValueType.GITHUB_TRIGGER);

    private static final Parser<Atom, Trigger> cronTrigger =
            betweenTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT,
                with(ImmutableTrigger::builder,
                        o -> options(
                                mandatory("spec", stringVal.map(v -> o.putConfiguration("spec", v))),
                                mandatory("entryPoint", stringVal.map(v -> o.putConfiguration("entryPoint", v))),
                                optional("activeProfiles", stringArrayVal.map(o::activeProfiles)),
                                optional("arguments", mapVal.map(o::arguments)),
                                optional("exclusive", stringVal.map(v -> o.putConfiguration("exclusive", v))))))
                        .map(t -> t.name("cron"))
                        .map(ImmutableTrigger.Builder::build);

    private static final Parser<Atom, Trigger> cronTriggerVal =
            orError(cronTrigger, YamlValueType.CRON_TRIGGER);

    private static final Parser<Atom, Trigger> manualTrigger =
            betweenTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT,
                    with(ImmutableTrigger::builder,
                            o -> options(
                                    optional("name", stringVal.map(v -> o.putConfiguration("name", v))),
                                    mandatory("entryPoint", stringVal.map(v -> o.putConfiguration("entryPoint", v))),
                                    optional("activeProfiles", stringArrayVal.map(o::activeProfiles)),
                                    optional("arguments", mapVal.map(o::arguments)))))
                    .map(t -> t.name("manual"))
                    .map(ImmutableTrigger.Builder::build);

    private static final Parser<Atom, Trigger> manualTriggerVal =
            orError(manualTrigger, YamlValueType.MANUAL_TRIGGER);

    private static final Parser<Atom, Trigger> oneopsTriggerV1 = in -> {
        throw new UnsupportedException("Version 1 of oneops trigger not supported");
    };

    private static final Parser<Atom, Trigger> oneopsTriggerV2 =
            with(ImmutableTrigger::builder,
                    o -> options(
                            optional("useInitiator", booleanVal.map(v -> o.putConfiguration("useInitiator", v))),
                            mandatory("entryPoint", stringVal.map(v -> o.putConfiguration("entryPoint", v))),
                            optional("activeProfiles", stringArrayVal.map(o::activeProfiles)),
                            optional("arguments", mapVal.map(o::arguments)),
                            optional("exclusive", stringVal.map(v -> o.putConfiguration("exclusive", v))),
                            mandatory("conditions", mapVal.map(o::putAllConditions)),
                            mandatory("version", intVal.map(v -> o.putConditions("version", v)))))
                    .map(t -> t.name("oneops"))
                    .map(ImmutableTrigger.Builder::build);

    private static final Parser<Atom, Trigger> oneopsTrigger =
            betweenTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT,
                    lookup("version", YamlValueType.INT, 2, oneopsTriggerV2, oneopsTriggerV1));

    private static final Parser<Atom, Trigger> oneopsTriggerVal =
            orError(oneopsTrigger, YamlValueType.ONEOPS_TRIGGER);

    private static final Parser<Atom, Trigger> genericTriggerV1 = in -> {
        throw new UnsupportedException("Version 1 of generic trigger not supported");
    };

    private static Parser<Atom, Trigger> genericTriggerV2(String triggerName) {
        return with(ImmutableTrigger::builder,
                o -> options(
                        mandatory("entryPoint", stringVal.map(v -> o.putConfiguration("entryPoint", v))),
                        optional("activeProfiles", stringArrayVal.map(o::activeProfiles)),
                        optional("arguments", mapVal.map(o::arguments)),
                        optional("exclusive", stringVal.map(v -> o.putConfiguration("exclusive", v))),
                        mandatory("conditions", mapVal.map(o::conditions)),
                        mandatory("version", intVal.map(v -> o.putConfiguration("version", v)))))
                .map(t -> t.name(triggerName))
                .map(ImmutableTrigger.Builder::build);
    }

    private static Parser<Atom, Trigger> genericTrigger(String triggerName)  {
        return betweenTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT,
                lookup("version", YamlValueType.INT, 2, genericTriggerV2(triggerName), genericTriggerV1));
    }

    private static Parser<Atom, Trigger> genericTriggerVal(String triggerName) {
        return orError(genericTrigger(triggerName), YamlValueType.GENERIC_TRIGGER);
    }

    private static final Parser<Atom, Trigger> triggerDef =
            betweenTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT,
                    choice(
                            satisfyField("github", atom -> githubTriggerVal.map(t -> addLocation(t, atom))),
                            satisfyField("cron", atom -> cronTriggerVal.map(t -> addLocation(t, atom))),
                            satisfyField("manual", atom -> manualTriggerVal.map(t -> addLocation(t, atom))),
                            satisfyField("oneops", atom -> oneopsTriggerVal.map(t -> addLocation(t, atom))),
                            satisfyAnyField(YamlValueType.GENERIC_TRIGGER, atom -> genericTriggerVal(atom.name).map(t -> addLocation(t, atom)))));

    private static Trigger addLocation(Trigger t, Atom atom) {
        return Trigger.builder().from(t)
                .location(atom.location)
                .build();
    }

    private static final Parser<Atom, Trigger> triggerVal =
            orError(triggerDef, YamlValueType.TRIGGER);

    private static final Parser<Atom, List<Trigger>> triggers =
            betweenTokens(JsonToken.START_ARRAY, JsonToken.END_ARRAY,
                    many1(triggerVal).map(Seq::toList));

    public static final Parser<Atom, List<Trigger>> triggersVal =
            orError(triggers, YamlValueType.TRIGGERS);

    private TriggersGrammar() {
    }
}
