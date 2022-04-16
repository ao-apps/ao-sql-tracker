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
import com.aoapps.sql.wrapper.DriverWrapper;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks {@linkplain Connection connections} obtained from other {@linkplain Driver drivers} for unclosed or unfreed
 * objects.
 *
 * @author  AO Industries, Inc.
 */
public abstract class DriverTracker extends DriverWrapper implements OnCloseHandler {

	private static final Logger logger = Logger.getLogger(DriverTracker.class.getName());

	protected DriverTracker() {
		// Do nothing
	}

	private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addOnClose(Runnable onCloseHandler) {
		onCloseHandlers.add(onCloseHandler);
	}

	private final Map<Connection, ConnectionTrackerImpl> trackedConnections = synchronizedMap(new IdentityHashMap<>());

	/**
	 * Gets all the connections that have not yet been closed.
	 *
	 * @return  The mapping from wrapped connection to tracker without any defensive copy.
	 *
	 * @see  ConnectionTrackerImpl#close()
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	public final Map<Connection, ConnectionTrackerImpl> getTrackedConnections() {
		return trackedConnections;
	}

	@Override
	protected ConnectionTrackerImpl newConnectionWrapper(Connection connection) {
		return ConnectionTrackerImpl.newIfAbsent(trackedConnections, this, connection, ConnectionTrackerImpl::new);
	}

	/**
	 * Calls onClose handlers, closes all tracked objects, then calls {@code super.onDeregister()}.
	 *
	 * @see  #addOnClose(java.lang.Runnable)
	 */
	@Override
	protected void onDeregister() {
		Throwable t0 = ConnectionTrackerImpl.clearRunAndCatch(onCloseHandlers);
		// Close tracked objects
		t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0, trackedConnections);
		try {
			super.onDeregister();
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) {
			Logger l;
			try {
				l = getParentLogger();
			} catch(SQLFeatureNotSupportedException e) {
				l = logger;
			}
			l.log(Level.WARNING, "Errors during deregister closing connections", t0);
		}
	}
}
