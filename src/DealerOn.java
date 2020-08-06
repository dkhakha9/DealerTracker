import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DealerOn extends WebEngine
{	
	private static List<String> VINs;
	
	DealerOn()
	{
		VINs = new ArrayList<>();
	}

	public static void main(String[] args)
	{
		List<Vehicle> vehicleList = new ArrayList<>();
		
		DealerOn vehicleFinder = new DealerOn();
		
		long startTime = System.currentTimeMillis();
		
		vehicleList = vehicleFinder.searchVehicles("https://www.lutherbrookdalehonda.com", "/searchnew.aspx?pn=100", "&pt=", "Vehicles)", 1, 1);
		//vehicleList = vehicleFinder.searchVehicles("https://www.lutherbrookdalehonda.com", "/searchnew.aspx?pn=100&Model=", "&pt=", "Vehicles)", 1, 1);
		//vehicleList = vehicleFinder.searchVehicles("https://www.schmelzvw.com", "/searchnew.aspx?pn=100", "&pt=", "Vehicles)", 1, 1);
		//vehicleList = vehicleFinder.searchVehicles("https://www.tomkadlec.com", "/searchused.aspx?pn=1000", "?pt=", "Vehicles)", 1, 1);
		
		System.out.println("Run time: " + ((System.currentTimeMillis() - startTime)/1000));
		
		System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList, 80));
		//System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList));
		
		Utils.dumpVehiclesToFile(vehicleList);
		//Utils.dumpDataToFile(lines);

	} /* main */
	
	/*@Override
	protected List<Vehicle> searchVehicles(String urlStrBase, String urlStrFinalDest, String expNumOfVehiclesKeyWord, String nextPage, int startPage, int pageInc)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		
		int expVehicleCount = 0;

		vehicles.addAll(searchVehiclesCustom(urlStrBase, urlStrFinalDest + "&Year=2017", expNumOfVehiclesKeyWord, nextPage, startPage, pageInc));
		System.out.println("Vehicles found FWD: " + vehicles.size());
		if (this.expectedVehicleCount > 0)
			expVehicleCount += this.expectedVehicleCount;
		
		vehicles.addAll(searchVehiclesCustom(urlStrBase, urlStrFinalDest + "&Year=2018", expNumOfVehiclesKeyWord, nextPage, startPage, pageInc));
		System.out.println("Vehicles found AWD: " + vehicles.size());
		if (this.expectedVehicleCount > 0)
			expVehicleCount += this.expectedVehicleCount;
		
		/*vehicles.addAll(searchVehiclesCustom(urlStrBase, urlStrFinalDest + "&DriveTrainType=4WD", expNumOfVehiclesKeyWord, nextPage, startPage, pageInc));
		System.out.println("Vehicles found 4WD: " + vehicles.size());
		if (this.expectedVehicleCount > 0)
			expVehicleCount += this.expectedVehicleCount;
		
		vehicles.addAll(searchVehiclesCustom(urlStrBase, urlStrFinalDest + "&DriveTrainType=RWD", expNumOfVehiclesKeyWord, nextPage, startPage, pageInc));
		System.out.println("Vehicles found RWD: " + vehicles.size());
		if (this.expectedVehicleCount > 0)
			expVehicleCount += this.expectedVehicleCount;

		this.expectedVehicleCount = expVehicleCount;
		return vehicles;
	} /* searchVehicles */
	
	/*protected List<Vehicle> searchVehiclesCustom(String urlStrBase, String urlStrFinalDest, String nextPage, String expNumOfVehiclesKeyWord, int startPage, int pageInc)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		
		int previousListLength, currentListLength;
		int pageNum = startPage;
		String urlString;
		
		setExpVehicleCntKeyWord(expNumOfVehiclesKeyWord);
		this.expectedVehicleCount = -1; // this indicates undetermined vehicle count
		
		do
		{
			previousListLength = vehicles.size();
			
			if (pageNum > 1)
				urlString = urlStrBase + urlStrFinalDest + nextPage + pageNum;
			else // first page
				urlString = urlStrBase + urlStrFinalDest;
			
			this.pageReadAttempts = 0;
	
			vehicles.addAll(searchVehiclesOnPage(urlString));
			
			currentListLength = vehicles.size();
			
			System.out.println("Vehicles found: " + currentListLength);

			pageNum += pageInc;
			
		// First two conditions are only used by DEALERCOM
		// Set to TRUE for other engines
		// When page number overcomes number of vehicles, page defaults to one
		// Make sure we don't read the content indefinitely
			
		// 3rd condition is satisfied when retry of page read failed 3 times in a row
		} while ( !this.firstVINfoundAgain && this.nextPageFound &&
				(previousListLength < vehicles.size()) && (currentListLength < VEHICLE_LIMIT));
		
		System.out.println("Expected vehicles count: " + this.expectedVehicleCount);
		System.out.println("URL: " + urlString);

		return vehicles;
	} /* searchVehiclesCustom */

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
		
		java.net.URL url;
		Scanner input;
		String line = "";
		
		Document doc;
		
		URLConnection yc;
		
		//System.out.println("URL: " + urlStr);
		
		try
		{
			url = new java.net.URL(urlStr);
			//input = new Scanner(url.openStream());
			yc = url.openConnection();
			
			yc.setUseCaches(false);
			yc.connect();
			input = new Scanner(new InputStreamReader(yc.getInputStream()));
		}
		catch (Exception ex)
		{
			return retrySearchOnPage(urlStr);
		}
		
		while (input.hasNext())
		{
			// Read next line
			line = input.nextLine();
				
			// -1 indicates that search is in progress and going as expected
			if (this.expectedVehicleCount == -1)
				this.expectedVehicleCount = findVehicleCount(line);
			
			// Look for vehicle description
			if (line.contains("data-name"))
			{
				doc = Jsoup.parse(line);
				
				Elements a = doc.getElementsByAttributeStarting("data-name");
				
				if (a.size() > 0)
				{			    
				    try
				    {
				    	String desc = a.first().dataset().get("name");
				    	if (Utils.hasModelYear(desc))
						{
							foundVehicle.setAndParseDescription(desc);
							
							// Store the vehicle if VIN has been found
							if (!foundVehicle.getVIN().isEmpty())
							{
								vehicles.add(foundVehicle);
								
								// Empty object for the next vehicle
								foundVehicle = new Vehicle();
							}
						}
				    }
				    catch (Exception ex)
				    {
				    	// Unreliable data, discard
				    	System.out.println("DEALERON data-name parsing error in line:\n" + line);
				    	System.out.println("Found vehicle details: " + foundVehicle.getVIN() + " " + foundVehicle.getDescription());
						foundVehicle = new Vehicle();
				    }
				} /* target element found */
			} /* line contains "data-name" */
			
			// Look for VIN
			if (line.contains("data-vin"))
			{
				// Extract VIN
				String vinCandidate = extractData(line, "vin");
				
				// Check for duplicates
				if (!VINs.contains(vinCandidate) && (vinCandidate.length() == 17))
				{
					foundVehicle.setVIN(vinCandidate);
					VINs.add(vinCandidate);
					
					vehicles.add(foundVehicle);
					
					// Empty object for the next vehicle
					foundVehicle = new Vehicle();
				} // valid non-duplicate VIN
			} /* line has VIN */
		} /* while input has lines */

			
		input.close(); // make sure buffer is closed
		
		return vehicles;
	} /* searchVehiclesOnPage */
	
	/*
	 * *** Key word and number of vehicles are expected to be on the same line ***
	 * *** Number of vehicles is followed by the key word                      ***
	 * *** textLine input is raw HTML line                                     ***
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
						String[] targetStrArray = elem.text().split(" ");
						
						int i;
						
						for (i = 1; i < targetStrArray.length; i++)
						{
							if (targetStrArray[i].contains(this.expVehicleCntKeyWord))
								break;
						}
						
						try
						{
							retVal = Integer.parseInt(targetStrArray[i-1].trim());
							//System.out.println("Found exp vehicle count: " + retVal);
						}
						catch (Exception ex)
						{
							//TODO log error
							System.out.println("Error parsing exp vehicle count");
							retVal = 0; // error, stop the search
						}
					} /* key word found */
				} /* html tag */
			} /* element has text */
		} /* for each element */
		
		return retVal;
	} /* findVehicleCount */
	
	private static String extractData(String dataLine, String dataName)
	{
		String retVal = "";
		
		String[] wordsAll = dataLine.trim().split(" ");
		
		if (wordsAll.length > 1)
		{
			String[] wordsSelected;
			
			for (int i = 0; i < wordsAll.length; i++)
			{
				if (wordsAll[i].contains("data-" + dataName))
				{
					wordsSelected = wordsAll[i].trim().split("=");
												
					if (wordsSelected.length > 1)
					{
						retVal = wordsSelected[1].replace("'", "");
						//System.out.println(retVal);
					} /* there is at least one word following data element name */
				} /* candidate string found */
			} // check all words in the line
		} // more than one word in line
		
		return retVal;
	} // extractData
}
