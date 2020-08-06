
public class DealerTrackerApp
{
	public static void main(String[] args)
	{
		String sandboxPrefix = "";
		
		if (args.length == 1)
			sandboxPrefix = args[0];
		
		DataCollector DealerSpy = new DataCollector(sandboxPrefix);
		
		DealerSpy.getInventory();
	}
}
