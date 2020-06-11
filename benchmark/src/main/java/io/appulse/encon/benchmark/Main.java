/*
 * Copyright 2020 the original author or authors.
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

package io.appulse.encon.benchmark;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.appulse.epmd.java.server.SubcommandServer;
import io.appulse.utils.SocketUtils;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.openjdk.jmh.runner.Defaults;
import org.openjdk.jmh.runner.NoBenchmarksException;
import org.openjdk.jmh.runner.ProfilersFailedException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.VerboseMode;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@Slf4j
public class Main {

  private static ExecutorService executor;

  private static Future<?> future;

  public static void main(String[] argv) throws RunnerException, IOException {
    try {
      CommandLineOptions cmdOptions = new CommandLineOptions(argv);

      Runner runner = new Runner(cmdOptions);

      if (cmdOptions.shouldHelp()) {
        cmdOptions.showHelp();
        return;
      }

      if (cmdOptions.shouldList()) {
        runner.list();
        return;
      }

      if (cmdOptions.shouldListWithParams()) {
        runner.listWithParams(cmdOptions);
        return;
      }

      if (cmdOptions.shouldListProfilers()) {
        cmdOptions.listProfilers();
        return;
      }

      if (cmdOptions.shouldListResultFormats()) {
        cmdOptions.listResultFormats();
        return;
      }

      startEpmd();
      try {
        // System.out.println("Press ENTER for start...");
        // System.console().readLine();
        runner.run();
        stopEpmd();
      } catch (NoBenchmarksException e) {
        stopEpmd();
        System.err.println("No matching benchmarks. Miss-spelled regexp?");

        if (cmdOptions.verbosity().orElse(Defaults.VERBOSITY) != VerboseMode.EXTRA) {
          System.err.println("Use " + VerboseMode.EXTRA + " verbose mode to debug the pattern matching.");
        } else {
          runner.list();
        }
        System.exit(1);
      } catch (ProfilersFailedException e) {
        stopEpmd();
        // This is not exactly an error, set non-zero exit code
        System.err.println(e.getMessage());
        System.exit(1);
      } catch (RunnerException e) {
        stopEpmd();
        System.err.print("ERROR: ");
        e.printStackTrace(System.err);
        System.exit(1);
      }
    } catch (CommandLineOptionException e) {
      stopEpmd();
      System.err.println("Error parsing command line:");
      System.err.println(" " + e.getMessage());
      System.exit(1);
    }
  }

  @SneakyThrows
  private static void startEpmd () {
    if (!SocketUtils.isPortAvailable(4369)) {
      return;
    }
    executor = Executors.newSingleThreadExecutor();

    val server = SubcommandServer.builder()
        .port(SocketUtils.findFreePort().orElseThrow(RuntimeException::new))
        .ip(InetAddress.getByName("0.0.0.0"))
        .build();

    future = executor.submit(() -> {
      try {
        server.run();
      } catch (Throwable ex) {
        log.error("popa", ex);
      }
    });
    SECONDS.sleep(1);
  }

  private static void stopEpmd () {
    if (executor == null) {
      return;
    }
    if (future != null) {
      future.cancel(true);
    }
    executor.shutdown();
  }
}
