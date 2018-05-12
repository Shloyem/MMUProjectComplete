package com.hit.view;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/*
 * This class represents a Command Line Interface, which is the user's first interaction with the system.
 * The class will be responsible for setting the system's behavior, and in our case which Cache Algorithm
 * will serve the MMU class, and what size the RAM capacity will be.
 */
public class CLI
{
	private InputStream m_In; 
	private OutputStream m_Out;
	private Scanner sc;
	
	/*
	 * Setting the class input and output streams 
	 */
	public CLI(InputStream i_In, OutputStream i_Out)
	{
		m_In = i_In;
		m_Out = i_Out;
	}
	
	/*
	 * The main method of this class. Gets the system configuration for: MMU Cache Algorithm, RAM Capacity
	 */
	public String[] getConfiguration()
	{
		String[] configurationInput = null;
		
		// Get first input and validate it between 	START - To start the system
		//											STOP  - To exit
		String firstAnswer = checkFirstInput();
		
		// If user input is STOP, print the exit message and close the input scanner
		// Otherwise, continue to get and validate the second input 
		if (firstAnswer == "STOP")
		{
			printGoodbyeMsgAndCloseScanner();
		}
		else // (firstAnswer == "START")
		{
			configurationInput = checkSecondInput();
		}	
		
		return configurationInput;
	}
	
	/*
	 * This method is the exit routine when user wishes to stop the system
	 */
	private void printGoodbyeMsgAndCloseScanner()
	{
		printGoodbyeMsg();
		sc.close();
	}
	
	/*
	 * This method gets and validates user input between 	START - To start the system
	 *														STOP  - To exit
	 */
	private String checkFirstInput()
	{
		sc = new Scanner(m_In);
		String userInput = null;
		String answer = null;
		printStartStopMsg();
		
		while(sc.hasNextLine())
		{
			userInput = sc.nextLine();
			
			if (userInput.toUpperCase().equals("STOP"))
			{
				answer = "STOP";
				break;
			}
			else if (userInput.toUpperCase().equals("START"))
			{
				answer = "START";
				break;
			}
			else
			{
				printErrorMsg();
				printStartStopMsg();
			}
		}
		
		return answer;
	}
	
	
	/*
	 * This method gets and validates user input for Cache Algorithm and RAM capacity
	 */
	private String[] checkSecondInput()
	{
		sc = new Scanner(m_In);
		String userInput = null;
		String[] userInputArr = null;
		Integer userNumInput;
		
		printAlgoAndRAMMsg();
		
		while(sc.hasNextLine())
		{
			userInput = sc.nextLine();
			userInputArr = userInput.split("\\s+");
			
			/*
			 * Get user input and check if it:
			 * 		- Fits one of the Cache Algorithms : MFU / LRU / Second change
			 *		- Has a valid capacity
			 */
			if(userInputArr.length == 3)
			{
				// Validate capacity entered it is a valid number
				userNumInput = parseStringToIntOrMinusOne(userInputArr[2]);
		
				if (userInputArr[0].toUpperCase().equals("SECOND") &&
						userInputArr[1].toUpperCase().equals("CHANCE") &&
						userNumInput != -1)				
				{
					break;
				}
			}
			else if (userInputArr.length == 2)
			{
				// Parse the capacity entered to validate it is a valid number
				userNumInput = parseStringToIntOrMinusOne(userInputArr[1]);
				
				if ((userInputArr[0].toUpperCase().equals("LRU") || userInputArr[0].toUpperCase().equals("MFU"))
						&& userNumInput != -1)
				{
					break;
				}
			}
			else if (userInputArr[0].toUpperCase().equals("STOP"))
			{
				printGoodbyeMsgAndCloseScanner();
				
				break;
			}
			
			//If it gets here non of the conditions succeeded
			printErrorMsg();
			printAlgoAndRAMMsg();
		}
		
		return userInputArr;
	}
	
	// Validating a string input is a valid number
	private int parseStringToIntOrMinusOne(String i_NumString)
	{
		int parsedInt;
		
		try
		{
			parsedInt = Integer.parseInt(i_NumString);
			
		}
		catch (NumberFormatException numException)
		{
			parsedInt = -1;
		}
		
		return parsedInt;
	}

	private void printAlgoAndRAMMsg()
	{
		System.out.println("Please enter required algorithm(LRU/MFU/Second chance) and RAM capacity");
	}

	private void printErrorMsg()
	{
		System.out.println("Not a valid command");
	}

	private void printStartStopMsg()
	{
		System.out.println("Please type START to start operation or STOP to stop it");
	}

	private void printGoodbyeMsg()
	{
		System.out.println("You selected to stop the system. Goodbye");
	}
	
	// Write to the output stream
	public void write(String i_InputStr)
	{
		PrintWriter printWriter = new PrintWriter(m_Out);
		printWriter.println(i_InputStr);
		printWriter.flush();
	}
}
