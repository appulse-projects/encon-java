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

package io.appulse.encon.connection.control;

import lombok.Getter;
import lombok.val;

/**
 * Enum of control message tags.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@Getter
public enum ControlMessageTag {

  /**
   * Link control message tag.
   */
  LINK(1, Link.class),

  /**
   * Send control message tag.
   */
  SEND(2, Send.class),

  /**
   * Exit control message tag.
   */
  EXIT(3, Exit.class),

  /**
   * Unlink control message tag.
   */
  UNLINK(4, Unlink.class),

  /**
   * Link node control message tag.
   */
  NODE_LINK(5, NodeLink.class),

  /**
   * Registration control message tag.
   */
  REG_SEND(6, SendToRegisteredProcess.class),

  /**
   * Group leader control message tag.
   */
  GROUP_LEADER(7, GroupLeader.class),

  /**
   * Exit2 control message tag.
   */
  EXIT2(8, Exit2.class),

  /**
   * Send trace token control message tag.
   */
  SEND_TT(12, SendTraceToken.class),

  /**
   * Exit trace token control message tag.
   */
  EXIT_TT(13, ExitTraceToken.class),

  /**
   * Send registration trace token control message tag.
   */
  REG_SEND_TT(16, SendToRegisteredProcessTraceToken.class),

  /**
   * Exit2 trace token control message tag.
   */
  EXIT2_TT(18, Exit2TraceToken.class),

  /**
   * Monitor process control message tag.
   */
  MONITOR_P(19, MonitorProcess.class),

  /**
   * Demonitor process control message tag.
   */
  DEMONITOR_P(20, DemonitorProcess.class),

  /**
   * Monitor process exit control message tag.
   */
  MONITOR_P_EXIT(21, MonitorProcessExit.class),

  /**
   * Undefined control message tag.
   */
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
