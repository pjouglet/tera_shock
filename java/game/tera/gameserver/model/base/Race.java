package tera.gameserver.model.base;

import java.io.File;

import rlib.data.DocumentXML;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;

import tera.Config;
import tera.gameserver.document.DocumentRaceAppearance;
import tera.gameserver.document.DocumentRaceStats;
import tera.gameserver.model.playable.PlayerAppearance;
import tera.gameserver.model.skillengine.funcs.Func;

/**
 * Перечисление рас игроков.
 *
 * @author Ronn
 */

@SuppressWarnings("unused")
public enum Race
{
	/** люди */
	HUMAN(0),
	/** эльфы */
	ELF(1),
	/** АМАНЫ */
	AMAN(2),
	/** кастаники */
	CASTANIC(3),
	/** попори */
	POPORI(4),
	/** барака */
	BARAKA(5),
	/** элин */
	ELIN(4);

	private static final Logger log = Loggers.getLogger(Race.class);

	/** список всех рас */
	public static final Race[] VALUES = values();

	/** кол-во всех рас */
	public static final int SIZE = VALUES.length;

	public static void init()
	{
		DocumentXML<Void> document = new DocumentRaceAppearance(new File(Config.SERVER_DIR + "/data/player_race_appearance.xml"));

		document.parse();

		document = new DocumentRaceStats(new File(Config.SERVER_DIR + "/data/player_race_stats.xml"));

		document.parse();

		log.info("race appearances initializable.");
	}

	public static Race valueOf(int id, Sex sex)
	{
		if(id == ELIN.id && sex == Sex.FEMALE)
			return ELIN;

		return values()[id];
	}

	/** стандартная мухская внешность */
	private PlayerAppearance male;

	/** стандартная женская внешность */
	private PlayerAppearance female;

	/** набор функций расы */
	private Array<Func> funcs;

	/** модификатор регена хп */
	private float regHp;
	/** модификатор регена мп */
	private float regMp;
	/** модификатор фактора атаки */
	private float powerFactor;
	/** модификатор фактора защиты */
	private float defenseFactor;
	/** модификатор фактора силы */
	private float impactFactor;
	/** модификатор фактора баланса */
	private float balanceFactor;
	/** модификатор скорости атаки */
	private float atkSpd;
	/** модификатор скорости бега */
	private float runSpd;
	/** модификатор шанса крита */
	private float critRate;
	/** модификатор защиты от крита */
	private float critRcpt;

	/** ид расы */
	private int id;

	private Race(int id)
	{
		this.id = id;
	}

	/**
	 * @param male мужская внешность.
	 */
	public void setMale(PlayerAppearance male)
	{
		this.male = male;
	}

	/**
	 * @param female женская внешность.
	 */
	public void setFemale(PlayerAppearance female)
	{
		this.female = female;
	}

	/**
	 * Получить базовую внешность расы указанного пола.
	 *
	 * @param sex пол расы.
	 * @return базовая внешность.
	 */
	public PlayerAppearance getAppearance(Sex sex)
	{
		return sex == Sex.MALE? male : female;
	}

	/**
	 * @param funcs набор функций расы.
	 */
	public void setFuncs(Array<Func> funcs)
	{
		this.funcs = funcs;
	}

	/**
	 * @return модификатор регена мп.
	 */
	public float getRegMp()
	{
		return regMp;
	}

	/**
	 * @return функции расы.
	 */
	public Array<Func> getFuncs()
	{
		return funcs;
	}

	/**
	 * @return ид расы.
	 */
	public int getId()
	{
		return id;
	}
}
