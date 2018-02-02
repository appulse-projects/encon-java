/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appulse.encon.java;

import static io.appulse.encon.java.DistributionFlag.BIG_CREATION;
import static io.appulse.encon.java.DistributionFlag.BIT_BINARIES;
import static io.appulse.encon.java.DistributionFlag.EXTENDED_PIDS_PORTS;
import static io.appulse.encon.java.DistributionFlag.EXTENDED_REFERENCES;
import static io.appulse.encon.java.DistributionFlag.FUN_TAGS;
import static io.appulse.encon.java.DistributionFlag.MAP_TAG;
import static io.appulse.encon.java.DistributionFlag.NEW_FLOATS;
import static io.appulse.encon.java.DistributionFlag.NEW_FUN_TAGS;
import static io.appulse.encon.java.DistributionFlag.UTF8_ATOMS;
import static io.appulse.epmd.java.core.model.NodeType.R6_ERLANG;
import static io.appulse.epmd.java.core.model.Protocol.TCP;
import static io.appulse.epmd.java.core.model.Version.R6;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

import java.util.HashSet;
import java.util.Set;

import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Value
public class Meta {

  public static final Meta DEFAULT = Meta.builder().build();

  NodeType type;

  Protocol protocol;

  Version high;

  Version low;

  Set<DistributionFlag> flags;

  @Builder
  Meta (NodeType type, Protocol protocol, Version high, Version low, @Singular Set<DistributionFlag> flags) {
    this.type = ofNullable(type).orElse(R6_ERLANG);
    this.protocol = ofNullable(protocol).orElse(TCP);
    this.high = ofNullable(high).orElse(R6);
    this.low = ofNullable(low).orElse(R6);
    this.flags = ofNullable(flags)
        .filter(it -> !it.isEmpty())
        .orElse(Default.FLAGS);
  }

  private static class Default {

    private static final Set<DistributionFlag> FLAGS = new HashSet<>(asList(
        EXTENDED_REFERENCES,
        EXTENDED_PIDS_PORTS,
        BIT_BINARIES,
        NEW_FLOATS,
        FUN_TAGS,
        NEW_FUN_TAGS,
        UTF8_ATOMS,
        MAP_TAG,
        BIG_CREATION
    ));
  }
}
