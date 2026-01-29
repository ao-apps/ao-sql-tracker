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

import com.aoapps.lang.Throwables;
import com.aoapps.sql.wrapper.ReaderWrapper;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Tracks a {@link Reader} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public class ReaderTracker extends ReaderWrapper
    implements OnCloseHandler, AllocationStacktraceProvider {

  private static final Logger logger = Logger.getLogger(ReaderTracker.class.getName());

  private final Exception allocationStacktrace;

  /**
   * Creates a new {@link Reader} tracker.
   */
  public ReaderTracker(ConnectionTrackerImpl connectionTracker, Reader wrapped) {
    super(connectionTracker, wrapped);
    if (logger.isLoggable(ALLOCATION_STACKTRACE_LOG_LEVEL)) {
      allocationStacktrace = new Exception("Stack trace at allocation");
    } else {
      allocationStacktrace = null;
    }
  }

  @Override
  public Exception getAllocationStacktrace() {
    return allocationStacktrace;
  }

  @Override
  public Logger getAllocationLogger() {
    return logger;
  }

  private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void addOnClose(Runnable onCloseHandler) {
    onCloseHandlers.add(onCloseHandler);
  }

  /**
   * Calls onClose handlers then {@code super.close()}.
   *
   * @see  ReaderTracker#addOnClose(java.lang.Runnable)
   */
  @Override
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
  public void close() throws IOException {
    Throwable t0 = ConnectionTrackerImpl.clearRunAndCatch(onCloseHandlers);
    try {
      super.close();
    } catch (Throwable t) {
      t0 = Throwables.addSuppressed(t0, t);
    }
    if (t0 != null) {
      throw Throwables.wrap(t0, IOException.class, IOException::new);
    }
  }
}
