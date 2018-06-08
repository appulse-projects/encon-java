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

package io.appulse.encon.common;

import io.appulse.epmd.java.core.model.NodeType;
import io.appulse.epmd.java.core.model.Protocol;
import io.appulse.epmd.java.core.model.Version;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

/**
 * Representation of remote Erlang node.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@Builder
@ToString(of = {
    "descriptor",
    "port"
})
@EqualsAndHashCode(of = {
    "descriptor",
    "protocol",
    "port"
})
public class RemoteNode {

  @NonNull
  NodeDescriptor descriptor;

  @NonNull
  Protocol protocol;

  @NonNull
  NodeType type;

  @NonNull
  Version high;

  @NonNull
  Version low;

  int port;
}
