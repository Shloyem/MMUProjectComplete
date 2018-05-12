package com.hit.processes;

import java.util.Arrays;
import java.util.List;

/*
 * This class represents ProcessCycle which is the life cycle of the process
 */
public class ProcessCycle
{
	private List<Long> pages;
	private int sleepMs;
	private List<byte[]> data;
	
	/*
	 * This constructor represents a Process Cycle constructor, which gets the relevant configuration to the process life cycle
	 */
	public ProcessCycle(List<Long> pages, int sleepMs, List<byte[]> data)
	{
		this.pages = pages;
		this.sleepMs = sleepMs;
		this.data = data;
	}

	public List<Long> getPages()
	{
		return pages;
	}

	public void setPages(List<Long> pages)
	{
		this.pages = pages;
	}

	public int getSleepMs()
	{
		return sleepMs;
	}

	public void setSleepMs(int sleepMs)
	{
		this.sleepMs = sleepMs;
	}

	public List<byte[]> getData()
	{
		return data;
	}

	public void setData(List<byte[]> data)
	{
		this.data = data;
	}
	
	@Override	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("This process cycle will sleep for " + sleepMs + "seconds,");
		sb.append("The pages in the process cycle are :");
		
		for (Long page : pages)
			sb.append(page.toString() + " ");
		
		sb.append("The data of the process cycle is :");
		
		for (byte[] dataPart : data)
			sb.append(Arrays.toString(dataPart)+" ");

		return sb.toString();
	}
}
