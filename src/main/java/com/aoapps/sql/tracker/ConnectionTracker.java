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

import com.aoapps.sql.wrapper.ConnectionWrapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.Executor;

/**
 * Tracks a {@link Connection} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public interface ConnectionTracker extends ConnectionWrapper, OnCloseHandler,
  TrackedArrays,
  TrackedBlobs,
  TrackedCallableStatements,
  TrackedClobs,
  TrackedDatabaseMetaDatas,
  TrackedInputStreams,
  TrackedNClobs,
  TrackedOutputStreams,
  TrackedParameterMetaDatas,
  TrackedPreparedStatements,
  TrackedReaders,
  TrackedRefs,
  TrackedResultSets,
  TrackedResultSetMetaDatas,
  TrackedRowIds,
  TrackedSQLDatas,
  TrackedSQLInputs,
  TrackedSQLOutputs,
  TrackedSQLXMLs,
  TrackedSavepoints,
  TrackedStatements,
  TrackedStructs,
  TrackedWriters {

  /**
   * Removes tracking of savepoints when auto-commit mode is enabled (which commits any transaction in-progress).
   */
  @Override
  void setAutoCommit(boolean autoCommit) throws SQLException;

  /**
   * Removes tracking of savepoints while committing the transaction.
   */
  @Override
  void commit() throws SQLException;

  /**
   * Removes tracking of savepoints while rolling-back the transaction.
   */
  @Override
  void rollback() throws SQLException;

  /**
   * Calls onClose handlers, closes all tracked objects, rolls-back any transaction in-progress and puts back in
   * auto-commit mode, then calls {@link ConnectionTrackerImpl#doClose()}.
   *
   * @see  #addOnClose(java.lang.Runnable)
   */
  @Override
  void close() throws SQLException;

  /**
   * Removes tracking of all savepoints after the given savepoint, the rolls-back to the given savepoint.
   */
  @Override
  void rollback(Savepoint savepoint) throws SQLException;

  /**
   * Removes tracking while releasing the savepoint.
   */
  @Override
  void releaseSavepoint(Savepoint savepoint) throws SQLException;

  /**
   * Calls onClose handlers, clears all tracking, then calls {@link ConnectionTrackerImpl#doAbort(java.util.concurrent.Executor)}.
   * Trusts the underlying implementation of {@link Connection#abort(java.util.concurrent.Executor)} will free all
   * resources.
   *
   * @see  #addOnClose(java.lang.Runnable)
   */
  @Override
  void abort(Executor executor) throws SQLException;
}
