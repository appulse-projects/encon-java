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

package io.appulse.encon.config;

import static java.util.Locale.US;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import io.appulse.encon.config.ConfigurationProgrammatical.ConfigurationProgrammaticalBuilder;
import io.appulse.encon.config.exception.ConfigDumpingException;
import io.appulse.encon.config.exception.ConfigLoadingException;
import io.appulse.encon.config.exception.ConfigLoadingFileNotFoundException;
import io.appulse.encon.config.exception.ConfigLoadingFileParsingException;
import io.appulse.encon.config.exception.ConfigLoadingMalformedFilePathException;

import lombok.NonNull;
import lombok.val;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
public interface Config {

  /**
   * Loads configuration and parses it into {@link Config} instance.
   * <p>
   * Configuration could be present in two forms:
   * <ul>
   * <li>- YAML files. Extensions: <b>.yml</b> and <b>.yaml</b>;</li>
   * <li>- property files. Extensions: <b>.properties</b> and <b>.xml</b>.</li>
   * </ul>
   *
   * @param fileName configuration resource location.
   *
   * @return parsed {@link Config} instance.
   *
   * @throws ConfigLoadingFileNotFoundException if configuration resource
   *         is not accessible.
   *
   * @throws ConfigLoadingFileParsingException in case of any error
   *         during parsing user's map.
   *
   * @throws ConfigLoadingException if any error occurs
   *         during the resource loading.
   */
  static Config load (@NonNull String fileName) {
    Path path;
    try {
      path = Paths.get(fileName);
    } catch (Exception ex) {
      throw new ConfigLoadingMalformedFilePathException(ex);
    }
    return load(path);
  }

  /**
   * Loads configuration and parses it into {@link Config} instance.
   * <p>
   * Configuration could be present in two forms:
   * <ul>
   * <li>- YAML files. Extensions: <b>.yml</b> and <b>.yaml</b>;</li>
   * <li>- property files. Extensions: <b>.properties</b> and <b>.xml</b>.</li>
   * </ul>
   *
   * @param file configuration resource location.
   *
   * @return parsed {@link Config} instance.
   *
   * @throws ConfigLoadingFileNotFoundException if configuration resource
   *         is not accessible.
   *
   * @throws ConfigLoadingFileParsingException in case of any error
   *         during parsing user's map.
   *
   * @throws ConfigLoadingException if any error occurs
   *         during the resource loading.
   */
  static Config load (@NonNull File file) {
    Path path;
    try {
      path = file.toPath();
    } catch (Exception ex) {
      throw new ConfigLoadingMalformedFilePathException(ex);
    }
    return load(path);
  }

  /**
   * Loads configuration and parses it into {@link Config} instance.
   * <p>
   * Configuration could be present in two forms:
   * <ul>
   * <li>- YAML files. Extensions: <b>.yml</b> and <b>.yaml</b>;</li>
   * <li>- property files. Extensions: <b>.properties</b> and <b>.xml</b>.</li>
   * </ul>
   *
   * @param path configuration resource location.
   *
   * @return parsed {@link Config} instance.
   *
   * @throws ConfigLoadingFileNotFoundException if configuration resource
   *         is not accessible.
   *
   * @throws ConfigLoadingFileParsingException in case of any error
   *         during parsing user's map.
   *
   * @throws ConfigLoadingException if any error occurs
   *         during the resource loading.
   */
  static Config load (@NonNull Path path) {
    URI uri;
    try {
      uri = path.toUri();
    } catch (Exception ex) {
      throw new ConfigLoadingMalformedFilePathException(ex);
    }
    return load(uri);
  }

  /**
   * Loads configuration and parses it into {@link Config} instance.
   * <p>
   * Configuration could be present in two forms:
   * <ul>
   * <li>- YAML files. Extensions: <b>.yml</b> and <b>.yaml</b>;</li>
   * <li>- property files. Extensions: <b>.properties</b> and <b>.xml</b>.</li>
   * </ul>
   *
   * @param uri remote or local configuration resource location.
   *
   * @return parsed {@link Config} instance.
   *
   * @throws ConfigLoadingFileNotFoundException if configuration resource
   *         is not accessible.
   *
   * @throws ConfigLoadingFileParsingException in case of any error
   *         during parsing user's map.
   *
   * @throws ConfigLoadingException if any error occurs
   *         during the resource loading.
   */
  static Config load (@NonNull URI uri) {
    URL url;
    try {
      url = uri.toURL();
    } catch (MalformedURLException ex) {
      throw new ConfigLoadingMalformedFilePathException(ex);
    }
    return load(url);
  }

  /**
   * Loads configuration and parses it into {@link Config} instance.
   * <p>
   * Configuration could be present in two forms:
   * <ul>
   * <li>- YAML files. Extensions: <b>.yml</b> and <b>.yaml</b>;</li>
   * <li>- property files. Extensions: <b>.properties</b> and <b>.xml</b>.</li>
   * </ul>
   *
   * @param url remote or local configuration resource location.
   *
   * @return parsed {@link Config} instance.
   *
   * @throws ConfigLoadingFileNotFoundException if configuration resource
   *         is not accessible.
   *
   * @throws ConfigLoadingFileParsingException in case of any error
   *         during parsing user's map.
   *
   * @throws ConfigLoadingException if any error occurs
   *         during the resource loading.
   */
  static Config load (@NonNull URL url) {
    InputStream inputStream;
    try {
      inputStream = url.openStream();
    } catch (IOException ex) {
      throw new ConfigLoadingFileNotFoundException(ex);
    }

    String path = url.getPath().toLowerCase(US);
    try {
      if (path.endsWith(".yml") || path.endsWith(".yaml")) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(inputStream);
        return load(map);
      }

      Properties properties = new Properties();
      if (path.endsWith(".properties")) {
        properties.load(inputStream);
      } else if (path.endsWith(".xml")) {
        properties.loadFromXML(inputStream);
      } else {
        throw new UnsupportedOperationException("Unsupported file extension " + path);
      }
      return load(properties);
    } catch (ConfigLoadingException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ConfigLoadingException(ex);
    }
  }

  /**
   * Parses properties {@link Map} into {@link Config} instance.
   *
   * @param properties user's defined properties for parsing into
   *                   {@link Config} instance.
   *
   * @return parsed {@link Config} instance
   *
   * @throws ConfigLoadingFileParsingException in case of any error
   *         during parsing user's map.
   */
  static Config load (@NonNull Properties properties) {
    Map<String, Object> map = properties.entrySet()
        .stream()
        .collect(toMap(it -> it.getKey().toString(), Entry::getValue));

    return load(map);
  }

  /**
   * Parses properties {@link Map} into {@link Config} instance.
   *
   * @param map user's defined properties for parsing into
   *            {@link Config} instance.
   *
   * @return parsed {@link Config} instance
   *
   * @throws ConfigLoadingFileParsingException in case of any error
   *         during parsing user's map.
   */
  static Config load (@NonNull Map<String, Object> map) {
    try {
      return new ConfigurationYaml(map);
    } catch (Exception ex) {
      throw new ConfigLoadingFileParsingException(ex);
    }
  }

  static ConfigurationProgrammaticalBuilder builder () {
    return ConfigurationProgrammatical.builder();
  }

  /**
   * Saves {@link Config} instance state to a file. If file doesn't exist, it will be created.
   *
   * @param fileName destination file name.
   *
   * @throws ConfigDumpingException in case of IO errors.
   */
  default void dumpTo (@NonNull String fileName) {
    val path = Paths.get(fileName);
    dumpTo(path);
  }

  /**
   * Saves {@link Config} instance state to a file. If file doesn't exist, it will be created.
   *
   * @param file destination file.
   *
   * @throws ConfigDumpingException in case of IO errors.
   */
  default void dumpTo (@NonNull File file) {
    val path = file.toPath();
    dumpTo(path);
  }

  /**
   * Saves {@link Config} instance state to a file. If file doesn't exist, it will be created.
   *
   * @param path destination path.
   *
   * @throws ConfigDumpingException in case of IO errors.
   */
  void dumpTo (Path path) throws ConfigDumpingException;

  /**
   * Tells if specific key exists or not in {@link Config} instance.
   *
   * @param key option's identifier.
   *
   * @return {@code tru} if exists and {@code false} otherwise.
   */
  boolean containsKey (String key);

  /**
   * Returns parsed value of specific type from specific prefix.
   *
   * @param <T> return value type.
   *
   * @param prefix configuration key offset.
   *
   * @param type return value type class.
   *
   * @return parsed value of specific type if key exists, empty value if not.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  <T> Optional<T> get (String prefix, Class<T> type);

  /**
   * Returns parsed value of specific type from specific prefix.
   *
   * @param <T> return value type.
   *
   * @param prefix configuration key offset.
   *
   * @param type return value type class.
   *
   * @param defaultValue default value
   *
   * @return parsed value of specific type if key exists, default value if not.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default <T> T get (String prefix, @NonNull Class<T> type, T defaultValue) {
    return get(prefix, type).orElse(defaultValue);
  }

  /**
   * Returns parsed value of specific type from the root of {@link Config} tree.
   *
   * @param <T> return value type.
   *
   * @param type return value type class.
   *
   * @return parsed value of specific type if exists, empty value if not.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default <T> Optional<T> get (@NonNull Class<T> type) {
    return get("", type);
  }

  /**
   * Returns parsed value of specific type from the root of {@link Config} tree.
   *
   * @param <T> return value type.
   *
   * @param type return value type class.
   *
   * @param defaultValue default value.
   *
   * @return parsed value of specific type if exists, default value if not.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default <T> T get (@NonNull Class<T> type, T defaultValue) {
    return get(type).orElse(defaultValue);
  }

  /**
   * Returns non-parsed value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Object> get (@NonNull String key) {
    return get(key, Object.class);
  }

  /**
   * Returns non-parsed value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Object get (@NonNull String key, Object defaultValue) {
    return get(key).orElse(defaultValue);
  }

  /**
   * Returns {@link String} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<String> getString (@NonNull String key) {
    return get(key, String.class);
  }

  /**
   * Returns {@link String} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default String getString (@NonNull String key, String defaultValue) {
    return getString(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Character} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Character> getChar (@NonNull String key) {
    return get(key, Character.class);
  }

  /**
   * Returns {@link Character} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Character getChar (@NonNull String key, Character defaultValue) {
    return getChar(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Byte} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Byte> getByte (@NonNull String key) {
    return get(key, Byte.class);
  }

  /**
   * Returns {@link Byte} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Byte getByte (@NonNull String key, Byte defaultValue) {
    return getByte(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Short} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Short> getShort (@NonNull String key) {
    return get(key, Short.class);
  }

  /**
   * Returns {@link Short} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Short getShort (@NonNull String key, Short defaultValue) {
    return getShort(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Integer} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Integer> getInteger (@NonNull String key) {
    return get(key, Integer.class);
  }

  /**
   * Returns {@link Integer} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Integer getInteger (@NonNull String key, Integer defaultValue) {
    return getInteger(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Long} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Long> getLong (@NonNull String key) {
    return get(key, Long.class);
  }

  /**
   * Returns {@link Long} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Long getLong (@NonNull String key, Long defaultValue) {
    return getLong(key).orElse(defaultValue);
  }

  /**
   * Returns {@link BigInteger} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<BigInteger> getBigInteger (@NonNull String key) {
    return get(key, BigInteger.class);
  }

  /**
   * Returns {@link BigInteger} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default BigInteger getBigInteger (@NonNull String key, BigInteger defaultValue) {
    return getBigInteger(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Float} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Float> getFloat (@NonNull String key) {
    return get(key, Float.class);
  }

  /**
   * Returns {@link Float} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Float getFloat (@NonNull String key, Float defaultValue) {
    return getFloat(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Double} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Double> getDouble (@NonNull String key) {
    return get(key, Double.class);
  }

  /**
   * Returns {@link Double} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Double getDouble (@NonNull String key, Double defaultValue) {
    return getDouble(key).orElse(defaultValue);
  }

  /**
   * Returns {@link BigDecimal} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<BigDecimal> getBigDecimal (@NonNull String key) {
    return get(key, BigDecimal.class);
  }

  /**
   * Returns {@link BigDecimal} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default BigDecimal getBigDecimal (@NonNull String key, BigDecimal defaultValue) {
    return getBigDecimal(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Boolean} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Boolean> getBoolean (@NonNull String key) {
    return get(key, Boolean.class);
  }

  /**
   * Returns {@link Boolean} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Boolean getBoolean (@NonNull String key, Boolean defaultValue) {
    return getBoolean(key).orElse(defaultValue);
  }

  /**
   * Returns {@link List} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<List<Object>> getList (@NonNull String key) {
    return getList(key, Object.class);
  }

  /**
   * Returns {@link List} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default List<Object> getList (@NonNull String key, List<Object> defaultValue) {
    return getList(key).orElse(defaultValue);
  }

  /**
   * Returns {@link List} value at specific key.
   *
   * @param <T> return value type.
   *
   * @param key option's key.
   *
   * @param type value's type.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  <T> Optional<List<T>> getList (@NonNull String key, @NonNull Class<T> type);

  /**
   * Returns {@link List} value at specific key.
   *
   * @param <T> return value type.
   *
   * @param key option's key.
   *
   * @param type value's type.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default <T> List<T> getList (@NonNull String key, @NonNull Class<T> type, List<T> defaultValue) {
    return getList(key, type).orElse(defaultValue);
  }

  /**
   * Returns configuration root {@link Map} instance.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Map<String, Object>> getRootMap () {
    return getMap("");
  }

  /**
   * Returns configuration root {@link Map} instance.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Map<String, Object> getRootMap (Map<String, Object> defaultValue) {
    return getRootMap().orElse(defaultValue);
  }

  /**
   * Returns configuration root {@link Map} instance parsed with specific value.
   *
   * @param <T> return value type.
   *
   * @param type value's type.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default <T> Optional<Map<String, T>> geRootMap (@NonNull Class<T> type) {
    return getMap("", type);
  }

  /**
   * Returns configuration root {@link Map} instance parsed with specific value.
   *
   * @param <T> return value type.
   *
   * @param type value's type.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default <T> Map<String, T> getRootMap (@NonNull Class<T> type, Map<String, T> defaultValue) {
    return geRootMap(type).orElse(defaultValue);
  }

  /**
   * Returns {@link Map} value at specific key.
   *
   * @param key option's key.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Optional<Map<String, Object>> getMap (@NonNull String key) {
    return getMap(key, Object.class);
  }

  /**
   * Returns {@link Map} value at specific key.
   *
   * @param key option's key.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default Map<String, Object> getMap (@NonNull String key, Map<String, Object> defaultValue) {
    return getMap(key).orElse(defaultValue);
  }

  /**
   * Returns {@link Map} value at specific key.
   *
   * @param <T> return value type.
   *
   * @param key option's key.
   *
   * @param type value's type.
   *
   * @return value at specific key if exists, or empty.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  <T> Optional<Map<String, T>> getMap (@NonNull String key, @NonNull Class<T> type);

  /**
   * Returns {@link Map} value at specific key.
   *
   * @param <T> return value type.
   *
   * @param key option's key.
   *
   * @param type value's type.
   *
   * @param defaultValue default value.
   *
   * @return value at specific key if exists, or default value.
   *
   * @throws ConfigMappingUnsupportedTypeException couldn't map specified type.
   *
   * @throws ConfigMappingNoMappingFunctionException there is no mapping function for type.
   *
   * @throws ConfigMappingEnumValueNotFoundException in case of errors with {@code enum} types.
   *
   * @throws ConfigMappingException common mapping error.
   */
  default <T> Map<String, T> getMap (@NonNull String key, @NonNull Class<T> type, Map<String, T> defaultValue) {
    return getMap(key, type).orElse(defaultValue);
  }
}
