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

import io.appulse.encon.spring.MatchingCaseMapping;

import io.appulse.encon.spring.ErlangMailbox;
import io.appulse.encon.spring.InjectMailbox;
import io.appulse.encon.spring.MailboxOperations;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @since 1.6.0
 * @author Artem Labazin
 */
@Slf4j
@ErlangMailbox(
    node = "echo-server",
    name = "echo"
)
public class EchoServer {

  @Delegate
  @InjectMailbox
  MailboxOperations self;

  @MatchingCaseMapping
  public void handle (ErlangPid sender, ErlangTerm payload) {
    log.info("a new message #1 type:\n  {}, {}", sender, payload);
    self.send(sender, new MyPojo1(self.pid(), payload));
  }

  @MatchingCaseMapping
  public void handle (MyPojo2 request) {
    log.info("a new message #2 type:\n  {}", request);
    MyPojo2 response = MyPojo2.builder()
        .sender(pid())
        .name(request.getName())
        .age(request.getAge())
        .male(request.isMale())
        .languages(request.getLanguages())
        .position(request.getPosition())
        .set(request.getSet())
        .listString(request.getListString())
        .bools(request.getBools())
        .build();

    send(request.getSender(), response);
  }
}
