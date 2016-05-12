package tera.gameserver.model.items;

/**
 * Перечисление ранков итемов.
 * 
 * @author Ronn
 */
public enum Rank
{
	COMMON("common"),
	UNCOMMON("uncommon"),
	RARE("rare"),
	EPIC("epic"),
	UNKNOWN("unknown"),
	UNKNOW("unknow");
	
	/**
	 * Получение нужного ранка по названию в xml.
	 * 
	 * @param name название в xml.
	 * @return соответствующий ранк.
	 */
	public static Rank valueOfXml(String name)
	{
		for(Rank type : values())
			if(type.getXmlName().equals(name))
				return type;
		
		throw new IllegalArgumentException(name);
	}

	/** хмл название */
	private String xmlName;
	
	private Rank(String xmlName)
	{
		this.xmlName = xmlName;
	}

	/**
	 * @return xml название.
	 */
	public final String getXmlName()
	{
		return xmlName;
	}
}
