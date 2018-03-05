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

package io.appulse.encon.java.module.generator.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.appulse.encon.java.Node;
import io.appulse.encon.java.NodeDescriptor;
import io.appulse.encon.java.module.NodeInternalApi;
import io.appulse.encon.java.protocol.type.ErlangReference;
import io.appulse.encon.java.util.TestMethodNamePrinter;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
public class ReferenceGeneratorModuleTest {

  @Rule
  public TestRule watcher = new TestMethodNamePrinter();

  @Test
  public void generateReference () {
    NodeDescriptor descriptor = NodeDescriptor.from("popa");

    Node node = mock(Node.class);
    when(node.getDescriptor()).thenReturn(descriptor);

    NodeInternalApi internal = mock(NodeInternalApi.class);
    when(internal.node()).thenReturn(node);
    when(internal.creation()).thenReturn(1);

    ReferenceGeneratorModule generator = new ReferenceGeneratorModule(internal);

    ErlangReference reference = generator.generateReference();
    assertThat(reference).isNotNull();

    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(reference.getDescriptor().getFullName())
          .isNotNull()
          .isEqualTo(descriptor.getFullName());

      softly.assertThat(reference.getIds())
          .isNotNull()
          .isEqualTo(new int[] { 1, 0, 0 });

      softly.assertThat(reference.getCreation())
          .isEqualTo(1);
    });
  }
}
