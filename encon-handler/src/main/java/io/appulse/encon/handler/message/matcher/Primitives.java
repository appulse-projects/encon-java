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

package io.appulse.encon.handler.message.matcher;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @since 1.4.0
 * @author alabazin
 */
final class Primitives {

  private static final Map<Class<?>, Object> PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES;

  static {
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES = new HashMap<Class<?>, Object>();
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Boolean.class, false);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Character.class, '\u0000');
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Byte.class, (byte) 0);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Short.class, (short) 0);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Integer.class, 0);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Long.class, 0L);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Float.class, 0F);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(Double.class, 0D);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(String.class, "");

    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(boolean.class, false);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(char.class, '\u0000');
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(byte.class, (byte) 0);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(short.class, (short) 0);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(int.class, 0);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(long.class, 0L);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(float.class, 0F);
    PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.put(double.class, 0D);
  }

  /**
   * Indicates if the given class is primitive type or a primitive wrapper.
   *
   * @param type The type to check
   *
   * @return {@code true} if primitive or wrapper, {@code false} otherwise.
   */
  public static boolean isPrimitiveOrWrapper (Class<?> type) {
    return PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.containsKey(type);
  }

  /**
   * Returns the boxed default value for a primitive or a primitive wrapper.
   *
   * @param primitiveOrWrapperType The type to lookup the default value
   *
   * @return The boxed default values as defined in Java Language Specification,
   *         {@code null} if the type is neither a primitive nor a wrapper
   */
  public static <T> T defaultValue (Class<T> primitiveOrWrapperType) {
    return (T) PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES.get(primitiveOrWrapperType);
  }

  private Primitives () {
  }
}
