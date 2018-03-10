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

package io.appulse.encon.java.module.lookup;

import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;

import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.epmd.java.core.model.response.NodeInfo;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class LookupModule implements LookupModuleApi {

  NodeInternalApi internal;

  @Override
  public Optional<RemoteNode> lookup (@NonNull String node) {
    val descriptor = NodeDescriptor.from(node);
    return lookup(descriptor);
  }

  @Override
  public Optional<RemoteNode> lookup (@NonNull ErlangPid pid) {
    val descriptor = pid.getDescriptor();
    return lookup(descriptor);
  }

  @Override
  public Optional<RemoteNode> lookup (@NonNull NodeDescriptor descriptor) {
    log.debug("Looking up: {}", descriptor);
    return internal.epmd()
        .lookup(descriptor.getShortName(), descriptor.getAddress())
        .filter(NodeInfo::isOk)
        .map(it -> RemoteNode.builder()
            .descriptor(descriptor)
            .protocol(it.getProtocol().get())
            .type(it.getType().get())
            .high(it.getHigh().get())
            .low(it.getLow().get())
            .port(it.getPort().get())
            .build()
        );
  }
}
