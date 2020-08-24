import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class WebEngine
{
	protected int expectedVehicleCount;
	protected String expVehicleCntKeyWord;
	protected int pageReadAttempts;
	
	// Only used by DEALERCOM
	// Needed for searchVehicles function compatibility
	protected boolean firstVINfoundAgain;
	protected boolean nextPageFound;
	
	// If vehicle count reached this number, it means that search is in infinite loop
	public static final int VEHICLE_LIMIT = 800;
	
	WebEngine()
	{
		expVehicleCntKeyWord = "";
		expectedVehicleCount = -1; // this indicates undetermined vehicle count
		pageReadAttempts = 0;
		firstVINfoundAgain = false;
		nextPageFound = true;
	}
	
	protected int getExpectedVehicleCount() {
		return expectedVehicleCount;
	}

	protected void setExpectedVehicleCount(int expectedVehicleCount) {
		this.expectedVehicleCount = expectedVehicleCount;
	}

	protected String getExpVehicleCntKeyWord() {
		return expVehicleCntKeyWord;
	}

	protected void setExpVehicleCntKeyWord(String expVehicleCntKeyWord) {
		this.expVehicleCntKeyWord = expVehicleCntKeyWord;
	}

	protected List<Vehicle> searchVehicles(SearchParams webParams)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		
		int previousListLength, currentListLength;
		int pageNum = webParams.startPage;
		String urlString;
		
		setExpVehicleCntKeyWord(webParams.expNumOfVehiclesKeyWord);
		this.expectedVehicleCount = -1; // this indicates undetermined vehicle count
		
		do
		{
			previousListLength = vehicles.size();
			
			if (pageNum > 1)
				urlString = webParams.urlStrBase + webParams.urlStrFinalDest + webParams.nextPage + pageNum;
			else // first page
				urlString = webParams.urlStrBase + webParams.urlStrFinalDest;
			
			this.pageReadAttempts = 0;
	
			vehicles.addAll(searchVehiclesOnPage(urlString));
			
			currentListLength = vehicles.size();
			
			//System.out.println("Vehicles found: " + currentListLength);

			pageNum += webParams.pageInc;
			
		// First two conditions are only used by DEALERCOM
		// Set to TRUE for other engines
		// When page number overcomes number of vehicles, page defaults to one
		// Make sure we don't read the content indefinitely
			
		// 3rd condition is satisfied when retry of page read failed 3 times in a row
		} while ( !this.firstVINfoundAgain && this.nextPageFound &&
				(previousListLength < vehicles.size()) && (currentListLength < VEHICLE_LIMIT));
		
		//System.out.println("Expected vehicles count: " + this.expectedVehicleCount);

		return vehicles;
	} /* searchVehicles */
	
	// Implemented by each subclass
	protected abstract List<Vehicle> searchVehiclesOnPage(String urlStr);
	
	/*
	 * *** Key word and number of vehicles are expected to be on the same line ***
	 * 
	 * Returns -1 if the keyWord is not found.
	 * Returns 0 if the keyWord is found, but the line does not
	 * contain valid integer.
	 * Returns integer number fetched after keyWord appearance.
	 */
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
	
	protected boolean dataIsValid(List<Vehicle> vehicleList, int minNumOfVehicles)
	{
		boolean retVal = false;
		
		int actNumOfVehicles = vehicleList.size();
		
		//if ((actNumOfVehicles == this.expectedVehicleCount) && (actNumOfVehicles > minNumOfVehicles))
		// Allow 3 missing vehicles
		if (Utils.isEqualWithinTolerance(this.expectedVehicleCount, actNumOfVehicles, 3) && (actNumOfVehicles > minNumOfVehicles))
		{
			List<String> uniqueVINs = new ArrayList<>();
			
			for (Vehicle vehicle : vehicleList)
				uniqueVINs.add(vehicle.getVIN());
			
			Set<String> vehicleSet = new HashSet<String>(uniqueVINs);
			
			//if(vehicleSet.size() == actNumOfVehicles)
			// Allow 2 duplicates
			if (Utils.isEqualWithinTolerance(vehicleSet.size(), actNumOfVehicles, 2))
			{
			    /* No duplicates, data is good */
				retVal = true;
			}
			else
			{
				System.out.println("Duplicate vehicles found: [Total] " + actNumOfVehicles + " :: " +  vehicleSet.size() + " [Unique]");
			}
		}
		else
		{
			System.out.println("Vehicle Count [Expected] " + this.expectedVehicleCount + " :: " + actNumOfVehicles + " [Actual]");
		}			

		//System.out.println("Vehicle Count [Expected] " + this.expectedVehicleCount + " :: " + actNumOfVehicles + " [Actual]");
		
		return retVal;
	} /* dataIsValid */
	
	protected boolean dataIsValid(List<Vehicle> vehicleList)
	{
		return dataIsValid(vehicleList, 0);
	} /* dataIsValid no min */
	
	// Retries search of vehicles on page up to 3 times using recursive call to searchVehiclesOnPage
	protected List<Vehicle> retrySearchOnPage(String urlStr)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		
		this.pageReadAttempts++;
		
		System.out.println("URL or new Scanner exception on attempt " + this.pageReadAttempts);
		
		if (this.pageReadAttempts < 3)
		{
			// retry
			return searchVehiclesOnPage(urlStr);
		}
		else
		{
			// Return empty list
			return vehicles;
		}
	} /* retrySearchOnPage */
} /* class WebEngine */
