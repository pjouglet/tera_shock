package tera.gameserver.model.items;

import rlib.util.array.Arrays;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.playable.Player;

/**
 * Перечисление описывающие типы брони
 *
 * @author Ronn
 */
public enum ArmorKind
{
	/** тяжелая */
	METAL("metal", PlayerClass.LANCER, PlayerClass.BERSERKER),
	/** легкая */
	LEATHER("leather", PlayerClass.WARRIOR, PlayerClass.SLAYER, PlayerClass.ARCHER),
	/** роба */
	CLOTH("cloth", PlayerClass.PRIEST, PlayerClass.SORCERER, PlayerClass.MYSTIC),
	/** остальное */
	OTHER("other", PlayerClass.values);

	/**
	 * Получение нужного кинда по названию в xml.
	 *
	 * @param name название в xml.
	 * @return соответствующий кинд.
	 */
	public static ArmorKind valueOfXml(String name)
	{
		for(ArmorKind type : values())
			if(type.getXmlName().equals(name))
				return type;

		throw new IllegalArgumentException("no enum " + name);
	}
	/** название в хмл */
	private String xmlName;

	/** список доступных классов */
	private PlayerClass[] classes;

	/**
	 * @param xmlName название енума в хмл.
	 * @param classes список доступных классов.
	 */
	private ArmorKind(String xmlName, PlayerClass... classes)
	{
		this.xmlName= xmlName;
		this.classes = classes;
	}

	/**
	 * Проверка на корректность игрока для одевания итема с данным типом.
	 *
	 * @param player проверяемый игрок.
	 * @return можно ли одеть.
	 */
	public boolean checkClass(Player player)
	{
		return Arrays.contains(classes, player.getPlayerClass());
	}

	/**
	 * @return the xmlName
	 */
	public final String getXmlName()
	{
		return xmlName;
	}
}
