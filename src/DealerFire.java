
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DealerFire extends WebEngine
{
	//private boolean fetchExpVehicleCount;
	private String prevLineText;
	
	DealerFire()
	{
		//fetchExpVehicleCount = false; // shows if expectedVehicleCount will appear in the next line
		prevLineText = "";
	}
	
	public static void main(String[] args)
	{
		List<Vehicle> vehicleList = new ArrayList<>();
		
		DealerFire vehicleFinder = new DealerFire();
		
		long startTime = System.currentTimeMillis();
		
		//vehicleList = vehicleFinder.searchVehicles("http://www.kenvancehonda.com", "/new-cars-eau-claire-wi", "?page=", "New cars in Eau Claire WI", 1, 1);
		//vehicleList = vehicleFinder.searchVehicles("http://www.kenvancehonda.com", "/used-cars-eau-claire-wi", "?page=", "Pre-Owned cars in Eau Claire WI", 1, 1);
		
		System.out.println("Run time: " + ((System.currentTimeMillis() - startTime)/1000));
		
		//System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList, 380));
		System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList));
		
		Utils.dumpVehiclesToFile(vehicleList);

	}
	
	protected List<Vehicle> searchVehiclesOnPage(String urlStr)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		
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
			
			// -1 indicates that search is in progress and is going as expected
			if (this.expectedVehicleCount == -1)
				this.expectedVehicleCount = findVehicleCount(line);
			
			if (line.contains("\"VIN\"") && line.contains("\"Model\""))
			{
				// Split the line into array of vehicles
				String[] vehiclesData = line.split("\\{|\\}");
				
				for (String singleVehicleData: vehiclesData)
				{
					// Only work with strings containing VIN
					if (singleVehicleData.contains("\"VIN\"") && singleVehicleData.contains("\"Model\""))
					{
						Vehicle foundVehicle = parseVehicleData(singleVehicleData);
						
						if (foundVehicle != null)
						{
							if (foundVehicle.getVIN().length() == 17)
							{
								vehicles.add(foundVehicle);
							}
							else
							{
								//TODO log error
							}	
						}
						else // error occurred in parseVehicleData function
						{
							//TODO log error
						}
					}
				} /* for each vehicle's data */
			}
		} /* while (input.hasNext()) */
		
		input.close();
		
		return vehicles;
	} /* searchVehicles */
	
	private Vehicle parseVehicleData(String vehicleData)
	{
		Vehicle parsedVehicle = new Vehicle();
		
		// Split single vehicle data string into array of characteristics
		String[] vehicleCharacteristics = vehicleData.split(":|,");
		
		int i = 0;
		
		while (i < vehicleCharacteristics.length - 1)
		{
			// Since we are going to access next elements,
			// -1 will give at least one element margin and
			// will prevent going out of bounds
			
			if(vehicleCharacteristics[i].contains("\"VIN\""))
			{
				parsedVehicle.setVIN(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"Model\""))
			{
				parsedVehicle.setModel(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"Make\""))
			{
				parsedVehicle.setMake(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"Trim\""))
			{
				parsedVehicle.setTrim(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"Year\""))
			{
				parsedVehicle.setYear(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			i++;
		}
		
		return parsedVehicle;
	} /* parseVehicleData */
	
	/*
	 *  *** Number of vehicles is expected to be on the line 
	 *      following the key word line ***
	 *
	 * Returns -1 if the keyWord is not found.
	 * Returns 0 if the keyWord is found, but the next line does not
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
					if (elem.text().compareTo(this.expVehicleCntKeyWord) == 0)
					{
						//this.fetchExpVehicleCount = true;
						try
						{
							retVal = Integer.parseInt(this.prevLineText.trim());
							//System.out.println("Found exp vehicle count: " + retVal);
						}
						catch (Exception ex)
						{
							//TODO log error
							System.out.println("Error parsing exp vehicle count: " + this.prevLineText);
							retVal = 0; // error, stop the search
						}
					}
					else
					{
						this.prevLineText = elem.text();
					}
					break;
				}
			} /* element has text */
		} /* for each element */
		
		return retVal;
	} /* findVehicleCount */
}
