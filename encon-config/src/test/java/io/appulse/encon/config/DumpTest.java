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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
public class DumpTest {

  static Path FOLDER;

  @BeforeClass
  @SneakyThrows
  public static void beforeClass () {
    FOLDER = Paths.get("dump_test_folder");

    if (Files.exists(FOLDER)) {
      Files.walk(FOLDER)
          .map(Path::toFile)
          .sorted(reverseOrder())
          .forEach(File::delete);
    }
    Files.createDirectories(FOLDER);
  }

  @AfterClass
  @SneakyThrows
  public static void afterClass () {
    FOLDER = Paths.get("dump_test_folder");

    if (Files.exists(FOLDER)) {
      Files.walk(FOLDER)
          .map(Path::toFile)
          .sorted(reverseOrder())
          .forEach(File::delete);
    }
    Files.deleteIfExists(FOLDER);
  }

  @Test
  public void dumpFileName () {
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
  public void dumpFile () {
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
  public void dumpPath () {
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
