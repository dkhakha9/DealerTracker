import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AutoWebing extends WebEngine
{

	public static void main(String[] args)
	{
		List<Vehicle> vehicleList = new ArrayList<>();
		
		AutoWebing vehicleFinder = new AutoWebing();
		
		long startTime = System.currentTimeMillis();
		
		//vehicleList = vehicleFinder.searchVehicles("https://www.buerklehonda.com", "/inventory/New/", "?page=", "Vehicles Found", 1, 1);
		//vehicleList = vehicleFinder.searchVehicles("https://www.buerklehonda.com", "/inventory/Used/", "?page=", "Vehicles Found", 1, 1);
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
		
		java.net.URL url;
		Scanner input;
				
		URLConnection yc;
		
		try
		{
			url = new java.net.URL(urlStr);
			
			yc = url.openConnection();
			
			yc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			
			yc.setUseCaches(false);
			yc.connect();
			input = new Scanner(new InputStreamReader(yc.getInputStream()));		
		}
		catch (Exception ex)
		{
			System.out.println(ex);
			return retrySearchOnPage(urlStr);
		}
		
		String line = "";
		String tempStr;
		
		String[] vehicleCharacteristics;
		
		while (input.hasNext())
		{
			// Read next line
			line = input.nextLine();
			
			// -1 indicates that search is in progress and going as expected
			if (this.expectedVehicleCount == -1)
				this.expectedVehicleCount = findVehicleCount(line);
			
			if (line.contains("name=\"year\""))
			{
				tempStr = Utils.getElementText(line);
				if (!tempStr.isEmpty())
					foundVehicle.setYear(tempStr);
				continue;
			}
			
			if (line.contains("name=\"model\""))
			{
				tempStr = Utils.getElementText(line);
				if (!tempStr.isEmpty())
					foundVehicle.setModel(tempStr);
				continue;
			}
			
			if (line.contains("name=\"make\""))
			{
				tempStr = Utils.getElementText(line);
				if (!tempStr.isEmpty())
					foundVehicle.setMake(tempStr);
				continue;
			}
			
			if (line.contains("name=\"trim\""))
			{
				tempStr = Utils.getElementText(line);
				if (!tempStr.isEmpty())
					foundVehicle.setTrim(tempStr);
				continue;
			}
			
			if (line.contains("\"#input_2_10\""))
			{
				vehicleCharacteristics = line.split("'");
				if (vehicleCharacteristics.length == 3)
				{
					String vin = vehicleCharacteristics[1].trim();
					
					if (vin.length() == 17)
					{
						foundVehicle.setVIN(vin);					
					}
				}
				continue;
			} // line contains "vin"
			
			if (line.contains("\"#input_2_15\""))
			{
				vehicleCharacteristics = line.split("'");
				if (vehicleCharacteristics.length == 3)
					foundVehicle.setColor(vehicleCharacteristics[1].trim());
				
				// VIN should be found at this point
				if (!foundVehicle.getVIN().isEmpty())
				{
					vehicles.add(foundVehicle); // save the vehicle
					foundVehicle = new Vehicle(); // clear the content for storing next vehicle
				}
				else
				{
					input.close(); // make sure resource is closed		
					
					System.out.println("AutoWebing Error: VIN is empty after Exterior Color extracted.");
					
					return vehicles;
				}
			}
		} /* while input has data */
		
		
		input.close(); // make sure resource is closed		
		
		//System.out.println("Completed.");
		
		return vehicles;
	} /* searchVehiclesOnPage */
}
