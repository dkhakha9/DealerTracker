
public class SearchParams
{
	public final String urlStrBase;
	public final String urlStrFinalDest;
	public final String nextPage;
	public final String expNumOfVehiclesKeyWord;
	public final int startPage;
	public final int pageInc;
	
	SearchParams(String urlStrBase, String urlStrFinalDest, String nextPage, String expNumOfVehiclesKeyWord, String startPage, String pageInc)
	{
		this.urlStrBase = urlStrBase;
		this.urlStrFinalDest = urlStrFinalDest;
		this.nextPage = nextPage;
		this.expNumOfVehiclesKeyWord = expNumOfVehiclesKeyWord;
		this.startPage = Integer.parseInt(startPage);
		this.pageInc = Integer.parseInt(pageInc);
	}
	
	public String getURL()
	{
		return (urlStrBase + urlStrFinalDest);
	}
}
