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

package io.appulse.encon.examples.echo.server.spring;

import io.appulse.encon.spring.ErlangMailbox;
import io.appulse.encon.spring.InjectMailbox;
import io.appulse.encon.spring.MailboxOperations;

import lombok.experimental.Delegate;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@ErlangMailbox(
    node = "echo-client"
)
public class EchoClient {

  @Delegate
  @InjectMailbox
  MailboxOperations self;
}
