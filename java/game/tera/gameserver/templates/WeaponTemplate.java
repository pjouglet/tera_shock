package tera.gameserver.templates;

import rlib.util.VarTable;
import tera.gameserver.model.equipment.SlotType;
import tera.gameserver.model.items.WeaponType;
import tera.gameserver.model.playable.Player;

/**
 * Модель шаблона оружия.
 *
 * @author Ronn
 */
public final class WeaponTemplate extends GearedTemplate
{
	public WeaponTemplate(WeaponType type, VarTable vars)
	{
		super(type, vars);

		slotType = SlotType.SLOT_WEAPON;
	}

	@Override
	public boolean checkClass(Player player)
	{
		return getType().checkClass(player);
	}

	@Override
	public int getClassIdItemSkill()
	{
		return -11;
	}

	@Override
	public final WeaponType getType()
	{
		return (WeaponType) type;
	}
}
