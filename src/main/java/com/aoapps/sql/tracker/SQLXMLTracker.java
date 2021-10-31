/*
 * ao-sql-tracker - Tracks JDBC API for unclosed or unfreed objects.
 * Copyright (C) 2020, 2021  AO Industries, Inc.
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

import com.aoapps.sql.wrapper.SQLXMLWrapper;
import java.sql.SQLException;
import java.sql.SQLXML;

/**
 * Tracks a {@link SQLXML} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public interface SQLXMLTracker extends SQLXMLWrapper, OnCloseHandler,
	TrackedInputStreams,
	TrackedOutputStreams,
	TrackedReaders,
	TrackedWriters {

	/**
	 * Calls onClose handlers, closes all tracked objects, then calls {@code super.free()}.
	 *
	 * @see  #addOnClose(java.lang.Runnable)
	 */
	@Override
	void free() throws SQLException;
}
