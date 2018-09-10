# Overview

This project contains the general-purpose data-binding functionality.

- [Add dependency](#add-dependency)
- [Start the Node](#start-the-node)
- [Create a Mailbox](#create-a-mailbox)
- [Connect the nodes](#connect-the-nodes)
- [Send message from Java](#send-message-from-java)
- [Receive message in Java](#receive-message-in-java)

## Add dependency

Adding encon's dependency to your `JVM` app:

**Maven**:

```xml
<dependencies>
  ...
  <dependency>
    <groupId>io.appulse.encon</groupId>
    <artifactId>encon</artifactId>
    <version>1.6.3</version>
  </dependency>
  ...
</dependencies>
```

**Gradle**:

```groovy
compile 'io.appulse.encon:encon:1.6.3'
```

## Start the Node

```java
import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.NodeConfig;


// Creating node's config.
// For more details - see encon-config project
NodeConfig config = NodeConfig.builder()
    .shortName(true) // true - for local nodes, false (default) - for remote accessable
    .cookie("secret")
    .build();

// Creates, registers in EPMD and starts a new Erlang node
Node node = Nodes.singleNode("echo-node", config);
```

## Create a Mailbox

```java
import io.appulse.encon.mailbox.Mailbox;


Mailbox mailbox = node.mailbox()
    .name("popa") // this is an optional
    .build();
```

## Connect the nodes

> **NOTICE:** You can initiate the connection from either Erlang/Elixir or Java side automatically by sending to a remote name using tuple format {Name, Node} or sending to a remote pid (if you have it).

You can initiate the connection between nodes from Erlang side. To do this, on Erlang side you can use `net_adm:ping`.

```erlang
(erlang@localhost)1> net_adm:ping('java@localhost').
pong
(erlang@localhost)2>
```

Also you could send a message to `{Name, Node}`, where `Node` is an atom like `'java@localhost'`, and `Name` is a pid or some registered name, which exists on the Java side.

```erlang
(erlang@localhost)1> {my_process, 'java@localhost'} ! hello.
hello
(erlang@localhost)2>
```

If the process exists on Java side, its mailbox will receive your message. You can check it from your code using one of the `Mailbox` methods: `receive()` or `receive(timeout, timeUnit)`, which can wait eternal or fixed amount of time for another message.

## Send message from Java

You can send messages using the family of `send` methods, which can deliver messages locally or remotely:

- send(**ErlangPid**, **ErlangTerm**) - sends a payload to a remote or local **PID**;
- send(**String**, **ErlangTerm**) - sends a message to local mailbox of this `Node` by its name;
- send(**String**, **String**, **ErlangTerm**) - sends a payload to a remote/local node and mailbox by its names.

To try this, open an Erlang shell and register shell with the name `'shell'`:

```erlang
(erlang@localhost)1> erlang:register(shell, self()).
true
(erlang@localhost)2>
```

Now we can try and send the message from Java (node connection will be established automatically):

```java
import static io.appulse.encon.terms.Erlang.atom;


mailbox.send("erlang@localhost", "shell", atom("hello"));
```

```erlang
(erlang@localhost) 1> flush().
Shell got hello
ok
(erlang@localhost) 2>
```

## Receive message in Java

```java
import io.appulse.encon.Node;
import io.appulse.encon.Nodes;
import io.appulse.encon.config.NodeConfig;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.mailbox.Mailbox;


public class Main {

  public static void main (String[] args) {
    NodeConfig config = NodeConfig.builder()
        .shortName(true)
        .build();

    Node node = Nodes.singleNode("java@localhost", config);

    Mailbox mailbox = node.mailbox()
        .name("my_process")
        .build();

    Message message = mailbox.receive();
    System.out.println("Incoming message: " + message.getBody().asText());
  }
}
```

Start erlang node and send a message:

```bash
$> erl -sname erlang@localhost
...
(erlang@localhost)1> {my_process, 'java@localhost'} ! hello.
```
