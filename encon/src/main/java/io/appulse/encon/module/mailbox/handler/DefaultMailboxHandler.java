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

package io.appulse.encon.module.mailbox.handler;

import static io.appulse.encon.module.connection.control.ControlMessageTag.EXIT2;
import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import io.appulse.encon.module.connection.control.Exit;
import io.appulse.encon.module.connection.regular.Message;
import io.appulse.encon.module.mailbox.Mailbox;
import io.appulse.encon.module.mailbox.exception.ReceivedExitException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@FieldDefaults(level = PRIVATE)
@SuppressFBWarnings("RpC_REPEATED_CONDITIONAL_TEST")
public class DefaultMailboxHandler implements ReceiveMailboxHandler {

  final ReentrantLock lock = new ReentrantLock();

  final Condition condition = lock.newCondition();

  CompletableFuture<Message> future = new CompletableFuture<>();

  boolean flag;

  @Override
  public void handle (Mailbox self, Message message) {
    lock.lock();
    try {
      while (!flag) {
        condition.await();
      }
      flag = false;
      if (message.getHeader().getTag() == EXIT2 || message.getHeader().getTag() == EXIT2) {
        val exit = (Exit) message.getHeader();
        val fromPid = exit.getFrom();
        val reason = exit.getReason();
        future.completeExceptionally(new ReceivedExitException(fromPid, reason));
      } else {
        future.complete(message);
      }
      future = new CompletableFuture<>();
    } catch (InterruptedException ex) {
      // noop
    } finally {
      lock.unlock();
    }
  }

  @Override
  public CompletableFuture<Message> receive () {
    lock.lock();
    try {
      flag = true;
      CompletableFuture<Message> result = future;
      condition.signalAll();
      return result;
    } finally {
      lock.unlock();
    }
  }
}
