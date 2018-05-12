package com.hit.memoryunits;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import com.hit.algorithm.IAlgoCache;
import com.hit.util.MMULogger;

public class MemoryManagementUnit
{
	private RAM ram;
	private IAlgoCache<Long,Long> iAlgoCache;

	/*
	 * Constructor that gets the selected cache algorithm and requested ram capacity, and creates an RAM class member instance
	 */
	public MemoryManagementUnit(int ramCapacity, IAlgoCache<Long, Long> iAlgoCache)
	{
		ram = new RAM(ramCapacity);
		this.iAlgoCache = iAlgoCache;
	}
	
	/*
	 * Returns a list containing all the requested pages. Pages are replaced according to the cache algorithm.
	 * Pages for writing will be obtained in RAM memory, while pages for reading will be given directly from HardDisk memory.
	 * Page Fault and Page Replacement will be performed the compatible HardDisk methods, and recorded into a log file through MMULogger. 
	 * Parameters:
	 * pageIds - a list of page IDS of which we want to get
	 * writePages - a Boolean list stating for each page ID if it represent a page to write to(true value) or read (false value).
	 * Returns:
	 * A list of page objects, containing all the requested pages from the [pageIds] 
	 */
	public List<Page<byte[]>> getPages(List<Long> pageIds, List<Boolean> writePages)
    {
		List<Long>	missingPagesIdsForWriting = new ArrayList<>();
		List<Page<byte[]>> pagesToBeReturned = new ArrayList<>();
		Page<byte[]> pageToInsert = null;		
		Page<byte[]> pageToReplace = null;
		
		// Get the pages that are present in RAM and cache algorithm memory (same pages) or null if they're missing 
		List<Long>	pageIdsOrNullIfMissing = this.iAlgoCache.getElement(pageIds);
		
		int pageIndex = 0;
		
		for (Long pageIdOrNull : pageIdsOrNullIfMissing)
		{
			// If a page is present in RAM for reading/writing, get it directly from RAM memory
			if (pageIdOrNull != null) 
			{
				pagesToBeReturned.add(ram.getPage(pageIds.get(pageIndex)));
			}
			// Otherwise this is page not present in RAM
				// If it is for writing - add it to the [missingPagesIdsForWriting] list for further action
				// Otherwise it is for reading - perform a page fault and return it directly from HardDisk, don't save it in RAM - which is allocated for writing pages 
			else					
			{
				if (writePages.get(pageIndex))	
				{
					missingPagesIdsForWriting.add(pageIds.get(pageIndex));
				}
				else								
				{
					// Document via logger the Page Fault command, with the compatible page IDs
					MMULogger.getInstance().write(MessageFormat.format("PF:{0}{1}", pageIds.get(pageIndex),
																		System.lineSeparator()), Level.INFO);
					pagesToBeReturned.add(HardDisk.getInstance().pageFault(pageIds.get(pageIndex)));
				}																
			}
			
			pageIndex++;
		}
		
		// Put the missing page-IDs inside the cache algorithm memory, and it will return a list of pages that need to be
		// replaced according to that algorithm
		List<Long> pageIDsToReplace = iAlgoCache.putElement(missingPagesIdsForWriting, missingPagesIdsForWriting);
		int IterationIndex = 0;
		
		//For each page ID of a page that needs to be replaced
		for (Long pageIDToReplace : pageIDsToReplace)
		{
			// If RAM is not full - a page fault will be performed
			// Otherwise - a page replacement will be performed
			if (pageIDToReplace == null)
			{
				pageToInsert = HardDisk.getInstance().pageFault(missingPagesIdsForWriting.get(IterationIndex));
				// Document via logger the Page Fault command, with the compatible page IDs
				MMULogger.getInstance().write(MessageFormat.format("PF:{0}{1}", pageToInsert.getPageId(),
																	System.lineSeparator()), Level.INFO);

			}
			else
			{
				// Get the page object of the page ID that needs to be replaced
				pageToReplace = ram.getPage(pageIDToReplace);
				// Perform a Page Replacement, so that [pageToReplace] goes back into HardDisk memory, and the page to be insert will be entered instead
				pageToInsert = HardDisk.getInstance().pageReplacement(pageToReplace, missingPagesIdsForWriting.get(IterationIndex));
				// Document via logger the Page Replacement command, with the compatible page IDs
				MMULogger.getInstance().write(MessageFormat.format("PR:MTH {0} MTR {1}{2}", pageIDToReplace,
																				pageToInsert.getPageId(),
																				System.lineSeparator()), Level.INFO);
				// After the replaced page was placed in HardDisk memory, we remove it from RAM, to make room for the inserted page
				ram.removePage(pageToReplace);
			}
			
			ram.addPage(pageToInsert);
			pagesToBeReturned.add(pageToInsert);
			IterationIndex++;	
		}
		
		return pagesToBeReturned;
    }
	
	public RAM getRam()
	{
		return ram;
	}
}