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

package io.appulse.encon.connection.control;

import lombok.Getter;
import lombok.val;

/**
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
public enum ControlMessageTag {

  LINK(1, Link.class),
  SEND(2, Send.class),
  EXIT(3, Exit.class),
  UNLINK(4, Unlink.class),
  NODE_LINK(5, NodeLink.class),
  REG_SEND(6, SendToRegisteredProcess.class),
  GROUP_LEADER(7, GroupLeader.class),
  EXIT2(8, Exit2.class),
  SEND_TT(12, SendTraceToken.class),
  EXIT_TT(13, ExitTraceToken.class),
  REG_SEND_TT(16, SendToRegisteredProcessTraceToken.class),
  EXIT2_TT(18, Exit2TraceToken.class),
  MONITOR_P(19, MonitorProcess.class),
  DEMONITOR_P(20, DemonitorProcess.class),
  MONITOR_P_EXIT(21, MonitorProcessExit.class),
  UNDEFINED(-1, ControlMessage.class);

  private final int code;

  private final Class<? extends ControlMessage> type;

  ControlMessageTag (int code, Class<? extends ControlMessage> type) {
    this.code = code;
    this.type = type;
  }

  public static ControlMessageTag of (int code) {
    for (val tag : values()) {
      if (tag.getCode() == code) {
        return tag;
      }
    }
    return UNDEFINED;
  }
}
