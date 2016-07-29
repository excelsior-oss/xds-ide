package com.excelsior.xds.parser.commons;

import org.eclipse.core.filesystem.IFileStore;

import com.excelsior.xds.core.text.TextPosition;
import com.excelsior.xds.core.todotask.TodoTask;

public class ParserEventListenerAdapter implements IParserEventListener {

	@Override
	public void taskTag(IFileStore file, TextPosition position, int endOffset,
			TodoTask task, String message) {
	}

	@Override
	public void endFileParsing(IFileStore file) {
	}

	@Override
	public void warning(IFileStore file, CharSequence chars, TextPosition position,
			int length, String message, Object... arguments) {
	}

	@Override
	public void error(IFileStore file, CharSequence chars, TextPosition position,
			int length, String message, Object... arguments) {
	}

	@Override
	public void logInternalError(IFileStore file, String message, Throwable exception) {
	}

	@Override
	public void logInternalError(IFileStore file, String message) {
	}

	@Override
	public void logInternalError(IFileStore file, Throwable exception) {
	}
}
