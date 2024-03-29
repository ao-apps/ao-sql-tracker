/*
 * ao-sql-tracker - Tracks JDBC API for unclosed or unfreed objects.
 * Copyright (C) 2020, 2021, 2022  AO Industries, Inc.
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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.util.Map;

/**
 * Gets all the {@linkplain PreparedStatement prepared statements} that have not yet been closed.
 * This only contains {@link PreparedStatement}, please see other method for {@link CallableStatement}.
 *
 * @author  AO Industries, Inc.
 */
public interface TrackedPreparedStatements {

  /**
   * Gets all the {@linkplain PreparedStatement prepared statements} that have not yet been closed.
   * This only contains {@link PreparedStatement}, please see other method for {@link CallableStatement}.
   *
   * @return  The mapping from wrapped prepared statement to tracker without any defensive copy.
   *
   * @see  TrackedCallableStatements#getTrackedCallableStatements()
   *
   * @see  PreparedStatement#close()
   */
  Map<PreparedStatement, ? extends PreparedStatementTracker> getTrackedPreparedStatements();
}
