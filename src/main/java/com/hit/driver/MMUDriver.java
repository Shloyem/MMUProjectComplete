package com.hit.driver;

import java.io.IOException;
import com.hit.controller.MMUController;
import com.hit.memoryunits.HardDisk;
import com.hit.model.MMUModel;
import com.hit.util.MMULogger;
import com.hit.view.CLI;
import com.hit.view.MMUView;

/*
 * This class is has the main method, and is the one to start the whole system
 */
public class MMUDriver
{
	public static void main(String[] args) throws IOException
	{
		// Creating the Command Line Interface, setting its input and output sources
		CLI cli = new CLI(System.in, System.out);
		String[] configuration;
		HardDisk.CreateHardDisk();
		
		// Getting the configuration to operate the system
		while((configuration = cli.getConfiguration())!= null)
		{
			/**
			* Build MVC model to demonstrate MMU system actions
			*/
			MMULogger.getInstance().open();
			MMUModel model = new MMUModel(configuration);
			MMUView view = new MMUView();
			MMUController controller = new MMUController(model, view);
			model.addObserver(controller);
			view.addObserver(controller);
			model.start();
		}
	}
}
