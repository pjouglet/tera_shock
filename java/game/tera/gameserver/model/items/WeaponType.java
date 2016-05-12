package tera.gameserver.model.items;

import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.playable.Player;

/**
 * Перечисление типов оружий.
 * 
 * @author Ronn
 */
public enum WeaponType
{
	/** дуалы */
	TWIN_SWORDS("twinSwords", PlayerClass.WARRIOR),
	/** копье */
	LANCE("lance", PlayerClass.LANCER),
	/** 2х ручный меч */
	GREATSWORD("greatSword", PlayerClass.SLAYER),
	/** 2х ручный блант */
	AXE("axe", PlayerClass.BERSERKER),
	/** лук */
	BOW("bow", PlayerClass.ARCHER),
	/** летающий диск сорка */
	DISC("disc", PlayerClass.SORCERER),
	/** большой посох */
	STAFF("staff", PlayerClass.PRIEST),
	/** маленький посох */
	SCEPTER("scepter", PlayerClass.MYSTIC);
	
	/**
	 * Получение нужного армор типа по названию в xml.
	 * 
	 * @param name название в xml.
	 * @return соответствующий армор тип.
	 */
	public static WeaponType valueOfXml(String name)
	{
		for(WeaponType type : values())
			if(type.getXmlName().equals(name))
				return type;
		
		throw new IllegalArgumentException();
	}
	
	/** название в хмл */
	private String xmlName;

	/** класс, который может экиперовать */
	private PlayerClass playerClass;
	
	/**
	 * @param xmlName название в хмл.
	 * @param playerClass класс игрока.
	 */
	private WeaponType(String xmlName, PlayerClass playerClass)
	{
		this.xmlName = xmlName;
		this.playerClass = playerClass;
	}
	
	/**
	 * @param player проверяемый игрок.
	 * @return подходит ли игрок к оружию.
	 */
	public boolean checkClass(Player player)
	{
		return playerClass.getId() == player.getClassId();
	}
	
	/**
	 * @return xmlName название в хмл.
	 */
	public final String getXmlName()
	{
		return xmlName;
	}
}
