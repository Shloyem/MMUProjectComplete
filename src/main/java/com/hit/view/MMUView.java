package com.hit.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/*
 * This class represent View part in MVC pattern, responsible for the user interface
 *  and interaction with the user, and reflects mostly data store in the Model part 
 */
public class MMUView extends Observable implements View
{
	public static int BYTES_IN_PAGE = 5;
	public static int NUM_MMU_PAGES = 20;
	public int numProcesses;
	public int ramCapacity;
	
	private final static Boolean ALLOW_SPAN_HORIZONAL = true;
	private final static Boolean ALLOW_SPAN_VERTICAL = true;
	
	private int pageFaultsCounter = 0;
	private int pageReplacementsCounter = 0;
	private List<String> commands;
	private Map<Integer,String[]> pagesInRamContent;
	private Map<Integer,Integer> pagesInRamAndProcessRelatedTo;
	private Set<Integer> processesRequested = new HashSet<Integer>();
	private List<Integer> pagesRequestedByPF;
	private Map<Integer, Integer> pagesRequestedByPR;

	private String currentCommand;
	private int replacedPageNum;
	private int currentPageNum;
	private int currentProcessNum;
	private String[] currentPageContent;
	private String currentGPCommand;
	private int lineCounter = 0;
	private String stringFormatGP = "GP:P| |\\[|, |\\]";
	
	private Table table;
	private Shell shell;
	private Display display;
	private TableColumn[] tableCols;
	private TableItem[] tableRows;
	private Label labelPF;
	private Text textPF;
	private Label labelPR;
	private Text textPR;
	private Button playButton;
	private Button playAllButton;
	private Button resetButton;
	private int lastCommandIndex;
	private Composite tableComposite;
	private Composite labelsComposite;
	private Composite playButtonsComposite;
	private Composite processesListComposite;
	private GridLayout gridLayout;
	private GridData gridData;
	private String EMPTY_STRING = "";
	private int buttonWidth = 70;
	private int buttonHeight = 35;
	private int fixedSpace = 20;
	private int widthOfListItem = 70;
	
	public MMUView()
	{
	}
	
	/*
	 * Sets the configuration of: bytes in page, number of processes, and number of pages
	 */
	public void setConfiguration(List<String> commands)
	{
		this.commands = commands;
		pagesInRamContent = new HashMap<Integer,String[]>();
		pagesInRamAndProcessRelatedTo = new HashMap<Integer,Integer>();
		pagesRequestedByPF = new LinkedList<Integer>();
		pagesRequestedByPR = new LinkedHashMap<Integer, Integer>();
		
		//Set the number of pages which can be present in the RAM, according to the commands format 
		String[] pagesNumTokens = commands.get(0).split("RC:");
		ramCapacity = Integer.parseInt(pagesNumTokens[1]);
		
		//Set the number of processes, according to the commands format
		String[] procNumTokens = commands.get(1).split("PN:");
		numProcesses = Integer.parseInt(procNumTokens[1]);
		
		//Set the number of bytes that each page contains
		for (String line : commands)
		{
			if (line.contains("["))
			{
				int begin = line.indexOf("[");
				int end = line.indexOf("]");
				String bytesString = line.substring(begin, end);
				BYTES_IN_PAGE = bytesString.split(",").length;
				break;
			}
		}
		
		lastCommandIndex = commands.size();
		NUM_MMU_PAGES = findHighestPageNum();
	}
	
	/*
	 * This method initials all the view components and opens the main view container
	 */
	@Override
	public void open()
	{
		//Call Controller to fetch the information for the GUI
		setChanged();
		notifyObservers();

		createAndShowGUI();
	}
	
	/*
	 * Creates and shows the Graphical User Interface 
	 */
	private void createAndShowGUI()
	{
		display = new Display();
		shell = new Shell(display);
		shell.setText("MMU Simulator");
		Rectangle screenSize = display.getPrimaryMonitor().getClientArea();
		int width = 1020;
		int height = 430;
		
		// Centering the window position
		shell.setBounds((screenSize.width/2) - (width/2), (screenSize.height/2) - (height/2), width, height);   
		shell.forceFocus();
		
		setCompositesSizeAndLayouts();
		
		createContentTable(tableComposite);
	    createPFandPRLabelsAndButtons(labelsComposite);
		createPlayButtons(playButtonsComposite);
		createProcessList(processesListComposite);
		
		shell.open();
		
		// run the event loop as long as the window is open
		while (!shell.isDisposed()) {
		    // read the next OS event queue and transfer it to a SWT event
		    if (!display.readAndDispatch())
		     {
		    // if there are currently no other OS event to process
		    // sleep until the next OS event is available
		        display.sleep();
		     }
		}
		// disposes all associated windows and their components
		display.dispose();
	}

	/*
	 * Dividing the main frame (shell) into different parts (composites). Spacing and ordering each part using GridLayout Layout and GridData Data Layout.
	 */
	private void setCompositesSizeAndLayouts()
	{
		shell.setLayout(new GridLayout(3, true));
		shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, ALLOW_SPAN_HORIZONAL, ALLOW_SPAN_VERTICAL, 3, 2));
		
		tableComposite = new Composite(shell, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, ALLOW_SPAN_HORIZONAL, ALLOW_SPAN_VERTICAL, 3, 1));
		tableComposite.setLayout(new GridLayout(1, true));
		
		playButtonsComposite = new Composite(shell, SWT.NONE);
		playButtonsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, ALLOW_SPAN_HORIZONAL, ALLOW_SPAN_VERTICAL, 1, 1));
		gridLayout = new GridLayout(2, true);
		gridLayout.horizontalSpacing = fixedSpace;
		gridLayout.verticalSpacing = fixedSpace;
		playButtonsComposite.setLayout(gridLayout);
		
		labelsComposite = new Composite(shell, SWT.NONE);
		labelsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, ALLOW_SPAN_HORIZONAL, ALLOW_SPAN_VERTICAL, 1, 1));
		gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = fixedSpace / 2;
		gridLayout.verticalSpacing= fixedSpace / 2;
		labelsComposite.setLayout(gridLayout);
		
		processesListComposite = new Composite(shell, SWT.NONE);
		processesListComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, ALLOW_SPAN_HORIZONAL, ALLOW_SPAN_VERTICAL, 1, 1));
		processesListComposite.setLayout(new GridLayout(1, false));
		
	}
	
	/*
	 * Creates the table reflecting the RAM memory at that moment
	 */
	private void createContentTable(final Composite parent)
	{
		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
	    table.setLinesVisible(true);
	    table.setHeaderVisible(true);
	    
	    setTable();
	    drawUpdatedTable();
	}
	
	/*
	 * Initial one - time allocating and setting of the table
	 */
	private void setTable()
	{
		//Create column headers
		tableCols = new TableColumn[NUM_MMU_PAGES];
		table.setHeaderBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
	    
	    for (int i = 1; i <= NUM_MMU_PAGES; i++) 
	    {
	    	tableCols[i-1] = new TableColumn(table, SWT.CENTER);
	    	tableCols[i-1].setWidth(45);
	    	tableCols[i-1].setAlignment(SWT.CENTER);
	    }
	    
	    //Create table rows
	    tableRows = new TableItem[BYTES_IN_PAGE];
	    
	    for (int i = 0; i < BYTES_IN_PAGE; i++) 
	    {
	        tableRows[i] = new TableItem(table, SWT.CENTER);
	    }
	}
	
	/*
	 * Draw table according to the relevant pages: pages which are in present in memory, and inserted by selected processes
	 */
	private void drawUpdatedTable()
	{
		// Set initial headers as empty at first
		for (int i = 0; i < NUM_MMU_PAGES; i++) 
		{
			tableCols[i].setText(EMPTY_STRING);
		}
	  
		// Set initial table data fields as 0
	    for (int i = 0; i < BYTES_IN_PAGE; i++) 
	    {
	        for (int j = 0; j < NUM_MMU_PAGES; j++) 
		    {
	        	tableRows[i].setText(j, "0");
		    }
	      }
	    
	    Boolean isEnteredBySelectedProcess = false;
	    
	    // For each page in the RAM memory, 
	    for(Integer pageNum : pagesInRamContent.keySet())
	    {
	    	Integer processOfPageNum = pagesInRamAndProcessRelatedTo.get(pageNum);
	    	isEnteredBySelectedProcess = processesRequested.contains(processOfPageNum);
	    	
	    	// If it was entered by a process that is selected by the user:
	    	//	-	Write the page number at the header (instead of empty string before)
		    //	-	Write the page content in the data fields of that page column (instead of 0's before)
	    	if (isEnteredBySelectedProcess)
	    	{
		    	String[] pageBytes = pagesInRamContent.get(pageNum);
		    	tableCols[pageNum-1].setText(Integer.toString(pageNum));
		    	
		    	for (int i = 0; i < BYTES_IN_PAGE; i++)
				{
		    		tableRows[i].setText(pageNum - 1, pageBytes[i]);
				}
		    }
	    }
	}

	/*
	 * Creates labels and texts showing the Page Fault and Page Replacements counters 
	 */
	private void createPFandPRLabelsAndButtons(final Composite parent)
	{
		labelPF = new Label(parent, SWT.NULL);
		labelPF.setText("Number of Page Faults:");
		
		textPF = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, !ALLOW_SPAN_HORIZONAL, !ALLOW_SPAN_VERTICAL, 1, 1);
	    gridData.widthHint = 30;
		textPF.setLayoutData(gridData);
		
		labelPR = new Label(parent, SWT.NULL);
		labelPR.setText("Number of Page Replacements:");
		
		textPR = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, !ALLOW_SPAN_HORIZONAL, !ALLOW_SPAN_VERTICAL, 1, 1);
	    gridData.widthHint = 30;
	    textPR.setLayoutData(gridData);
	}
	
	private void updateLabelsPRPF()
	{
		textPF.setText(Integer.toString(pageFaultsCounter));
		textPR.setText(Integer.toString(pageReplacementsCounter));
	}
	
	/*
	 * Creates "PLAY", "PLAY ALL", "RESET" buttons and their suitable click - events 
	 */
	private void createPlayButtons(final Composite parent)
	{
		// Create "Play" button 
		playButton = new Button(parent, SWT.NONE);
		playButton.setText("PLAY");
		gridData = new GridData(SWT.FILL, SWT.FILL, !ALLOW_SPAN_HORIZONAL, !ALLOW_SPAN_VERTICAL, 1, 1);
		gridData.widthHint = buttonWidth;
		gridData.heightHint= buttonHeight;
		playButton.setLayoutData(gridData);
		
		// Click-event of performing one move
		playButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (lineCounter < lastCommandIndex)
				{
					oneStep();
					drawUpdatedTable();
					updateLabelsPRPF();
				}
				else
				{
					playButton.setEnabled(false);
					playAllButton.setEnabled(false);
				}
			}
		});
		
		// Create "Play All" button
		playAllButton = new Button(parent, SWT.NONE);
		playAllButton.setText("PLAY ALL");
		gridData = new GridData(SWT.FILL, SWT.FILL, !ALLOW_SPAN_HORIZONAL, !ALLOW_SPAN_VERTICAL, 1, 1);
		gridData.widthHint = buttonWidth;
		gridData.heightHint= buttonHeight;
		playAllButton.setLayoutData(gridData);
		
		
		// Click-event of performing all the moves till the end.
		playAllButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				while (lineCounter < lastCommandIndex)
				{
					oneStep();
				}
				
				drawUpdatedTable();
				updateLabelsPRPF();
				
				playButton.setEnabled(false);
				playAllButton.setEnabled(false);
			}
		});
		
		// Create "Reset" button
		resetButton = new Button(parent, SWT.NONE);
		resetButton.setText("RESET");
		gridData = new GridData(SWT.CENTER, SWT.FILL, !ALLOW_SPAN_HORIZONAL, !ALLOW_SPAN_VERTICAL, 2, 1);
		gridData.widthHint = buttonWidth;
		gridData.heightHint= buttonHeight;
		resetButton.setLayoutData(gridData);
		
		// Click-event of resetting the system to the way it was first opened
		resetButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				lineCounter = 0;
				pageFaultsCounter = 0;
				pageReplacementsCounter = 0;
				pagesInRamContent.clear();
				pagesInRamAndProcessRelatedTo.clear();
				pagesRequestedByPF.clear();
				pagesRequestedByPR.clear();
				updateLabelsPRPF();
				drawUpdatedTable();
				playButton.setEnabled(true);
				playAllButton.setEnabled(true);
			}
		});
	}
	
	/*
	 * Creates a list of the systems processes, for the user to select which of them he wants the system to reflect.
	 */
	private void createProcessList(final Composite parent)
	{
		Label processesLabel = new Label(parent, SWT.NULL);
		processesLabel.setText("Processes:");
		processesLabel.pack();
		
		final org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List (processesListComposite, SWT.BORDER | SWT.MULTI );
		gridData = new GridData(SWT.FILL, SWT.FILL, !ALLOW_SPAN_HORIZONAL, !ALLOW_SPAN_VERTICAL, 1, 1);
		
		gridData.widthHint = widthOfListItem;
		gridData.heightHint = list.getItemHeight() * numProcesses;
		list.setLayoutData(gridData);

		for (int i = 0; i < numProcesses; i++) 
		{
			list.add ("Process " + Integer.toString(i));
		}
		
		// Click - event of adding the selected processes into a list, to filter pages of selected processes only
		list.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				int [] selections = list.getSelectionIndices();
				processesRequested.clear();

				for (int selection : selections)
				{
					processesRequested.add(selection);
				}
			}
		});
	}
	
	/* 
	 * This method performs one step which is either a: Page Fault / Page Replacement / Update of present in memory page.
	 * This step reflects a step committed by any the system's processes and NOT of the selected ones only.
	 */
	private Boolean oneStep()
	{
		Boolean isStepDone = false;
		
		// Iterate through the commands until a step was done, or they are finished
		while (lineCounter < commands.size() && isStepDone == false)
		{
			currentCommand = commands.get(lineCounter);
			
			// If a page fault was performed, add the page number into a list 
			if (currentCommand.contains("PF"))
			{
				currentPageNum = getPageNumberFromPF(currentCommand);
				pagesRequestedByPF.add(currentPageNum);
			}
			// If a page fault was performed, add the entered and replaced page numbers into a map
			else if (currentCommand.contains("PR:"))
			{
				replacedPageNum = getReplacedPageNumberFromPR(currentCommand);
				currentPageNum = getEnteringPageNumberFromPR(currentCommand);
				pagesRequestedByPR.put(currentPageNum, replacedPageNum);
			}
			// If a Get Page command was called
			else if (currentCommand.contains("GP"))
			{
				currentGPCommand = currentCommand;
				currentPageNum = getPageNumberFromGP(currentGPCommand);
				currentProcessNum = getProcessNumberFromGP(currentGPCommand);
				currentPageContent = getContentFromGP(currentGPCommand);

				// If there were Page Faults that need to be dealt with (that didn't yet have a Get Page command with the page)
				if (pagesRequestedByPF.size() > 0)
				{
					// If its the matching page from the Page Fault, increase the Page Faults counter, and the page will be added to memory after the condition
					if (pagesRequestedByPF.get(0) == currentPageNum)
					{
						pagesRequestedByPF.remove(0);
						pageFaultsCounter ++;
					}
				}
				// If there were Page Replacements that need to be dealt with (that didn't yet have a Get Page command with the page)
				else if (pagesRequestedByPR.size() > 0)
				{
					Integer pageToEnter = pagesRequestedByPR.keySet().iterator().next();
					
					// If its the matching page from the Page Replacements that needs to be entered, increase the Page Replacements counter,
					// remove the replaced page from memory, and the page will be added to memory after the condition
					if (pageToEnter == currentPageNum)
					{
						replacedPageNum = pagesRequestedByPR.values().iterator().next();
						pagesRequestedByPR.remove(currentPageNum);
						pagesInRamContent.remove(replacedPageNum);
						pagesInRamAndProcessRelatedTo.remove(replacedPageNum);
						pageReplacementsCounter ++;
					}
				}
				
				// Enter the new page to insert if it was caused by Page Fault or Page Replacement, or update the content of a page already in RAM 
				pagesInRamContent.put(currentPageNum, currentPageContent);
				pagesInRamAndProcessRelatedTo.put(currentPageNum, currentProcessNum);
				
				isStepDone = true;
			}
			
			lineCounter++;
		}
		
		return isStepDone;
	}
	
	/*
	 *  A method that sets the amount of pages for the table
	 */
	private int findHighestPageNum()
	{
		int pageNum = 0;
		int maxPageNum = 0;
		
		for (String command : commands)
		{
			if (command.contains("GP:"))
			{
				pageNum = getPageNumberFromGP(command);
				
				if (pageNum > maxPageNum)
					maxPageNum = pageNum;
			}
		}
		
		return maxPageNum;
	}
	 
	private int getPageNumberFromPF(String command)
	{
		String[] commandTokens = command.split("PF:");
		
		return Integer.parseInt(commandTokens[1]);
	}

	private int getPageNumberFromGP(String command)
	{
		String[] commandTokens = command.split(stringFormatGP);
		return Integer.parseInt(commandTokens[2]);
	}
	
	private int getReplacedPageNumberFromPR(String command)
	{
		String[] commandTokens = command.split("PR:MTH | MTR ");
		return Integer.parseInt(commandTokens[1]);
	}
	
	private int getEnteringPageNumberFromPR(String command)
	{
		String[] commandTokens = command.split("PR:MTH | MTR ");
		return Integer.parseInt(commandTokens[2]);
	}
	
	private int getProcessNumberFromGP(String command)
	{
		String[] commandTokens = command.split(stringFormatGP);
		return Integer.parseInt(commandTokens[1]);
	}
	
	private String[] getContentFromGP(String command)
	{
		String[] commandTokens = command.split(stringFormatGP);
		return Arrays.copyOfRange(commandTokens, 4, 4 + BYTES_IN_PAGE);
	}
}
