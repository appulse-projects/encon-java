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

package io.appulse.encon.examples.load.config;

import java.io.File;

import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.Config;

/**
 *
 * @since 1.6.2
 * @author Artem Labazin
 */
public class Server {

  Nodes nodes;

  Config config;

  public void start () {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("nodes.yml").getFile());

    config = Config.load(file);
    nodes = Nodes.start(config);
  }

  public Node getNode (String name) {
    return nodes.node(name)
        .orElseThrow(RuntimeException::new);
  }

  public void stop () {
    nodes.close();
  }
}
