package edu.tamu.scholars.middleware.shared;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Iterator;

import org.springframework.lang.Nullable;

public interface Cursor<T> extends Iterator<T>, Closeable {

	enum State {
		READY, OPEN, FINISHED, CLOSED
	}

	@Nullable
	Serializable getCursorMark();

	Cursor<T> open();

	long getPosition();

	boolean isOpen();

	boolean isClosed();

}
