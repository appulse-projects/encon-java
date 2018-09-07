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

package io.appulse.encon.terms;

import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import io.appulse.encon.terms.type.ErlangAtom;
import io.appulse.encon.terms.type.ErlangBinary;
import io.appulse.encon.terms.type.ErlangBitString;
import io.appulse.encon.terms.type.ErlangExternalFunction;
import io.appulse.encon.terms.type.ErlangFloat;
import io.appulse.encon.terms.type.ErlangFunction;
import io.appulse.encon.terms.type.ErlangInteger;
import io.appulse.encon.terms.type.ErlangList;
import io.appulse.encon.terms.type.ErlangMap;
import io.appulse.encon.terms.type.ErlangNil;
import io.appulse.encon.terms.type.ErlangPid;
import io.appulse.encon.terms.type.ErlangPort;
import io.appulse.encon.terms.type.ErlangReference;
import io.appulse.encon.terms.type.ErlangString;
import io.appulse.encon.terms.type.ErlangTuple;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Enumeration of all available term types.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public enum TermType {

  /**
   * The tag used for atom cache.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>1</th>
   * </tr>
   * <tr>
   * <td>82</td>
   * <td>AtomCacheReferenceIndex</td>
   * </tr>
   * </table>
   * <p>
   * Refers to the atom with AtomCacheReferenceIndex in the
   * <a href="http://erlang.org/doc/apps/erts/erl_ext_dist.html#distribution_header">distribution header</a>.
   */
  ATOM_CACHE_REF(82),

  /**
   * The tag used for small integer.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>1</th>
   * </tr>
   * <tr>
   * <td>97</td>
   * <td>Int</td>
   * </tr>
   * </table>
   * <p>
   * Unsigned 8-bit integer.
   */
  SMALL_INTEGER(97, ErlangInteger.class),

  /**
   * The tag used for integer.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * </tr>
   * <tr>
   * <td>98</td>
   * <td>Int</td>
   * </tr>
   * </table>
   * <p>
   * Signed 32-bit integer in big-endian format.
   */
  INTEGER(98, ErlangInteger.class),

  /**
   * The tag used for small big numbers.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>1</th>
   * <th>n</th>
   * </tr>
   * <tr>
   * <td>110</td>
   * <td>n</td>
   * <td>Sign</td>
   * <td>d(0) ... d(n-1)</td>
   * </tr>
   * </table>
   * <p>
   * Bignums are stored in unary form with a Sign byte, that is, 0 if the binum is positive and 1 if it is negative.
   * The digits are stored with the least significant byte stored first. To calculate the integer, the following
   * formula can be used:
   * <p>
   * {@code B = 256}
   * <p>
   * {@code (d0*B^0 + d1*B^1 + d2*B^2 + ... d(N-1)*B^(n-1))}
   */
  SMALL_BIG(110, ErlangInteger.class),

  /**
   * The tag used for large big numbers.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>1</th>
   * <th>n</th>
   * </tr>
   * <tr>
   * <td>111</td>
   * <td>n</td>
   * <td>Sign</td>
   * <td>d(0) ... d(n-1)</td>
   * </tr>
   * </table>
   * <p>
   * Same as {@link #SMALL_BIG} except that the length field is an unsigned 4 byte integer.
   * <p>
   * @see TermType#SMALL_BIG
   */
  LARGE_BIG(111, ErlangInteger.class),

  /**
   * The tag used for float.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>31</th>
   * </tr>
   * <tr>
   * <td>99</td>
   * <td>Float string</td>
   * </tr>
   * </table>
   * <p>
   * A float is stored in string format. The format used in sprintf to format the float is "%.20e" (there are more
   * bytes allocated than necessary). To unpack the float, use sscanf with format "%lf".
   * <p>
   * This term is used in minor version 0 of the external format; it has been superseded by {@link #NEW_FLOAT}.
   * <p>
   * @see TermType#NEW_FLOAT
   */
  FLOAT(99, ErlangFloat.class),

  /**
   * The tag used for new float.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>8</th>
   * </tr>
   * <tr>
   * <td>70</td>
   * <td>IEEE float</td>
   * </tr>
   * </table>
   * <p>
   * A float is stored as 8 bytes in big-endian IEEE format.
   * <p>
   * This term is used in minor version 1 of the external format.
   */
  NEW_FLOAT(70, ErlangFloat.class),

  /**
   * The tag used for reference.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>N</th>
   * <th>4</th>
   * <th>1</th>
   * </tr>
   * <tr>
   * <td>101</td>
   * <td>Node</td>
   * <td>ID</td>
   * <td>Creation</td>
   * </tr>
   * </table>
   * <p>
   * Encodes a reference object (an object generated with {@code erlang:make_ref/0}). The Node term is an encoded
   * atom, that is, {@link #ATOM_UTF8}, {@link #SMALL_ATOM_UTF8}, or {@link #ATOM_CACHE_REF}. The ID field contains a
   * big-endian unsigned integer, but <b>is to be regarded as uninterpreted data</b>, as this field is node-specific.
   * Creation is a byte containing a node serial number, which makes it possible to separate old (crashed) nodes from
   * a new one.
   * <p>
   * In ID, only 18 bits are significant; the rest are to be 0. In Creation, only two bits are significant; the rest
   * are to be 0. See {@link #NEW_REFERENCE}.
   * <p>
   * @see TermType#NEW_REFERENCE
   */
  REFERENCE(101, ErlangReference.class),

  /**
   * The tag used for new style reference.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>2</th>
   * <th>N</th>
   * <th>1</th>
   * <th>N'</th>
   * </tr>
   * <tr>
   * <td>114</td>
   * <td>Len</td>
   * <td>Node</td>
   * <td>Creation</td>
   * <td>ID ...</td>
   * </tr>
   * </table>
   * <p>
   * Node and Creation are as in {@link #REFERENCE}.
   * <p>
   * ID contains a sequence of big-endian unsigned integers (4 bytes each, so N' is a multiple of 4), but is to be
   * regarded as uninterpreted data.
   * <p>
   * N' = 4 * Len.
   * <p>
   * In the first word (4 bytes) of ID, only 18 bits are significant, the rest are to be 0. In Creation, only two bits
   * are significant, the rest are to be 0.
   * <p>
   * NEW_REFERENCE_EXT was introduced with distribution version 4. In version 4, N' is to be at most 12.
   * <p>
   * @see TermType#REFERENCE
   */
  NEW_REFERENCE(114, ErlangReference.class),

  /**
   * The tag used for new style reference. The same as {@link TermType#NEW_REFERENCE}, but newer (use bigger
   * {@code creation} length).
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>2</th>
   * <th>N</th>
   * <th>4</th>
   * <th>N'</th>
   * </tr>
   * <tr>
   * <td>90</td>
   * <td>Len</td>
   * <td>Node</td>
   * <td>Creation</td>
   * <td>ID ...</td>
   * </tr>
   * </table>
   * <p>
   * @see TermType#NEW_REFERENCE
   */
  NEWER_REFERENCE(90, ErlangReference.class),

  /**
   * The tag used for port.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>N</th>
   * <th>4</th>
   * <th>1</th>
   * </tr>
   * <tr>
   * <td>102</td>
   * <td>Node</td>
   * <td>ID</td>
   * <td>Creation</td>
   * </tr>
   * </table>
   * <p>
   * Encodes a port object (obtained from {@code erlang:open_port/2}). The ID is a node-specific identifier for a
   * local port. Port operations are not allowed across node boundaries. The Creation works just like in
   * {@link #REFERENCE}.
   * <p>
   * @see TermType#REFERENCE
   */
  PORT(102, ErlangPort.class),

  /**
   * The tag used for port. The same as {@link TermType#PORT}, but newer (use bigger {@code creation} length).
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>N</th>
   * <th>4</th>
   * <th>4</th>
   * </tr>
   * <tr>
   * <td>89</td>
   * <td>Node</td>
   * <td>ID</td>
   * <td>Creation</td>
   * </tr>
   * </table>
   * <p>
   * @see TermType#PORT
   */
  NEW_PORT(89, ErlangPort.class),

  /**
   * The tag used for pid.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>N</th>
   * <th>4</th>
   * <th>4</th>
   * <th>1</th>
   * </tr>
   * <tr>
   * <td>103</td>
   * <td>Node</td>
   * <td>ID</td>
   * <td>Serial</td>
   * <td>Creation</td>
   * </tr>
   * </table>
   * <p>
   * Encodes a process identifier object (obtained from {@code erlang:spawn/3} or friends). The ID and Creation fields
   * works just like in {@link #REFERENCE}, while the Serial field is used to improve safety. In ID, only 15 bits are
   * significant; the rest are to be 0.
   * <p>
   * @see TermType#REFERENCE
   */
  PID(103, ErlangPid.class),

  /**
   * The tag used for pid. The same as {@link TermType#PID}, but newer (use bigger {@code creation} length).
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>N</th>
   * <th>4</th>
   * <th>4</th>
   * <th>4</th>
   * </tr>
   * <tr>
   * <td>88</td>
   * <td>Node</td>
   * <td>ID</td>
   * <td>Serial</td>
   * <td>Creation</td>
   * </tr>
   * </table>
   * <p>
   * @see TermType#PID
   */
  NEW_PID(88, ErlangPid.class),

  /**
   * The tag used for small tuple.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>N</th>
   * </tr>
   * <tr>
   * <td>104</td>
   * <td>Arity</td>
   * <td>Elements</td>
   * </tr>
   * </table>
   * <p>
   * Encodes a tuple. The arity field is an unsigned byte that determines how many elements that follows in section
   * Elements.
   */
  SMALL_TUPLE(104, ErlangTuple.class),

  /**
   * The tag used for large tuple.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>N</th>
   * </tr>
   * <tr>
   * <td>105</td>
   * <td>Arity</td>
   * <td>Elements</td>
   * </tr>
   * </table>
   * <p>
   * Same as {@link #SMALL_TUPLE} except that Arity is an unsigned 4 byte integer in big-endian format.
   * <p>
   * @see TermType#SMALL_TUPLE
   */
  LARGE_TUPLE(105, ErlangTuple.class),

  /**
   * The tag used for map.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>N</th>
   * </tr>
   * <tr>
   * <td>116</td>
   * <td>Arity</td>
   * <td>Pairs</td>
   * </tr>
   * </table>
   * <p>
   * Encodes a map. The <b>Arity</b> field is an unsigned 4 byte integer in big-endian format that determines the
   * number of key-value pairs in the map. Key and value pairs (Ki => Vi) are encoded in section Pairs in the
   * following order: K1, V1, K2, V2,..., Kn, Vn.
   * <p>
   * Duplicate keys are <b>not allowed</b> within the same map.
   */
  MAP(116, ErlangMap.class),

  /**
   * The tag used for nil.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * </tr>
   * <tr>
   * <td>106</td>
   * </tr>
   * </table>
   * <p>
   * The representation for an empty list, that is, the Erlang syntax {@code []}.
   */
  NIL(106, ErlangNil.class),

  /**
   * The tag used for string.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>2</th>
   * <th>Len</th>
   * </tr>
   * <tr>
   * <td>107</td>
   * <td>Length</td>
   * <td>Characters</td>
   * </tr>
   * </table>
   * <p>
   * String does <b>not</b> have a corresponding Erlang representation, but is an optimization for sending lists of
   * bytes (integer in the range 0-255) more efficiently over the distribution. As field Length is an unsigned 2 byte
   * integer (big-endian), implementations must ensure that lists longer than 65535 elements are encoded as
   * {@link #LIST}.
   * <p>
   * @see TermType#LIST
   */
  STRING(107, ErlangString.class),

  /**
   * The tag used for list.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>&nbsp;</th>
   * <th>&nbsp;</th>
   * </tr>
   * <tr>
   * <td>108</td>
   * <td>Length</td>
   * <td>Elements</td>
   * <td>Tail</td>
   * </tr>
   * </table>
   * <p>
   * Length is the number of elements that follows in section Elements. Tail is the final tail of the list; it is
   * {@link #NIL} for a proper list, but can be any type if the list is improper (for example, [a|b])
   * <p>
   * @see TermType#NIL
   */
  LIST(108, ErlangList.class),

  /**
   * The tag used for binary data.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>Len</th>
   * </tr>
   * <tr>
   * <td>109</td>
   * <td>Len</td>
   * <td>Data</td>
   * </tr>
   * </table>
   * <p>
   * Binaries are generated with bit syntax expression or with
   * {@code erlang:list_to_binary/1}, {@code erlang:term_to_binary/1}, or as input from binary ports. The Len length
   * field is an unsigned 4 byte integer (big-endian).
   */
  BINARY(109, ErlangBinary.class),

  /**
   * This is the encoding of internal functions.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>N1</th>
   * <th>N2</th>
   * <th>N3</th>
   * <th>N4</th>
   * <th>N5</th>
   * </tr>
   * <tr>
   * <td>117</td>
   * <td>NumFree</td>
   * <td>Pid</td>
   * <td>Module</td>
   * <td>Index</td>
   * <td>Uniq</td>
   * <td>FreeVars</td>
   * </tr>
   * </table>
   * <p>
   * Description:
   * <p>
   * <table border="1">
   * <tr>
   * <th>Block</th>
   * <th>Description</th>
   * </tr>
   * <tr>
   * <td>Pid</td>
   * <td>
   * A process identifier as in {@link #PID}. Represents the process in which the fun was created
   * </td>
   * </tr>
   * <tr>
   * <td>Module</td>
   * <td>
   * Encoded as an atom, using {@link #ATOM_UTF8}, {@link #SMALL_ATOM_UTF8}, or
   * {@link #ATOM_CACHE_REF}. This is the module that the fun is implemented in
   * </td>
   * </tr>
   * <tr>
   * <td>Index</td>
   * <td>
   * An integer encoded using {@link #SMALL_INTEGER} or {@link #INTEGER}.
   * It is typically a small index into the module's fun table
   * </td>
   * </tr>
   * <tr>
   * <td>Uniq</td>
   * <td>
   * An integer encoded using {@link #SMALL_INTEGER} or {@link #INTEGER}.
   * Uniq is the hash value of the parse for the function
   * </td>
   * </tr>
   * <tr>
   * <td>FreeVars</td>
   * <td>
   * NumFree number of terms, each one encoded according to its type
   * </td>
   * </tr>
   * </table>
   */
  FUNCTION(117, ErlangFunction.class),

  /**
   * This is the new encoding of internal funs: fun F/A and fun(Arg1,..) -> ... end.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>1</th>
   * <th>16</th>
   * <th>4</th>
   * <th>4</th>
   * <th>N1</th>
   * <th>N2</th>
   * <th>N3</th>
   * <th>N4</th>
   * <th>N5</th>
   * </tr>
   * <tr>
   * <td>112</td>
   * <td>Size</td>
   * <td>Arity</td>
   * <td>Uniq</td>
   * <td>Index</td>
   * <td>NumFree</td>
   * <td>Module</td>
   * <td>OldIndex</td>
   * <td>OldUniq</td>
   * <td>Pid</td>
   * <td>FreeVars</td>
   * </tr>
   * </table>
   * <p>
   * Description:
   * <p>
   * <table border="1">
   * <tr>
   * <th>Block</th><th>Description</th>
   * </tr>
   * <tr>
   * <td>Size</td>
   * <td>
   * The total number of bytes, including field Size
   * </td>
   * </tr>
   * <tr>
   * <td>Arity</td>
   * <td>
   * The arity of the function implementing the fun
   * </td>
   * </tr>
   * <tr>
   * <td>Uniq</td>
   * <td>
   * The 16 bytes MD5 of the significant parts of the Beam file
   * </td>
   * </tr>
   * <tr>
   * <td>Index</td>
   * <td>
   * An index number. Each fun within a module has an unique index. Index is stored in big-endian byte order
   * </td>
   * </tr>
   * <tr>
   * <td>NumFree</td>
   * <td>
   * The number of free variables
   * </td>
   * </tr>
   * <tr>
   * <td>Module</td>
   * <td>
   * Encoded as an atom, using {@link #ATOM_UTF8}, {@link #SMALL_ATOM_UTF8}, or {@link #ATOM_CACHE_REF}.
   * Is the module that the fun is implemented in
   * </td>
   * </tr>
   * <tr>
   * <td>OldIndex</td>
   * <td>
   * An integer encoded using {@link #SMALL_INTEGER} or {@link #INTEGER}.
   * Is typically a small index into the module's fun table
   * </td>
   * </tr>
   * <tr>
   * <td>OldUniq</td>
   * <td>
   * An integer encoded using {@link #SMALL_INTEGER} or {@link #INTEGER}.
   * Uniq is the hash value of the parse tree for the fun
   * </td>
   * </tr>
   * <tr>
   * <td>Pid</td>
   * <td>
   * A process identifier as in {@link #PID}. Represents the process in which the fun was created
   * </td>
   * </tr>
   * <tr>
   * <td>FreeVars</td>
   * <td>
   * NumFree number of terms, each one encoded according to its type
   * </td>
   * </tr>
   * </table>
   */
  NEW_FUNCTION(112, ErlangFunction.class),

  /**
   * The tag used for external functions.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>N1</th>
   * <th>N2</th>
   * <th>N3</th>
   * </tr>
   * <tr>
   * <td>113</td>
   * <td>Module</td>
   * <td>Function</td>
   * <td>Arity</td>
   * </tr>
   * </table>
   * <p>
   * This term is the encoding for external functions: {@code fun M:F/A}.
   * <p>
   * Module and Function are atoms (encoded using {@link #ATOM_UTF8}, {@link #SMALL_ATOM_UTF8}, or
   * {@link #ATOM_CACHE_REF}).
   * <p>
   * Arity is an integer encoded using {@link #SMALL_INTEGER}.
   * <p>
   * @see TermType#ATOM_UTF8
   * <p>
   * @see TermType#SMALL_ATOM_UTF8
   * <p>
   * @see TermType#ATOM_CACHE_REF
   * <p>
   * @see TermType#SMALL_INTEGER
   */
  EXTERNAL_FUNCTION(113, ErlangExternalFunction.class),

  /**
   * The tag used for bitstring.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>4</th>
   * <th>1</th>
   * <th>Len</th>
   * </tr>
   * <tr>
   * <td>77</td>
   * <td>Len</td>
   * <td>Bits</td>
   * <td>Data</td>
   * </tr>
   * </table>
   * <p>
   * This term represents a bitstring whose length in bits does not have to be a multiple of 8. The Len field is an
   * unsigned 4 byte integer (big-endian). The Bits field is the number of bits (1-8) that are used in the last byte
   * in the data field, counting from the most significant bit to the least significant.
   */
  BIT_BINNARY(77, ErlangBitString.class),

  /**
   * The tag used for UTF8 encoded atom.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>2</th>
   * <th>Len</th>
   * </tr>
   * <tr>
   * <td>118</td>
   * <td>Len</td>
   * <td>AtomName</td>
   * </tr>
   * </table>
   * <p>
   * An atom is stored with a 2 byte unsigned length in big-endian order, followed by Len bytes containing the
   * AtomName encoded in UTF-8.
   * <p>
   * For more information on encoding of atoms, see the
   * <a href="http://erlang.org/doc/apps/erts/erl_ext_dist.html#utf8_atoms">note on UTF-8 encoded atoms</a> in the
   * beginning of this section.
   */
  ATOM_UTF8(118, ErlangAtom.class),

  /**
   * The tag used for small UTF8 encoded atom.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>1</th>
   * <th>Len</th>
   * </tr>
   * <tr>
   * <td>119</td>
   * <td>Len</td>
   * <td>AtomName</td>
   * </tr>
   * </table>
   * <p>
   * An atom is stored with a 1 byte unsigned length, followed by Len bytes containing the AtomName encoded in UTF-8.
   * Longer atoms encoded in UTF-8 can be represented using {@link #ATOM_UTF8}.
   * <p>
   * For more information on encoding of atoms, see the
   * <a href="http://erlang.org/doc/apps/erts/erl_ext_dist.html#utf8_atoms">note on UTF-8 encoded atoms</a> in the
   * beginning of this section.
   */
  SMALL_ATOM_UTF8(119, ErlangAtom.class),

  /**
   * The tag used for atom.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>2</th>
   * <th>Len</th>
   * </tr>
   * <tr>
   * <td>100</td>
   * <td>Len</td>
   * <td>AtomName</td>
   * </tr>
   * </table>
   * <p>
   * An atom is stored with a 2 byte unsigned length in big-endian order, followed by Len numbers of 8-bit Latin-1
   * characters that forms the AtomName. The maximum allowed value for Len is 255.
   * <p>
   * @deprecated use UTF-8 atoms instead
   */
  ATOM(100, ErlangAtom.class),

  /**
   * The tag used for small atom.
   * <p>
   * Structure:
   * <p>
   * <table border="1">
   * <tr>
   * <th>1</th>
   * <th>1</th>
   * <th>Len</th>
   * </tr>
   * <tr>
   * <td>115</td>
   * <td>Len</td>
   * <td>AtomName</td>
   * </tr>
   * </table>
   * <p>
   * An atom is stored with a 1 byte unsigned length, followed by Len numbers of 8-bit Latin-1 characters that forms
   * the AtomName.
   * <p>
   * {@link #SMALL_ATOM} was introduced in ERTS 5.7.2 and require an exchange of distribution flag
   * <a href="http://erlang.org/doc/apps/erts/erl_dist_protocol.html#dflags">DFLAG_SMALL_ATOM_TAGS</a> in the
   * <a href="http://erlang.org/doc/apps/erts/erl_dist_protocol.html#distribution_handshake">distribution
   * handshake</a>.
   * <p>
   * @deprecated use UTF-8 atoms instead
   */
  SMALL_ATOM(115, ErlangAtom.class),

  /**
   * The tag used for compressed term.
   */
  COMPRESSED(80),

  /**
   * Unknown term type.
   */
  UNKNOWN(-1);

  private static final Map<Integer, TermType> VALUES;

  static {
    VALUES = Stream.of(TermType.values())
        .collect(toMap(it -> (int) it.getCode(), Function.identity()));
  }

  @Getter
  byte code;

  @Getter
  Class<? extends ErlangTerm> type;

  TermType (int code) {
    this(code, null);
  }

  TermType (int code, Class<? extends ErlangTerm> type) {
    this.code = (byte) code;
    this.type = type;
  }

  /**
   * Parsing term type from {@code byte} number.
   *
   * @param code {@code byte} representation of type
   *
   * @return parsed {@link TermType} instance
   */
  public static TermType of (int code) {
    return VALUES.getOrDefault(code, UNKNOWN);
  }
}
