/*
 * Copyright 2018 Appulse.
 *
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
 */

package io.appulse.encon;

import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.tuple;
import static lombok.AccessLevel.PRIVATE;

import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.mailbox.Mailbox;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 23.06.2018
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class ModulePing {

  Node node;

  boolean ping (@NonNull String node) {
    log.debug("Ping node name: {}", node);
    val remoteDescriptor = NodeDescriptor.from(node);
    return ping(remoteDescriptor);
  }

  boolean ping (@NonNull NodeDescriptor remoteDescriptor) {
    log.debug("Ping descriptor: {}", remoteDescriptor);
    if (node.getDescriptor().equals(remoteDescriptor)) {
      return true;
    }
    RemoteNode remote = node.lookup(remoteDescriptor);
    log.debug("Lookup result is present: {}", remote != null);
    return remote != null
           ? ping(remote)
           : false;
  }

  boolean ping (@NonNull RemoteNode remote) {
    log.debug("Ping remote node: {}", remote);
    if (node.connect(remote) == null) {
      log.debug("Remote node {} is not available", remote);
      return false;
    }
    log.debug("Remote node {} is available", remote);

    Mailbox mailbox = node.mailbox()
        .build();

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
      mailbox.receive();
      mailbox.getNode().remove(mailbox);
      log.debug("Returning from ping method");
      return true;
    } catch (Exception ex) {
      return false;
    }
  }
}
