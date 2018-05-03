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

package io.appulse.encon.module.generator.pid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.appulse.encon.module.NodeInternalApi;
import io.appulse.encon.Node;
import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.terms.type.ErlangPid;
import io.appulse.utils.test.TestMethodNamePrinter;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class PidGeneratorModuleTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void generate () {
    NodeDescriptor descriptor = NodeDescriptor.from("popa");

    Node node = mock(Node.class);
    when(node.getDescriptor()).thenReturn(descriptor);

    NodeInternalApi internal = mock(NodeInternalApi.class);
    when(internal.node()).thenReturn(node);
    when(internal.creation()).thenReturn(1);

    PidGeneratorModule generator = new PidGeneratorModule(internal);

    ErlangPid pid = generator.generatePid();
    assertThat(pid).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(pid.getDescriptor().getFullName())
          .isNotNull()
          .isEqualTo(descriptor.getFullName());

      softly.assertThat(pid.getId())
          .isEqualTo(1);

      softly.assertThat(pid.getSerial())
          .isEqualTo(0);

      softly.assertThat(pid.getCreation())
          .isEqualTo(1);
    });
  }
}
