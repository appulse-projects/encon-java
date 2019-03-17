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

package io.appulse.encon.connection;

import static lombok.AccessLevel.PRIVATE;
import static io.appulse.encon.connection.Connection.State.ACTIVE;
import static io.appulse.encon.connection.Connection.State.INACTIVE;
import static io.appulse.encon.connection.Connection.State.CLOSED;

import java.io.Closeable;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.regular.ConnectionHandler;
import io.appulse.encon.connection.regular.Message;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@ToString(of = {
  "destination",
  "state"
})
@EqualsAndHashCode(of = {
  "destination",
  "state"
})
@FieldDefaults(level = PRIVATE, makeFinal = true)
public final class Connection implements Closeable {

  @Getter
  RemoteNode destination;

  ConnectionHandler handler;

  Consumer<RemoteNode> onClose;

  Queue<MessageFuture> backlog = new LinkedBlockingQueue<>();

  AtomicReference<Throwable> error = new AtomicReference<>(null);

  AtomicReference<State> state = new AtomicReference<>(CLOSED);

  @Builder
  private Connection (@NonNull RemoteNode destination,
                      @NonNull ConnectionHandler handler,
                      @NonNull Consumer<RemoteNode> onClose
  ) {
    this.destination = destination;
    this.handler = handler;
    this.onClose = onClose;
  }

  public State getState () {
    return state.get();
  }

  public CompletableFuture<Void> send (@NonNull Message message) {
    val future = new CompletableFuture<Void>();

    val exception = error.get();
    if (exception != null) {
      future.completeExceptionally(exception);
      return future;
    }

    val messageFuture = MessageFuture.of(message, future);
    if (state.get() == ACTIVE) {
      sendInternal(messageFuture);
    } else {
      backlog.add(messageFuture);
    }
    return future;
  }

  public void activate () {
    if (error.get() != null) {
      log.warn("couldn't activate the conection to '{}', it was closed manually", destination);
      return;
    }
    if (!state.compareAndSet(CLOSED, INACTIVE)) {
      log.warn("conection to '{}' is already activated or in the middle of the process", destination);
      return;
    }

    while (!backlog.isEmpty()) {
      val message = backlog.poll();
      sendInternal(message);
    }
    state.set(ACTIVE);
  }

  @Override
  public void close () {
    close(new IllegalStateException("Connection is already closed"));
  }

  public void close (Throwable throwable) {
    if (!error.compareAndSet(null, throwable)) {
      log.warn("connection to '{}' is already closed", destination);
      return;
    }
    state.set(CLOSED);

    while (!backlog.isEmpty()) {
      val message = backlog.poll();
      message.getFuture().completeExceptionally(throwable);
    }

    onClose.accept(destination);
  }

  private void sendInternal (MessageFuture messageFuture) {
    handler.send(messageFuture.getMessage(), messageFuture.getFuture());
  }

  public enum State {
    CLOSED,
    INACTIVE,
    ACTIVE;
  }

  @Value(staticConstructor = "of")
  private static class MessageFuture {

    Message message;

    CompletableFuture<Void> future;
  }
}
