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

package io.appulse.encon.config.exception;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class ConfigMappingEnumValueNotFoundException extends ConfigMappingUnsupportedTypeException {

  String nodeAsString;

  public ConfigMappingEnumValueNotFoundException (Class<?> type, String nodeAsString) {
    super(type);
    this.nodeAsString = nodeAsString;
  }

  @Override
  public String getMessage () {
    return new StringBuilder()
        .append("Enum class ").append(getType().getName())
        .append(" couldn't be instantiated from string ").append(nodeAsString)
        .toString();
  }
}