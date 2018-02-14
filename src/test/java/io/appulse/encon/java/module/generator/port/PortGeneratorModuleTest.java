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

package io.appulse.encon.java.module.generator.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.appulse.encon.java.Node;
import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.module.NodeInternalApi;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import io.appulse.encon.java.protocol.type.ErlangPort;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
public class PortGeneratorModuleTest {

  @Test
  public void generate () {
    NodeDescriptor descriptor = NodeDescriptor.from("popa");

    Node node = mock(Node.class);
    when(node.getDescriptor()).thenReturn(descriptor);

    NodeInternalApi internal = mock(NodeInternalApi.class);
    when(internal.node()).thenReturn(node);
    when(internal.creation()).thenReturn(1);

    PortGeneratorModule generator = new PortGeneratorModule(internal);

    ErlangPort port = generator.generatePort();
    assertThat(port).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(port.getDescriptor().getFullName())
          .isNotNull()
          .isEqualTo(descriptor.getFullName());

      softly.assertThat(port.getId())
          .isEqualTo(1);

      softly.assertThat(port.getCreation())
          .isEqualTo(1);
    });
  }
}
