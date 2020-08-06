import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.time.Year;

public class inventoryScanner
{
	public static void main(String[] args)
	{
		String urlString = "http://www.walserhonda.com/new-inventory/index.htm";
		//String urlString = "http://www.richfieldbloomingtonhonda.com/honda.aspx";
		
		try
		{
			FileWriter outFile = new FileWriter("data.txt");
			
			java.net.URL url = new java.net.URL(urlString);
			Scanner input = new Scanner(url.openStream());
			
			boolean searchVehicle = true;
			//boolean searchVIN = false;
			
			String vehicle = "", VIN = "N/A", Engine = "N/A"; // if N/A appears in the report, then vehicle was not found
			
			while (input.hasNext())
			{
				String line = input.nextLine();
				
				Document doc = Jsoup.parse(line);
				
				if (searchVehicle)
				{
					if (line.lastIndexOf("href") > -1)
					{
						//System.out.println("href found");
						Element link = doc.select("a").first();
						
						try
						{
							if (isVehicle(link.text()))
							{
								searchVehicle = false;
								vehicle = link.text();
								VIN = "";
								Engine = "";
							}
						}
						catch (Exception ex)
						{
							//keep searching
						}
					}
				}
				else // search VIN
				{
					//outFile.write(line + System.getProperty("line.separator"));
					Elements allElements = doc.getAllElements();
					
					for (Element elem: allElements)
					{
						if (elem.hasText())
						{
							if (elem.tagName().compareTo("html") == 0)
							{
								//System.out.println(elem.text());
								String[] wordsAll = elem.text().split(":|,|;");
								if (wordsAll.length > 1)
								{
									String[] wordsSelected;
									
									for (int i = 0; i < wordsAll.length; i++)
									{
										if ((wordsAll[i].lastIndexOf("Engine") > -1) && Engine.isEmpty())
										{
											wordsSelected = wordsAll[i+1].trim().split(" ");

											if (wordsSelected.length > 0)
											{
												// Grab up to 3 words following the "Engine"
												for (int j = 0; (j < wordsSelected.length) && (j < 3); j++)
												{
													if (j > 0)
														Engine = Engine + " ";
													
													Engine = Engine + wordsSelected[j];
												}
												//System.out.println("Engine: " + Engine);
											}
										}
										
										if ((wordsAll[i].lastIndexOf("VIN") > -1) && VIN.isEmpty())
										{
											wordsSelected = wordsAll[i+1].trim().split(" ");
											if (wordsSelected.length > 0)
												VIN = wordsSelected[0]; //grab one word following the "VIN"
										}
									}
								}
							}
						}
					}
					
					if (!VIN.isEmpty() && !Engine.isEmpty())
					{
						//outFile.write("VIN: " + VIN + " Model Code: " + modelCode + " Engine: " + Engine + System.getProperty("line.separator"));
						outFile.write("Vehicle: " + vehicle + " VIN: " + VIN + " Engine: " + Engine + System.getProperty("line.separator"));
						searchVehicle = true;
						vehicle = "N/A"; // this value should never appear in the report
					}
				}
			}
			
			input.close();
			
			outFile.close();
		}
		catch (Exception ex)
		{
			System.out.println("Error: " + ex.getMessage());
			System.exit(1);
		}
		System.out.println("Completed.");
	}
	
	/*
	 * Returns TRUE if string starts with YEAR MAKE MODEL, e.g. 2017 Honda Accord
	 */
	static boolean isVehicle (String str)
	{
		String[] words = str.split(" ");
		
		boolean retVal = true;
		
		if (words.length > 2)
		{
			// Word 1 - Year
			// Should be less than or equal to current year + 1
			try
			{
				if (Integer.parseUnsignedInt(words[0]) > Year.now().getValue() + 1)
					retVal = false;
			}
			catch (NumberFormatException ex)
			{
				// Not a number
				retVal = false;
			}
			
			// Word 2 - Make
			if (!isMake(words[1]))
				retVal = false;
			
			// Word 3 - Model
			if (!isModel(words[2]))
				retVal = false;
		}
		else /* not enough words */
		{
			retVal = false;
		}
		
		return retVal;
	}
	
	static boolean isMake (String str)
	{
		if (str.compareTo("Honda") == 0)
			return true;
		else
			return false;
	}
	
	static boolean isModel (String str)
	{
		if ((str.compareToIgnoreCase("Accord") == 0) ||
			(str.compareToIgnoreCase("Civic") == 0)  ||
			(str.compareToIgnoreCase("CR-V") == 0)	||
			(str.compareToIgnoreCase("Fit") == 0)	||
			(str.compareToIgnoreCase("HR-V") == 0)||
			(str.compareToIgnoreCase("Odyssey") == 0)	||
			(str.compareToIgnoreCase("Pilot") == 0)	||
			(str.compareToIgnoreCase("Ridgeline") == 0)		)
			return true;
		else
			return false;
	}
}
