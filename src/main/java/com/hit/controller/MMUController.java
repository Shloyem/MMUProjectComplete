package com.hit.controller;

import java.util.Observable;
import java.util.Observer;

import com.hit.model.MMUModel;
import com.hit.model.Model;
import com.hit.util.MMULogger;
import com.hit.view.MMUView;
import com.hit.view.View;

/*
 * This class serves as an observer to MMUView and MMUModel, according to MVC design.
 * It is the way View and Model interact with each other 
 */
public class MMUController implements Controller, Observer
{
	Model model;
	View view;
	
	public MMUController(Model model, View view)
	{
		this.model = model;
		this.view = view;
	}
	
	/*
	 * This class is an observer, and therefore this method gets an update when observables notify
	 */
	@Override
	public void update(Observable o, Object arg) 
	{
		//If view notifies, make model read the data needed (commands) and use it to configure the view object 
		if(o == view)
		{
			model.readData();
			((MMUView)view).setConfiguration(((MMUModel)model).getCommands());
		}
		// If model notifies, it finished running the processes, and view is ready to be opened 
		else if(o == model)
		{
			MMULogger.getInstance().close();
			System.out.println("Done !");
			((MMUView)view).numProcesses = ((MMUModel)model).numProcesses;
			((MMUView)view).ramCapacity = ((MMUModel)model).ramCapacity;
			view.open();
		}
	}

}
