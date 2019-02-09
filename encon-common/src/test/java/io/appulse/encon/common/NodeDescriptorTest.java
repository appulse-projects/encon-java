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

package io.appulse.encon.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
@DisplayName("Check node descriptor")
class NodeDescriptorTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("parse short node's name with localhost host")
  void fullShortName () throws Exception {
    InetAddress address = InetAddress.getByName("localhost");
    String fullName = "popa@localhost";
    NodeDescriptor.removeFromCache(fullName);

    NodeDescriptor descriptor = NodeDescriptor.from(fullName, true);
    assertThat(descriptor).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(descriptor.getNodeName())
          .isNotNull()
          .isEqualTo("popa");

      softly.assertThat(descriptor.getHostName())
          .isNotNull()
          .isEqualTo("localhost");

      softly.assertThat(descriptor.getFullName())
          .isNotNull()
          .isEqualTo(fullName);

      softly.assertThat(descriptor.getAddress())
          .isNotNull()
          .isEqualTo(address);

      softly.assertThat(descriptor.isShortName())
          .isTrue();

      softly.assertThat(descriptor.isLongName())
          .isFalse();
    });
  }

  @Test
  @DisplayName("parse short node's name without host")
  void shortName () throws Exception {
    InetAddress address = InetAddress.getLoopbackAddress();
    String tmp = InetAddress.getLocalHost().getHostName();
    int dotIndex = tmp.indexOf('.');
    String hostName = dotIndex > 0
                      ? tmp.substring(0, dotIndex)
                      : tmp;

    String node = "popa";
    NodeDescriptor.removeFromCache(node);

    NodeDescriptor descriptor = NodeDescriptor.from(node);
    assertThat(descriptor).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(descriptor.getNodeName())
          .isNotNull()
          .isEqualTo(node);

      softly.assertThat(descriptor.getHostName())
          .isNotNull()
          .isEqualTo(hostName);

      softly.assertThat(descriptor.getFullName())
          .isNotNull()
          .isEqualTo(node + "@" + hostName);

      softly.assertThat(descriptor.getAddress())
          .isNotNull()
          .isEqualTo(address);

      softly.assertThat(descriptor.isShortName())
          .isTrue();

      softly.assertThat(descriptor.isLongName())
          .isFalse();
    });
  }

  @Test
  @DisplayName("parse full node's name with host")
  void fullLongName () throws Exception {
    InetAddress address = InetAddress.getByName("localhost");
    String fullName = "popa@localhost";
    NodeDescriptor.removeFromCache(fullName);

    NodeDescriptor descriptor = NodeDescriptor.from(fullName, false);
    assertThat(descriptor).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(descriptor.getNodeName())
          .isNotNull()
          .isEqualTo("popa");

      softly.assertThat(descriptor.getHostName())
          .isNotNull()
          .isEqualTo("localhost");

      softly.assertThat(descriptor.getFullName())
          .isNotNull()
          .isEqualTo(fullName);

      softly.assertThat(descriptor.getAddress())
          .isNotNull()
          .isEqualTo(address);

      softly.assertThat(descriptor.isShortName())
          .isFalse();

      softly.assertThat(descriptor.isLongName())
          .isTrue();
    });
  }
}
