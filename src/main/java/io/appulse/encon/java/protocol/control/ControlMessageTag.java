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

package io.appulse.encon.java.protocol.control;

import lombok.Getter;

/**
 *
 * @author Artem Labazin
 * @since 0.0.1
 */
@Getter
public enum ControlMessageTag {

  LINK(1),
  SEND(2),
  EXIT(3),
  UNLINK(4),
  NODE_LINK(5),
  REG_SEND(6),
  GROUP_LEADER(7),
  EXIT2(8),
  SEND_TT(12),
  EXIT_TT(13),
  REG_SEND_TT(16),
  EXIT2_TT(18),
  MONITOR_P(19),
  DEMONITOR_P(20),
  MONITOR_P_EXIT(21);

  private final int code;

  ControlMessageTag (int code) {
    this.code = code;
  }
}
