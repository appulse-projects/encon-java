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

package io.appulse.encon.databind.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.appulse.encon.databind.serializer.Serializer;

/**
 * Annotation for specifying custom {@link Serializer} for field or type.
 *
 * @since 1.1.0
 * @author Artem Labazin
 */
@Documented
@Target({ ANNOTATION_TYPE, FIELD, TYPE })
@Retention(RUNTIME)
public @interface TermSerialize {

  /**
   * Custom Erlang term serializer class for type or field.
   * It should extends {@link Serializer} interface.
   *
   * @return custom term serializer
   */
  Class<? extends Serializer<?>> value ();
}
