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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.appulse.encon.config.exception.ConfigLoadingFileNotFoundException;
import io.appulse.utils.ResourceUtils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@DisplayName("Configuration loading tests")
class LoadTest {

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("try to load nonexistent configuration file")
  void loadNonexistentFile () {
    assertThatThrownBy(() -> Config.load("nonexistent.yml"))
        .isInstanceOf(ConfigLoadingFileNotFoundException.class);
  }

  @Test
  @DisplayName("load configuration from map")
  void loadMap () {
    Map<String, Object> subMap = new HashMap<>();
    subMap.put("name", "popa");
    subMap.put("my.age", 11);

    Map<String, Object> map = new HashMap<>();
    map.put("one.two.three.four", 4);
    map.put("one.two.three.five.six", 6);
    map.put("one.two.three.seven", asList(1, 2, 3, subMap));
    map.put("zero", 0);

    Config config = Config.load(map);
    checkConfig(config, "<none>");
  }

//  @Test
//  @DisplayName("load configuration from properties")
  void loadProperties () {
    Properties properties = new Properties();
    properties.put("one.two.three.four", 4);
    properties.put("one.two.three.five.six", 6);
    properties.put("one.two.three.seven.[0]", 1);
    properties.put("one.two.three.seven.1", 2);
    properties.put("one.two.three.seven.[2]", 3);
    properties.put("one.two.three.seven.3.name", "popa");
    properties.put("one.two.three.seven.[3].my.age", 11);
    properties.put("zero", 0);

    Config config = Config.load(properties);
    checkConfig(config, "<none>");
  }

  @Test
  @DisplayName("load configuration by URL")
  void loadUrl () {
    getUrlFiles().forEach(url -> {
      Config config = Config.load(url);
      checkConfig(config, url.toString());
    });
  }

  @Test
  @DisplayName("load configuration by URI")
  void loadUri () {
    getUriFiles().forEach(uri -> {
      Config config = Config.load(uri);
      checkConfig(config, uri.toString());
    });
  }

  @Test
  @DisplayName("load configuration by File")
  void loadFile () {
    getUriFiles().stream().map(File::new).forEach(file -> {
      Config config = Config.load(file);
      checkConfig(config, file.toString());
    });
  }

  @Test
  @DisplayName("load configuration by Path")
  void loadPath () {
    getUriFiles().stream().map(Paths::get).forEach(path -> {
      Config config = Config.load(path);
      checkConfig(config, path.toString());
    });
  }

  @Test
  @DisplayName("load configuration by file name")
  void loadFileName () {
    getUriFiles().stream().map(Paths::get).map(Path::toString).forEach(fileName -> {
      Config config = Config.load(fileName);
      checkConfig(config, fileName);
    });
  }

  private void checkConfig (Config config, String file) {
    assertThat(config).isNotNull();

    assertThat(config.getString("one.two.three.four"))
        .as("Key 'one.two.three.four' at file %s", file)
        .isPresent()
        .hasValue("4");

    assertThat(config.getString("one.two.three.five.six"))
        .as("Key 'one.two.three.five.six' at file %s", file)
        .isPresent()
        .hasValue("6");

    assertThat(config.get("one.two.three.seven"))
        .as("Key 'one.two.three.seven' at file %s", file)
        .isPresent()
        .get()
        .isInstanceOf(List.class);

    assertThat(config.getString("one.two.three.seven.0"))
        .as("Key 'one.two.three.seven.0' at file %s", file)
        .isPresent()
        .hasValue("1");

    assertThat(config.getString("one.two.three.seven.[1]"))
        .as("Key 'one.two.three.seven.[1]' at file %s", file)
        .isPresent()
        .hasValue("2");

    assertThat(config.getString("one.two.three.seven.2"))
        .as("Key 'one.two.three.seven.2' at file %s", file)
        .isPresent()
        .hasValue("3");

    assertThat(config.getString("one.two.three.seven.[3].name"))
        .as("Key 'one.two.three.seven.[3].name' at file %s", file)
        .isPresent()
        .hasValue("popa");

    assertThat(config.getString("one.two.three.seven.3.my.age"))
        .as("Key 'one.two.three.seven.3.my.age' at file %s", file)
        .isPresent()
        .hasValue("11");

    assertThat(config.getString("zero"))
        .as("Key 'zero' at file %s", file)
        .isPresent()
        .hasValue("0");
  }

  @SneakyThrows
  private List<URI> getUriFiles () {
    return getUrlFiles().stream()
        .map(it -> {
          try {
            return it.toURI();
          } catch (Exception ex) {
            return null;
          }
        })
        .collect(toList());
  }

  private List<URL> getUrlFiles () {
    return ResourceUtils.getResourceUrls("load", "file.y*");
  }
}
