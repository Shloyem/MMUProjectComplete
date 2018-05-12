package com.hit.processes;

import java.util.List;

/*
 * This class represents container of Process Cycles, which associated to each thread
 */
public class ProcessCycles
{
	private List<ProcessCycle> processCycles;

	public List<ProcessCycle> getProcessCycles()
	{
		return processCycles;
	}

	public void setProcessCycles(List<ProcessCycle> processCycles)
	{
		this.processCycles = processCycles;
	}

	private ProcessCycles(List<ProcessCycle> processCycles)
	{
		this.processCycles = processCycles;
	}
}
