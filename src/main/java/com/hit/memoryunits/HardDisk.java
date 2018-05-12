package com.hit.memoryunits;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import com.hit.util.MMULogger;

public class HardDisk
{
	private static final Integer DEFAULT_DISK_SIZE = 100;
	private static final String DEFAULT_FILE_NAME = "resources/HardDisk.txt";
	private static volatile HardDisk hardDiskInstance = null; 	// Lazy Initialization (If required only then)
	private static Map<Long,Page<byte[]>> hardDiskMemory;
	
	/*
	 * Constructor which uses the default HardDisk file to read its input and place it in inner class [hardDiskMemory] map
	 * It is private as part of Singleton design pattern - only one instance of this class
	 */
	private HardDisk()
	{
		File hdFile = new File(DEFAULT_FILE_NAME);
		
		// If the file exists read it's content to this class mapping, Otherwise create it
		if (hdFile.exists())	
		{
			readFromHdFile();
		}
		else
		{
			CreateHardDisk();
		}
	}
	
	/*
	 * Singleton design pattern implementation - creating one and only instance of this class
	 */
	public static HardDisk getInstance()
	{
		// Thread Safe
		if (hardDiskInstance == null)
		{
			synchronized(HardDisk.class)
			{
				if (hardDiskInstance == null)
				{
					hardDiskInstance = new HardDisk();
				}
			}
		}
		
		return hardDiskInstance;
	}
	
	/*
	 * Creating a new HardDisk file (which will be used for by this class)
	 */
	public static void CreateHardDisk()
	{
		File hdFile = new File(DEFAULT_FILE_NAME);
			
		if (hdFile.exists())
		{
			hdFile.delete();
		}
		
		try
		{
			hdFile.createNewFile();
		} catch (Exception e)
		{
			MMULogger.getInstance().write(MessageFormat.format("Error ! failed to create Hard Disk file correctly:{0}{1}", e.getMessage(),
																											System.lineSeparator()), Level.SEVERE);
		}
		hardDiskMemory = new HashMap<Long,Page<byte[]>>(DEFAULT_DISK_SIZE);
		
		//Before receiving the actual pages content from the configuration file, fill it with '0'
		for (Long i =  0l; i < DEFAULT_DISK_SIZE; i++)
		{
			byte[] initalPageContent = {'0'};
			hardDiskMemory.put(i, new Page<byte[]>(initalPageContent, i));
		}
		
		ObjectOutputStream output = null;
		
		try
		{
			output = new ObjectOutputStream(new FileOutputStream(DEFAULT_FILE_NAME));
			output.writeObject(hardDiskMemory);
			output.flush();
		}catch (Exception e)
		{
			MMULogger.getInstance().write(MessageFormat.format("Error ! failed writing to the newly created Hard Disk file:{0}{1}", e.getMessage(),
																											System.lineSeparator()), Level.SEVERE);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				output.close();
			} catch (Exception e)
			{
				MMULogger.getInstance().write(MessageFormat.format("Error ! could not close output stream correctly:{0}{1}", e.getMessage(),
																												System.lineSeparator()), Level.SEVERE);
			}
		}
	}

	/*
	 * If a Page Fault (asking for a page while the memory is not full) is performed, get it from the current HardDisk file
	 * Parameters: 
	 * pageId - ID of the requested page
	 * Returns:
	 * Page object of the request page [pageId] ID
	 */
	public Page<byte[]> pageFault(Long pageId)
	{
		readFromHdFile();
		
		return hardDiskMemory.get(pageId);
	}
	
	/*
	 * If a Page Replacement (inserting a page to RAM memory when its full, and pulling one page out to make room for it) is performed
	 * put back the page being pulled out in the HD memory with it's updated content, and return the requested page for the RAM
	 * Parameters: 
	 * moveToHdPage - The page which has been replaced and got out of the RAM memory
	 * moveToRamId - The page requested to be inserted to RAM memory
	 * Returns:
	 * Page object of requested [moveToRamId] ID
	 */
	public Page<byte[]> pageReplacement(Page<byte[]> moveToHdPage, java.lang.Long moveToRamId)
	{
		readFromHdFile();
		hardDiskMemory.put(moveToHdPage.getPageId(), moveToHdPage);
		Page<byte[]> pageToRam = hardDiskMemory.get(moveToRamId);
		writeToHdFile();
		
		return pageToRam;
	}

	/*
	 * Read HD file content, and fill the class member memory map with it, using streams
	 */
	@SuppressWarnings("unchecked")
	private void readFromHdFile()
	{
		FileInputStream		fileInputStream = null;
		ObjectInputStream	objInputStream = null;
		
		try
		{
			fileInputStream = 	new FileInputStream(DEFAULT_FILE_NAME);
			objInputStream	=	new ObjectInputStream(fileInputStream);
			
			hardDiskMemory = (HashMap<Long, Page<byte[]>>) objInputStream.readObject();
		}
		catch (ClassNotFoundException | IOException e)
		{
			MMULogger.getInstance().write(MessageFormat.format("Error ! Failed reading from Hard Disk file correctly:{0}{1}", e.getMessage(),
																											System.lineSeparator()), Level.SEVERE);
		}
		finally
		{
			try
			{
				fileInputStream.close();
				objInputStream.close();
			} catch (IOException e)
			{
				MMULogger.getInstance().write(MessageFormat.format("Error ! Failed closing input streams:{0}{1}", e.getMessage(),
																												System.lineSeparator()), Level.SEVERE);
			}
		}
	}
	
	/*
	 * Write the current class memory map content to the HD file, using streams
	 */
	private void writeToHdFile()
	{
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objOutputStream = null;
		
		try
		{
			fileOutputStream = new FileOutputStream(DEFAULT_FILE_NAME);
			objOutputStream = new ObjectOutputStream(fileOutputStream);
			objOutputStream.writeObject(hardDiskMemory);
			objOutputStream.flush();
		}
		catch (IOException e)
		{
			MMULogger.getInstance().write(MessageFormat.format("Error ! failed writing to Hard Disk file :{0}{1}", e.getMessage(),
																													System.lineSeparator()), Level.SEVERE);
		}
		finally
		{
			try
			{
				fileOutputStream.close();
				objOutputStream.close();
			} catch (IOException e)
			{
				MMULogger.getInstance().write(MessageFormat.format("Error ! failed closing file and object output streams:{0}{1}", e.getMessage(),
																																	System.lineSeparator()), Level.SEVERE);
			}
		}
	}
}

