/*
 * Copyright 2019 the original author or authors.
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
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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

  ModuleLookup (@NonNull EpmdClient epmd) {
    this.epmd = epmd;
    cache = new ConcurrentHashMap<>();
  }

  CompletableFuture<Optional<RemoteNode>> lookup (@NonNull String node) {
    val descriptor = NodeDescriptor.from(node);
    return lookup(descriptor);
  }

  CompletableFuture<Optional<RemoteNode>> lookup (@NonNull ErlangPid pid) {
    val descriptor = pid.getDescriptor();
    return lookup(descriptor);
  }

  CompletableFuture<Optional<RemoteNode>> lookup (@NonNull NodeDescriptor descriptor) {
    val cached = cache.get(descriptor);
    return cached == null || cached.isNotAlive()
           ? findNode(descriptor)
           : completedFuture(of(cached));
  }

  void removeFromCache (@NonNull RemoteNode remoteNode) {
    val remote = cache.remove(remoteNode.getDescriptor());
    log.debug("Clear lookup cache for {} (existed: {})",
              remoteNode, remote != null);
  }

  private CompletableFuture<Optional<RemoteNode>> findNode (NodeDescriptor descriptor) {
    return epmd.lookup(descriptor.getNodeName(), descriptor.getAddress())
        .thenApply(nodeInfo -> nodeInfo
            .filter(NodeInfo::isOk)
            .map(it -> RemoteNode.builder()
                .descriptor(descriptor)
                .protocol(it.getProtocol().get())
                .type(it.getType().get())
                .high(it.getHigh().get())
                .low(it.getLow().get())
                .port(it.getPort().get())
                .extra(it.getExtra())
                .build()
            )
            .filter(RemoteNode::isAlive)
        )
        .thenApply(optional -> {
          optional.ifPresent(remoteNode -> cache.put(descriptor, remoteNode));
          return optional;
        });
  }
}
