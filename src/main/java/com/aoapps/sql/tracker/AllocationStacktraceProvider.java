/*
 * ao-sql-tracker - Tracks JDBC API for unclosed or unfreed objects.
 * Copyright (C) 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-sql-tracker.
 *
 * ao-sql-tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-sql-tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-sql-tracker.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.sql.tracker;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks the stack trace at allocation time when logging at {@link #ALLOCATION_STACKTRACE_LOG_LEVEL} or higher.
 *
 * @author  AO Industries, Inc.
 */
interface AllocationStacktraceProvider {

  /**
   * The logging level that enables allocation stack trace logging.
   */
  Level ALLOCATION_STACKTRACE_LOG_LEVEL = Level.FINER;

  /**
   * Gets the stacktrace at allocation time or {@code null} if not available due to logging level.
   */
  Exception getAllocationStacktrace();

  /**
   * Gets the logger to be used for allocation information.
   */
  Logger getAllocationLogger();
}
