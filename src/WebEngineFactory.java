
public class WebEngineFactory
{
	// Returns instance of dealer specific web engine
	// Returns null if web engine name is unknown
	public static WebEngine getWebEngineObject(String webEngineName)
	{
		WebEngine webEngObj;
		
		if (webEngineName.equalsIgnoreCase("DEALERCOM"))
			webEngObj = new DealerCOM();
		else if (webEngineName.equalsIgnoreCase("DEALERFIRE"))
			webEngObj = new DealerFire();
		else if (webEngineName.equalsIgnoreCase("EBIZAUTOS"))
			webEngObj = new EBizAutos();
		else if (webEngineName.equalsIgnoreCase("DEALERON"))
			webEngObj = new DealerOn();
		else if (webEngineName.equalsIgnoreCase("DEALERINSPIRE"))
			webEngObj = new DealerInspire();
		else if (webEngineName.equalsIgnoreCase("AUTOWEBING"))
			webEngObj = new AutoWebing();
		else /* unknown value */
			webEngObj = null;
		
		return webEngObj;
	} /* getWebEngine */
	
	public static VehicleSearchConfig getConfigNew()
	{
		VehicleSearchConfig webConfig = new VehicleSearchConfig();
		
		return webConfig;
	} /* getConfigNew */
}
