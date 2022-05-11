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

import static java.util.Collections.synchronizedMap;

import com.aoapps.lang.Throwables;
import com.aoapps.sql.wrapper.ArrayWrapperImpl;
import com.aoapps.sql.wrapper.StatementWrapperImpl;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks an {@link Array} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public class ArrayTrackerImpl extends ArrayWrapperImpl implements ArrayTracker {

  public ArrayTrackerImpl(ConnectionTrackerImpl connectionTracker, StatementWrapperImpl stmtWrapper, Array wrapped) {
    super(connectionTracker, stmtWrapper, wrapped);
  }

  private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void addOnClose(Runnable onCloseHandler) {
    onCloseHandlers.add(onCloseHandler);
  }

  private final Map<ResultSet, ResultSetTrackerImpl> trackedResultSets = synchronizedMap(new IdentityHashMap<>());

  @Override
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
  public final Map<ResultSet, ResultSetTrackerImpl> getTrackedResultSets() {
    return trackedResultSets;
  }

  @Override
  protected ResultSetTrackerImpl wrapResultSet(ResultSet results) throws SQLException {
    return ConnectionTrackerImpl.getIfAbsent(trackedResultSets, results,
        () -> (ResultSetTrackerImpl) super.wrapResultSet(results),
        ResultSetTrackerImpl::getWrapped
    );
  }

  /**
   * {@inheritDoc}
   *
   * @see  ResultSetTrackerImpl#close()
   */
  @Override
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public void free() throws SQLException {
    Throwable t0 = ConnectionTrackerImpl.clearRunAndCatch(onCloseHandlers);
    // Close tracked objects
    t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0, trackedResultSets);
    try {
      super.free();
    } catch (Throwable t) {
      t0 = Throwables.addSuppressed(t0, t);
    }
    if (t0 != null) {
      throw Throwables.wrap(t0, SQLException.class, SQLException::new);
    }
  }
}
