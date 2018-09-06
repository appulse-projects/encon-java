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

package io.appulse.encon;

import static lombok.AccessLevel.PACKAGE;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.appulse.encon.common.Meta;
import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.connection.Connection;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.mailbox.ModuleMailbox;
import io.appulse.encon.terms.type.ErlangPid;
import io.appulse.encon.terms.type.ErlangReference;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.request.Registration;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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

  static Node newInstance (@NonNull String name, @NonNull NodeConfig config) {

    val descriptor = NodeDescriptor.from(name, config.getShortName());
    log.debug("Creating new Node '{}' with config:\n  {}\n", descriptor.getFullName(), config);

    val meta = Meta.builder()
        .type(config.getType())
        .protocol(config.getProtocol())
        .low(config.getLowVersion())
        .high(config.getHighVersion())
        .flags(config.getDistributionFlags())
        .build();

    val epmd = new EpmdClient(config.getEpmdPort());
    val creation = epmd.register(Registration.builder()
        .name(descriptor.getNodeName())
        .port(config.getServer().getPort())
        .type(meta.getType())
        .protocol(meta.getProtocol())
        .high(meta.getHigh())
        .low(meta.getLow())
        .build()
    );
    log.debug("Node '{}' was registered", descriptor.getFullName());

    Node node = Node.builder()
        .descriptor(descriptor)
        .meta(meta)
        .epmd(epmd)
        .creation(creation)
        .config(config)
        .build();

    node.moduleMailbox.registerNetKernelMailbox();

    config.getMailboxes().forEach(it -> {
      node.mailbox()
          .name(it.getName())
          .build();
    });

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

  ModulePing modulePing;

  ModuleLookup moduleLookup;

  ModuleMailbox moduleMailbox;

  ModuleConnection moduleConnection;

  ModuleServer moduleServer;

  ModuleClient moduleClient;

  @Builder
  private Node (@NonNull NodeDescriptor descriptor,
                @NonNull Meta meta,
                @NonNull EpmdClient epmd,
                int creation,
                @NonNull NodeConfig config
  ) {
    this.descriptor = descriptor;
    this.meta = meta;
    this.epmd = epmd;

    cookie = config.getCookie();
    port = config.getServer().getPort();

    generatorPid = new GeneratorPid(descriptor.getFullName(), creation);
    generatorPort = new GeneratorPort(descriptor.getFullName(), creation);
    generatorReference = new GeneratorReference(descriptor.getFullName(), creation);

    modulePing = new ModulePing(this);
    moduleLookup = new ModuleLookup(epmd);
    moduleConnection = new ModuleConnection(
        descriptor.getNodeName(),
        config.getServer().getBossThreads(),
        config.getServer().getWorkerThreads()
    );
    moduleServer = new ModuleServer(this, moduleConnection, port);
    moduleClient = new ModuleClient(this, moduleConnection, config.getShortName());
    moduleMailbox = new ModuleMailbox(this, () -> generatorPid.generate());
  }

  /**
   * Searches remote node (locally or on remote machine) by its name.
   *
   * @param name short (like 'node-name') or full (like 'node-name@example.com') remote node's name
   *
   * @return {@link RemoteNode} instance
   */
  public RemoteNode lookup (String name) {
    return moduleLookup.lookup(name);
  }

  /**
   * Searches remote node (locally or on remote machine) by its {@link ErlangPid}.
   *
   * @param pid remote's node pid
   *
   * @return {@link RemoteNode} instance
   */
  public RemoteNode lookup (ErlangPid pid) {
    return moduleLookup.lookup(pid);
  }

  /**
   * Searches remote node (locally or on remote machine) by its identifier.
   *
   * @param nodeDescriptor identifier of the remote node
   *
   * @return {@link RemoteNode} instance
   */
  public RemoteNode lookup (NodeDescriptor nodeDescriptor) {
    return moduleLookup.lookup(nodeDescriptor);
  }

  /**
   * Pings remote node by its name.
   *
   * @param name short (like 'node-name') or full (like 'node-name@example.com') remote node's name
   *
   * @return future container with successful/unsuccessful result
   */
  public boolean ping (String name) {
    return modulePing.ping(name);
  }

  /**
   * Pings remote node by its identifier.
   *
   * @param nodeDescriptor identifier of the remote node
   *
   * @return future container with successful/unsuccessful result
   */
  public boolean ping (NodeDescriptor nodeDescriptor) {
    return modulePing.ping(nodeDescriptor);
  }

  /**
   * Pings remote node by its remote node descriptor.
   *
   * @param remote remote node descriptor
   *
   * @return future container with successful/unsuccessful result
   */
  public boolean ping (RemoteNode remote) {
    return modulePing.ping(remote);
  }

  /**
   * Generates a new {@link ErlangReference} instance.
   *
   * @return a new {@link ErlangReference} instance.
   */
  public ErlangReference newReference () {
    return generatorReference.generate();
  }

  /**
   * A new mailbox builder.
   *
   * @return mailbox builder
   */
  public ModuleMailbox.NewMailboxBuilder mailbox () {
    return moduleMailbox.mailbox();
  }

  /**
   * Registers already created mailbox with specific name.
   *
   * @param mailbox mailbox instance for registration
   *
   * @param name    mailbox's registration name
   *
   * @return {@code true} if it was registered successfully, {@code false} otherwise
   */
  public boolean register (Mailbox mailbox, String name) {
    return moduleMailbox.register(mailbox, name);
  }

  /**
   * Deregisters a mailbox by its name.
   * The mailbox keeps running, but it becomes unavailable by name anymore.
   *
   * @param name mailbox's registration name
   */
  public void deregister (String name) {
    moduleMailbox.deregister(name);
  }

  /**
   * Searches local mailbox by its name.
   *
   * @param name the name of searching mailbox
   *
   * @return {@link Mailbox} instance
   */
  public Mailbox mailbox (String name) {
    return moduleMailbox.mailbox(name);
  }

  /**
   * Searches local mailbox by its pid.
   *
   * @param pid the pid of searching mailbox
   *
   * @return {@link Mailbox} instance
   */
  public Mailbox mailbox (ErlangPid pid) {
    return moduleMailbox.mailbox(pid);
  }

  /**
   * Removes a mailbox and cleanup its resources.
   *
   * @param mailbox the mailbox for removing
   */
  public void remove (Mailbox mailbox) {
    moduleMailbox.remove(mailbox);
  }

  /**
   * Removes a mailbox and cleanup its resources by its name.
   *
   * @param name mailbox's registration name
   */
  public void remove (String name) {
    moduleMailbox.remove(name);
  }

  /**
   * Removes a mailbox and cleanup its resources by its pid.
   *
   * @param pid the pid of searching mailbox
   */
  public void remove (ErlangPid pid) {
    moduleMailbox.remove(pid);
  }

  /**
   * Returns a map of all available node's mailboxes.
   *
   * @return a map of all available node's mailboxes
   */
  public Map<ErlangPid, Mailbox> mailboxes () {
    return moduleMailbox.mailboxes();
  }

  /**
   * Asynchronous connection method to {@link RemoteNode}.
   *
   * @param remote remote node descriptor
   *
   * @return connection future container
   */
  public CompletableFuture<Connection> connectAsync (RemoteNode remote) {
    return moduleClient.connectAsync(remote);
  }

  /**
   * Synchronous connection method to {@link RemoteNode}.
   *
   * @param remote remote node descriptor
   *
   * @return new or cached connection
   */
  public Connection connect (RemoteNode remote) {
    return moduleClient.connect(remote);
  }

  /**
   * Synchronous connection method to {@link RemoteNode}.
   *
   * @param remote  remote node descriptor
   *
   * @param timeout amount of units which need to wait of connection
   *
   * @param unit    timeout unit, like {@link TimeUnit#SECONDS}
   *
   * @return new or cached connection
   */
  public Connection connect (RemoteNode remote, long timeout, TimeUnit unit) {
    return moduleClient.connect(remote, timeout, unit);
  }

  /**
   * Checks if a remote node is available or not.
   *
   * @param remote remote node descriptor
   *
   * @return {@code true} if node is accessable and {@code false} otherwise
   */
  public boolean isAvailable (RemoteNode remote) {
    return moduleClient.isAvailable(remote);
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
