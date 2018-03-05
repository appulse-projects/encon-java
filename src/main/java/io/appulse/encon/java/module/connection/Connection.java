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

package io.appulse.encon.java.module.connection;

import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;

import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.module.connection.control.ControlMessage;
import io.appulse.encon.java.module.connection.control.Send;
import io.appulse.encon.java.module.connection.control.SendToRegisteredProcess;
import io.appulse.encon.java.module.connection.regular.ClientRegularHandler;
import io.appulse.encon.java.module.connection.regular.Container;
import io.appulse.encon.java.protocol.term.ErlangTerm;
import io.appulse.encon.java.protocol.type.ErlangAtom;
import io.appulse.encon.java.protocol.type.ErlangPid;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 17.02.2018
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Connection implements Closeable {

  @NonNull
  @Getter
  RemoteNode remote;

  @NonNull
  ClientRegularHandler handler;

  public void sendRegistered (@NonNull ErlangPid from, @NonNull String mailbox, @NonNull ErlangTerm message) {
    sendReg(from, new ErlangAtom(mailbox), message);
  }

  public void sendReg (@NonNull ErlangPid from, @NonNull ErlangAtom mailbox, @NonNull ErlangTerm message) {
    send(new SendToRegisteredProcess(from, mailbox), message);
  }

  public void send (@NonNull ErlangPid to, @NonNull ErlangTerm message) {
    send(new Send(to), message);
  }

  @Override
  public void close () {
    log.debug("Closing connection to {}", remote);
    handler.close();
    log.debug("Connection to {} was closed", remote);
  }

  @SneakyThrows
  private void send (ControlMessage control, ErlangTerm payload) {
    // while (!handler.wasAdded()) {
    //   log.debug("Waiting {} connection to send message", remote);
    //   TimeUnit.SECONDS.sleep(1);
    // }

    // if (handler.wasAdded()) {
    handler.send(new Container(control, payload));
    // } else {
    // log.warn("Handler was not added");
    // }
  }
}
