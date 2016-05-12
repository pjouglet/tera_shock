package tera.gameserver.model.base;

/**
 * Класс с типами полов
 *
 * @author Ronn
 */
public enum Sex
{
	MALE,
	FEMALE;

	public static Sex valueOf(int index)
	{
		return values()[index];
	}

	/** список всех полов */
	public static final Sex[] VALUES = values();

	/** кол-во всех полов */
	public static final int SIZE = VALUES.length;
}
