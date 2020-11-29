/*
 * ao-sql-tracker - Tracks JDBC API for unclosed or unfreed objects.
 * Copyright (C) 2020  AO Industries, Inc.
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
package com.aoindustries.sql.tracker;

import com.aoindustries.lang.Throwables;
import com.aoindustries.sql.wrapper.ResultSetWrapperImpl;
import com.aoindustries.sql.wrapper.StatementWrapperImpl;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks a {@link ResultSet} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public class ResultSetTrackerImpl extends ResultSetWrapperImpl implements ResultSetTracker {

	public ResultSetTrackerImpl(ConnectionTrackerImpl connectionTracker, StatementWrapperImpl stmtTracker, ResultSet wrapped) {
		super(connectionTracker, stmtTracker, wrapped);
	}

	private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addOnClose(Runnable onCloseHandler) {
		onCloseHandlers.add(onCloseHandler);
	}

	private final Map<Array,ArrayTrackerImpl> trackedArrays = synchronizedMap(new IdentityHashMap<>());
	private final Map<Blob,BlobTrackerImpl> trackedBlobs = synchronizedMap(new IdentityHashMap<>());
	private final Map<Clob,ClobTrackerImpl> trackedClobs = synchronizedMap(new IdentityHashMap<>());
	private final Map<InputStream,InputStreamTracker> trackedInputStreams = synchronizedMap(new IdentityHashMap<>());
	private final Map<NClob,NClobTrackerImpl> trackedNClobs = synchronizedMap(new IdentityHashMap<>());
	private final Map<Reader,ReaderTracker> trackedReaders = synchronizedMap(new IdentityHashMap<>());
	private final Map<Ref,RefTrackerImpl> trackedRefs = synchronizedMap(new IdentityHashMap<>());
	private final Map<ResultSetMetaData,ResultSetMetaDataTrackerImpl> trackedResultSetMetaDatas = synchronizedMap(new IdentityHashMap<>());
	private final Map<RowId,RowIdTrackerImpl> trackedRowIds = synchronizedMap(new IdentityHashMap<>());
	private final Map<SQLXML,SQLXMLTrackerImpl> trackedSQLXMLs = synchronizedMap(new IdentityHashMap<>());

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Array,ArrayTrackerImpl> getTrackedArrays() {
		return trackedArrays;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Blob,BlobTrackerImpl> getTrackedBlobs() {
		return trackedBlobs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Clob,ClobTrackerImpl> getTrackedClobs() {
		return trackedClobs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<InputStream,InputStreamTracker> getTrackedInputStreams() {
		return trackedInputStreams;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<NClob,NClobTrackerImpl> getTrackedNClobs() {
		return trackedNClobs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Reader,ReaderTracker> getTrackedReaders() {
		return trackedReaders;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Ref,RefTrackerImpl> getTrackedRefs() {
		return trackedRefs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<ResultSetMetaData,ResultSetMetaDataTrackerImpl> getTrackedResultSetMetaDatas() {
		return trackedResultSetMetaDatas;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<RowId,RowIdTrackerImpl> getTrackedRowIds() {
		return trackedRowIds;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<SQLXML,SQLXMLTrackerImpl> getTrackedSQLXMLs() {
		return trackedSQLXMLs;
	}

	@Override
	protected ArrayTrackerImpl wrapArray(Array array) {
		return ConnectionTrackerImpl.getIfAbsent(trackedArrays, array,
			() -> (ArrayTrackerImpl)super.wrapArray(array),
			ArrayTrackerImpl::getWrapped
		);
	}

	@Override
	protected BlobTrackerImpl wrapBlob(Blob blob) {
		return ConnectionTrackerImpl.getIfAbsent(trackedBlobs, blob,
			() -> (BlobTrackerImpl)super.wrapBlob(blob),
			BlobTrackerImpl::getWrapped
		);
	}

	@Override
	protected ClobTrackerImpl wrapClob(Clob clob) {
		return ConnectionTrackerImpl.getIfAbsent(trackedClobs, clob,
			() -> (ClobTrackerImpl)super.wrapClob(clob),
			ClobTrackerImpl::getWrapped
		);
	}

	@Override
	protected InputStreamTracker wrapInputStream(InputStream in) {
		return ConnectionTrackerImpl.getIfAbsent(
			trackedInputStreams, in,
			() -> (InputStreamTracker)super.wrapInputStream(in),
			InputStreamTracker::getWrapped
		);
	}

	@Override
	protected NClobTrackerImpl wrapNClob(NClob nclob) {
		return ConnectionTrackerImpl.getIfAbsent(trackedNClobs, nclob,
			() -> (NClobTrackerImpl)super.wrapNClob(nclob),
			NClobTrackerImpl::getWrapped
		);
	}

	@Override
	protected ReaderTracker wrapReader(Reader in) {
		return ConnectionTrackerImpl.getIfAbsent(
			trackedReaders, in,
			() -> (ReaderTracker)super.wrapReader(in),
			ReaderTracker::getWrapped
		);
	}

	@Override
	protected RefTrackerImpl wrapRef(Ref ref) {
		return ConnectionTrackerImpl.getIfAbsent(trackedRefs, ref,
			() -> (RefTrackerImpl)super.wrapRef(ref),
			RefTrackerImpl::getWrapped
		);
	}

	@Override
	protected ResultSetMetaDataTrackerImpl wrapResultSetMetaData(ResultSetMetaData metaData) {
		return ConnectionTrackerImpl.getIfAbsent(trackedResultSetMetaDatas, metaData,
			() -> (ResultSetMetaDataTrackerImpl)super.wrapResultSetMetaData(metaData),
			ResultSetMetaDataTrackerImpl::getWrapped
		);
	}

	@Override
	protected RowIdTrackerImpl wrapRowId(RowId rowId) {
		return ConnectionTrackerImpl.getIfAbsent(trackedRowIds, rowId,
			() -> (RowIdTrackerImpl)super.wrapRowId(rowId),
			RowIdTrackerImpl::getWrapped
		);
	}

	@Override
	protected SQLXMLTrackerImpl wrapSQLXML(SQLXML sqlXml) {
		return ConnectionTrackerImpl.getIfAbsent(trackedSQLXMLs, sqlXml,
			() -> (SQLXMLTrackerImpl)super.wrapSQLXML(sqlXml),
			SQLXMLTrackerImpl::getWrapped
		);
	}

	/**
	 * @see  ArrayTrackerImpl#close()
	 * @see  BlobTrackerImpl#close()
	 * @see  ClobTrackerImpl#close()
	 * @see  InputStreamTracker#close()
	 * @see  NClobTrackerImpl#close()
	 * @see  ReaderTracker#close()
	 * @see  RefTrackerImpl#close()
	 * @see  ResultSetMetaDataTrackerImpl#close()
	 * @see  RowIdTrackerImpl#close()
	 * @see  SQLXMLTrackerImpl#close()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch", "unchecked"})
	public void close() throws SQLException {
		Throwable t0 = ConnectionTrackerImpl.clearRunAndCatch(onCloseHandlers);
		// Close tracked objects
		t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0,
			trackedArrays,
			trackedBlobs,
			trackedClobs,
			trackedInputStreams,
			trackedNClobs,
			trackedReaders,
			trackedRefs,
			trackedResultSetMetaDatas,
			trackedRowIds,
			trackedSQLXMLs
		);
		try {
			super.close();
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}
}
