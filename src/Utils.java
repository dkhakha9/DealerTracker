import java.io.FileWriter;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Utils
{
	public static void main(String[] args)
	{
		//System.out.println(isMultiWordModel("Sierra2500HD"));
		java.sql.Date prevDay = new java.sql.Date(new Date().getTime() - 24*3600*1000);
		Calendar currentDate = Calendar.getInstance();
		currentDate.get(Calendar.DAY_OF_WEEK);
		System.out.println(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
	}

	private static final ArrayList<String> multiWordModels = new ArrayList<String>(Arrays.asList(new String[]
			{"santafe",
			 "grandcherokee",
			 "yukonxl",
			 "sierra1500",
			 "sierra2500hd",
			 "sierra3500hd",
			 "grandwagoneer",
			 "townand",
			 "grandcaravan"}));
	
	public static void dumpVehiclesToFile(List<Vehicle> vehicleList)
	{
		try
		{
			FileWriter outFile = new FileWriter("data.txt", true);
			
			for (Vehicle vehicle: vehicleList)
			{
				//vehicle.composeDescription();
				//System.out.println("VIN: " + vehicle.getVIN() + " " + vehicle.getDescription());
				outFile.write(vehicle.getVIN() + " | " + vehicle.getYear() + " | " + vehicle.getMake() + " | " + vehicle.getModel() + " | " + vehicle.getTrim() + " | " + vehicle.getColor() + System.getProperty("line.separator"));
			}
			
			outFile.close();
		}
		catch (Exception ex)
		{
			System.out.println("File writing failed");
		}
	} /* dumpVehiclesToFile */
	
	public static void dumpDataToFile(Set<String> data)
	{
		try
		{
			FileWriter outFile = new FileWriter("data.txt", true);
			
			for (String dataLine: data)
			{
				//vehicle.composeDescription();
				//System.out.println("VIN: " + vehicle.getVIN() + " " + vehicle.getDescription());
				outFile.write(dataLine + System.getProperty("line.separator"));
			}
			
			outFile.close();
		}
		catch (Exception ex)
		{
			System.out.println("File writing failed");
		}
	} /* dumpDataToFile */
	
	public static void dumpLineToFile(String line)
	{
		try
		{
			FileWriter outFile = new FileWriter("data.txt", true);
			
			outFile.write(line + System.getProperty("line.separator"));
			
			outFile.close();
		}
		catch (Exception ex)
		{
			System.out.println("File writing failed");
		}
	} /* dumpDataToFile */
	
	/*
	 * Returns TRUE is the input has at least 3 words and the first word is 4 digit year
	 */
	public static boolean hasModelYear (String str)
	{
		String[] words = str.split(" ");
		
		boolean retVal = true;
		
		if (words.length > 2)
		{
			// Word 1 - Year
			
			// Should be 4 digit number
			if (words[0].length() != 4)
				return false;
			
			// Should be less than or equal to current year + 1 and greater than 1980
			try
			{
				int year = Integer.parseUnsignedInt(words[0]);
				if ((year > (Year.now().getValue() + 1)) || (year < 1980))
					retVal = false;
			}
			catch (NumberFormatException ex)
			{
				// Not a number
				retVal = false;
			}
		}
		else /* not enough words */
		{
			retVal = false;
		} /* check number of words */
		
		return retVal;
	} /* hasModelYear */
	
	public static boolean hasModelYear(ArrayList<String> words)
	{
		if (words.size() < 3)
		{
			return false;
		}
		else if (hasModelYear(words.toString().replace("[", "").replace("]", "").replace(",", "")))
		{
			return true;
		}
		else
		{
			words.remove(0);
			return hasModelYear(words);
		}
	} /* hasModelYear(ArrayList<String> words) */
	
	public static boolean isMultiWordModel(String wd)
	{
		return (multiWordModels.contains(wd.toLowerCase()));
	} /* isMultiWordModel */
	
	public static boolean isEqualWithinTolerance(int num1, int num2, int tol)
	{
		if (Math.abs(num1 - num2) <= tol )
		{
			return true;
		}
		else
		{
			return false;
		}
	} /* isEqualWithinTolerance */
	
	// Returns text of all elements after parsing line using Jsoup
	public static String getElementText(String line)
	{
		String retVal = "";
		
		Document doc = Jsoup.parse(line);

		Elements allElements = doc.getAllElements();
		
		for (Element elem: allElements)
		{
			if (elem.hasText())
			{
				// Jsoup wraps HTML tag around each line. This tag now has text of all children.
				if (elem.tagName().compareTo("html") == 0)
				{
					retVal = elem.text();
				}
			}
		}
		
		return retVal;
	} /* getElementText */
}
