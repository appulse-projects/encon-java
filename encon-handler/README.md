# Overview

Different classes for convenient mailbox handling.

## Usage

First of all, add dependency:

**Maven**:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>io.appulse.encon</groupId>
    <artifactId>encon-handler</artifactId>
    <version>1.6.3</version>
  </dependency>
  ...
</dependencies>
```

**Gradle**:

```groovy
compile 'io.appulse.encon:encon-handler:1.6.3'
```

### Basics

Let's create a `dummy` message handler implementation:

```java

import io.appulse.encon.connection.control.ControlMessage;
import io.appulse.encon.handler.message.MessageHandler;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;



class DummyMessageHandler implements MessageHandler {

  @Override
  public void handle (Mailbox self, ControlMessage header, ErlangTerm body) {
    System.out.println("A new message");
  }
}
```

Now we can use it in one of the prepared `MailboxHandler` implementation:

```java

import io.appulse.encon.handler.mailbox.BlockingMailboxHandler;
import io.appulse.encon.handler.mailbox.MailboxHandler;



MailboxHandler handler = BlockingMailboxHandler.builder()
  .messageHandler(new DummyMessageHandler())
  .mailbox(myMailbox)
  .build();

// start it in a separate thread:
handler.startExecutor();
// ...
handler.close();
```

### Advanced

Imagine, you have two services:

```java

public static class MyService1 {

  public void handler1 () {
    // ...
  }

  public void handler2 (int a, String b, boolean c) {
    // ...
  }
}

public static class MyService2 {

  public void popa (int a) throws Exception {
    // ...
  }
}
```

And now, you would like to route incoming messages to those services. You can do it the following way:

```java

import static io.appulse.encon.handler.message.matcher.Matchers.anyString;
import static io.appulse.encon.handler.message.matcher.Matchers.anyInt;
import static io.appulse.encon.handler.message.matcher.Matchers.eq;

import io.appulse.encon.handler.message.MessageHandler;
import io.appulse.encon.handler.message.matcher.MethodMatcherMessageHandler;



MyService1 service1 = new MyService1();
MyService2 service2 = new MyService2();

MessageHandler handler = MethodMatcherMessageHandler.builder()
    .wrap(service1)
        // redirects '[]' (empty list) to method MyService1.handler1
        .list(it -> it.handler1())
        // redirects tuple {any_number, any_string, atom(false)}
        // to MyService1.handler2
        .tuple(it -> it.handler2(anyInt(), anyString(), eq(false)))
    .wrap(service2)
        // redirects {42} to MyService2.popa
        .tuple(it -> it.popa(42))
    .build();

// start it in a separate thread:
handler.startExecutor();
// ...

// handler is a Closeable object
handler.close();
```
