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

package io.appulse.encon.benchmark;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.appulse.epmd.java.server.cli.CommonOptions;
import io.appulse.epmd.java.server.command.server.ServerCommandExecutor;
import io.appulse.epmd.java.server.command.server.ServerCommandOptions;
import io.appulse.utils.SocketUtils;

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
public class Main {

  private static ExecutorService executor;

  private static ServerCommandExecutor server;

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

  private static void startEpmd () {
    if (!SocketUtils.isPortAvailable(4369)) {
      return;
    }
    executor = Executors.newSingleThreadExecutor();

    ServerCommandOptions options = new ServerCommandOptions();
    options.setChecks(true);

    server = new ServerCommandExecutor(new CommonOptions(), options);
    executor.execute(server::execute);
  }

  private static void stopEpmd () {
    if (executor == null) {
      return;
    }
    server.close();
    executor.shutdown();
  }
}
