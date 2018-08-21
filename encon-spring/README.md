# Overview

Package for Encon's Spring integration.

Example:

```java

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Set;

import io.appulse.encon.databind.annotation.AsErlangAtom;
import io.appulse.encon.databind.annotation.AsErlangList;
import io.appulse.encon.databind.annotation.AsErlangTuple;
import io.appulse.encon.databind.annotation.IgnoreField;
import io.appulse.encon.spring.ErlangMailbox;
import io.appulse.encon.spring.InjectMailbox;
import io.appulse.encon.spring.MailboxOperations;
import io.appulse.encon.spring.MatchingCaseMapping;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangInteger;
import io.appulse.encon.terms.type.ErlangPid;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import lombok.NoArgsConstructor;


@ErlangMailbox(           // registers a new Bean
    node = "echo-server", // node's name, it will be autocreated
    name = "echo"         // mailbox's name for "self" field (see it below)
)
class EchoServer {

  @Delegate // use Lombok's helper annotation, for easy mailbox use
  @InjectMailbox // instead of @Autowire/@Inject you MUST use this annotation
  MailboxOperations self;

  // receives a tuple: {pid, 42, payload}
  @MatchingCaseMapping("{any(), eq(42), any()}")
  public void handle (ErlangPid sender, ErlangInteger number, ErlangTerm payload) {
    // some code

    // send response back
    self.send(sender, tuple("your", "response", "object"));
  }

  // receives a tuple with POJO's fields
  @MatchingCaseMapping
  public void handle (MyPojo request) {
    // some code
  }

  @Data
  @AsErlangTuple // the way, how to wrap POJO's fields
  @NoArgsConstructor // be sure, what you have a no-args constructor
  @FieldDefaults(level = PRIVATE)
  @EqualsAndHashCode(exclude = "ignored")
  public static class MyPojo {

    String name; // ErlangString

    int age; // ErlangInteger

    boolean male; // ErlangAtom

    // this field will be ignored
    // during serialization/deserialization
    @IgnoreField
    int ignored;

    List<String> languages; // ErlangList<ErlangString>

    @AsErlangAtom
    String position; // ErlangAtom

    @AsErlangList
    Set<String> set; // ErlangList<ErlangString>

    @AsErlangList
    String listString; // ErlangList
  }
}

```
