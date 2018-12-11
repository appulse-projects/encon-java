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

import static java.util.Comparator.reverseOrder;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.appulse.utils.ResourceUtils;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
@DisplayName("Dumping configuration tests")
class DumpTest {

  static Path FOLDER;

  @BeforeAll
  @SneakyThrows
  static void beforeAll () {
    FOLDER = Paths.get("dump_test_folder");

    if (Files.exists(FOLDER)) {
      Files.walk(FOLDER)
          .map(Path::toFile)
          .sorted(reverseOrder())
          .forEach(File::delete);
    }
    Files.createDirectories(FOLDER);
  }

  @AfterAll
  @SneakyThrows
  static void afterAll () {
    FOLDER = Paths.get("dump_test_folder");

    if (Files.exists(FOLDER)) {
      Files.walk(FOLDER)
          .map(Path::toFile)
          .sorted(reverseOrder())
          .forEach(File::delete);
    }
    Files.deleteIfExists(FOLDER);
  }

  @BeforeEach
  void beforeEach (TestInfo testInfo) {
    System.out.println("- " + testInfo.getDisplayName());
  }

  @Test
  @DisplayName("dump by file name")
  void dumpFileName () {
    ResourceUtils.getResourceUrls("dump", "file.*").forEach(url -> {
      Config config1 = Config.load(url);

      String name = url.getPath().endsWith(".yml")
                    ? "file_name-dump.yml"
                    : "file_name-dump.properties";

      Path dumpPath = FOLDER.resolve(name);
      config1.dumpTo(dumpPath.toString());

      Config config2 = Config.load(dumpPath);
      assertThat(config2).isEqualTo(config1);
    });
  }

  @Test
  @DisplayName("dump by file object")
  void dumpFile () {
    ResourceUtils.getResourceUrls("dump", "file.*").forEach(url -> {
      Config config1 = Config.load(url);

      String name = url.getPath().endsWith(".yml")
                    ? "file-dump.yml"
                    : "file-dump.properties";

      Path dumpPath = FOLDER.resolve(name);
      config1.dumpTo(dumpPath.toFile());

      Config config2 = Config.load(dumpPath);
      assertThat(config2).isEqualTo(config1);
    });
  }

  @Test
  @DisplayName("dump by file path")
  void dumpPath () {
    ResourceUtils.getResourceUrls("dump", "file.*").forEach(url -> {
      Config config1 = Config.load(url);

      String name = url.getPath().endsWith(".yml")
                    ? "path-dump.yml"
                    : "path-dump.properties";

      Path dumpPath = FOLDER.resolve(name);
      config1.dumpTo(dumpPath);

      Config config2 = Config.load(dumpPath);
      assertThat(config2).isEqualTo(config1);
    });
  }
}
