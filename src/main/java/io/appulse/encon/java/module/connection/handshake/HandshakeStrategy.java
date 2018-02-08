
package io.appulse.encon.java.module.connection.handshake;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import io.appulse.encon.java.Node;
import io.appulse.encon.java.RemoteNode;
import io.appulse.encon.java.protocol.exception.NodeConnectionException;
import io.appulse.encon.java.module.connection.handshake.exception.HandshakeException;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeAcknowledgeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeMessage;
import io.appulse.encon.java.module.connection.handshake.message.ChallengeReplyMessage;
import io.appulse.encon.java.module.connection.handshake.message.Message;
import io.appulse.encon.java.module.connection.handshake.message.NameMessage;
import io.appulse.encon.java.module.connection.handshake.message.StatusMessage;
import io.appulse.utils.SocketUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HandshakeStrategy {

  public Socket handshake (@NonNull Node from, @NonNull RemoteNode to) {
    val socket = createSocket(to);

    val highestCommonVerion = HandshakeUtils.findHighestCommonVerion(from, to);
    val nameMessage = NameMessage.builder()
        .fullNodeName(from.getDescriptor().getFullName())
        .distribution(highestCommonVerion)
        .flags(from.getMeta().getFlags())
        .build();

    send(socket, nameMessage);

    val status = receive(socket, StatusMessage.class).getStatus();
    switch (status) {
    case OK:
      break;
    case OK_SIMULTANEOUS:
    case NOK:
    case NOT_ALLOWED:
    case ALIVE:
    default:
      throw new RuntimeException();
    }

    val remoteChallenge = receive(socket, ChallengeMessage.class).getChallenge();
    val digest = HandshakeUtils.generateDigest(remoteChallenge, from.getCookie());
    val myChallenge = ThreadLocalRandom.current().nextInt();

    val challengeReplyMessage = ChallengeReplyMessage.builder()
        .challenge(myChallenge)
        .digest(digest)
        .build();

    send(socket, challengeReplyMessage);

    val peerDigest = receive(socket, ChallengeAcknowledgeMessage.class).getDigest();
    val myDigest = HandshakeUtils.generateDigest(myChallenge, from.getCookie());
    if (!Arrays.equals(peerDigest, myDigest)) {
      throw new HandshakeException();
    }

    log.debug("Sucessfull handshake from {} to {}",
              from.getDescriptor().getFullName(),
              to.getDescriptor().getFullName());

    return socket;
  }

  @SneakyThrows
  private void send (@NonNull Socket socket, @NonNull Message message) {
    log.debug("Sending: {}", message);
    val outputStream = socket.getOutputStream();
    outputStream.write(message.toBytes());
    outputStream.flush();
  }

  @SneakyThrows
  public <T extends Message> T receive (@NonNull Socket socket, @NonNull Class<T> type) {
    val length = SocketUtils.readBytes(socket, 2).getShort();
    val buffer = SocketUtils.readBytes(socket, length);
    val response = Message.parse(buffer, type);
    log.debug("Received: {}", response);
    return response;
  }

  private Socket createSocket (@NonNull RemoteNode to) {
    val address = to.getDescriptor().getAddress();
    val port = to.getPort();

    val socket = new Socket();
    val timeout = (int) SECONDS.toMillis(5);
    try {
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
      socket.setSoTimeout(timeout);
      socket.connect(new InetSocketAddress(address, port), timeout);
    } catch (IOException ex) {
      val message = String.format("Couldn't connect to Node (%s:%d), maybe it is down",
                                  address.toString(), port);
      log.error(message, ex);
      throw new NodeConnectionException(message, ex);
    }
    return socket;
  }
}
