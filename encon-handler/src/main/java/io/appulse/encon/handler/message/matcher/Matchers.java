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

import static io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.ANY;
import static io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.NOT_NULL;
import static io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.NULL;
import static io.appulse.encon.handler.message.matcher.Primitives.defaultValue;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.Contains;
import io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.EndsWith;
import io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.Equals;
import io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.InstanceOf;
import io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.Matches;
import io.appulse.encon.handler.message.matcher.MethodArgumentMatcher.StartsWith;

/**
 * The set of pattern matching helpers.
 *
 * @since 1.4.0
 * @author alabazin
 */
public final class Matchers {

  /**
   * Matches <strong>anything</strong>, including nulls and varargs.
   * <p>
   * For primitive types use {@code #any*()} family or {@link #isA(Class)} or {@link #any(Class)}.
   *
   * @param <T> the accepted type
   *
   * @return {@code null}
   */
  public static <T> T any () {
    reportMatcher(ANY);
    return null;
  }

  /**
   * Matches any object of given type, excluding nulls.
   *
   * @param <T>  the accepted type
   *
   * @param type the class of the accepted type
   *
   * @return {@code null}
   */
  public static <T> T any (Class<T> type) {
    reportMatcher(new InstanceOf(type));
    return defaultValue(type);
  }

  /**
   * {@code Object} argument that implements the given class.
   *
   * @param <T>  the accepted type
   *
   * @param type the class of the accepted type
   *
   * @return {@code null}
   */
  public static <T> T isA (Class<T> type) {
    reportMatcher(new InstanceOf(type));
    return defaultValue(type);
  }

  /**
   * Any {@code boolean} or <strong>non-null</strong> {@code Boolean}.
   *
   * @return {@code false}
   */
  public static boolean anyBoolean () {
    reportMatcher(new InstanceOf(Boolean.class));
    return defaultValue(Boolean.class);
  }

  /**
   * Any {@code byte} or <strong>non-null</strong> {@code Byte}.
   *
   * @return {@code 0}
   */
  public static byte anyByte () {
    reportMatcher(new InstanceOf(Byte.class));
    return defaultValue(Byte.class);
  }

  /**
   * Any {@code char} or <strong>non-null</strong> {@code Character}.
   *
   * @return {@code 0}
   */
  public static char anyChar () {
    reportMatcher(new InstanceOf(Character.class));
    return defaultValue(Character.class);
  }

  /**
   * Any {@code short} or <strong>non-null</strong> {@code Short}.
   *
   * @return {@code 0}
   */
  public static short anyShort () {
    reportMatcher(new InstanceOf(Short.class));
    return defaultValue(Short.class);
  }

  /**
   * Any {@code int} or <strong>non-null</strong> {@code Integer}.
   *
   * @return {@code 0}
   */
  public static int anyInt () {
    reportMatcher(new InstanceOf(Integer.class));
    return defaultValue(Integer.class);
  }

  /**
   * Any {@code long} or <strong>non-null</strong> {@code Long}.
   *
   * @return {@code 0}
   */
  public static long anyLong () {
    reportMatcher(new InstanceOf(Long.class));
    return defaultValue(Long.class);
  }

  /**
   * Any {@code float} or <strong>non-null</strong> {@code Float}.
   *
   * @return {@code 0}
   */
  public static float anyFloat () {
    reportMatcher(new InstanceOf(Float.class));
    return defaultValue(Float.class);
  }

  /**
   * Any {@code double} or <strong>non-null</strong> {@code Double}.
   *
   * @return {@code 0}
   */
  public static double anyDouble () {
    reportMatcher(new InstanceOf(Double.class));
    return defaultValue(Double.class);
  }

  /**
   * Any <strong>non-null</strong> {@code String}.
   *
   * @return empty String ("")
   */
  public static String anyString () {
    reportMatcher(new InstanceOf(String.class));
    return defaultValue(String.class);
  }

  /**
   * Any <strong>non-null</strong> {@code List}.
   *
   * @param <T> the accepted type
   *
   * @return empty List
   */
  public static <T> List<T> anyList () {
    reportMatcher(new InstanceOf(List.class));
    return emptyList();
  }

  /**
   * Any <strong>non-null</strong> {@code Set}.
   *
   * @param <T> the accepted type
   *
   * @return empty Set
   */
  public static <T> Set<T> anySet () {
    reportMatcher(new InstanceOf(Set.class));
    return emptySet();
  }

  /**
   * Any <strong>non-null</strong> {@code Map}.
   *
   * @param <K> the accepted key type
   *
   * @param <V> the accepted value type
   *
   * @return empty Map
   */
  public static <K, V> Map<K, V> anyMap () {
    reportMatcher(new InstanceOf(Map.class));
    return emptyMap();
  }

  /**
   * Any <strong>non-null</strong> {@code Collection}.
   *
   * @param <T> the accepted type
   *
   * @return empty Collection
   */
  public static <T> Collection<T> anyCollection () {
    reportMatcher(new InstanceOf(Collection.class));
    return emptyList();
  }

  /**
   * Any <strong>non-null</strong> {@code Iterable}.
   *
   * @param <T> the accepted type
   *
   * @return empty Iterable
   */
  public static <T> Iterable<T> anyIterable () {
    reportMatcher(new InstanceOf(Iterable.class));
    return emptyList();
  }

  /**
   * {@code boolean} argument that is equal to the given value.
   *
   * @param value the given value.
   *
   * @return {@code false}
   */
  public static boolean eq (boolean value) {
    reportMatcher(new Equals(value));
    return defaultValue(Boolean.class);
  }

  /**
   * {@code byte} argument that is equal to the given value.
   *
   * @param value the given value.
   *
   * @return {@code 0}
   */
  public static byte eq (byte value) {
    reportMatcher(new Equals(value));
    return defaultValue(Byte.class);
  }

  /**
   * {@code char} argument that is equal to the given value.
   *
   * @param value the given value.
   *
   * @return {@code 0}
   */
  public static char eq (char value) {
    reportMatcher(new Equals(value));
    return defaultValue(Character.class);
  }

  /**
   * {@code short} argument that is equal to the given value.
   *
   * @param value the given value.
   *
   * @return {@code 0}
   */
  public static short eq (short value) {
    reportMatcher(new Equals(value));
    return defaultValue(Short.class);
  }

  /**
   * {@code int} argument that is equal to the given value.
   *
   * @param value the given value.
   *
   * @return {@code 0}
   */
  public static int eq (int value) {
    reportMatcher(new Equals(value));
    return defaultValue(Integer.class);
  }

  /**
   * {@code long} argument that is equal to the given value.
   *
   * @param value the given value.
   *
   * @return {@code 0}
   */
  public static long eq (long value) {
    reportMatcher(new Equals(value));
    return defaultValue(Long.class);
  }

  /**
   * {@code float} argument that is equal to the given value.
   *
   * @param value the given value.
   *
   * @return {@code 0}
   */
  public static float eq (float value) {
    reportMatcher(new Equals(value));
    return defaultValue(Float.class);
  }

  /**
   * {@code double} argument that is equal to the given value.
   *
   * @param value the given value.
   *
   * @return {@code 0}
   */
  public static double eq (double value) {
    reportMatcher(new Equals(value));
    return defaultValue(Double.class);
  }

  /**
   * Object argument that is equal to the given value.
   *
   * @param value the given value
   *
   * @param <T>   the accepted type
   *
   * @return {@code null}
   */
  public static <T> T eq (T value) {
    reportMatcher(new Equals(value));
    return (T) defaultValue(value.getClass());
  }

  /**
   * {@code null} argument.
   *
   * @param <T> the accepted type
   *
   * @return {@code null}
   */
  public static <T> T isNull () {
    reportMatcher(NULL);
    return null;
  }

  /**
   * Not {@code null} argument.
   *
   * @param <T> the accepted type
   *
   * @return {@code null}
   */
  public static <T> T isNotNull () {
    reportMatcher(NOT_NULL);
    return null;
  }

  /**
   * {@code String} argument that contains the given substring.
   *
   * @param substring the substring
   *
   * @return empty String ("")
   */
  public static String contains (String substring) {
    reportMatcher(new Contains(substring));
    return defaultValue(String.class);
  }

  /**
   * {@code String} argument that contains the given substring.
   *
   * @param pattern the regular expression.
   *
   * @return empty String ("").
   */
  public static String regex (String pattern) {
    reportMatcher(new Matches(pattern));
    return defaultValue(String.class);
  }

  /**
   * {@code Pattern} argument that matches the given regular expression.
   *
   * @param pattern the regular expression
   *
   * @return empty String ("")
   */
  public static String regex (Pattern pattern) {
    reportMatcher(new Matches(pattern));
    return defaultValue(String.class);
  }

  /**
   * {@code String} argument that ends with the given suffix.
   *
   * @param suffix the suffix
   *
   * @return empty String ("")
   */
  public static String endsWith (String suffix) {
    reportMatcher(new EndsWith(suffix));
    return defaultValue(String.class);
  }

  /**
   * {@code String} argument that starts with the given prefix.
   *
   * @param prefix the prefix
   *
   * @return empty String ("")
   */
  public static String startsWith (String prefix) {
    reportMatcher(new StartsWith(prefix));
    return defaultValue(String.class);
  }

  /**
   * Allows creating custom argument matchers.
   *
   * @param <T>     the accepted type
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code null}
   */
  public static <T> T argThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return null;
  }

  /**
   * Allows creating custom {@code boolean} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code false}
   */
  public static boolean booleanThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return defaultValue(Boolean.class);
  }

  /**
   * Allows creating custom {@code byte} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static byte byteThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return defaultValue(Byte.class);
  }

  /**
   * Allows creating custom {@code char} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static char charThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return defaultValue(Character.class);
  }

  /**
   * Allows creating custom {@code short} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static short shortThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return defaultValue(Short.class);
  }

  /**
   * Allows creating custom {@code int} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static int intThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return defaultValue(Integer.class);
  }

  /**
   * Allows creating custom {@code long} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static long longThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return defaultValue(Long.class);
  }

  /**
   * Allows creating custom {@code float} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static float floatThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return defaultValue(Float.class);
  }

  /**
   * Allows creating custom {@code double} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static double doubleThat (MethodArgumentMatcher matcher) {
    reportMatcher(matcher);
    return defaultValue(Double.class);
  }

  private static void reportMatcher (MethodArgumentMatcher argumentMatcher) {
    ThreadLocalStorage.add(argumentMatcher);
  }

  private Matchers () {
  }
}
