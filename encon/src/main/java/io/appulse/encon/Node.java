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

package io.appulse.encon;

import static lombok.AccessLevel.PACKAGE;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.Closeable;

import io.appulse.encon.NodesConfig.NodeConfig;
import io.appulse.encon.common.Meta;
import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.mailbox.ModuleMailbox;
import io.appulse.encon.terms.type.ErlangReference;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.request.Registration;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Node's state holder and provider of all needed functions.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Slf4j
@ToString(of = {
    "descriptor",
    "cookie",
    "port",
    "meta"
})
@FieldDefaults(level = PACKAGE, makeFinal = true)
public final class Node implements Closeable {

  //  static Node newInstance (@NonNull String name, @NonNull Config config) {
  //    NodeConfig nodeConfig = config.get(NodeConfig.class, NodeConfig.DEFAULT);
  @SneakyThrows
  static Node newInstance (@NonNull String name, @NonNull NodeConfig nodeConfig) {
    NodeConfig configCopy = new NodeConfig(nodeConfig);

    NodeDescriptor descriptor = NodeDescriptor.from(name, configCopy.getShortName());
    log.debug("Creating new Node '{}' with config:\n  {}\n", descriptor.getFullName(), configCopy);

    Meta meta = Meta.builder()
        .type(configCopy.getType())
        .protocol(configCopy.getProtocol())
        .low(configCopy.getLowVersion())
        .high(configCopy.getHighVersion())
        .flags(configCopy.getDistributionFlags())
        .build();

    EpmdClient epmd = new EpmdClient(configCopy.getEpmdPort());
    int creation = epmd
        .register(Registration.builder()
            .name(descriptor.getNodeName())
            .port(configCopy.getServer().getOrFindAndSetPort())
            .type(meta.getType())
            .protocol(meta.getProtocol())
            .high(meta.getHigh())
            .low(meta.getLow())
            .build())
        .thenApply(response -> {
          if (!response.isOk()) {
            throw new IllegalStateException("Couldn't register the node " + name);
          }
          return response.getCreation();
        })
        .get(2, SECONDS);
    log.debug("Node '{}' was registered", descriptor.getFullName());

    Node node = Node.builder()
        .descriptor(descriptor)
        .meta(meta)
        .epmd(epmd)
        .creation(creation)
        .config(configCopy)
        .build();

    node.moduleMailbox.registerNetKernelMailbox();

    log.debug("Node '{}' was created", descriptor.getFullName());
    return node;
  }

  @Getter
  NodeDescriptor descriptor;

  @Getter
  String cookie;

  @Getter
  int port;

  @Getter
  Meta meta;

  @Getter
  EpmdClient epmd;

  GeneratorPid generatorPid;

  GeneratorPort generatorPort;

  GeneratorReference generatorReference;

  ModulePinger modulePinger;

  ModuleDiscovery moduleDiscovery;

  ModuleMailbox moduleMailbox;

  ModuleConnection moduleConnection;

  ModuleServer moduleServer;

  ModuleClient moduleClient;

  ModuleRemoteProcedureCall moduleRemoteProcedureCall;

  @Builder
  private Node (@NonNull NodeDescriptor descriptor,
                @NonNull Meta meta,
                @NonNull EpmdClient epmd,
                int creation,
                @NonNull NodeConfig config
  ) {
    NodeConfig configCopy = new NodeConfig(config);

    this.descriptor = descriptor;
    this.meta = meta;
    this.epmd = epmd;

    cookie = configCopy.getCookie();
    port = configCopy.getServer().getOrFindAndSetPort();

    generatorPid = new GeneratorPid(descriptor.getFullName(), creation);
    generatorPort = new GeneratorPort(descriptor.getFullName(), creation);
    generatorReference = new GeneratorReference(descriptor.getFullName(), creation);

    modulePinger = new ModulePinger(this);
    moduleDiscovery = new ModuleDiscovery(epmd);
    moduleConnection = new ModuleConnection(
        descriptor.getNodeName(),
        configCopy.getServer().getBossThreads(),
        configCopy.getServer().getWorkerThreads()
    );
    moduleServer = new ModuleServer(this, moduleConnection, port);
    moduleClient = new ModuleClient(this, moduleConnection, configCopy.getShortName());
    moduleMailbox = new ModuleMailbox(this, () -> generatorPid.generate());

    moduleRemoteProcedureCall = new ModuleRemoteProcedureCall(this);
  }

  /**
   * Returns reference on a {@link ModuleRemoteProcedureCall} instance
   * for calling remote node's functions.
   *
   * @return {@link ModuleRemoteProcedureCall} instance.
   */
  public ModuleRemoteProcedureCall rpc () {
    return moduleRemoteProcedureCall;
  }

  /**
   * Returns reference on a {@link ModuleDiscovery} instance
   * for searching the remote nodes.
   *
   * @return {@link ModuleDiscovery} instance.
   */
  public ModuleDiscovery discovery () {
    return moduleDiscovery;
  }

  /**
   * Returns reference on a {@link ModuleClient} instance
   * for working with the outgoing connections.
   *
   * @return {@link ModuleClient} instance.
   */
  public ModuleClient client () {
    return moduleClient;
  }

  /**
   * Returns reference on a {@link ModuleMailbox} instance
   * for working with the mailboxes.
   *
   * @return {@link ModuleMailbox} instance.
   */
  public ModuleMailbox mailboxes () {
    return moduleMailbox;
  }

  /**
   * Returns reference on a {@link ModulePinger} instance
   * for pinging the remote nodes.
   *
   * @return {@link ModulePinger} instance.
   */
  public ModulePinger pinger () {
    return modulePinger;
  }

  /**
   * Generates a new {@link ErlangReference} instance.
   *
   * @return a new {@link ErlangReference} instance.
   */
  public ErlangReference newReference () {
    return generatorReference.generate();
  }

  @Override
  public void close () {
    log.debug("Closing node '{}'", descriptor.getFullName());

    if (moduleMailbox != null) {
      moduleMailbox.close();
    }
    if (moduleServer != null) {
      moduleServer.close();
    }
    if (moduleClient != null) {
      moduleClient.close();
    }
    if (epmd != null) {
      epmd.stop(descriptor.getNodeName());
      epmd.close();
      log.debug("Node '{}' was deregistered from EPMD", descriptor.getFullName());
    }
    log.debug("Node '{}' was closed", descriptor.getFullName());
  }
}
