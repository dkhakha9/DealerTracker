import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class fCl
{
	public static void main(String[] args)
	{
		//String line = "<li class=\"hidden-sm\"><strong>VIN #:</strong> 1HGCT1B87HA004289</li>";
		String line = "226 Matches							</div>";
		//String line = "<dl> <dt>Engine:</dt> <dd>2.4L I-4 cyl<span class=\"separator\">,</span></dd> <dt>Transmission:</dt> <dd>continuously variable automatic<span class=\"separator\">,</span></dd> <dt>Exterior Color:</dt> <dd>Modern Steel<span class=\"separator\">,</span></dd> <dt>Interior Color:</dt> <dd>Gray<span class=\"separator\">,</span></dd></dl><dl class='last'> <dt>Model Code:</dt> <dd>CR2F7HJW<span class=\"separator\">,</span></dd> <dt>Stock #:</dt> <dd>H014122</dd></dl><dl class='vin'><dt>VIN:</dt><dd>1HGCR2F73HA132230</dd></dl> <span class='ddc-more'>More<span class='hellip'>&hellip;</span></span>";
		
		Document doc = Jsoup.parse(line);
		
		Elements allElements = doc.getAllElements();
		
		String Engine = "N/A";
		
		for (Element elem: allElements)
		{
			if (elem.hasText())
			{
				//System.out.println(elem.text());
				if (elem.tagName().compareTo("html") == 0)
				{
					System.out.println(elem.text());
					
					if (inventoryScanner.isVehicle(elem.text()))
					{
						System.out.println("Vehicle!");
					}
					else
					{
						System.out.println("Not Vehicle...");
					}
					
					String[] wordsAll = elem.text().split(":|,|;");
					if (wordsAll.length > 1)
					{
						String[] wordsSelected;
						for (int i = 0; i < wordsAll.length; i++)
						{
							if (wordsAll[i].lastIndexOf("Engine") > -1)
							{
								wordsSelected = wordsAll[i+1].trim().split(" ");

								if (wordsSelected.length > 0)
								{
									Engine = "";
									// Grab up to 3 words following the "Engine"
									for (int j = 0; (j < wordsSelected.length) && (j < 3); j++)
									{
										if (j > 0)
											Engine = Engine + " ";
										
										Engine = Engine + wordsSelected[j];
									}
									System.out.println("Engine: " + Engine);
								}
							}
							
							if ((wordsAll[i].lastIndexOf("VIN #") > -1) || (wordsAll[i].lastIndexOf("VIN") > -1))
							{
								wordsSelected = wordsAll[i+1].trim().split(" ");
								if (wordsSelected.length > 0)
									System.out.println("VIN: " + wordsSelected[0]); //grab one word following the "VIN"
							}
						}
					}
				}
			}
		}
	}
	
}