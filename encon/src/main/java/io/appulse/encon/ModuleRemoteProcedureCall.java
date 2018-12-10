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

package io.appulse.encon;

import static io.appulse.encon.terms.Erlang.NIL;
import static io.appulse.encon.terms.Erlang.atom;
import static io.appulse.encon.terms.Erlang.list;
import static io.appulse.encon.terms.Erlang.tuple;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.appulse.encon.common.NodeDescriptor;
import io.appulse.encon.common.RemoteNode;
import io.appulse.encon.connection.regular.Message;
import io.appulse.encon.exception.NoSuchRemoteNodeException;
import io.appulse.encon.mailbox.Mailbox;
import io.appulse.encon.terms.ErlangTerm;
import io.appulse.encon.terms.type.ErlangAtom;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * Module with set of methods for remote procedure calls.
 *
 * @since 2.0.0
 * @author Artem Labazin
 */
@AllArgsConstructor(access = PACKAGE)
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ModuleRemoteProcedureCall {

  Node node;

  /**
   * Send an RPC request to the remote Erlang node. This convenience function
   * creates the following message and sends it to 'rex' on the remote node:
   *
   * <pre>
   * { selfPid, { :call, :module_name, :function_name, [arguments], :user } }
   * </pre>
   *
   * <p>
   * Note that this method has unpredicatble results if the remote node is not
   * an Erlang node.
   * </p>
   *
   * <p>
   * The response will be send back to this node in format:
   * <pre>
   * { :rex, response_body }
   * </pre>
   * </p>
   *
   * @param remoteNodeName remote node name
   *
   * @param module the name of the Erlang module containing the function to be called.
   *
   * @param function the name of the function to call.
   *
   * @param args a list of Erlang terms, to be used as arguments to the function.
   *
   * @return response holder, instance of {@link RpcResponse}.
   */
  public RpcResponse call (@NonNull String remoteNodeName, @NonNull String module, @NonNull String function, ErlangTerm ...args) {
    return call(remoteNodeName, atom(module), atom(function), args);
  }

  /**
   * Send an RPC request to the remote Erlang node. This convenience function
   * creates the following message and sends it to 'rex' on the remote node:
   *
   * <pre>
   * { selfPid, { :call, :module_name, :function_name, [arguments], :user } }
   * </pre>
   *
   * <p>
   * Note that this method has unpredicatble results if the remote node is not
   * an Erlang node.
   * </p>
   *
   * <p>
   * The response will be send back to this node in format:
   * <pre>
   * { :rex, response_body }
   * </pre>
   * </p>
   *
   * @param descriptor remote node descriptor
   *
   * @param module the name of the Erlang module containing the function to be called.
   *
   * @param function the name of the function to call.
   *
   * @param args a list of Erlang terms, to be used as arguments to the function.
   *
   * @return response holder, instance of {@link RpcResponse}.
   */
  public RpcResponse call (@NonNull NodeDescriptor descriptor, @NonNull String module, @NonNull String function, ErlangTerm ...args) {
    return call(descriptor, atom(module), atom(function), args);
  }

  /**
   * Send an RPC request to the remote Erlang node. This convenience function
   * creates the following message and sends it to 'rex' on the remote node:
   *
   * <pre>
   * { selfPid, { :call, :module_name, :function_name, [arguments], :user } }
   * </pre>
   *
   * <p>
   * Note that this method has unpredicatble results if the remote node is not
   * an Erlang node.
   * </p>
   *
   * <p>
   * The response will be send back to this node in format:
   * <pre>
   * { :rex, response_body }
   * </pre>
   * </p>
   *
   * @param remote remote node descriptor
   *
   * @param module the name of the Erlang module containing the function to be called.
   *
   * @param function the name of the function to call.
   *
   * @param args a list of Erlang terms, to be used as arguments to the function.
   *
   * @return response holder, instance of {@link RpcResponse}.
   */
  public RpcResponse call (@NonNull RemoteNode remote, @NonNull String module, @NonNull String function, ErlangTerm ...args) {
    return call(remote, atom(module), atom(function), args);
  }

  /**
   * Send an RPC request to the remote Erlang node. This convenience function
   * creates the following message and sends it to 'rex' on the remote node:
   *
   * <pre>
   * { selfPid, { :call, :module_name, :function_name, [arguments], :user } }
   * </pre>
   *
   * <p>
   * Note that this method has unpredicatble results if the remote node is not
   * an Erlang node.
   * </p>
   *
   * <p>
   * The response will be send back to this node in format:
   * <pre>
   * { :rex, response_body }
   * </pre>
   * </p>
   *
   * @param remoteNodeName remote node name
   *
   * @param module the atom of the Erlang module containing the function to be called.
   *
   * @param function the atom of the function to call.
   *
   * @param args a list of Erlang terms, to be used as arguments to the function.
   *
   * @return response holder, instance of {@link RpcResponse}.
   */
  public RpcResponse call (@NonNull String remoteNodeName, @NonNull ErlangAtom module, @NonNull ErlangAtom function, ErlangTerm ...args) {
    val descriptor = NodeDescriptor.from(remoteNodeName);
    return call(descriptor, module, function, args);
  }

  /**
   * Send an RPC request to the remote Erlang node. This convenience function
   * creates the following message and sends it to 'rex' on the remote node:
   *
   * <pre>
   * { selfPid, { :call, :module_name, :function_name, [arguments], :user } }
   * </pre>
   *
   * <p>
   * Note that this method has unpredicatble results if the remote node is not
   * an Erlang node.
   * </p>
   *
   * <p>
   * The response will be send back to this node in format:
   * <pre>
   * { :rex, response_body }
   * </pre>
   * </p>
   *
   * @param descriptor remote node descriptor
   *
   * @param module the atom of the Erlang module containing the function to be called.
   *
   * @param function the atom of the function to call.
   *
   * @param args a list of Erlang terms, to be used as arguments to the function.
   *
   * @return response holder, instance of {@link RpcResponse}.
   */
  public RpcResponse call (@NonNull NodeDescriptor descriptor,
                           @NonNull ErlangAtom module,
                           @NonNull ErlangAtom function,
                           ErlangTerm ...args
  ) {
    RemoteNode remote = node.lookup(descriptor);
    if (remote == null) {
      throw new NoSuchRemoteNodeException(descriptor);
    }
    return call(remote, module, function, args);
  }

  /**
   * Send an RPC request to the remote Erlang node. This convenience function
   * creates the following message and sends it to 'rex' on the remote node:
   *
   * <pre>
   * { selfPid, { :call, :module_name, :function_name, [arguments], :user } }
   * </pre>
   *
   * <p>
   * Note that this method has unpredicatble results if the remote node is not
   * an Erlang node.
   * </p>
   *
   * <p>
   * The response will be send back to this node in format:
   * <pre>
   * { :rex, response_body }
   * </pre>
   * </p>
   *
   * @param remote remote node descriptor
   *
   * @param module the atom of the Erlang module containing the function to be called.
   *
   * @param function the atom of the function to call.
   *
   * @param args a list of Erlang terms, to be used as arguments to the function.
   *
   * @return response holder, instance of {@link RpcResponse}.
   */
  @SuppressWarnings("PMD.AccessorClassGeneration")
  public RpcResponse call (@NonNull RemoteNode remote, @NonNull ErlangAtom module, @NonNull ErlangAtom function, ErlangTerm ...args) {
    ErlangTerm argumentsList;
    if (args == null || args.length == 0) {
      argumentsList = NIL;
    } else if (args.length == 1 && args[0].isList()) {
      argumentsList = args[0];
    } else {
      argumentsList = list(args);
    }

    Mailbox mailbox = node.mailbox().build();
    mailbox.send(remote, "rex", tuple(
        mailbox.getPid(),
        tuple(
            atom("call"),
            module,
            function,
            argumentsList,
            atom("user")
        )
    ));
    return new RpcResponse(mailbox);
  }

  /**
   * Remote procedure call response holder.
   */
  @FieldDefaults(level = PRIVATE)
  public static final class RpcResponse {

    Mailbox mailbox;

    final AtomicReference<ErlangTerm> response = new AtomicReference<>(null);

    private RpcResponse (Mailbox mailbox) {
      this.mailbox = mailbox;
    }

    /**
     * Checks if a response was coming or not.
     *
     * @return {@code true} if this holder has a response, {@code false} otherwise.
     */
    public boolean hasResponse () {
      return response.get() != null || (mailbox != null && mailbox.size() == 0);
    }

    /**
     * Receive an RPC reply from the remote Erlang node in <b>asynchronous</b> manner.
     * This convenience function receives a message from the remote node, and expects it to have
     * the following format:
     *
     * <pre>
     * { :rex, ErlangTerm }
     * </pre>
     *
     * @return the second element of the tuple if the received message is a
     *         two-tuple, otherwise empty. No further error checking is
     *         performed.
     */
    public Optional<ErlangTerm> getAsync () {
      ErlangTerm result = response.get();
      if (result == null) {
        result = getSync(1, NANOSECONDS);
      }
      return ofNullable(result);
    }

    /**
     * Receive an RPC reply from the remote Erlang node in <b>synchronous</b> manner.
     * This convenience function receives a message from the remote node, and expects it to have
     * the following format:
     *
     * <pre>
     * { :rex, ErlangTerm }
     * </pre>
     *
     * @return the second element of the tuple if the received message is a
     *         two-tuple, otherwise null. No further error checking is
     *         performed.
     */
    public ErlangTerm getSync () {
      ErlangTerm result = response.get();
      if (result != null) {
        return result;
      }
      return mailbox.receive()
            .getBody()
            .get(1)
            .map(this::process)
            .orElse(null);
    }

    /**
     * Receive an RPC reply from the remote Erlang node in <b>synchronous</b> manner.
     * This convenience function receives a message from the remote node, and expects it to have
     * the following format:
     *
     * <pre>
     * { :rex, ErlangTerm }
     * </pre>
     *
     * @param timeout how long to wait before giving up, in units of
     *        {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the
     *        {@code timeout} parameter
     *
     * @return the second element of the tuple if the received message is a
     *         two-tuple, otherwise null. No further error checking is
     *         performed. It also could return {@code null} if the specified
     *         waiting time elapses before an element is available
     */
    public ErlangTerm getSync (long timeout, TimeUnit unit) {
      ErlangTerm result = response.get();
      if (result != null) {
        return result;
      }
      Message message = mailbox.receive(timeout, unit);
      if (message == null) {
        return null;
      }
      return message.getBody()
          .get(1)
          .map(this::process)
          .orElse(null);
    }

    @SuppressWarnings("PMD.NullAssignment")
    private ErlangTerm process (ErlangTerm term) {
      if (!response.compareAndSet(null, term)) {
        return null;
      }
      mailbox.close();
      mailbox = null;
      return term;
    }
  }
}
