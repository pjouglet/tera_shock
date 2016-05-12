package tera.gameserver.model.items;

/**
 * Перечисление типов кристалов.
 * 
 * @author Ronn
 */
public enum CrystalType
{
	WEAPON("weapon"),
	ARMOR("armor");
	
	/**
	 * Получение нужного типа по названию в xml.
	 * 
	 * @param name название в xml.
	 * @return соответствующий тип.
	 */
	public static CrystalType valueOfXml(String name)
	{
		for(CrystalType type : values())
			if(type.getXmlName().equals(name))
				return type;
		
		throw new IllegalArgumentException("no enum " + name);
	}

	/** название в хмл */
	private String xmlName;

	/**
	 * @param xmlName название в хмл.
	 */
	private CrystalType(String xmlName)
	{
		this.xmlName = xmlName;
	}
	
	/**
	 * @return the xmlName
	 */
	public final String getXmlName()
	{
		return xmlName;
	}
}
