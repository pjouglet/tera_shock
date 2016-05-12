package tera.gameserver.model.base;

/**
 * Перечисление типов классов игроков.
 *
 * @author Ronn
 */
public enum PlayerClass
{
	/** воин */
	WARRIOR(0, false, false, 1.057F),
	/** копейщик */
	LANCER(1, false, false, 1.057F),
	/** убийца */
	SLAYER(2, false, false, 1.057F),
	/** берсеркер */
	BERSERKER(3, false, false, 1.0575F),
	/** волшебник */
	SORCERER(4, true, true, 1.058F),
	/** лучник */
	ARCHER(5, false, true, 1.057F),
	/** прист */
	PRIEST(6, true, true, 1.0581F),
	/** мистик */
	MYSTIC(7, true, true, 1.0581F);

	/** список всех классов */
	public static final PlayerClass[] values = values();

	/** кол-во классов */
	public static final int length = values.length;

	/**
	 * @param id ид класса.
	 * @return тип класса.
	 */
	public static PlayerClass getClassById(int id)
	{
		if(id < 0 || id >= length)
			return null;

		return values[id];
	}

	/** ид класса */
	private final int id;

	/** модификация хп */
	private final float hpMod;

	/** является ли магом */
	private final boolean mage;
	/** является ли класс дальним */
	private final boolean range;

	/**
	 * @param id ид класса.
	 * @param mage является ли класс магическим.
	 * @param range является ли класс дальним.
	 * @param level уровень класса.
	 * @param hpMod модификатор хп.
	 */
	private PlayerClass(int id, boolean mage, boolean range, float hpMod)
	{
		this.id = id;
		this.mage = mage;
		this.range = range;
		this.hpMod = hpMod;
	}

	/**
	 * @return модификатор хп.
	 */
	public float getHpMod()
	{
		return hpMod;
	}

	/**
	 * @return id ид класса.
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * @return является ли класс магическим.
	 */
	public final boolean isMage()
	{
		return mage;
	}

	/**
	 * @return является ли класс дальним.
	 */
	public final boolean isRange()
	{
		return range;
	}
}