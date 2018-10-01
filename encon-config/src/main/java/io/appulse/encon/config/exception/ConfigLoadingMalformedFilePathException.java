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

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
public class ConfigLoadingMalformedFilePathException extends ConfigLoadingException {

  private static final long serialVersionUID = -6755810629678253064L;

  public ConfigLoadingMalformedFilePathException () {
    super();
  }

  public ConfigLoadingMalformedFilePathException (String message) {
    super(message);
  }

  public ConfigLoadingMalformedFilePathException (String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigLoadingMalformedFilePathException (Throwable cause) {
    super(cause);
  }
}
