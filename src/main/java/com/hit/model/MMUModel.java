package com.hit.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.hit.algorithm.IAlgoCache;
import com.hit.memoryunits.MemoryManagementUnit;
import com.hit.processes.Process;
import com.hit.processes.ProcessCycles;
import com.hit.processes.RunConfiguration;
import com.hit.util.MMULogger;

/*
 * MMUModel class is the model part of MVC design. It is the part responsible for operating and fetching the data to the whole system.
 */
public class MMUModel extends Observable implements Model
{
	public int numProcesses;
	public int ramCapacity;
	private IAlgoCache<Long, Long> algo;
	private String algoType;
	private List<String> data = null;
	
	/*
	 * This constructor gets and sets the RAM Capacity, and Cache Algorithm, according to the configurations passed to it. 
	 */
	public MMUModel(String[] configuration) throws IOException
	{
		ramCapacity = Integer.parseInt(configuration[configuration.length - 1]);
		MMULogger.getInstance().write(MessageFormat.format("RC:{0}{1}", ramCapacity, System.lineSeparator()), Level.INFO);
		
			if (configuration.length == 3)
				algoType = concatTwoStringArrayParts(configuration);
			else 	//(configuration.length == 2), otherwise it wouldn't have been verified
				algoType = configuration[0];
			 
			algo = AlgoFactory.getAlgo(algoType, ramCapacity);
	}
	
	/*
	 * This method start the model process
	 */
	@Override
	public void start()
	{
		// Creating MMU and setting it according to the given configurations
		MemoryManagementUnit mmu = new MemoryManagementUnit(ramCapacity, algo);
		
		// Reading the JSON configuration file
		RunConfiguration runConfig = readConfigurationFile();
		
		// Creating the processes and connecting them to the MMU
		List<ProcessCycles> processCycles = runConfig.getProcessCycles();
		List<Process> processes = createProcesses(processCycles, mmu);
		
		// Running the processes simultaneously
		runProcesses(processes);
		
		// Letting the observers (controller) know that model finished running the processes
		setChanged();
		notifyObservers("Ready");
	}
	
	//This method reads the data from a current source
	@Override
	public void readData()
	{
		data = new ArrayList<>();
		
		try
		{
			data = Files.readAllLines(Paths.get(MMULogger.DEFAULT_FILE_NAME));
		} catch (IOException e)
		{
			MMULogger.getInstance().write(MessageFormat.format("Error ! failed to read log file:{0}{1}", e.getMessage(),
																											System.lineSeparator()), Level.SEVERE);
		}
		
		data.removeAll(Arrays.asList(null,""));
	}
	
	public List<String> getCommands()
	{
		return data;
	}
	
	// Running the processes simultaneously, using Executor class
	private void runProcesses(List<Process> processes)
	{
		MMULogger.getInstance().write(MessageFormat.format("PN:{0}{1}{1}", processes.size(), System.lineSeparator()), Level.INFO);
		numProcesses = processes.size();
		
		ExecutorService executor = Executors.newCachedThreadPool();
		
		for (Process process : processes)
		{
			executor.execute(process);
		}
		
		// Forces this thread to wait for all the processes threads termination
		executor.shutdown();
		
		try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e)
		{
			MMULogger.getInstance().write(MessageFormat.format("Error ! execution of the processes stopped before ending:{0}{1}", e.getMessage(),
																														System.lineSeparator()), Level.SEVERE);
		}
	}

	/* 
	 * Creating the processes and connecting them to the MMU
	 * There is the same amount of processes and and process cycles, as each process cycle represents a process in our project
	 */
	private static List<Process> createProcesses(List<ProcessCycles> processCycles, MemoryManagementUnit mmu)
	{
		List<Process> processessReturned = new ArrayList<>(processCycles.size());
		int procID = 0;
		
		for (ProcessCycles processCyclesUnit : processCycles)
		{
			// Create new process, assign process ID, MMU reference , and ProcessCycles unit
			processessReturned.add(new Process(procID, mmu, processCyclesUnit));
			procID++;
		}
		
		return processessReturned;
	}
	
	// Reading the JSON configuration file
	private RunConfiguration readConfigurationFile()
	{
		Gson gson = new Gson();
		JsonReader jr = null;
		
		try
		{
			jr = new JsonReader(new FileReader("resources/Configuration.json"));
		} catch (FileNotFoundException e)
		{
			MMULogger.getInstance().write(MessageFormat.format("Error ! configuration file not found:{0}{1}", e.getMessage(),
																												System.lineSeparator()), Level.SEVERE);
		}
		
		RunConfiguration runConfiguration = gson.fromJson(jr, RunConfiguration.class);
		
		return runConfiguration;
	}

	// Concatenating two string array object to one string
	private String concatTwoStringArrayParts(String[] stringArrParts)
	{
		return stringArrParts[0].concat(" ").concat(stringArrParts[1]);
	}
}

