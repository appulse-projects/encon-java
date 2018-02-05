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

package io.appulse.encon.java;

import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import javax.management.Descriptor;

import io.appulse.encon.java.exception.NodeAlreadyRegisteredException;
import io.appulse.epmd.java.client.EpmdClient;
import io.appulse.epmd.java.core.model.request.Registration;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Slf4j
@Getter
@FieldDefaults(level = PRIVATE)
public final class Node implements Closeable {

  final NodeDescriptor descriptor;

  final String cookie;

  final int port;

  final Meta meta;

  EpmdClient epmd;

  @Delegate
  GeneratorPid generatorPid;

  @Delegate
  GeneratorPort generatorPort;

  @Delegate
  GeneratorReference generatorReference;

  @Builder
  private Node (@NonNull String name, String cookie, int port, Meta meta) {
    descriptor = NodeDescriptor.from(name);

    this.cookie = ofNullable(cookie).orElse(Default.COOKIE);
    this.port = port;
    this.meta = ofNullable(meta).orElse(Meta.DEFAULT);
  }

  public boolean isRegistered () {
    return epmd != null;
  }

  public Node register () {
    if (isRegistered()) {
      throw new NodeAlreadyRegisteredException();
    }
    epmd = new EpmdClient();
    return selfRegistration();
  }

  public Node register (int epmdPort) {
    if (isRegistered()) {
      throw new NodeAlreadyRegisteredException();
    }
    epmd = new EpmdClient(epmdPort);
    return selfRegistration();
  }

  public boolean ping (@NonNull String node) {
    val remote = NodeDescriptor.from(node);
    return descriptor.equals(remote);
  }

  @Override
  public void close () {
    epmd.stop(descriptor.getShortName());
    epmd.close();
    epmd = null;

    generatorPid = null;
    generatorPort = null;
    generatorReference = null;

    log.debug("Node '{}' was closed", descriptor.getFullName());
  }

  private Node selfRegistration () {
    val creation = epmd.register(Registration.builder()
        .name(descriptor.getShortName())
        .port(port)
        .type(meta.getType())
        .protocol(meta.getProtocol())
        .high(meta.getHigh())
        .low(meta.getLow())
        .build()
    );

    generatorPid = new GeneratorPid(descriptor.getFullName(), creation);
    generatorPort = new GeneratorPort(descriptor.getFullName(), creation);
    generatorReference = new GeneratorReference(descriptor.getFullName(), creation);

    return this;
  }

  private static class Default {

    private static final String COOKIE = getDefaultCookie();

    private static String getDefaultCookie () {
      val cookieFile = Paths.get(getHomeDir(), ".erlang.cookie");
      if (!Files.exists(cookieFile)) {
        return "";
      }

      try {
        return Files.lines(cookieFile)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(it -> !it.isEmpty())
            .findFirst()
            .orElse("");
      } catch (IOException ex) {
        return "";
      }
    }

    private static String getHomeDir () {
      val home = System.getProperty("user.home");
      if (!System.getProperty("os.name").toLowerCase(ENGLISH).contains("windows")) {
        return home;
      }

      val drive = System.getenv("HOMEDRIVE");
      val path = System.getenv("HOMEPATH");
      return drive != null && path != null
             ? drive + path
             : home;
    }
  }
}
