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

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.protocol.type.ErlangPid;
import io.appulse.epmd.java.core.model.response.NodeInfo;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class LookupModule implements LookupModuleApi {

  NodeInternalApi internal;

  Map<NodeDescriptor, RemoteNode> cache;

  Function<NodeDescriptor, RemoteNode> compute;

  public LookupModule (@NonNull NodeInternalApi internal) {
    this.internal = internal;

    cache = new ConcurrentHashMap<>();
    compute = descriptor -> {
      return this.internal.epmd()
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
          )
          .orElse(null);
    };
  }

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
    log.debug("Look up\n  {}\n", descriptor);
    return ofNullable(lookupUnsafe(descriptor));
  }

  public RemoteNode lookupUnsafe (String node) {
    val descriptor = NodeDescriptor.from(node);
    return lookupUnsafe(descriptor);
  }

  public RemoteNode lookupUnsafe (ErlangPid pid) {
    val descriptor = pid.getDescriptor();
    return lookupUnsafe(descriptor);
  }

  public RemoteNode lookupUnsafe (NodeDescriptor descriptor) {
    return cache.computeIfAbsent(descriptor, compute);
  }

  public void remove (@NonNull RemoteNode remoteNode) {
    val remote = cache.remove(remoteNode.getDescriptor());
    log.debug("Clear lookup cache for {} (existed: {})",
              remoteNode, remote != null);

    if (log.isDebugEnabled() && remote == null) {
      val keys = cache.keySet()
          .stream()
          .map(NodeDescriptor::toString)
          .map("  - "::concat)
          .collect(joining("\n"));

      log.debug("Cache keys:\n{}", keys);
    }
  }
}
