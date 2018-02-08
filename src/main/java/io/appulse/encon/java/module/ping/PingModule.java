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

import static io.appulse.encon.java.protocol.request.ArrayItems.items;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.module.mailbox.Mailbox;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PingModule implements PingModuleApi {

  NodeInternalApi internal;

  @Override
  public CompletableFuture<Boolean> ping (@NonNull String node) {
    val remoteDescriptor = NodeDescriptor.from(node);
    return ping(remoteDescriptor);
  }

  @Override
  public CompletableFuture<Boolean> ping (@NonNull NodeDescriptor remoteDescriptor) {
    if (internal.node().getDescriptor().equals(remoteDescriptor)) {
      return CompletableFuture.completedFuture(TRUE);
    }
    Optional<RemoteNode> optional = internal.node().lookup(remoteDescriptor);
    return optional.isPresent()
           ? ping(optional.get())
           : CompletableFuture.completedFuture(FALSE);
  }

  @Override
  public CompletableFuture<Boolean> ping (@NonNull RemoteNode remote) {
    if (!internal.connections().isAvailable(remote)) {
      return CompletableFuture.completedFuture(FALSE);
    }

    CompletableFuture<Boolean> future = new CompletableFuture<>();
    Mailbox mailbox = internal.node().createMailbox((self, message) -> future.complete(TRUE));
    CompletableFuture<Boolean> result = future.whenComplete((response, exception) -> {
      internal.node().remove(mailbox);
    });

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

    return result;
  }
}
