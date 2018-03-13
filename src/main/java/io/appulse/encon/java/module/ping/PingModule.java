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

package io.appulse.encon.java.module.ping;

import static io.appulse.encon.java.module.mailbox.request.ArrayItems.items;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.mailbox.Mailbox;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class PingModule implements PingModuleApi {

  NodeInternalApi internal;

  @Override
  public CompletableFuture<Boolean> ping (@NonNull String node) {
    log.debug("Ping node name: {}", node);
    val remoteDescriptor = NodeDescriptor.from(node);
    return ping(remoteDescriptor);
  }

  @Override
  public CompletableFuture<Boolean> ping (@NonNull NodeDescriptor remoteDescriptor) {
    log.debug("Ping descriptor: {}", remoteDescriptor);
    if (internal.node().getDescriptor().equals(remoteDescriptor)) {
      return CompletableFuture.completedFuture(TRUE);
    }
    Optional<RemoteNode> optional = internal.node().lookup(remoteDescriptor);
    log.debug("Lookup result is present: {}", optional.isPresent());
    return optional.isPresent()
           ? ping(optional.get())
           : CompletableFuture.completedFuture(FALSE);
  }

  @Override
  public CompletableFuture<Boolean> ping (@NonNull RemoteNode remote) {
    log.debug("Ping remote node: {}", remote);
    if (!internal.connections().isAvailable(remote)) {
      log.debug("Remote node {} is not available", remote);
      return CompletableFuture.completedFuture(FALSE);
    }
    log.debug("Remote node {} is available", remote);

    CompletableFuture<Boolean> future = new CompletableFuture<>();
    Mailbox mailbox = internal.node()
        .mailbox()
        .handler(new PingReceiveHandler(future))
        .build();

    mailbox.request().makeTuple()
        .addAtom("$gen_call")
        .addTuple(items()
            .add(mailbox.getPid())
            .add(internal.node().generateReference())
        )
        .addTuple(items()
            .addAtom("is_auth")
            .addAtom(internal.node().getDescriptor().getFullName())
        )
        .send(remote, "net_kernel");

    log.debug("Returning from ping method");
    return future;
  }
}
