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

package io.appulse.encon;

import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.terms.type.ErlangPid;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.response.NodeInfo;

import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @since 1.2.0
 * @author Artem Labazin
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ModuleLookup {

  EpmdClient epmd;

  Map<NodeDescriptor, RemoteNode> cache;

  Function<NodeDescriptor, RemoteNode> compute;

  ModuleLookup (@NonNull EpmdClient epmd) {
    this.epmd = epmd;

    cache = new ConcurrentHashMap<>();
    compute = descriptor -> {
      return this.epmd
          .lookup(descriptor.getNodeName(), descriptor.getAddress())
          .filter(NodeInfo::isOk)
          .map(it -> RemoteNode.builder()
              .descriptor(descriptor)
              .protocol(it.getProtocol().get())
              .type(it.getType().get())
              .high(it.getHigh().get())
              .low(it.getLow().get())
              .port(it.getPort().get())
              .build()
          )
          .orElse(null);
    };
  }

  RemoteNode lookup (@NonNull String node) {
    val descriptor = NodeDescriptor.from(node);
    return lookup(descriptor);
  }

  RemoteNode lookup (@NonNull ErlangPid pid) {
    val descriptor = pid.getDescriptor();
    return lookup(descriptor);
  }

  RemoteNode lookup (@NonNull NodeDescriptor descriptor) {
    return cache.computeIfAbsent(descriptor, compute);
  }

  void remove (@NonNull RemoteNode remoteNode) {
    val remote = cache.remove(remoteNode.getDescriptor());
    log.debug("Clear lookup cache for {} (existed: {})",
              remoteNode, remote != null);
  }
}
