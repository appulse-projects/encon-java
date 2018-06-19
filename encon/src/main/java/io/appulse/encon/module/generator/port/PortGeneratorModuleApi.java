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

package io.appulse.encon.module.generator.port;

import io.appulse.encon.terms.type.ErlangPort;

/**
 * Internal {@link ErlangPort} generator.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
public interface PortGeneratorModuleApi {

  /**
   * Generates a new {@link ErlangPort} instance.
   *
   * @return a new {@link ErlangPort} instance.
   */
  ErlangPort generatePort ();
}
