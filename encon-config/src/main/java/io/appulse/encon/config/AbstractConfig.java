/*
 * Copyright 2019 the original author or authors.
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

import static io.appulse.encon.config.ConfigTree.NodeType.LIST;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.Locale.US;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import io.appulse.encon.config.ConfigTree.ConfigNode;
import io.appulse.encon.config.ConfigTree.ConfigNodeList;
import io.appulse.encon.config.ConfigTree.ConfigNodeMap;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Artem Labazin
 * @since 2.0.0
 */
abstract class AbstractConfig implements Config {

  @Override
  @SneakyThrows
  public void dumpTo (@NonNull Path path) {
    if (Files.notExists(path)) {
      Files.createFile(path);
    }

    Map<String, Object> map = getConfigTree().toMap();
    @Cleanup Writer writer = Files.newBufferedWriter(path, CREATE);

    String fileName = path.toString().toLowerCase(US);
    if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
      Yaml yaml = new Yaml();
      yaml.dump(map, writer);
      return;
    }

    Properties properties = new Properties();
    properties.putAll(map);

    if (fileName.endsWith(".properties")) {
      properties.store(writer, fileName);
    } else if (fileName.endsWith(".xml")) {
      @Cleanup OutputStream outputStream = Files.newOutputStream(path);
      properties.storeToXML(outputStream, fileName, fileName);
    } else {
      throw new UnsupportedOperationException("Unsupported file extension " + fileName);
    }
  }

  @Override
  public boolean containsKey (@NonNull String key) {
    return getConfigTree().containsKey(key);
  }

  @Override
  public <T> Optional<T> get (String key, @NonNull Class<T> type) {
    ConfigNode node = getConfigTree().get(key);
    if (node == null) {
      return empty();
    }
    T result = ConfigNodeMapping.map(node, type);
    return ofNullable(result);
  }

  @Override
  public <T> Optional<List<T>> getList (@NonNull String key, @NonNull Class<T> type) {
    ConfigNode node = getConfigTree().get(key);
    if (node == null || node.getType() != LIST) {
      return empty();
    }
    List<T> result = ConfigNodeMapping.toGenericList((ConfigNodeList) node, type);
    return ofNullable(result);
  }

  @Override
  public <T> Optional<Map<String, T>> getMap (@NonNull String key, @NonNull Class<T> type) {
    ConfigNode node = getConfigTree().get(key);
    if (node == null) {
      return empty();
    }
    Map<String, T> result = ConfigNodeMapping.toGenericMap((ConfigNodeMap) node, type);
    return ofNullable(result);
  }

  abstract ConfigTree getConfigTree ();
}
