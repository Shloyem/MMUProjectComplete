package com.hit.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/*
 * This class represent a logger, created to visually reflect and document the system operations into a log file
 */
public class MMULogger
{
	public final static String DEFAULT_FILE_NAME = "resources/log.txt";
	private FileHandler handler;
	private static volatile MMULogger MMULoggerInstance = null;
	private static Formatter formatter;
	
	// Private default constructor for creating a handler and setting the handler format.
	private MMULogger()
	{
		formatter = new OnlyMessageFormatter();
		
		setHandler();
	}
	
	// Creating a new log file and file handler and it's format
	private void setHandler()
	{
		try
		{
			File logFile = new File(DEFAULT_FILE_NAME);

			if (logFile.exists())
			{
				logFile.delete();
			}
			
			handler = new FileHandler(DEFAULT_FILE_NAME);
		}
		catch (NullPointerException|IOException|SecurityException e) 
		{
			e.printStackTrace();
		}		

		handler.setFormatter(formatter);
	}
	
	// Method for implementing single tone pattern, which returns an instance of MMULogger class.
	public static MMULogger getInstance()
	{
		// Thread Safe
		if (MMULoggerInstance == null)
		{
			synchronized(MMULogger.class)
			{
				if (MMULoggerInstance == null)
				{
					MMULoggerInstance = new MMULogger();
				}
			}
		}
		
		return MMULoggerInstance;
	}
	
	// This method is used to write a log record with specified level.
	public synchronized void write(String command, Level level)
	{
		if (handler == null)
		{
			setHandler();
		}
		
		handler.publish(new LogRecord(level, command));
	}
	
	// Closing the file handler when finished running the system 
	public void close() 
	{
		if (handler != null) 
		{
			handler.close();
			handler = null;
		}
	}
	
	// Sets up a new handler before running the system
	public void open()
	{
		if (handler == null) 
		{
			setHandler();
		}
	}

	// Nested formatter class for handler.
	public class OnlyMessageFormatter extends Formatter
	{
		public OnlyMessageFormatter() { super(); }

		@Override
		public String format(LogRecord record)
		{
			return record.getMessage();
		}
	}
	
}
