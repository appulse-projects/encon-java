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

package io.appulse.encon.java.module.server;

import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.encon.java.module.NodeInternalApi;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE)
public class ServerModule implements ServerModuleApi, Closeable {

  final NodeInternalApi internal;

  ServerSocket serverSocket;

  ExecutorService executor;

  ExecutorService main;

  volatile boolean closed;

  @SneakyThrows
  public void start (int port) {

    executor = Executors.newCachedThreadPool();
    main = Executors.newSingleThreadExecutor();
    main.execute(new Runner(port));
  }

  @Override
  @SneakyThrows
  public void close () {
    if (closed) {
      return;
    }
    closed = true;

    log.debug("Closing server...");
    try {
      serverSocket.close();
    } catch (IOException ex) {
    }
    log.debug("Server socket was closed");

    if (!executor.isShutdown()) {
      executor.shutdown();
    }
    if (!main.isShutdown()) {
      main.shutdown();
    }
    log.info("Server was closed");
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  private class Runner implements Runnable {

    int port;

    @Override
    @SneakyThrows
    public void run () {
      serverSocket = new ServerSocket(port);
      log.debug("Server node started at port: {}", port);

      try {
        while (true) {
          log.debug("Waiting new connection");
          val socket = serverSocket.accept();
          log.debug("New connection was accepted");
          val handler = new ServerWorker(socket, internal);
          executor.execute(handler);
        }
      } catch (Throwable ex) {
        if (!closed) {
          log.error("Error during server module work", ex);
        }
      } finally {
        close();
      }
    }
  }
}
