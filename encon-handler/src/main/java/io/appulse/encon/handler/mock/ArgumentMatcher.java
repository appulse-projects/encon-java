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
package io.appulse.encon.handler.mock;

import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import java.util.regex.Pattern;

import io.appulse.encon.handler.mock.matcher.ContainsExtraTypeInfo;
import io.appulse.encon.handler.mock.matcher.EqualsBuilder;
import io.appulse.encon.handler.mock.matcher.VarargMatcher;
import io.appulse.encon.terms.ErlangTerm;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 *
 *
 * @param <T> type of argument
 *
 * @since 1.4.0
 * @author alabazin
 */
public interface ArgumentMatcher {


  ArgumentMatcher ANY = value -> true;
  ArgumentMatcher NULL = Objects::isNull;
  ArgumentMatcher NOT_NULL = Objects::nonNull;

  /**
   * Informs if this matcher accepts the given argument.
   * <p>
   * The method should <b>never</b> assert if the argument doesn't match. It
   * should only return false.
   *
   * @param term the argument
   *
   * @return true if this matcher accepts the given argument.
   */
  boolean matches (ErlangTerm term);

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public static class InstanceOf implements ArgumentMatcher<Object> {

    @NonNull
    Class<?> clazz;

    @Override
    public boolean matches (Object argument) {
      return argument != null &&
             Primitives.isAssignableFromWrapper(argument.getClass(), clazz) &&
             clazz.isAssignableFrom(argument.getClass());
    }

    public static class VarArgAware extends InstanceOf implements VarargMatcher {

      public VarArgAware (Class<?> clazz) {
        super(clazz);
      }
    }
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public static class ReflectionEquals implements ArgumentMatcher<Object> {

    Object wanted;

    String[] excludeFields;

    @Override
    public boolean matches (Object argument) {
      return EqualsBuilder.reflectionEquals(wanted, argument, excludeFields);
    }
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public static class Same implements ArgumentMatcher<Object> {

    Object wanted;

    @Override
    public boolean matches (Object argument) {
      return wanted == argument;
    }
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public static class Equals implements ArgumentMatcher<Object>, ContainsExtraTypeInfo {

    Object wanted;

    @Override
    public boolean matches(Object argument) {
      return Objects.deepEquals(wanted, argument);
    }

    @Override
    public String toStringWithType() {
      return wanted.getClass().getSimpleName();
    }

    @Override
    public boolean typeMatches(Object target) {
      return wanted != null &&
             target != null &&
             target.getClass() == wanted.getClass();
    }
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public static class Contains implements ArgumentMatcher<String> {

    @NonNull
    String substring;

    @Override
    public boolean matches (String argument) {
      return argument != null &&
             argument.contains(substring);
    }
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public static class Matches implements ArgumentMatcher<Object> {

    @NonNull
    Pattern pattern;

    public Matches (@NonNull String pattern) {
      this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean matches (Object argument) {
      return argument instanceof String &&
             pattern.matcher((String) argument).matches();
    }
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public static class EndsWith implements ArgumentMatcher<String> {

    @NonNull
    String suffix;

    @Override
    public boolean matches (String argument) {
      return argument != null &&
             argument.endsWith(suffix);
    }
  }

  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  public static class StartsWith implements ArgumentMatcher<String> {

    @NonNull
    String prefix;

    @Override
    public boolean matches (String argument) {
      return argument != null &&
             argument.startsWith(prefix);
    }
  }
}
