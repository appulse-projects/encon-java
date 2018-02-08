
package io.appulse.encon.java.protocol.control;

import io.appulse.encon.java.protocol.type.IntegralNumber;
import io.appulse.encon.java.protocol.type.Nil;
import io.appulse.encon.java.protocol.type.Pid;
import io.appulse.encon.java.protocol.type.Tuple;
import lombok.NonNull;

public abstract class ControlMessage {


  public static Tuple link (@NonNull Pid from, @NonNull Pid to) {
    return Tuple.builder()
        .add(IntegralNumber.from(1))
        .add(from)
        .add(to)
        .build();
  }

  public static Tuple send (@NonNull Pid to) {
    return Tuple.builder()
        .add(IntegralNumber.from(2))
        .add(new Nil())
        .add(to)
        .build();
  }

  public static Tuple exit (@NonNull Pid from, @NonNull Pid to, @NonNull String reason) {
    return Tuple.builder()
        .add(IntegralNumber.from(3))
        .add(from)
        .add(to)
        .add(null)
        .build();
  }

  public static Tuple unlink (@NonNull Pid from, @NonNull Pid to) {
    return Tuple.builder()
        .add(IntegralNumber.from(4))
        .add(from)
        .add(to)
        .build();
  }

  public static Tuple nodeLink () {
    return Tuple.builder()
        .add(IntegralNumber.from(5))
        .build();
  }

  public static Tuple regSend (@NonNull Pid from, @NonNull String toName) {
    return Tuple.builder()
        .add(IntegralNumber.from(6))
        .add(from)
        .add(new Nil())
        .add(null)
        .build();
  }
}
