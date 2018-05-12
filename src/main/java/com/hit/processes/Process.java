package com.hit.processes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import com.hit.memoryunits.MemoryManagementUnit;
import com.hit.memoryunits.Page;
import com.hit.util.MMULogger;

/*
 * This class simulates a process in the real operation system environment.
 * it should run in separate java thread in order to achieve its own completely independent.
 */
public class Process implements Runnable
{
	private MemoryManagementUnit mmu; 
	private int id;
	private ProcessCycles processCycles;
	
	/*
	 * This constructor represents a process constructor, which gets 3 configure parameters to simulate real process
	 */
	public Process (int id, MemoryManagementUnit mmu, ProcessCycles processCycles)
	{
		this.id = id;
		this.mmu = mmu;
		this.processCycles = processCycles;
	}
	
	public int getID()
	{
		return id;
	}

	public void setID(int id)
	{
		this.id = id;
	}

	public ProcessCycles getProcessCycles()
	{
		return processCycles;
	}

	public void setProcessCycles(ProcessCycles processCycles)
	{
		this.processCycles = processCycles;
	}
	
	/*
	 * This method is executed as simultaneous threads and is their logic method.
	 * Each process contains a unit of process cycles, read from the configuration file, which states which pages to bring using MMU's getPages,
	 * what new data to write on them, and for how many milliseconds this process thread should sleep. 
	 */
	@Override
	public void run()
	{				
		for (ProcessCycle processCycle: processCycles.getProcessCycles())
		{
			List<Boolean> writePages = new ArrayList<Boolean>(processCycle.getData().size());
			Boolean isWritingPage;
			
			// Deciding for each page if it is for writing. If there is data to write - it is for writing.
			for(byte[] pageData : processCycle.getData())
			{
				isWritingPage = (pageData != null) ? true : false;
				writePages.add(isWritingPage);
			}
			
			// Get the pages requested by the settings
			List<Long> pagesNum = processCycle.getPages();
			List<Page<byte[]>> pagesFromMMU = null;
			
			// Synchronize MMU reference to make it thread safe and prevent a race condition situation 
			synchronized (mmu)
			{
				pagesFromMMU = mmu.getPages(pagesNum, writePages);
				int pageIndex = 0;
				
				// For each page
				for(byte[] data : processCycle.getData())
				{
					// Write in the log file that there was a GET Page command with process No, page ID, and data written to it
					MMULogger.getInstance().write(MessageFormat.format("GP:P{0} {1} {2}{3}{3}", this.getID(),
																			pagesFromMMU.get(pageIndex).getPageId(), 
																			Arrays.toString(data),
																			System.lineSeparator()), Level.INFO);
					
					// Update its content if its for writing
					if (writePages.get(pageIndex) && pagesFromMMU.get(pageIndex) != null)
					{
						pagesFromMMU.get(pageIndex).setContent(data);
					}
					
					pageIndex++;
				}
			}
			
			try
			{
				// Thread will sleep for the amount of milliseconds in the settings
				Thread.sleep(processCycle.getSleepMs());
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
