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

import static io.appulse.encon.databind.TermMapper.deserialize;
import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import java.util.regex.Pattern;

import io.appulse.encon.terms.ErlangTerm;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Method argument matcher.
 *
 * @since 1.4.0
 * @author alabazin
 */
public interface MethodArgumentMatcher {

  /**
   * Argument matcher for any types.
   */
  MethodArgumentMatcher ANY = term -> true;

  /**
   * Argument matcher for {@code null} types.
   */
  MethodArgumentMatcher NULL = term -> term == null || term.isNil();

  /**
   * Argument matcher for non-{@code null} types.
   */
  MethodArgumentMatcher NOT_NULL = term -> term != null && !term.isNil();

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

  /**
   * Matches instances of specific class type.
   */
  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  class InstanceOf implements MethodArgumentMatcher {

    @NonNull
    Class<?> clazz;

    @Override
    public boolean matches (ErlangTerm term) {
      if (term == null) {
        return false;
      }
      try {
        return deserialize(term, clazz) != null;
      } catch (Exception ex) {
        return false;
      }
    }
  }

  /**
   * Matches terms with equals content value.
   */
  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  class Equals implements MethodArgumentMatcher {

    @NonNull
    Object wanted;

    @Override
    public boolean matches (ErlangTerm term) {
      Object deserialized = deserialize(term, wanted.getClass());
      return Objects.deepEquals(wanted, deserialized);
    }
  }

  /**
   * Matches text terms which contains specified substring.
   */
  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  class Contains implements MethodArgumentMatcher {

    @NonNull
    String substring;

    @Override
    public boolean matches (ErlangTerm term) {
      return term != null &&
             term.isTextual() &&
             term.asText().contains(substring);
    }
  }

  /**
   * Text pattern matcher.
   */
  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  class Matches implements MethodArgumentMatcher {

    @NonNull
    Pattern pattern;

    public Matches (@NonNull String pattern) {
      this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean matches (ErlangTerm term) {
      return term != null &&
             term.isTextual() &&
             pattern.matcher(term.asText()).matches();
    }
  }

  /**
   * Matches strings with specified suffix.
   */
  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  class EndsWith implements MethodArgumentMatcher {

    @NonNull
    String suffix;

    @Override
    public boolean matches (ErlangTerm term) {
      return term != null &&
             term.asText().endsWith(suffix);
    }
  }

  /**
   * Matches strings with specified prefix.
   */
  @RequiredArgsConstructor
  @FieldDefaults(level = PRIVATE, makeFinal = true)
  class StartsWith implements MethodArgumentMatcher {

    @NonNull
    String prefix;

    @Override
    public boolean matches (ErlangTerm term) {
      return term != null &&
             term.asText().startsWith(prefix);
    }
  }
}
