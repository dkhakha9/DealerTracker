import java.util.HashMap;

public class WebEngineFactorySingleton
{
	private static WebEngineFactorySingleton instance = null;
	
	private HashMap<String, WebEngine> webEngObjects;
	
	private WebEngineFactorySingleton()
	{
		webEngObjects = new HashMap<String, WebEngine>();
	}
	
	public static synchronized WebEngineFactorySingleton getInstance()
	{
		if (instance == null)
		{
			instance = new WebEngineFactorySingleton();
		}
		
		return instance;
	}
	
	// Returns instance of dealer specific web engine
	// Returns null if web engine name is unknown
	public WebEngine getWebEngineObject(String webEngineName)
	{
		WebEngine webEngObj = webEngObjects.get(webEngineName);
		
		if (webEngObj == null)
		{
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
			
			webEngObjects.put(webEngineName, webEngObj);
		}
		
		return webEngObj;
	} /* getWebEngine */
	
	public static VehicleSearchConfig getConfigNew()
	{
		VehicleSearchConfig webConfig = new VehicleSearchConfig();
		
		return webConfig;
	} /* getConfigNew */
}
