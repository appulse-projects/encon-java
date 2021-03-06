/*
 * Copyright 2020 the original author or authors.
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

package io.appulse.encon.connection.control.exception;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Exception during parsing a field of a control message.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class ControlMessageFieldParsingException extends ControlMessageParsingException {

  private static final long serialVersionUID = 7639697351744349460L;

  int index;

  Class<?> expectingType;

  public ControlMessageFieldParsingException (String message, int index, Class<?> expectingType) {
    super(message);
    this.index = index;
    this.expectingType = expectingType;
  }
}
