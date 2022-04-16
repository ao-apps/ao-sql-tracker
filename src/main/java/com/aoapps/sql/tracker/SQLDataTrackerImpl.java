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
import com.aoapps.sql.wrapper.SQLDataWrapperImpl;
import com.aoapps.sql.wrapper.SQLInputWrapperImpl;
import com.aoapps.sql.wrapper.SQLOutputWrapperImpl;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Tracks a {@link SQLData} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public class SQLDataTrackerImpl extends SQLDataWrapperImpl implements SQLDataTracker {

	private static final Logger logger = Logger.getLogger(SQLDataTrackerImpl.class.getName());

	public SQLDataTrackerImpl(ConnectionTrackerImpl connectionTracker, SQLData wrapped) {
		super(connectionTracker, wrapped);
	}

	private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addOnClose(Runnable onCloseHandler) {
		onCloseHandlers.add(onCloseHandler);
	}

	private final Map<SQLInput, SQLInputTrackerImpl> trackedSQLInputs = synchronizedMap(new IdentityHashMap<>());
	private final Map<SQLOutput, SQLOutputTrackerImpl> trackedSQLOutputs = synchronizedMap(new IdentityHashMap<>());

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	public final Map<SQLInput, SQLInputTrackerImpl> getTrackedSQLInputs() {
		return trackedSQLInputs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	public final Map<SQLOutput, SQLOutputTrackerImpl> getTrackedSQLOutputs() {
		return trackedSQLOutputs;
	}

	@Override
	protected SQLInputWrapperImpl wrapSQLInput(SQLInput sqlInput) {
		return ConnectionTrackerImpl.getIfAbsent(
			trackedSQLInputs, sqlInput,
			() -> (SQLInputTrackerImpl)super.wrapSQLInput(sqlInput),
			SQLInputTrackerImpl::getWrapped
		);
	}

	@Override
	protected SQLOutputWrapperImpl wrapSQLOutput(SQLOutput sqlOutput) {
		return ConnectionTrackerImpl.getIfAbsent(
			trackedSQLOutputs, sqlOutput,
			() -> (SQLOutputTrackerImpl)super.wrapSQLOutput(sqlOutput),
			SQLOutputTrackerImpl::getWrapped
		);
	}

	/**
	 * @see  SQLInputTrackerImpl#close()
	 * @see  SQLOutputTrackerImpl#close()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch", "unchecked"})
	public void close() throws SQLException {
		Throwable t0 = ConnectionTrackerImpl.clearRunAndCatch(onCloseHandlers);
		// Close tracked objects
		t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0, logger, SQLDataTrackerImpl.class, "close()", "trackedSQLInputs", trackedSQLInputs);
		t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0, logger, SQLDataTrackerImpl.class, "close()", "trackedSQLOutputs", trackedSQLOutputs);
		try {
			super.close();
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}
}
