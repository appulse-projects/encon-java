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

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.appulse.encon.connection.regular.Message;

/**
 *
 * @author alabazin
 */
interface MailboxQueue {

  static MailboxQueue DUMMY = new MailboxQueue() {

    @Override
    public void add (Message message) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Message get () {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int size () {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MailboxQueueType type () {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  };

  static MailboxQueue from (Queue<Message> queue, MailboxQueueType type) {
    if (queue == null && type == null) {
      return new BlockingMailboxQueue(new LinkedBlockingQueue<>());
    }

    if (type == null) {
      return queue instanceof BlockingQueue
             ? new BlockingMailboxQueue((BlockingQueue) queue)
             : new NonBlockingMailboxQueue(queue);
    }

    if (queue == null) {
      return type == BLOCKING
             ? new BlockingMailboxQueue(new LinkedBlockingQueue<>())
             : new NonBlockingMailboxQueue(new ConcurrentLinkedQueue<>());
    }

    if (type == BLOCKING && !(queue instanceof BlockingQueue)) {
      throw new IllegalArgumentException();
    }

    return type == BLOCKING
           ? new BlockingMailboxQueue((BlockingQueue<Message>) queue)
           : new NonBlockingMailboxQueue(queue);
  }

  void add (Message message);

  Message get ();

  int size ();

  MailboxQueueType type ();
}
