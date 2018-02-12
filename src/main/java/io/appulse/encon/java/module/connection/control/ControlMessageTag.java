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

  LINK(1, LinkControlMessage.class),
  SEND(2, SendControlMessage.class),
  EXIT(3, ExitControlMessage.class),
  UNLINK(4, UnlinkControlMessage.class),
  NODE_LINK(5, NodeLinkControlMessage.class),
  REG_SEND(6, RegistrationSendControlMessage.class),
  GROUP_LEADER(7, GroupLeaderControlMessage.class),
  EXIT2(8, Exit2ControlMessage.class),
  SEND_TT(12, SendTraceTokenControlMessage.class),
  EXIT_TT(13, ExitTraceTokenControlMessage.class),
  REG_SEND_TT(16, RegistrationSendTraceTokenControlMessage.class),
  EXIT2_TT(18, Exit2TraceTokenControlMessage.class),
  MONITOR_P(19, MonitorProcessControlMessage.class),
  DEMONITOR_P(20, DemonitorProcessControlMessage.class),
  MONITOR_P_EXIT(21, MonitorProcessExitControlMessage.class);

  private final int code;

  private final Class<? extends ControlMessage> type;

  ControlMessageTag (int code, Class<? extends ControlMessage> type) {
    this.code = code;
    this.type = type;
  }
}
