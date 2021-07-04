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
 * along with ao-sql-tracker.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoapps.sql.tracker;

import com.aoapps.sql.wrapper.ParameterMetaDataWrapper;
import java.sql.ParameterMetaData;
import java.sql.SQLException;

/**
 * Tracks a {@link ParameterMetaData} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public interface ParameterMetaDataTracker extends ParameterMetaDataWrapper, OnCloseHandler {

	/**
	 * Calls onClose handlers, closes all tracked objects, then calls {@code super.close()}.
	 *
	 * @see  #addOnClose(java.lang.Runnable)
	 */
	@Override
	void close() throws SQLException;
}