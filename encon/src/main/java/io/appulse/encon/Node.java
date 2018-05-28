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

import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;

import io.appulse.encon.common.Meta;
import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.module.NodeInternalApi;
import io.appulse.encon.module.connection.ConnectionModule;
import io.appulse.encon.module.connection.ConnectionModuleApi;
import io.appulse.encon.module.generator.pid.PidGeneratorModule;
import io.appulse.encon.module.generator.pid.PidGeneratorModuleApi;
import io.appulse.encon.module.generator.port.PortGeneratorModule;
import io.appulse.encon.module.generator.port.PortGeneratorModuleApi;
import io.appulse.encon.module.generator.reference.ReferenceGeneratorModule;
import io.appulse.encon.module.generator.reference.ReferenceGeneratorModuleApi;
import io.appulse.encon.module.lookup.LookupModule;
import io.appulse.encon.module.lookup.LookupModuleApi;
import io.appulse.encon.module.mailbox.MailboxModule;
import io.appulse.encon.module.mailbox.MailboxModuleApi;
import io.appulse.encon.module.mailbox.NetKernelMailboxHandler;
import io.appulse.encon.module.ping.PingModule;
import io.appulse.encon.module.ping.PingModuleApi;
import io.appulse.encon.module.server.ServerModule;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.request.Registration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Slf4j
@Setter(PRIVATE)
@ToString(of = {
    "descriptor",
    "cookie",
    "port",
    "meta"
})
@FieldDefaults(level = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public final class Node implements PingModuleApi, Closeable {

  @SuppressWarnings({ "checkstyle:MethodLength", "checkstyle:AnonInnerLength", "PMD.ExcessiveMethodLength" })
  static Node newInstance (@NonNull String name, @NonNull NodeConfig config) {
    val node = new Node();

    val descriptor = NodeDescriptor.from(name);
    node.setDescriptor(descriptor);
    log.debug("Creating new Node '{}' with config:\n  {}\n", descriptor.getFullName(), config);

    node.setCookie(config.getCookie());
    node.setPort(config.getServer().getPort());

    val meta = Meta.builder()
        .type(config.getType())
        .protocol(config.getProtocol())
        .low(config.getLow())
        .high(config.getHigh())
        .flags(config.getDistributionFlags())
        .build();
    node.setMeta(meta);

    val epmd = new EpmdClient(config.getEpmdPort());
    val creation = epmd.register(Registration.builder()
        .name(descriptor.getShortName())
        .port(config.getServer().getPort())
        .type(meta.getType())
        .protocol(meta.getProtocol())
        .high(meta.getHigh())
        .low(meta.getLow())
        .build()
    );
    node.setEpmd(epmd);
    log.debug("Node '{}' was registered", descriptor.getFullName());

    val internal = new NodeInternalApi() {

      @Override
      public Node node () {
        return node;
      }

      @Override
      public MailboxModule mailboxes () {
        return node.mailboxModule;
      }

      @Override
      public LookupModule lookups () {
        return node.lookupModule;
      }

      @Override
      public EpmdClient epmd () {
        return epmd;
      }

      @Override
      public int creation () {
        return creation;
      }

      @Override
      public ConnectionModule connections () {
        return node.connectionModule;
      }

      @Override
      public NodeConfig config () {
        return config;
      }

      @Override
      public void clearCachesFor (@NonNull RemoteNode remoteNode) {
        if (node.connectionModule != null) {
          node.connectionModule.remove(remoteNode);
        }
        if (node.lookupModule != null) {
          node.lookupModule.remove(remoteNode);
        }
      }
    };

    node.setPidGeneratorModule(new PidGeneratorModule(internal));
    node.setPortGeneratorModule(new PortGeneratorModule(internal));
    node.setReferenceGeneratorModule(new ReferenceGeneratorModule(internal));

    node.setPingModule(new PingModule(internal));
    node.setLookupModule(new LookupModule(internal));

    node.setMailboxModule(new MailboxModule(internal));
    node.setConnectionModule(new ConnectionModule(internal));
    node.setServerModule(new ServerModule(internal));

    node.mailbox()
        .name("net_kernel")
        .handler(new NetKernelMailboxHandler())
        .build();

    config.getMailboxes().forEach(it -> {
      node.mailbox()
          .name(it.getName())
          .handler(it.getHandler())
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

  @Delegate(types = PingModuleApi.class)
  PingModule pingModule;

  @Delegate(types = LookupModuleApi.class)
  LookupModule lookupModule;

  @Delegate(types = PidGeneratorModuleApi.class)
  PidGeneratorModule pidGeneratorModule;

  @Delegate(types = PortGeneratorModuleApi.class)
  PortGeneratorModule portGeneratorModule;

  @Delegate(types = ReferenceGeneratorModuleApi.class)
  ReferenceGeneratorModule referenceGeneratorModule;

  @Delegate(types = MailboxModuleApi.class)
  MailboxModule mailboxModule;

  @Delegate(types = ConnectionModuleApi.class)
  ConnectionModule connectionModule;

  ServerModule serverModule;

  @Override
  @SuppressWarnings("PMD.NullAssignment")
  public void close () {
    log.debug("Closing node '{}'", descriptor.getFullName());

    pingModule = null;
    lookupModule = null;
    pidGeneratorModule = null;
    portGeneratorModule = null;
    referenceGeneratorModule = null;

    if (mailboxModule != null) {
      mailboxModule.close();
      mailboxModule = null;
    }
    if (connectionModule != null) {
      connectionModule.close();
      connectionModule = null;
    }
    if (serverModule != null) {
      serverModule.close();
      serverModule = null;
    }
    if (epmd != null) {
      epmd.stop(descriptor.getShortName());
      epmd.close();
      epmd = null;
      log.debug("Node '{}' was deregistered from EPMD", descriptor.getFullName());
    }
    log.debug("Node '{}' was closed", descriptor.getFullName());
  }
}
