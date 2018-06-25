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
package io.appulse.encon.mailbox;

import static io.appulse.encon.mailbox.MailboxQueueType.BLOCKING;
import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.BlockingQueue;

import io.appulse.encon.connection.regular.Message;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author alabazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class BlockingMailboxQueue implements MailboxQueue {

  BlockingQueue<Message> queue;

  @Override
  public void add (Message message) {
    queue.add(message);
  }

  @Override
  @SneakyThrows
  public Message get () {
    return queue.take();
  }

  @Override
  public int size () {
    return queue.size();
  }

  @Override
  public MailboxQueueType type () {
    return BLOCKING;
  }
}
