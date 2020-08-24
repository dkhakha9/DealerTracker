import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

public class DealerInspire extends WebEngine
{
	private static List<String> VINs;
	
	DealerInspire()
	{
		VINs = new ArrayList<>();
	}
	
	public static void main(String[] args)
	{
		/*ArrayList<String> test = new ArrayList<String>();
		test.add("New");
		test.add("2017");
		test.add("Honda");
		System.out.println(test.toString().replace("[", "").replace("]", "").replace(",", ""));
		test.remove(0);
		System.out.println(test.toString().replace("[", "").replace("]", "").replace(",", ""));*/
		
		List<Vehicle> vehicleList = new ArrayList<>();
		
		DealerInspire vehicleFinder = new DealerInspire();
		
		long startTime = System.currentTimeMillis();
		
		//vehicleList = vehicleFinder.searchVehicles("https://www.invergrovehonda.com", "/used-vehicles/", "NA", "Matching Vehicles", 1, 1);
		//vehicleList = vehicleFinder.searchVehicles("https://www.lutherbrookdalehonda.com/searchused.aspx?pn=1000", "?pt=", "Vehicles)", 1, 1);
		
		System.out.println("Run time: " + ((System.currentTimeMillis() - startTime)/1000));
		
		System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList, 80));
		//System.out.println("Data is valid: " + vehicleFinder.dataIsValid(vehicleList));
		
		Utils.dumpVehiclesToFile(vehicleList);
	}
	
	@Override
	protected List<Vehicle> searchVehicles(SearchParams webParams)
	{
		List<Vehicle> vehicles = new ArrayList<>();
		
		int previousListLength, currentListLength;
		int pageNum = webParams.startPage;
				
		setExpVehicleCntKeyWord(webParams.expNumOfVehiclesKeyWord);
		this.expectedVehicleCount = -1; // this indicates undetermined vehicle count
		
		// Find total vehicle count
		searchVehiclesOnPage(webParams.urlStrBase + webParams.urlStrFinalDest);
		
		do
		{
			previousListLength = vehicles.size();
			
			this.pageReadAttempts = 0;
	
			vehicles.addAll(searchVehiclesOnPageAJAXVersion(webParams.urlStrBase, webParams.urlStrFinalDest, pageNum));
			
			currentListLength = vehicles.size();
			
			//System.out.println("Vehicles found: " + currentListLength);

			pageNum += webParams.pageInc;

		} while ((previousListLength < vehicles.size()) && (currentListLength < VEHICLE_LIMIT));
		
		//System.out.println("Expected vehicles count: " + this.expectedVehicleCount);

		return vehicles;
	} /* searchVehicles */
	
	// This function is re-purposed for just finding a vehicle count
	protected List<Vehicle> searchVehiclesOnPage(String urlStr)
	{
		List<Vehicle> vehicles = new ArrayList<>();
				
		URL url;
		Scanner input;
		String line;
		
		URLConnection yc;
		
		try
		{
			url = new java.net.URL(urlStr);
			
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
			line = input.nextLine();
			
			// -1 indicates that search is in progress and going as expected
			if (this.expectedVehicleCount == -1)
				this.expectedVehicleCount = findVehicleCount(line);
			else
				break; // vehicle count found, exit
		} /* while input has lines */
			
		input.close(); // make sure resource is closed
		
		// Always return empty list
		return vehicles;
	} /* searchVehiclesOnPage */
	
	protected List<Vehicle> searchVehiclesOnPageAJAXVersion(String urlStrBase, String urlStrFinalDest, int pageNum)
	{
		List<Vehicle> vehicles = new ArrayList<>();
				
		URL url;
		Scanner input;
		String rawData, line;
		
		//System.out.println(urlStrBase + urlStrFinalDest);

		try
		{
			String urlParameters  = "action=im_ajax_call&perform=get_results&_post_id=4&page=" + pageNum + "&show_all_filters=false&_referer=" + urlStrFinalDest;
			byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
			int    postDataLength = postData.length;
			//String request        = urlStrBase;
			url                   = new URL( urlStrBase );
			HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
			conn.setDoOutput( true );
			conn.setInstanceFollowRedirects( false );
			conn.setRequestMethod( "POST" );
			conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty( "charset", "utf-8");
			conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
			conn.setUseCaches( false );
			DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
			wr.write( postData );
			wr.close();
			//System.out.println("POST Response Code :: " + conn.getResponseCode());
			input = new Scanner(new InputStreamReader(conn.getInputStream()));
		}
		catch (Exception ex)
		{
			System.out.println("Error sending AJAX: " + ex);
			return vehicles;
		}
		
		while (input.hasNext())
		{
			rawData = input.nextLine();

			// Content arrives in a single line
			
			//Utils.dumpLineToFile(line);
			
			//line.replace(" t hrkruijm6bjktc/", "").replace("\\n", "").replace("\\t", "");
			//Utils.dumpLineToFile(unescapeJava(line));
		
			BufferedReader reader = new BufferedReader(new StringReader(unescapeJava(rawData)));
			
			do
			{
				try
				{
					line = reader.readLine();
					//Utils.dumpLineToFile(line);
					if (line == null)
						break;
				}
				catch (Exception ex)
				{
					System.out.println("Error reading line: " + ex);
					return vehicles;
				}
				
				if (line.contains("data-vehicle=") && line.contains("\"vin\""))
				{
					Vehicle foundVehicle = parseVehicleData(line);
					
					if (foundVehicle != null)
					{
						String newVIN = foundVehicle.getVIN();
						if (!VINs.contains(newVIN) && (newVIN.length() == 17))
						{
							vehicles.add(foundVehicle);
							VINs.add(newVIN);
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
			} while (line != null);
		} /* while input has lines */
			
		input.close(); // make sure resource is closed
		
		return vehicles;
	} /* searchVehiclesOnPageAJAXVersion */
	
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
			
			if(vehicleCharacteristics[i].contains("\"vin\""))
			{
				parsedVehicle.setVIN(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"model\""))
			{
				parsedVehicle.setModel(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"make\""))
			{
				parsedVehicle.setMake(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"trim\""))
			{
				parsedVehicle.setTrim(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"model_year\""))
			{
				parsedVehicle.setYear(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			if(vehicleCharacteristics[i].contains("\"ext_color\""))
			{
				parsedVehicle.setColor(vehicleCharacteristics[i+1].replace("\"", "")); // remove parenthesis
				i = i + 2;
				continue;
			}
			
			i++;
		}
		
		return parsedVehicle;
	} /* parseVehicleData */
}
