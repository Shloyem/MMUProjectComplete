package com.hit.processes;

import java.util.List;

/*
 * This class represents the configuration of all threads, it contains list of ProcessCycles that's associated with each thread
 */
public class RunConfiguration
{
	private List<ProcessCycles> processesCycles;

	public List<ProcessCycles> getProcessCycles()
	{
		return processesCycles;
	}

	public void setProcessCycles(List<ProcessCycles> processCycles)
	{
		this.processesCycles = processCycles;
	}

	public RunConfiguration(List<ProcessCycles> processCycles)
	{
		this.processesCycles = processCycles;
	}
}
