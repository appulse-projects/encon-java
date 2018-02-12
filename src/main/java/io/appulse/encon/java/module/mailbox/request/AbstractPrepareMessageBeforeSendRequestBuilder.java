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

package io.appulse.encon.java.module.mailbox.request;

import io.appulse.encon.java.module.mailbox.Mailbox;
import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.protocol.type.ErlangPid;

import lombok.NonNull;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
abstract class AbstractPrepareMessageBeforeSendRequestBuilder extends RequestInvoker {

  protected AbstractPrepareMessageBeforeSendRequestBuilder (@NonNull Mailbox mailbox) {
    super(mailbox);
  }

  @Override
  public void send (@NonNull ErlangPid pid) {
    prepareMessage();
    super.send(pid);
  }

  @Override
  public void send (@NonNull String mailbox) {
    prepareMessage();
    super.send(mailbox);
  }

  @Override
  public void send (@NonNull String node, @NonNull String mailbox) {
    prepareMessage();
    super.send(node, mailbox);
  }

  @Override
  public void send (@NonNull NodeDescriptor descriptor, @NonNull String mailbox) {
    prepareMessage();
    super.send(descriptor, mailbox);
  }

  @Override
  public void send (@NonNull RemoteNode remote, @NonNull String mailbox) {
    prepareMessage();
    super.send(remote, mailbox);
  }

  abstract protected void prepareMessage ();
}
