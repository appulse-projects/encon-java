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

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.tuple;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.FALSE;

import java.util.concurrent.CompletableFuture;

import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.Connection;
import io.appulse.encon.mailbox.Mailbox;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * The module with set of methods for pinging the remote nodes.
 *
 * @since 1.2.0
 * @author Artem Labazin
 */
@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ModulePinger {

  @NonNull
  Node node;

  /**
   * Pings remote node by its name.
   *
   * @param name short (like 'node-name') or full (like 'node-name@example.com') remote node's name
   *
   * @return future container with successful/unsuccessful result
   */
  public CompletableFuture<Boolean> ping (@NonNull String nodeName) {
    log.debug("Ping node name: {}", nodeName);
    val remoteDescriptor = NodeDescriptor.from(nodeName);
    return ping(remoteDescriptor);
  }

  /**
   * Pings remote node by its identifier.
   *
   * @param nodeDescriptor identifier of the remote node
   *
   * @return future container with successful/unsuccessful result
   */
  public CompletableFuture<Boolean> ping (@NonNull NodeDescriptor remoteDescriptor) {
    log.debug("Ping descriptor: {}", remoteDescriptor);
    if (node.getDescriptor().equals(remoteDescriptor)) {
      return completedFuture(TRUE);
    }
    return node.discovery()
        .lookup(remoteDescriptor)
        .thenComposeAsync(response -> {
          log.debug("Lookup result is present: {}", response.isPresent());
          return response.isPresent()
                 ? ping(response.get())
                 : completedFuture(FALSE);
        });
  }

  /**
   * Pings remote node by its remote node descriptor.
   *
   * @param remote remote node descriptor
   *
   * @return future container with successful/unsuccessful result
   */
  public CompletableFuture<Boolean> ping (@NonNull RemoteNode remote) {
    log.debug("Ping remote node: {}", remote);
    Connection connection = null;
    try {
      connection = node.client().connect(remote);
    } catch (Exception ex) {
      log.error("Error during node {} connection", remote, ex);
    }
    if (connection == null) {
      log.debug("Remote node {} is not available", remote);
      return completedFuture(FALSE);
    }
    log.debug("Remote node {} is available", remote);

    Mailbox mailbox = node.mailboxes().create();

    mailbox.send(remote, "net_kernel", tuple(
        atom("$gen_call"),
        tuple(
            mailbox.getPid(),
            node.newReference()
        ),
        tuple(
            atom("is_auth"),
            atom(node.getDescriptor().getFullName())
        )
    ));

    try {
      // TODO: why sync?
      mailbox.receive(2, SECONDS);
      mailbox.close();
      log.debug("Returning from ping method");
      return completedFuture(TRUE);
    } catch (Exception ex) {
      return completedFuture(FALSE);
    }
  }
}
