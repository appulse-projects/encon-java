/*
 * Copyright 2018 the original author or authors..
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
package io.appulse.encon.handler.mock.matcher;

/**
 * Intended to use in certain ArgumentMatchers.
 * When ArgumentMatcher fails, chance is that the actual object has the same output of toString() than
 * the wanted object. This looks weird when failures are reported.
 * Therefore when matcher fails but toString() yields the same outputs,
 * we will try to use the {@link #toStringWithType()} method.
 *
 * @author alabazin
 */
public interface ContainsExtraTypeInfo {

  /**
   * Returns more verbose description of the object which include type information
   */
  String toStringWithType();

  /**
   * Checks if target target has matching type.
   * If the type matches, there is no point in rendering result from {@link #toStringWithType()}
   */
  boolean typeMatches(Object target);
}
