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

package io.appulse.encon.java.module.connection;

import static lombok.AccessLevel.PRIVATE;

import java.net.Socket;

import io.appulse.encon.java.Node;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.protocol.control.ControlMessage;
import io.appulse.encon.java.protocol.control.SendControlMessage;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.Pid;
import io.appulse.utils.Bytes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Connection {

  @NonNull
  Node node;

  @NonNull
  RemoteNode peer;

  @NonNull
  Socket socket;

  public void send (@NonNull String mailbox, @NonNull ErlangTerm term) {

  }

  @SneakyThrows
  public void send (@NonNull Pid to, @NonNull ErlangTerm message) {
    send(new SendControlMessage(to), message);
  }

  @SneakyThrows
  public void send (@NonNull ControlMessage controlMessage, @NonNull ErlangTerm message) {
    val bytes = Bytes.allocate()
        .put4B(0)
        .put(new byte[0]) // distribution header
        .put(controlMessage.toBytes()) // control message
        .put(message.toBytes());

    val array = bytes.put4B(0, bytes.limit() - Integer.BYTES).array();

    socket.getOutputStream().write(array);
  }
}
