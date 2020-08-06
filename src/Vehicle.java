
public class Vehicle
{
	private String VIN;
	private String Make;
	private String Model;
	private String Year;
	private String Trim;
	private String Color;
	
	// Raw string "Year Make Model Trim ... " obtained from source
	// e.g."2017 Honda Accord EX"
	private String Description;
	
	Vehicle()
	{
		VIN = "";
		Make = "";
		Model = "";
		Year = "";
		Trim = "";
		Color = "";
	}
	
	Vehicle(String vin)
	{
		VIN = vin;
		Make = "NA";
		Model = "NA";
		Year = "NA";
		Trim = "NA";
		Color = "NA";
	}
	
	public String getDescription() {
		return Description;
	}
	
	public void setDescription(String description)
	{
		Description = description;
	}

	public void setAndParseDescription(String description)
	{
		Description = description;
		
		parseDescription(description);
	}
	
	public void composeDescription()
	{
		setDescription(this.Year + " " + this.Make + " " + this.Model + " " + this.Trim + " " + this.Color);
	}

	public String getVIN() {
		return VIN;
	}
	
	public void setVIN(String vin) {
		VIN = vin;
	}

	public String getColor() {
		return Color;
	}

	public void setColor(String color) {
		Color = color;
	}

	public String getMake() {
		return Make;
	}

	public void setMake(String make) {
		Make = make;
	}

	public String getModel() {
		return Model;
	}

	public void setModel(String model) {
		Model = model;
	}

	public String getYear() {
		return Year;
	}

	public void setYear(String year) {
		Year = year;
	}

	public String getTrim() {
		return Trim;
	}

	public void setTrim(String trim)
	{
		Trim = trim.replace("'", ""); // this messes up SQL queries
		
		// Limit string length to DB field length
		
		if (Trim.length() > 50)
		{
			String[] trimWords = Trim.split(" ");
			
			if (trimWords.length > 3)
			{
				if ((trimWords[0].length() + trimWords[1].length() + trimWords[2].length()) < 50)
					Trim = trimWords[0] + " " + trimWords[1] + " " + trimWords[2];
				else
					Trim = "NA"; // unrealistic string, ignore
			}
		}
	} /* setTrim */
	
	public void fillEmptyFields()
	{
		if (Make.isEmpty())
			Make = "NA";
		
		if (Model.isEmpty())
			Model = "NA";
		
		if (Year.isEmpty())
			Year = "NA";
		
		if (Trim.isEmpty())
			Trim = "NA";
		
		if (Color.isEmpty())
			Color = "NA";
	} /* fillEmptyFields */
	
	private void parseDescription(String desc)
	{
		// Extract and set Year, Make, Model, Trim
		String[] words = desc.split(" ");
		
		if (words.length > 4)
		{
			setYear(words[0]);
			
			setMake(words[1]);
			
			if (Utils.isMultiWordModel(words[2] + words[3]))
			{
				setModel(words[2] + " " + words[3]);
				setTrim(words[4]);
			}
			else
			{						
				setModel(words[2]);
				setTrim(words[3] + " " + words[4]);
			}
		}
		else if (words.length == 4)
		{
			setYear(words[0]);
			
			setMake(words[1]);
			
			if (Utils.isMultiWordModel(words[2]+words[3]))
			{
				setModel(words[2] + " " + words[3]);
				// clear the value to remove leftovers from previously checked vehicle
				setTrim("");
			}
			else
			{						
				setModel(words[2]);
				setTrim(words[3]);
			}
		}
		else if (words.length == 3)
		{
			setYear(words[0]);
			
			setMake(words[1]);
						
			setModel(words[2]);
			
			// clear the value to remove leftovers from previously checked vehicle
			setTrim("");
		}
	} /* parseDescription */
}
