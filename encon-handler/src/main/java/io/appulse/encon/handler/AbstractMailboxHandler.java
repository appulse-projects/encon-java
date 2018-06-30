/*
 * Copyright 2018 the original author or authors..
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
package io.appulse.encon.handler;

import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;

import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.mailbox.exception.ReceivedExitException;
import io.appulse.utils.threads.AppulseExecutors;
import io.appulse.utils.threads.AppulseThreadFactory;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;

/**
 *
 * @since 1.4.0
 * @author alabazin
 */
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public abstract class AbstractMailboxHandler implements Closeable {

  MessageHandler messageHandler;

  Mailbox self;

  @NonFinal
  ExecutorService executorService;

  public void handleSync () {
    Message message;
    try {
      message = getMessage();
    } catch (ReceivedExitException ex) {
      return;
    }
    val header = message.getHeader();
    val body = message.getBody();
    messageHandler.handle(self, header, body);
  }

  public synchronized void handleAsync () {
    if (executorService != null) {
      return;
    }

    executorService = AppulseExecutors.newSingleThreadExecutor()
        .threadFactory(AppulseThreadFactory.builder()
            .name(createThreadName())
            .build())
        .build();

    executorService.execute(() -> {
      while (!Thread.interrupted()) {
        handleSync();
      }
    });
  }

  @Override
  public void close() {
    if (executorService == null) {
      return;
    }
    executorService.shutdown();
  }

  protected abstract Message getMessage ();

  private String createThreadName () {
    StringBuilder builder = new StringBuilder()
        .append(self.getPid().toString());

    String name = self.getName();
    if (name == null) {
      builder.append("(no name)");
    } else {
      builder.append('(').append(name).append(')');
    }

    return builder.toString();
  }
}
