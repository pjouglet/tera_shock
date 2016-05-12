package tera.gameserver.model.items;

import tera.gameserver.model.equipment.SlotType;

/**
 * Перечисление типов обычных итемов.
 * 
 * @author Ronn
 */
public enum CommonType
{
	/** деньги */
	MONEY("money", SlotType.NONE),
	/** банки */
	POTION("potion", SlotType.NONE),
	/** свиток */
	SCROLL("scroll", SlotType.NONE),
	/** квест */
	QUEST("quest", SlotType.NONE),
	/** материал */
	MATERIAL("material", SlotType.NONE),
	/** упаковка */
	PACKAGE("package", SlotType.NONE),
	/** самоактивирующиеяся */
	HERB("herb", SlotType.NONE),
	/** остальные */
	OTHER("other", SlotType.NONE);
	
	/**
	 * Получение нужного кинда по названию в xml.
	 * 
	 * @param name название в xml.
	 * @return соответствующий кинд.
	 */
	public static CommonType valueOfXml(String name)
	{
		for(CommonType type : values())
			if(type.getXmlName().equals(name))
				return type;
		
		throw new IllegalArgumentException();
	}
	/** название в хмл */
	private String xmlName;
	
	/** тип слота, куда вставляется итем */
	private SlotType slot;

	/**
	 * @param xmlName название в хмл.
	 * @param slot слот, в который можно одеть.
	 */
	private CommonType(String xmlName, SlotType slot)
	{
		this.xmlName = xmlName;
		this.slot = slot;
	}
	
	/**
	 * @return слот, в который вставляется итем.
	 */
	public final SlotType getSlot()
	{
		return slot;
	}

	/**
	 * @return the xmlName
	 */
	public final String getXmlName()
	{
		return xmlName;
	}
}
