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
import com.aoapps.sql.wrapper.BlobWrapperImpl;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Tracks a {@link Blob} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public class BlobTrackerImpl extends BlobWrapperImpl implements BlobTracker {

  private static final Logger logger = Logger.getLogger(BlobTrackerImpl.class.getName());

  public BlobTrackerImpl(ConnectionTrackerImpl connectionTracker, Blob wrapped) {
    super(connectionTracker, wrapped);
  }

  private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void addOnClose(Runnable onCloseHandler) {
    onCloseHandlers.add(onCloseHandler);
  }

  private final Map<InputStream, InputStreamTracker> trackedInputStreams = synchronizedMap(new IdentityHashMap<>());
  private final Map<OutputStream, OutputStreamTracker> trackedOutputStreams = synchronizedMap(new IdentityHashMap<>());

  @Override
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
  public final Map<InputStream, InputStreamTracker> getTrackedInputStreams() {
    return trackedInputStreams;
  }

  @Override
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
  public final Map<OutputStream, OutputStreamTracker> getTrackedOutputStreams() {
    return trackedOutputStreams;
  }

  @Override
  protected InputStreamTracker wrapInputStream(InputStream in) {
    return ConnectionTrackerImpl.getIfAbsent(
        trackedInputStreams, in,
        () -> (InputStreamTracker) super.wrapInputStream(in),
        InputStreamTracker::getWrapped
    );
  }

  @Override
  protected OutputStreamTracker wrapOutputStream(OutputStream out) {
    return ConnectionTrackerImpl.getIfAbsent(
        trackedOutputStreams, out,
        () -> (OutputStreamTracker) super.wrapOutputStream(out),
        OutputStreamTracker::getWrapped
    );
  }

  /**
   * @see  InputStreamTracker#close()
   * @see  OutputStreamTracker#close()
   */
  @Override
  @SuppressWarnings({"UseSpecificCatch", "TooBroadCatch", "unchecked"})
  public void free() throws SQLException {
    Throwable t0 = ConnectionTrackerImpl.clearRunAndCatch(onCloseHandlers);
    // Close tracked objects
    t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0, logger, BlobTrackerImpl.class, "free()", "trackedInputStreams", trackedInputStreams);
    t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0, logger, BlobTrackerImpl.class, "free()", "trackedOutputStreams", trackedOutputStreams);
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
