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

package io.appulse.encon.java.util;

import static lombok.AccessLevel.PRIVATE;
import static java.util.Optional.empty;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.response.NodeInfo;
import io.appulse.epmd.java.server.cli.CommonOptions;
import io.appulse.epmd.java.server.command.server.ServerCommandExecutor;
import io.appulse.epmd.java.server.command.server.ServerCommandOptions;
import io.appulse.utils.SocketUtils;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = PRIVATE)
public class TestEpmdServer {

  EpmdClient client;

  ServerCommandExecutor server;

  ExecutorService executorService;

  int port;

  public void start () {
    port = SocketUtils.findFreePort()
        .orElseThrow(RuntimeException::new);

    val commonOptions = new CommonOptions();
    commonOptions.setPort(port);
    server = new ServerCommandExecutor(commonOptions, new ServerCommandOptions());

    executorService = Executors.newSingleThreadExecutor();
    executorService.execute(() -> server.execute());

    client = new EpmdClient(port);
  }

  public void stop () {
    client.close();
    server.close();
    executorService.shutdownNow();
    executorService = null;
  }

  public int getPort () {
    return port;
  }

  public Optional<NodeInfo> lookup (@NonNull String node) {
    return client.lookup(node);
  }
}
