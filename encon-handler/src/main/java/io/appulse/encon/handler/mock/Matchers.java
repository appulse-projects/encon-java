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

import static io.appulse.encon.handler.mock.ArgumentMatcher.ANY;
import static io.appulse.encon.handler.mock.ArgumentMatcher.NOT_NULL;
import static io.appulse.encon.handler.mock.ArgumentMatcher.NULL;
import static io.appulse.encon.handler.mock.ThreadLocalStorage.stack;
import static io.appulse.encon.handler.mock.Primitives.defaultValue;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import io.appulse.encon.handler.mock.ArgumentMatcher.Contains;
import io.appulse.encon.handler.mock.ArgumentMatcher.EndsWith;
import io.appulse.encon.handler.mock.ArgumentMatcher.Equals;
import io.appulse.encon.handler.mock.ArgumentMatcher.InstanceOf;
import io.appulse.encon.handler.mock.ArgumentMatcher.Matches;
import io.appulse.encon.handler.mock.ArgumentMatcher.ReflectionEquals;
import io.appulse.encon.handler.mock.ArgumentMatcher.Same;
import io.appulse.encon.handler.mock.ArgumentMatcher.StartsWith;

/**
 *
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
   * @param <T> the accepted type
   *
   * @param type the class of the accepted type
   *
   * @return {@code null}
   */
  public static <T> T any (Class<T> type) {
    reportMatcher(new InstanceOf.VarArgAware(type));
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
    return false;
  }

  /**
   * Any {@code byte} or <strong>non-null</strong> {@code Byte}.
   *
   * @return {@code 0}
   */
  public static byte anyByte () {
    reportMatcher(new InstanceOf(Byte.class));
    return 0;
  }

  /**
   * Any {@code char} or <strong>non-null</strong> {@code Character}.
   *
   * @return {@code 0}
   */
  public static char anyChar () {
    reportMatcher(new InstanceOf(Character.class));
    return 0;
  }

  /**
   * Any {@code short} or <strong>non-null</strong> {@code Short}.
   *
   * @return {@code 0}
   */
  public static short anyShort () {
    reportMatcher(new InstanceOf(Short.class));
    return 0;
  }

  /**
   * Any {@code int} or <strong>non-null</strong> {@code Integer}.
   *
   * @return {@code 0}
   */
  public static int anyInt () {
    reportMatcher(new InstanceOf(Integer.class));
    return 0;
  }

  /**
   * Any {@code long} or <strong>non-null</strong> {@code Long}.
   *
   * @return {@code 0}
   */
  public static long anyLong () {
    reportMatcher(new InstanceOf(Long.class));
    return 0;
  }

  /**
   * Any {@code float} or <strong>non-null</strong> {@code Float}.
   *
   * @return {@code 0}
   */
  public static float anyFloat () {
    reportMatcher(new InstanceOf(Float.class));
    return 0;
  }

  /**
   * Any {@code double} or <strong>non-null</strong> {@code Double}.
   *
   * @return {@code 0}
   */
  public static double anyDouble () {
    reportMatcher(new InstanceOf(Double.class));
    return 0;
  }

  /**
   * Any <strong>non-null</strong> {@code String}.
   *
   * @return empty String ("")
   */
  public static String anyString () {
    reportMatcher(new InstanceOf(String.class));
    return "";
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
    return false;
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
    return 0;
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
    return 0;
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
    return 0;
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
    return 0;
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
    return 0;
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
    return 0;
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
    return 0;
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
    return value == null
           ? null
           : (T) defaultValue(value.getClass());
  }

  /**
   * Object argument that is reflection-equal to the given value with support for excluding
   * selected fields from a class.
   *
   * @param value         the given value
   *
   * @param excludeFields fields to exclude, if field does not exist it is ignored
   *
   * @param <T>           the accepted type
   *
   * @return {@code null}
   */
  public static <T> T refEq (T value, String... excludeFields) {
    reportMatcher(new ReflectionEquals(value, excludeFields));
    return null;
  }

  /**
   * Object argument that is the same as the given value.
   *
   * @param <T>   the type of the object, it is passed through to prevent casts
   *
   * @param value the given value
   *
   * @return {@code null}
   */
  public static <T> T same (T value) {
    reportMatcher(new Same(value));
    return value == null
           ? null
           : (T) defaultValue(value.getClass());
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
    return "";
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
    return "";
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
    return "";
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
    return "";
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
    return "";
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
  public static <T> T argThat (ArgumentMatcher<T> matcher) {
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
  public static boolean booleanThat (ArgumentMatcher<Boolean> matcher) {
    reportMatcher(matcher);
    return false;
  }

  /**
   * Allows creating custom {@code byte} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static byte byteThat (ArgumentMatcher<Byte> matcher) {
    reportMatcher(matcher);
    return 0;
  }

  /**
   * Allows creating custom {@code char} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static char charThat (ArgumentMatcher<Character> matcher) {
    reportMatcher(matcher);
    return 0;
  }

  /**
   * Allows creating custom {@code short} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static short shortThat (ArgumentMatcher<Short> matcher) {
    reportMatcher(matcher);
    return 0;
  }

  /**
   * Allows creating custom {@code int} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static int intThat (ArgumentMatcher<Integer> matcher) {
    reportMatcher(matcher);
    return 0;
  }

  /**
   * Allows creating custom {@code long} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static long longThat (ArgumentMatcher<Long> matcher) {
    reportMatcher(matcher);
    return 0;
  }

  /**
   * Allows creating custom {@code float} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static float floatThat (ArgumentMatcher<Float> matcher) {
    reportMatcher(matcher);
    return 0;
  }

  /**
   * Allows creating custom {@code double} argument matchers.
   *
   * @param matcher decides whether argument matches
   *
   * @return {@code 0}
   */
  public static double doubleThat (ArgumentMatcher<Double> matcher) {
    reportMatcher(matcher);
    return 0;
  }

  private static void reportMatcher (ArgumentMatcher<?> argumentMatcher) {
    stack().push(argumentMatcher);
  }

  // https://github.com/mockito/mockito/blob/release/2.x/src/main/java/org/mockito/ArgumentMatchers.java#L1103
  private Matchers () {
  }
}
