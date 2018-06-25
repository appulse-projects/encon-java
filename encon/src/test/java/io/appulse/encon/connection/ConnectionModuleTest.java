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

package io.appulse.encon.connection;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;

import org.junit.Test;

/**
 *
 * @author Artem Labazin
 * @since 1.0.0
 */
public class ConnectionModuleTest {

  @Test
  public void futureTests () {
    CompletableFuture<String> future = CompletableFuture.completedFuture("Hello world");
    assertThat(future)
        .isCompleted()
        .isCompletedWithValue("Hello world");

    assertThat(future.cancel(true))
        .isFalse();

    assertThat(future.complete("popa"))
        .isFalse();

    assertThat(future)
        .isCompleted()
        .isCompletedWithValue("Hello world");

    CompletableFuture<String> exFuture = future.exceptionally(ex -> "uh?");

    assertThat(future.completeExceptionally(new RuntimeException()))
        .isFalse();

    assertThat(exFuture.isCompletedExceptionally())
        .isFalse();

    assertThat(exFuture)
        .isCompleted()
        .isCompletedWithValue("Hello world");
  }
}
