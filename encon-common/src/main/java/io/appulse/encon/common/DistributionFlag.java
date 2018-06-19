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

package io.appulse.encon.common;

import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Enumeration of all available distribution capability flags.
 *
 * @since 1.0.0
 * @author Artem Labazin
 */
@FieldDefaults(level = PRIVATE, makeFinal = true)
public enum DistributionFlag {

  /**
   * The node is to be published and part of the global namespace.
   */
  PUBLISHED(1),

  /**
   * The node implements an atom cache (obsolete).
   */
  ATOM_CACHE(2),

  /**
   * The node implements extended (3 × 32 bits) references.
   * This is required today. If not present, the connection is refused.
   */
  EXTENDED_REFERENCES(4),

  /**
   * The node implements distributed process monitoring.
   */
  DIST_MONITOR(8),

  /**
   * The node uses separate tag for functions (lambdas) in the distribution protocol.
   */
  FUN_TAGS(0x10),

  /**
   * The node implements distributed named process monitoring.
   * <p>
   * Not used.
   */
  DIST_MONITOR_NAME(0x20),

  /**
   * The (hidden) node implements atom cache (obsolete).
   * <p>
   * Not supported.
   */
  HIDDEN_ATOM_CACHE(0x40),

  /**
   * The node understand new function tags.
   */
  NEW_FUN_TAGS(0x80),

  /**
   * The node can handle extended pids and ports. This is required today.
   * If not present, the connection is refused.
   */
  EXTENDED_PIDS_PORTS(0x100),

  /**
   * Unknown and not supported tag...
   */
  EXPORT_PTR_TAG(0x200),

  /**
   * The node supports bit binaries.
   */
  BIT_BINARIES(0x400),

  /**
   * The node understands new float format.
   */
  NEW_FLOATS(0x800),

  /**
   * The node supports UTF IO.
   */
  UNICODE_IO(0x1000),

  /**
   * The node implements atom cache in distribution header.
   */
  DIST_HDR_ATOM_CACHE(0x2000),

  /**
   * The node understand the SMALL_ATOM_EXT tag.
   */
  SMALL_ATOM_TAGS(0x4000),

  /**
   * The node understand UTF-8 encoded atoms.
   */
  UTF8_ATOMS(0x10000),

  /**
   * The node supports map data types.
   */
  MAP_TAG(0x20000),

  /**
   * The node creation is big integer.
   */
  BIG_CREATION(0x40000);

  @Getter
  int code;

  DistributionFlag (int code) {
    this.code = code;
  }

  /**
   * Converts set of distribution flags to integer number
   * via bitwise inclusive OR operator.
   *
   * @param flags set of distribution flags
   *
   * @return integer representation of flags
   */
  public static int bitwiseOr (DistributionFlag... flags) {
    return Stream.of(flags)
        .map(DistributionFlag::getCode)
        .reduce(0, (left, right) -> left | right);
  }

  /**
   * Converts set of distribution flags to integer number
   * via bitwise inclusive OR operator.
   *
   * @param flags set of distribution flags
   *
   * @return integer representation of flags
   */
  public static int bitwiseOr (Collection<DistributionFlag> flags) {
    return flags.stream()
        .map(DistributionFlag::getCode)
        .reduce(0, (left, right) -> left | right);
  }

  /**
   * Parses integer to set of distribution flags via bitwise AND operation.
   *
   * @param number represents a set of distribution flags
   *
   * @return set of parsed distribution flags
   */
  public static Set<DistributionFlag> parse (int number) {
    return Stream.of(values())
        .filter(it -> (number & it.getCode()) != 0)
        .collect(toSet());
  }
}
