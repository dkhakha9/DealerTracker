import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;

public class VehicleSearchConfig
{
	private ResultSet resultSet;
	private ResultSetMetaData resultMetaData;
	private HashMap<String, String> currentRow;
	private boolean allDealersProcessed;
	private boolean lastConfigValid;
	
	VehicleSearchConfig()
	{
		resultSet = null;
		resultMetaData = null;
		currentRow = null;
		allDealersProcessed = true;
		lastConfigValid = false;
	}
	
	public void fetchSearchConfig(Connection connection)
	{
		try
		{			
			Statement statement = connection.createStatement();
			
			resultSet = statement.executeQuery
					("select dealer_id, url, min_new, web_engine, url_new, url_used, url_nextpage, ttl_key_new, ttl_key_used, "
					 + "startpagenew, pageincnew, startpageused, pageincused "
					 + "from DEALERWEB left outer join WEBPARAMS "
					 + "on lower(WEBPARAMS.param_id) = lower(DEALERWEB.param_id)");
		}
		catch(Exception ex)
		{
			System.out.println("Error fetching dealer search config: " + ex);
			System.out.println(ex.getMessage());
			return;
		}
		
		try
		{
			resultMetaData = resultSet.getMetaData();
		}
		catch(Exception ex)
		{
			System.out.println("Error fetching metadata: " + ex);
			System.out.println(ex.getMessage());
			return;
		}
		
		this.nextDealer();
		
		/*
		1 - DEALER_ID
		2 - URL
		3 - MIN_NEW
		4 - WEB_ENGINE
		5 - URL_NEW
		6 - URL_USED
		7 - URL_NEXTPAGE
		8 - TTL_KEY_NEW
		9 - TTL_KEY_USED
		10 - STARTPAGENEW
		11 - PAGEINCNEW
		12 - STARTPAGEUSED
		13 - PAGEINCUSED
		*/
	} /* fetchSearchConfig */
	
	public void nextDealer()
	{
		boolean nextDealerAvailable;
		
		try
		{
			nextDealerAvailable = resultSet.next();
		}
		catch(Exception ex)
		{
			System.out.println("Error moving cursor to the next row of the fetched dealer config: " + ex);
			System.out.println(ex.getMessage());
			nextDealerAvailable = false;
		}
		
		if (nextDealerAvailable)
		{
			try
			{
				currentRow = resultSetRowToHashMap();
				lastConfigValid = true;
			}
			catch(Exception ex)
			{
				System.out.println("Error generating map from the current row of the fetched dealer config: " + ex);
				System.out.println(ex.getMessage());
				currentRow = null;
				lastConfigValid = false;
			}
		}
		
		allDealersProcessed = !nextDealerAvailable;
	} /* nextDealer */
	
	private HashMap<String, String> resultSetRowToHashMap() throws SQLException
	{
		String columnValue;
		
		int resultColumnCount = resultMetaData.getColumnCount();
		
		HashMap<String, String> row = new HashMap<String, String>(resultColumnCount);
		
		for (int columnIndex = 1; columnIndex <= resultColumnCount; columnIndex++)
		{			
			int sqlTypes = resultMetaData.getColumnType(columnIndex);
			
			if (sqlTypes == Types.INTEGER)
			{
				columnValue = String.valueOf(resultSet.getInt(columnIndex));
			}
			else
			{
				columnValue = resultSet.getString(columnIndex);
			}
			
			row.put(resultMetaData.getColumnName(columnIndex).toLowerCase(), columnValue);
		}
		
		return row;
	} /* resultSetRowToHashMap */
	
	public boolean allDealersProcessed()
	{
		return allDealersProcessed;
	}
	
	public boolean isValid()
	{
		return lastConfigValid;
	}
	
	public String getWebEngineName()
	{
		return currentRow.get("web_engine");
	}
	
	public int getDealerID()
	{
		return Integer.parseInt(currentRow.get("dealer_id"));
	}
	
	public SearchParams getNew()
	{
		return new SearchParams(currentRow.get("url"), currentRow.get("url_new"),
				currentRow.get("url_nextpage"), currentRow.get("ttl_key_new"), currentRow.get("startpagenew"), currentRow.get("pageincnew"));
	} /* getNew */
	
	public SearchParams getUsed()
	{
		return new SearchParams(currentRow.get("url"), currentRow.get("url_used"),
							currentRow.get("url_nextpage"), currentRow.get("ttl_key_used"), currentRow.get("startpageused"), currentRow.get("pageincused"));
	} /* getUsed */
}
