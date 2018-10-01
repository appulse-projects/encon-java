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

import static lombok.AccessLevel.PRIVATE;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ConfigMappingUnsupportedTypeException extends ConfigMappingException {

  private static final long serialVersionUID = -6947696128448927769L;

  Class<?> type;

  public ConfigMappingUnsupportedTypeException (Class<?> type) {
    super();
    this.type = type;
  }

  @Override
  public String getMessage () {
    return new StringBuilder()
        .append("Unsupported mapping type ")
        .append(type.getClass().getName())
        .toString();
  }
}
