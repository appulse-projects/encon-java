# Overview

This project contains the general-purpose data-binding functionality.

## Usage

First of all, add encon's dependency:

**Maven**:

```xml
<dependencies>
    ...
    <dependency>
        <groupId>io.appulse.encon</groupId>
        <artifactId>encon</artifactId>
        <version>1.3.0</version>
    </dependency>
    ...
</dependencies>
```

**Gradle**:

```groovy
compile 'io.appulse.encon.java:encon:1.3.0'
```

Let's create a new `Erlang` node:

```java

import io.appulse.encon.java.config.NodeConfig;
import io.appulse.encon.java.Node;
import io.appulse.encon.java.Nodes;


// Creating node's config.
// For more details - see encon-config project
NodeConfig config = NodeConfig.builder()
    .cookie("secret")
    .build();

// Creates, registers and starts a new Erlang node
Node node = Nodes.singleNode("echo-node", config);

```

After `node` creation, we could register several mailboxes:

> **IMPORTANT:** The main differenece between `MailboxQueueType.NON_BLOCKING` and `MailboxQueueType.BLOCKING` mailbox types is the way to extract the values from a queue.
>
> * `MailboxQueueType.NON_BLOCKING` - uses java.util.Queue.poll() under the hood;
>
> * `MailboxQueueType.BLOCKING` - uses java.util.concurrent.BlockingQueue.take() method (automatically performs type checking)

```java

import static io.appulse.encon.mailbox.MailboxQueueType.NON_BLOCKING;
import static io.appulse.encon.java.terms.Erlang.tuple;

import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;


// Mailbox #1
// ----------
// Default mailbox's type is `MailboxQueueType.BLOCKING`,
// it uses `java.util.concurrent.LinkedBlockingQueue` under the hood.
//
Mailbox mailbox1 = node.mailbox()
    .name("echo-mailbox-1")
    .build();

for (int count = 0; count < 3; count++) {
  // Mailbox.receive() is a blocking operation in that case
  Message message = mailbox.receive();

  ErlangTerm body = message.getBody();
  mailbox1.send("another-node", "another-mailbox", tuple(
    mailbox1.getPid(), body.getUnsafe(1)
  ));
}


// Mailbox #2
// ----------
// Specify `MailboxQueueType.NON_BLOCKING` type, in this case
// it uses `java.util.concurrent.ConcurrentLinkedQueue` under the hood.
//
Mailbox mailbox2 = node.mailbox()
    .name("echo-mailbox-2")
    .type(NON_BLOCKING)
    .build();

while (true) {
  // Mailbox.receive() is a non-blocking operation in this case,
  // that is why we have `if` clause.
  Message message = mailbox2.receive();
  if (message == null) {
    continue;
  }
  ErlangTerm body = message.getBody();
  mailbox2.send("another-node", "another-mailbox", tuple(
    mailbox2.getPid(), body.getUnsafe(1)
  ));
}


// Mailbox #3
// ----------
// Specify `MailboxQueueType.NON_BLOCKING` type and
// set our own `java.util.Queue` instance.
//
Mailbox mailbox3 = node.mailbox()
    .name("echo-mailbox-2")
    .type(NON_BLOCKING)
    .queue(new LinkedBlockingQueue<>())
    .build();

while (true) {
  // Mailbox.receive() is a non-blocking operation in this case,
  // that is why we have `if` clause.
  Message message = mailbox3.receive();
  if (message == null) {
    continue;
  }
  ErlangTerm body = message.getBody();
  mailbox3.send("another-node", "another-mailbox", tuple(
    mailbox3.getPid(), body.getUnsafe(1)
  ));
}

```
