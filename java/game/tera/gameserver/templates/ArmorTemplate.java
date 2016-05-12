package tera.gameserver.templates;

import rlib.util.VarTable;
import tera.gameserver.model.items.ArmorKind;
import tera.gameserver.model.items.ArmorType;
import tera.gameserver.model.playable.Player;

/**
 * Модель шаблона брони и бижутерии.
 *
 * @author Ronn
 */
public final class ArmorTemplate extends GearedTemplate
{
	/** тип брони */
	protected ArmorKind armorKind;

	public ArmorTemplate(ArmorType type, VarTable vars)
	{
		super(type, vars);

		try
		{
			armorKind = ArmorKind.valueOfXml(vars.getString("kind", "other"));
			slotType = type.getSlot();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public boolean checkClass(Player player)
	{
		return armorKind.checkClass(player);
	}

	/**
	 * @return тип материала брони.
	 */
	public final ArmorKind getArmorKind()
	{
		return armorKind;
	}

	@Override
	public ArmorType getType()
	{
		return (ArmorType) type;
	}

	@Override
	public String toString()
	{
		return "ArmorTemplate  armorKind = " + armorKind + ", requiredLevel = " + requiredLevel + ", attack = " + attack + ", impact = " + impact + ", defence = " + defence + ", balance = " + balance + ", sockets = " + sockets + ", extractable = " + extractable + ", enchantable = " + enchantable + ", remodelable = " + remodelable + ", dyeable = " + dyeable + ", bindType = " + bindType + ", name = " + name + ", itemId = " + itemId + ", itemLevel = " + itemLevel + ", buyPrice = " + buyPrice + ", sellPrice = " + sellPrice + ", slotType = " + slotType + ", rank = " + rank + ", itemClass = " + itemClass + ", stackable = " + stackable + ", sellable = " + sellable + ", bank = " + bank + ", guildBank = " + guildBank + ", tradable = " + tradable + ", deletable = " + deletable + ", type = " + type;
	}
}
