package tera.gameserver.model.items;

/**
 * Перечисление типов боундинга.
 * 
 * @author Ronn
 */
public enum BindType
{
	UNKNOWN("unknown"),
	UNKNOW("unknow"),
	NONE("none"),
	ON_PICK_UP("onPickUp"),
	ON_EQUIP("onEquip");
	
	/**
	 * Получение нужного боунд типа по названию в xml.
	 * 
	 * @param name название в xml.
	 * @return соответствующий боунд тип.
	 */
	public static BindType valueOfXml(String name)
	{
		for(BindType type : values())
			if(type.getXmlName().equals(name))
				return type;
		
		throw new IllegalArgumentException("no enum " + name);
	}
	
	/** xml название */
	private String xmlName;

	/**
	 * @param xmlName название в хмл.
	 */
	private BindType(String xmlName)
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
