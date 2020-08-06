
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DealerCOM extends WebEngine
{
	private String firstVIN;
	
	DealerCOM()
	{
		firstVIN = "";
		nextPageFound = false;
	}
	
	public static void main(String[] args)
	{
		List<Vehicle> vehicleList = new ArrayList<>();
		
		DealerCOM vehicleFinder = new DealerCOM();
		
		long startTime = System.currentTimeMillis();
		
		vehicleList = vehicleFinder.searchVehicles("http://www.walsernissanburnsville.com", "/new-inventory/index.htm?", "&start=", "Vehicles matching", 0, 16);
		//vehicleList = vehicleFinder.searchVehicles("http://www.lutherauto.com", "/used-inventory/index.htm?accountId=lutherbrookdalehonda", "&start=", "Vehicles matching", 0, 16);
		//vehicleList = vehicleFinder.searchVehicles("http://www.walserhonda.com", "/new-inventory/index.htm", "?start=", "Vehicles matching", 0, 16);
		
		System.out.println("Run time: " + ((System.currentTimeMillis() - startTime)/1000));
		
		//System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList, 60));
		System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList));
		
		Utils.dumpVehiclesToFile(vehicleList);
	}
	
	protected List<Vehicle> searchVehiclesOnPage(String urlStr)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		Vehicle foundVehicle = new Vehicle();
		
		this.nextPageFound = false; // this page might be the last. Flag will be flipped otherwise below
		
		java.net.URL url;
		Scanner input;
				
		try
		{
			url = new java.net.URL(urlStr);
			input = new Scanner(url.openStream());
		}
		catch (Exception ex)
		{
			return retrySearchOnPage(urlStr);
		}
		
		String line = "";
		
		String[] vehicleCharacteristics;
		
		while (input.hasNext())
		{
			// Read next line
			line = input.nextLine();
			
			// -1 indicates that search is in progress and going as expected
			if (this.expectedVehicleCount == -1)
				this.expectedVehicleCount = findVehicleCount(line);
			
			if (line.compareTo("Next") == 0)
				this.nextPageFound = true; // not the last page yet
			
			if (line.contains("\"make\""))
			{
				vehicleCharacteristics = line.split(":");
				if (vehicleCharacteristics.length == 2)
					foundVehicle.setMake(vehicleCharacteristics[1].trim().replace("\"", "").replace(",", "")); // remove parenthesis and comma
			}
			
			if (line.contains("\"model\""))
			{
				vehicleCharacteristics = line.split(":");
				if (vehicleCharacteristics.length == 2)
					foundVehicle.setModel(vehicleCharacteristics[1].trim().replace("\"", "").replace(",", "").replace("\\x20", " ")); // remove parenthesis and comma
			}
			
			if (line.contains("\"modelYear\""))
			{
				vehicleCharacteristics = line.split(":");
				if (vehicleCharacteristics.length == 2)
					foundVehicle.setYear(vehicleCharacteristics[1].trim().replace("\"", "").replace(",", "")); // remove parenthesis and comma
			}
			
			if (line.contains("\"trim\""))
			{
				vehicleCharacteristics = line.split(":");
				if (vehicleCharacteristics.length == 2)
					foundVehicle.setTrim(vehicleCharacteristics[1].trim().replace("\"", "").replace(",", "").replace("null", "NA").replace("\\x20", " ")); // remove parenthesis and comma
			}
			
			if (line.contains("\"exteriorColor\""))
			{
				vehicleCharacteristics = line.split(":");
				if (vehicleCharacteristics.length == 2)
					foundVehicle.setColor(vehicleCharacteristics[1].trim().replace("\"", "").replace(",", "").replace("null", "NA").replace("\\x20", " ")); // remove parenthesis and comma
			}
			
			if (line.contains("\"vin\""))
			{
				vehicleCharacteristics = line.split(":");
				if (vehicleCharacteristics.length == 2)
				{
					String vin = vehicleCharacteristics[1].trim().replace("\"", "").replace(",", ""); // remove parenthesis and comma
					
					if (vin.length() == 17)
					{							
						if (vin.compareTo(this.firstVIN) == 0)
						{
							this.firstVINfoundAgain = true;
							break; // we are back to the first page - exit the loop
						}
						else
						{
							foundVehicle.setVIN(vin);
							vehicles.add(foundVehicle); // save the vehicle as soon as VIN has been found
							
							foundVehicle = new Vehicle(); // clear the content for storing next vehicle
							
							if (this.firstVIN.isEmpty())
								this.firstVIN = vin; // if this is the first VIN, save it for later
						}
					}
				}
			} // line contains "vin"
		} /* while input has data */
		
		
		input.close(); // make sure resource is closed		
		
		//System.out.println("Completed.");
		
		return vehicles;
	} /* searchVehiclesOnPage */
}