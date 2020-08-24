import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataCollector
{
	private String sandBox;
	
	DataCollector(String sandBoxPrefix)
	{
		sandBox = sandBoxPrefix;
	}
	
	public void getInventory()
	{
		String dbURL = System.getenv("AHU");
		String dbUser = System.getenv("AHL");
		String dbPW = System.getenv("AHP");
		
		Connection connection;

		try
		{
			connection = DriverManager.getConnection(dbURL, dbUser, dbPW);
			
			connection.setAutoCommit(false);
		}
		catch(Exception ex)
		{
			System.out.println("Database is not available: " + ex);
			System.out.println(ex.getMessage());
			return;
		}
		
		if (sandBox.isEmpty())
			System.out.println("Connected successfully to production");
		else
			System.out.println("Connected successfully to sand box");
			
		collectData(connection);
		
		/*List<Vehicle> dealerStock = new ArrayList<>();
		
		dealerStock.add(new Vehicle("1HGCT1B88GA003361"));
		dealerStock.add(new Vehicle("1HGCT1B88GA003111"));
		//dealerStock.add(new Vehicle("1HGCT1B88GA001122"));
		//dealerStock.add(new Vehicle("1HGCT1B88GA003244"));  // duplicate vin in vehicles only
		//dealerStock.add(new Vehicle("1HGCT1B88GA003222"));  // duplicate vin in both
		dealerStock.add(new Vehicle("1HGCT1B88GA001133"));
		
		updateDealerStock(connection, 2, "sbnewcars", dealerStock);
		checkReturnUsed(connection, "sbusedcars", "1GCVKREC4FZ298516", 9);
		
		try
		{
			connection.commit();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to commit data: " + ex.getMessage());
		}*/
		
		try
		{
			connection.close();
		}
		catch(Exception ex)
		{
			System.out.println("Unable to close database connection: " + ex);
			System.out.println(ex.getMessage());
		}

	} /* getInventory */
	
	private void updateDealerStock(Connection dbConnection, int dealerID, String dbTable, List<Vehicle> dealerStock)
	{
		ResultSet lastKnownDealerStock = null;
		Statement getLastKnownDealerStock = null;
		
		try
		{
			getLastKnownDealerStock = dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			
			lastKnownDealerStock = getLastKnownDealerStock.executeQuery
					("select VIN, DATE_SOLD from " + dbTable + " where dealer_id = " + dealerID
					 + " and DATE_SOLD is null");
		}
		catch (Exception ex)
		{
			System.out.println("Unable to select from " + dbTable +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			return;
		}
		
		String currentVIN;
		boolean vehicleInStock;
		java.sql.Date transactDate;
		
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == 2)
			transactDate = new java.sql.Date(new Date().getTime() - 2*24*3600*1000); // Monday - go back two days (no Sunday transactions)
		else
			transactDate = new java.sql.Date(new Date().getTime() - 24*3600*1000); // Go back 1 day for all days other than Monday
		
		while (true)
		{
			try
			{
				if (!lastKnownDealerStock.next())
					break; // no more vehicles, exit the while loop
				
				currentVIN = lastKnownDealerStock.getString(1);
				//System.out.println("Processing VIN: " + currentVIN);
			}
			catch (Exception ex)
			{
				System.out.println("Unexpected error occured while iterating through dealer "
							+  dealerID + " stock in " + dbTable + ": " + ex.getMessage());
				return;
			}
			
			// Check if this vehicle is still in stock
			vehicleInStock = false;
			
			for (Vehicle vehicle : dealerStock)
			{
				if (currentVIN.compareTo(vehicle.getVIN()) == 0)
				{
					// Still in stock
					vehicleInStock = true;
					
					// Remove from the list
					dealerStock.remove(dealerStock.indexOf(vehicle));
					
					break; // exit the for loop
				}
			}
			
			if (!vehicleInStock)
			{
				// Mark as sold
				try
				{
					lastKnownDealerStock.updateDate(2, transactDate);
					lastKnownDealerStock.updateRow();
					//System.out.println("Sold vehicle detected for " + dealerID + ": " + currentVIN);
				}
				catch (Exception ex)
				{
					System.out.println("Unable to mark vehicle " + currentVIN + " as sold for dealer "
							+  dealerID + " in " + dbTable + ": " + ex.getMessage());
				}
			}					
		} /* loop through last known stock for a dealer */
				
		// Now dealerStock list has only new arrivals.
		// Put them into the database
		Statement insertNewArrivals = null;
		
		try
		{
			insertNewArrivals = dbConnection.createStatement();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to create stement for table " + dbTable +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			return;
		}
		
		//System.out.println("Processing new arrivals: " + dealerStock.size());
		
		short insertNum = 0;
		
		for (Vehicle vehicle : dealerStock)
		{
			//System.out.println("Insert into " + dbTable + " (VIN, DEALER_ID, DATE_ARRIVED) Values ('"
			//		+ vehicle.getVIN() + "', " + dealerID + ", TO_DATE('" + transactDate.toString() + "', 'yyyy-mm-dd'))");
			
			//System.out.println("Insert into " + sandBox + "VEHICLES Values ('" + vehicle.getVIN() + "', '" +
			//			vehicle.getYear() + "', '" + vehicle.getMake() + "', '" + vehicle.getModel() + "', '" + vehicle.getTrim() + "')");
			
			try
			{
				insertNum = 1;
				
				insertNewArrivals.executeUpdate("Insert into " + dbTable + " (VIN, DEALER_ID, DATE_ARRIVED) Values ('"
					+ vehicle.getVIN() + "', " + dealerID + ", TO_DATE('" + transactDate.toString() + "', 'yyyy-mm-dd'))");
				
				insertNum = 2;
				
				// Eliminate empty fields and limit string length to increase chances of Insert success
				vehicle.fillEmptyFields();
				
				String trim = vehicle.getTrim().substring(0, Math.min(50, vehicle.getTrim().length()));
				String color = vehicle.getColor().substring(0, Math.min(50, vehicle.getColor().length()));
				
				insertNewArrivals.executeUpdate("Insert into " + sandBox + "VEHICLES Values ('" + vehicle.getVIN() + "', '" +
						vehicle.getYear() + "', '" + vehicle.getMake() + "', '" + vehicle.getModel() + "', '" + trim + "', '" + color + "')");
			}
			catch (Exception ex)
			{
				// proceed after logging the error
				// this will be executed only for new cars
				if (insertNum == 1)
				{
					if (!checkReturn(dbConnection, dbTable, vehicle.getVIN(), dealerID))
					{
						//System.out.println("Unsuccessful returned vehicle");
					}
					else
					{
						// Normal Operation
					}
				}
				else
				{
					System.out.println("Unable to execute insert " + insertNum + " of new vehicle " + vehicle.getVIN() +
							" for dealer " +  dealerID + ": " + ex.getMessage());
				}
			} /* try to insert vehicles */
		} /* for each new arrival */

		try
		{
			dbConnection.commit();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to commit data: " + ex.getMessage());
		}
		
		try
		{
			getLastKnownDealerStock.close();
			insertNewArrivals.close();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to close statements in updateDealerStock: " + ex.getMessage());
		}
	} /* updateDealerStock */
	
	// Returns TRUE if legitimate return or dealer swap
	private boolean checkReturn(Connection conn, String tableName, String vin, int dealerID)
	{
		boolean retVal = true;
		
		Statement backInStock = null;
		ResultSet resultSet = null;
		
		try
		{
			backInStock = conn.createStatement();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to create statement in checkReturn for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			return false;
		}
		
		try
		{
			resultSet = backInStock.executeQuery
				("select dealer_id, date_sold from " + tableName + " where vin = '" + vin + "'");
		}
		catch (Exception ex)
		{
			System.out.println("Unable to execute query in checkReturn for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			return false;
		}
		
		try
		{
			// Result should contain one record
			if (resultSet.next())
			{
				Statement helloAgain = conn.createStatement();
				
				if (resultSet.getInt(1) == dealerID)
				{
					// Process return
					helloAgain.executeUpdate("Update " + tableName + " set date_sold = null where vin = '" + vin + "'");
				}
				else
				{
					// Dealer swap					
					if (resultSet.getDate(2) != null)
					{
						// Vehicle left one dealer and appeared at another
						helloAgain.executeUpdate("Update " + tableName + " set dealer_id = " + dealerID + ", date_arrived = TO_DATE('" + resultSet.getDate(2).toString() + "', 'yyyy-mm-dd'), date_sold = null where vin = '" + vin + "'");
						
						// Insert into dealer swap table
						helloAgain.executeUpdate("Insert into " + sandBox + "DEALERSWAP Values ('" + vin + "', " + resultSet.getInt(1) + ", " + dealerID + ", TO_DATE('" + resultSet.getDate(2).toString() + "', 'yyyy-mm-dd'))");
					}
					else
					{
						System.out.println("Possesion conflict " + vin + ", dealer 1 = " + resultSet.getInt(1) + ", dealer 2 = " + dealerID);
						retVal = false;
					}
				}
				
				helloAgain.close();
			}
			else
			{
				// Unexpected result
				retVal = false;
				System.out.println("Result set in checkReturn returned zero rows for " + vin + ", dealer = " + dealerID);
			} /* resultSet.next() */
		}
		catch (Exception ex)
		{
			System.out.println("Unable to execute update in checkReturn for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			retVal = false;
		}
		
		try
		{
			resultSet.close();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to close resultSet in checkReturn for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			retVal = false;
		}
		
		try
		{
			backInStock.close();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to close backInStock statement in checkReturn for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			retVal = false;
		}
		
		return retVal;
	} /* checkReturn */
	
	// Returns TRUE if legitimate return or dealer swap
	private boolean checkReturnUsed(Connection conn, String tableName, String vin, int dealerID)
	{
		boolean retVal = false;
		
		Statement backInStock = null;
		ResultSet resultSet = null;
		int recordCount = 0;
		
		try
		{
			backInStock = conn.createStatement();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to create statement in checkReturnUsed for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			return false;
		}
		
		try
		{
			resultSet = backInStock.executeQuery
				("select date_arrived, date_sold from " + tableName +
				 " where vin = '" + vin + "'" + " and dealer_id = " + dealerID +
				 " order by date_arrived DESC");
		}
		catch (Exception ex)
		{
			System.out.println("Unable to execute query in checkReturnUsed for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			return false;
		}
		
		try
		{
			// Compiler wants in to be initialized. Set to year ago
			java.sql.Date dateReturned = new java.sql.Date(new Date().getTime() - (long)365*24*3600*1000);
			
			// Looking for the latest two records
			while (resultSet.next())
			{
				recordCount++;
				
				if (recordCount == 1) // newest record in used
				{
					// if date_sold is empty save date_arrived else return false
					if (resultSet.getDate(2) == null)
					{
						dateReturned = resultSet.getDate(1);
					}
					else
					{
						System.out.println("[WARNING] Unknown business case in checkReturnUsed: " +
											vin + " was not inserted as new arrival for dealer " + 
											dealerID + " but considered as return candidate.");
						resultSet.close();
						backInStock.close();
						return false;
					}
				}
				else if (recordCount == 2)
				{
					// if (saved_date_arrived - date_sold) < 31 days process return else return false
					java.sql.Date dateDissapeared = resultSet.getDate(2);
					java.sql.Date dateArrived = resultSet.getDate(1);
					
					if (dateDissapeared != null)
					{
						if (
							((dateReturned.getTime() - dateDissapeared.getTime()) < (long)31*24*3600*1000) &&
						     (dateReturned.getTime() >= dateDissapeared.getTime())
						   )
						{
							// Legitimate return
							Statement helloAgain = conn.createStatement();
							
							helloAgain.executeUpdate("delete from " + tableName + " where vin = '" + vin + "'" + " and date_sold is null and dealer_id = " + dealerID + " and date_arrived = TO_DATE('" + dateReturned.toString() + "', 'yyyy-mm-dd')");
							
							helloAgain.executeUpdate("Update " + tableName + " set date_sold = null where vin = '" + vin + "'" + " and dealer_id = " + dealerID + " and date_arrived = TO_DATE('" + dateArrived.toString() + "', 'yyyy-mm-dd')");
							
							helloAgain.close();
							
							retVal = true;
						}
						else
						{
							retVal = false;
						}
					}
					else
					{
						System.out.println("[ERROR] checkReturnUsed: " +
								vin + " has two arrival records for " + 
								dealerID + ". Inventory scan should not allow this to happen.");
						retVal = false;
					}
					
					break;
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println("Unable to iterate through result set in checkReturnUsed for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			retVal = false;
		}
		
		if (recordCount == 0)
		{
			System.out.println("[WARNING] checkReturnUsed: " +
					vin + " has no records of arrival to dealer " + 
					dealerID + " but considered as return candidate.");
			retVal = false;
		}
		
		try
		{
			resultSet.close();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to close resultSet in checkReturnUsed for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			retVal = false;
		}
		
		try
		{
			backInStock.close();
		}
		catch (Exception ex)
		{
			System.out.println("Unable to close backInStock statement in checkReturnUsed for table " + tableName +
								" for dealer " +  dealerID + ": " + ex.getMessage());
			retVal = false;
		}
		return retVal;
	} /* checkReturnUsed */
	
	private void collectData(Connection connection)
	{		
		WebEngine dealerWebEngine;
		
		VehicleSearchConfig searchConfig = new VehicleSearchConfig();
		
		List<Vehicle> dealerStock = new ArrayList<>();
		
		searchConfig.fetchSearchConfig(connection);
			
		//boolean deactivated = true;
			
		while (!searchConfig.allDealersProcessed())
		{
			if (searchConfig.isValid())
			{
				dealerWebEngine = WebEngineFactory.getWebEngineObject(searchConfig.getWebEngineName());
				
				//if ((dealerWebEngine == null) || (resultSet.getInt(1) < 40))
				//if ((dealerWebEngine == null) || (resultSet.getInt(1) != 12))
				if (dealerWebEngine == null)
					System.out.println("Unknown web engine: " + searchConfig.getWebEngineName());
				else
				{
					SearchParams newCars = searchConfig.getNew();
					
					System.out.println("Processing URL: " + newCars.getURL());
	
					// Search new
					dealerStock = dealerWebEngine.searchVehicles(newCars);
					
					//if (dealerWebEngine.dataIsValid(dealerStock, resultSet.getInt(3)))
					if (dealerWebEngine.dataIsValid(dealerStock))
					{
						System.out.println("[NEW] Data collected successfully");
						//dumpVehiclesToFile(dealerStock);
						updateDealerStock(connection, searchConfig.getDealerID(), sandBox + "NEWCARS", dealerStock);
					}
					else
					{
						System.out.println("[NEW] Invalid data, ignore");
					}
					
					SearchParams usedCars = searchConfig.getUsed();
					
					System.out.println("Processing URL: " + usedCars.getURL());
					
					// Search used
					dealerStock = dealerWebEngine.searchVehicles(usedCars);
					
					if (dealerWebEngine.dataIsValid(dealerStock))
					{
						System.out.println("[USED] Data collected successfully");
						
						updateDealerStock(connection, searchConfig.getDealerID(), sandBox + "USEDCARS", dealerStock);
					}
					else
					{
						System.out.println("[USED] Invalid data, ignore");
					}
				} /* web engine determined */
			}
			else
			{
				System.out.println("Invalid dealer config, ignore");
			}
			
			searchConfig.nextDealer();
		} /* loop through all dealers */
	} /* collectData */
} /* DataCollector */
