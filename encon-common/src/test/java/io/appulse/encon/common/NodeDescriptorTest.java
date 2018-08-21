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

package io.appulse.encon.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;

import io.appulse.utils.test.TestMethodNamePrinter;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class NodeDescriptorTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void fullShortName () throws Exception {
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
  public void shortName () throws Exception {
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
  public void fullLongName () throws Exception {
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
