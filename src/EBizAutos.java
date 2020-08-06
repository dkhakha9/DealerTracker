
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class EBizAutos extends WebEngine
{

	public static void main(String[] args)
	{
		List<Vehicle> vehicleList = new ArrayList<>();
		
		EBizAutos vehicleFinder = new EBizAutos();
		
		long startTime = System.currentTimeMillis();
		
		vehicleList = vehicleFinder.searchVehicles("http://www.richfieldbloomingtonhonda.com", "/inventory.aspx", "?_page=", "Hondas Found", 1, 1);
		//vehicleList = vehicleFinder.searchVehicles("http://www.richfieldbloomingtonhonda.com/used-cars.aspx", "?_page=", "Vehicles Found");
		
		System.out.println("Run time: " + ((System.currentTimeMillis() - startTime)/1000));
		
		System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList, 400));
		//System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList));
		
		Utils.dumpVehiclesToFile(vehicleList);
	}
	
	/*
	 * Algorithm:
	 * Parse each line as HTML using Jsoup.
	 * Check if vehicle description is present in the text. Save if found.
	 * Check if VIN is present in the text. Save the vehicle if found.
	 */
	protected List<Vehicle> searchVehiclesOnPage(String urlStr)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		Vehicle foundVehicle = new Vehicle();
		List<String> VINs = new ArrayList<>();
		
		boolean nextLineHasTrim = false;
		
		java.net.URL url;
		Scanner input;
		String line;
		
		try
		{
			url = new java.net.URL(urlStr);
			input = new Scanner(url.openStream());
		}
		catch (Exception ex)
		{
			return retrySearchOnPage(urlStr);
		}
		
		while (input.hasNext())
		{
			line = input.nextLine();

			Document doc = Jsoup.parse(line);
			
			//System.out.println(line);
			
			Elements allElements = doc.getAllElements();
			
			for (Element elem: allElements)
			{
				if (elem.hasText())
				{
					// Jsoup wraps HTML tag around each line. This tag now has text of all children.
					if (elem.tagName().compareTo("html") == 0)
					{
						// -1 indicates that search is in progress and going as expected
						if (this.expectedVehicleCount == -1)
							this.expectedVehicleCount = findVehicleCount(line);
						
						if (nextLineHasTrim)
						{
							foundVehicle.setTrim(elem.text());
							nextLineHasTrim = false;
							break; // stop looping through each element, skip to the next line
						}
						// TODO: function is incomplete and only knows few Honda models
						if (Utils.hasModelYear(elem.text()))
						{
							foundVehicle.setAndParseDescription(elem.text());
							nextLineHasTrim = true;
							break; // stop looping through each element, skip to the next line
						}
						else // check for VIN
						{
							//System.out.println(elem.text());
							String[] wordsAll = elem.text().split(":|,|;");
							
							if (wordsAll.length > 1)
							{
								String[] wordsSelected;
								
								for (int i = 0; i < wordsAll.length - 1; i++)
								{
									// Since we are going to access next elements,
									// -1 will give at least one element margin and
									// will prevent going out of bounds

									if (wordsAll[i].contains("VIN") && foundVehicle.getVIN().isEmpty())
									{
										wordsSelected = wordsAll[i+1].trim().split(" ");
										
										if (wordsSelected.length > 0)
										{
											// Check for duplicates
											if (!VINs.contains(wordsSelected[0]) && (wordsSelected[0].length() == 17))
											{
												foundVehicle.setVIN(wordsSelected[0]); //grab one word following the "VIN"
												VINs.add(wordsSelected[0]);
												vehicles.add(foundVehicle);
												
												// Empty object for the next vehicle
												foundVehicle = new Vehicle();
												
												break; // stop looping through each element, skip to the next line
											}
										}
									} /* VIN candidate string found */
								} // check all words in the line
							} // more than one word in line
						} // search vehicle and VIN
					} /* if HTML tag */
				} /* elements has text */
				
				if (!foundVehicle.getVIN().isEmpty())
				{					
					break; // stop looping through each element, skip to the next line
				}
			} /* loop through each element */
		} /* while input has lines */
			
		input.close(); // make sure resource is closed
		
		return vehicles;
	} /* searchVehiclesOnPage */
	
	/*
	 * *** Key word and number of vehicles are expected to be on the same line ***
	 * *** textLine input is expected to be parsed element text                ***
	 * 
	 * Returns -1 if the keyWord is not found.
	 * Returns 0 if the keyWord is found, but the line does not
	 * contain valid integer.
	 * Returns integer number fetched after keyWord appearance.
	 */
	@Override
	protected int findVehicleCount(String textLine)
	{		
		int retVal = -1;
		
		Document doc = Jsoup.parse(textLine);
		
		Elements allElements = doc.getAllElements();
		
		for (Element elem: allElements)
		{
			if (elem.hasText())
			{
				//System.out.println(elem.text());
				if (elem.tagName().compareTo("html") == 0)
				{
					if (elem.text().contains(this.expVehicleCntKeyWord))
					{
						try
						{
							String[] targetStrArray = elem.text().split(" ");
							retVal = Integer.parseInt(targetStrArray[0].trim());
							//System.out.println("Found exp vehicle count: " + retVal);
						}
						catch (Exception ex)
						{
							//TODO log error
							System.out.println("Error parsing exp vehicle count: " + elem.text());
							retVal = 0; // error, stop the search
						}
						break;
					}
				}
			} /* element has text */
		} /* for each element */
		
		return retVal;
	} /* findVehicleCount */
}
