package tera.gameserver.model.npc.playable;

import tera.gameserver.model.playable.PlayerAppearance;

/**
 * Модель внешности НПС на базе внешности игрока.
 *
 * @author Ronn
 */
public class NpcAppearance extends PlayerAppearance
{
	/** ид шляпы */
	private int hatId;
	/** ид маски */
	private int maskId;
	/** ид перчаток */
	private int glovesId;
	/** ид ботинок */
	private int bootsId;
	/** ид брони */
	private int armorId;
	/** ид пушки */
	private int weaponId;

	/**
	 * @return ид брони.
	 */
	public int getArmorId()
	{
		return armorId;
	}

	/**
	 * @return ид ботинок.
	 */
	public int getBootsId()
	{
		return bootsId;
	}

	/**
	 * @return ид перчаток.
	 */
	public int getGlovesId()
	{
		return glovesId;
	}

	/**
	 * @return ид кепки.
	 */
	public int getHatId()
	{
		return hatId;
	}

	/**
	 * @return
	 */
	public int getMaskId()
	{
		return maskId;
	}

	/**
	 * @return ид пушки.
	 */
	public int getWeaponId()
	{
		return weaponId;
	}
}
