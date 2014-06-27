package org.z.global.zk;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class TotemLogger implements Logger {

	private static Logger logger;
	private static String name;

	@SuppressWarnings("static-access")
	public TotemLogger(Logger logger, String name) {
		this.logger = logger;
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public void trace(String msg) {
		logger.trace(msg);
	}

	@Override
	public void trace(String format, Object arg) {
		logger.trace(format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		logger.trace(format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object[] argArray) {
		logger.trace(format, argArray);
	}

	@Override
	public void trace(String msg, Throwable t) {
		logger.trace(msg + stringifyException(t));
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return false;
	}

	@Override
	public void trace(Marker marker, String msg) {
		logger.trace(marker, msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		logger.trace(marker, format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		logger.trace(marker, format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object[] argArray) {
		logger.trace(marker, format, argArray);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		logger.trace(marker, msg, stringifyException(t));
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public void debug(String msg) {
		logger.debug(msg);
	}

	@Override
	public void debug(String format, Object arg) {
		logger.debug(format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		logger.debug(format, arg1, arg2);

	}

	@Override
	public void debug(String format, Object[] argArray) {
		logger.debug(format, argArray);
	}

	@Override
	public void debug(String msg, Throwable t) {
		logger.debug(msg, t);

	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return false;
	}

	@Override
	public void debug(Marker marker, String msg) {
		logger.debug(marker, msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		logger.debug(marker, format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		logger.debug(marker, format, arg1, arg2);

	}

	@Override
	public void debug(Marker marker, String format, Object[] argArray) {
		logger.debug(marker, format, argArray);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		logger.debug(marker, msg, t);

	}

	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public void info(String msg) {
		logger.info(msg);
	}

	@Override
	public void info(String format, Object arg) {
		logger.info(format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		logger.info(format, arg1, arg2);

	}

	@Override
	public void info(String format, Object[] argArray) {
		logger.info(format, argArray);

	}

	@Override
	public void info(String msg, Throwable t) {
		logger.info(msg + stringifyException(t));
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return false;
	}

	@Override
	public void info(Marker marker, String msg) {
		logger.info(marker, msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		logger.info(marker, format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		logger.info(marker, format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object[] argArray) {
		logger.info(marker, format, argArray);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		logger.info(marker, msg, stringifyException(t));

	}

	@Override
	public boolean isWarnEnabled() {
		return false;
	}

	@Override
	public void warn(String msg) {
		logger.warn(msg);
	}

	@Override
	public void warn(String format, Object arg) {
		logger.warn(format, arg);
	}

	@Override
	public void warn(String format, Object[] argArray) {
		logger.warn(format, argArray);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		logger.warn(format, arg1, arg2);
	}

	@Override
	public void warn(String msg, Throwable t) {
		logger.warn(msg + stringifyException(t));
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return false;
	}

	@Override
	public void warn(Marker marker, String msg) {
		logger.warn(marker, msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		logger.warn(marker, format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		logger.warn(marker, format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object[] argArray) {
		logger.warn(marker, format, argArray);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		logger.warn(marker, msg, stringifyException(t));
	}

	@Override
	public boolean isErrorEnabled() {
		return false;
	}

	@Override
	public void error(String msg) {
		logger.error(msg);
	}

	@Override
	public void error(String format, Object arg) {
		logger.error(format, arg);

	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		logger.error(format, arg1, arg2);
	}

	@Override
	public void error(String format, Object[] argArray) {
		logger.error(format, argArray);
	}

	@Override
	public void error(String msg, Throwable t) {
		logger.error(msg + stringifyException(t));
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return false;
	}

	@Override
	public void error(Marker marker, String msg) {
		logger.error(marker, msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		logger.error(marker, format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		logger.error(marker, format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object[] argArray) {
		logger.error(marker, format, argArray);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		logger.error(marker, msg, stringifyException(t));
	}

	public static String stringifyException(Throwable e) {
		StringWriter stm = new StringWriter();
		PrintWriter wrt = new PrintWriter(stm);
		e.printStackTrace(wrt);
		wrt.close();
		return stm.toString();
	}


}
