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
    <version>1.6.0</version>
  </dependency>
  ...
</dependencies>
```

**Gradle**:

```groovy
compile 'io.appulse.encon.java:encon:1.6.0'
```

Let's create a new `Erlang` node:

```java

import io.appulse.encon.java.config.NodeConfig;
import io.appulse.encon.java.Node;
import io.appulse.encon.java.Nodes;


// Creating node's config.
// For more details - see encon-config project
NodeConfig config = NodeConfig.builder()
    .shortNamed(true) // true - for local nodes, false (default) - for remote accessable
    .cookie("secret")
    .build();

// Creates, registers in EPMD and starts a new Erlang node
Node node = Nodes.singleNode("echo-node", config);

```

After `node` creation, we could register several mailboxes:

> **IMPORTANT:** The main differenece between `MailboxQueueType.NON_BLOCKING` and `MailboxQueueType.BLOCKING` mailbox types is the way to extract the values from a queue.
>
> * `MailboxQueueType.NON_BLOCKING` - uses java.util.Queue.poll() under the hood;
>
> * `MailboxQueueType.BLOCKING` - uses java.util.concurrent.BlockingQueue.take() method (automatically performs type checking)

```java

import static io.appulse.encon.java.terms.Erlang.tuple;

import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;


// Mailbox #1
// ----------
// Mailbox's type is `MailboxQueueType.BLOCKING`, by default
// it uses `java.util.concurrent.LinkedBlockingQueue` under the hood.
//
Mailbox mailbox1 = node.mailbox()
    .name("echo-mailbox-1")
    .build();

for (int count = 0; count < 3; count++) {
  // Mailbox.receive() is a blocking operation
  Message message = mailbox.receive();

  ErlangTerm body = message.getBody();
  mailbox1.send("another-node", "another-mailbox", tuple(
    mailbox1.getPid(), body.getUnsafe(1)
  ));
}


// Mailbox #2
// ----------
// Set your own `java.util.concurrent.BlockingQueue` instance.
// `java.util.concurrent.SynchronousQueue` instance in that case.
//
Mailbox mailbox3 = node.mailbox()
    .name("echo-mailbox-2")
    .queue(new SynchronousQueue<>())
    .build();

while (true) {
  Message message = mailbox3.receive();

  ErlangTerm body = message.getBody();
  mailbox3.send("another-node", "another-mailbox", tuple(
    mailbox3.getPid(), body.getUnsafe(1)
  ));
}

```
