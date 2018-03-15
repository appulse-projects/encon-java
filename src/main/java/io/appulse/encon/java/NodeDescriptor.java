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

import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@Value
@EqualsAndHashCode(exclude = "address")
@AllArgsConstructor(access = PRIVATE)
public class NodeDescriptor implements Serializable {

  private static final InetAddress LOCALHOST;

  private static final long serialVersionUID = 7324588959922091097L;

  static {
    try {
      LOCALHOST = InetAddress.getLocalHost();
    } catch (UnknownHostException ex) {
      throw new IllegalArgumentException("Couldn't determine localhost address", ex);
    }
  }

  @SneakyThrows
  public static NodeDescriptor from (String str) {
    val tokens = str.split("@", 2);
    val shortName = tokens[0];
    val fullName = tokens.length == 2
                   ? str
                   : shortName + '@' + LOCALHOST.getHostName();

    val address = tokens.length == 2
                  ? InetAddress.getByName(tokens[1])
                  : LOCALHOST;

    return new NodeDescriptor(shortName, fullName, address);
  }

  String shortName;

  String fullName;

  InetAddress address;
}
