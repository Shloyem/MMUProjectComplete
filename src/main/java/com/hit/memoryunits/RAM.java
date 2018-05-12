package com.hit.memoryunits;
import java.util.*;

public class RAM
{
	private Integer initialCapacity;
	private static final Integer DEFAULT_CAPACITY = 5;
	private Map<Long,Page<byte[]>> ramMemory;
	
	/*
	 * Constructor that gets the RAM capacity as an argument.
	 */
	public RAM(int capacity)
	{
		if (capacity > 0)
		{
			initialCapacity = capacity;
		}
		else
		{
			initialCapacity = DEFAULT_CAPACITY;
		}
		
		ramMemory = new HashMap<>(initialCapacity);
	}
	
	public Integer getInitialCapacity()
	{
		return initialCapacity;
	}
	
	public void setInitialCapacity(Integer initialCapacity)
	{
		this.initialCapacity = initialCapacity;
	}
	
	public void addPage(Page<byte[]> addPage)
	{
		boolean isFullCapacity = ramMemory.size() >= initialCapacity;
		
		if (!isFullCapacity)
		{
			ramMemory.put(addPage.getPageId(), addPage);
		}
	}
	
	public void addPages(Page<byte[]>[] addPages)
	{
		for(Page<byte[]> page : addPages)
		{
			if (ramMemory.size() < initialCapacity)
			{
				ramMemory.put(page.getPageId(), page);
			}
		}
	}
	
	public Page<byte[]> getPage(Long pageId)
	{
		return ramMemory.get(pageId);
	}
	
	public Map<Long,Page<byte[]>> getPages()
	{
		return ramMemory;
	}
	
	@SuppressWarnings("unchecked")
	public Page<byte[]>[] getPages(Long[] pageIds)
	{
		List<Page<byte[]>> returnedPages = new ArrayList<Page<byte[]>>(pageIds.length);
		
		for(Long pageId : pageIds)
		{
			if (pageId != null)
			{
				if(ramMemory.containsKey(pageId))
				{
					returnedPages.add(ramMemory.get(pageId));		
				}
			}
		}
		
		return (Page<byte[]>[])returnedPages.toArray();
	}
	
	public void removePage(Page<byte[]> removedPage)
	{
		ramMemory.remove(removedPage.getPageId());
	}
	
	public void removePages(Page<byte[]>[] removedPages)
	{
		for(Page<byte[]> page: removedPages)
		{
			ramMemory.remove(page.getPageId());
		}
	}
	
	public void setPages(Map<Long,Page<byte[]>> pages)
	{
			ramMemory.clear();
			ramMemory.putAll(pages);
	}
	
	public boolean CheckIfRamIsFull()
	{
		return initialCapacity == ramMemory.size();
	}
	
}
