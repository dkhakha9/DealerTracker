
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class DealerCOMBuffer
{
	private String firstVIN;
	private boolean firstVINfoundAgain;
	private boolean nextPageFound;
	
	DealerCOMBuffer()
	{
		firstVIN = "";
		firstVINfoundAgain = true;
		nextPageFound = false;
	}
	
	public static void main(String[] args)
	{
		List<Vehicle> vehicleList = new ArrayList<>();
		
		DealerCOMBuffer vehicleFinder = new DealerCOMBuffer();
		
		long startTime = System.currentTimeMillis();
		
		vehicleList = vehicleFinder.searchVehicles("http://www.walserhonda.com/new-inventory/index.htm", "?start=");
		
		System.out.println("Run time: " + ((System.currentTimeMillis() - startTime)/1000));
		
		for (Vehicle vehicle: vehicleList)
		{
			vehicle.composeDescription();
			//System.out.println("VIN: " + vehicle.getVIN() + " " + vehicle.getDescription());
		}
		
	}
	
	public List<Vehicle> searchVehicles(String urlStr, String nextPage)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		
		int previousListLength;
		int pageNum = 0;
		String urlString;
		
		do
		{
			previousListLength = vehicles.size();
			
			if (pageNum > 1)
				urlString = urlStr + nextPage + pageNum;
			else // first page
				urlString = urlStr;
				
			vehicles.addAll(searchVehiclesOnPage(urlString));
			
			System.out.println("Vehicles found: " + vehicles.size());
			
			pageNum += 16;
			
			if (previousListLength == vehicles.size())
			{
				//TODO log error. New vehicles are obtained every time even with invalid URL
			}
		// When page number overcomes number of vehicles, page defaults to one
		// Make sure we don't read the content indefinitely
		} while ( !this.firstVINfoundAgain || this.nextPageFound);

		return vehicles;
	} /* searchVehicles */
	
	private List<Vehicle> searchVehiclesOnPage(String urlStr)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		Vehicle foundVehicle = new Vehicle();
		
		this.nextPageFound = false; // this page might be the last. Flag will be flipped otherwise below
		
		java.net.URL url;
		//Scanner input;
		BufferedReader input;
				
		try
		{
			url = new java.net.URL(urlStr);
			//input = new Scanner(url.openStream());
			input = new BufferedReader(new InputStreamReader(url.openStream()));
		}
		catch (Exception ex)
		{
			// TODO may want to throw an exception to indicate invalid URL
			
			System.out.println("URL or new BufferedReader exception");
			
			// Return empty list
			return vehicles;
		}
		
		String line = "";
		boolean readBufferSuccess = true;
		String[] vehicleCharacteristics;
		
		while (readBufferSuccess)
		{
			// Read next line
			try
			{
				line = input.readLine();
				if (line == null)
					readBufferSuccess = false; // no more data in the buffer
			}
			catch (Exception ex)
			{
				// TODO log an error
				System.out.println("Error reading buffer.");
				readBufferSuccess = false;
			}
			
			if (readBufferSuccess)
			{
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
						foundVehicle.setModel(vehicleCharacteristics[1].trim().replace("\"", "").replace(",", "")); // remove parenthesis and comma
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
						foundVehicle.setTrim(vehicleCharacteristics[1].trim().replace("\"", "").replace(",", "")); // remove parenthesis and comma
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
								break; // we are back to the first page - exit the loop
							}
							else
							{
								foundVehicle.setVIN(vin);
								vehicles.add(foundVehicle); // save the vehicle as soon as VIN has been found
								
								if (this.firstVIN.isEmpty())
									this.firstVIN = vin; // if this is the first VIN, save it for later
							}
						}
					}
				}
			} /* if (readBufferSuccess) */
		} /* while readBuffer */
		
		try
		{
			input.close(); // make sure buffer is closed
		}
		catch (Exception ex)
		{

		}
		
		//System.out.println("Completed.");
		
		return vehicles;
	} /* searchVehiclesOnPage */

}
