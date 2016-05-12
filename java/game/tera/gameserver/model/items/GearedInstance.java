package tera.gameserver.model.items;

import rlib.util.Strings;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.playable.Player;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель экиперуемых вещей.
 * 
 * @author Ronn
 */
public class GearedInstance extends ItemInstance
{
	/** лист кристалов итема */
	protected CrystalList crystals;

	/** имя владельца */
	protected String ownerName;

	/**
	 * @param objectId
	 * @param template
	 */
	public GearedInstance(int objectId, ItemTemplate template)
	{
		super(objectId, template);

		this.ownerName = Strings.EMPTY;

		if (template.getSockets() > 0)
			crystals = new CrystalList(template.getSockets(), objectId);

		updateEnchantStats();
	}

	@Override
	public boolean equipmentd(Character character, boolean showMessage)
	{
		Player player = character.getPlayer();

		if (player != null)
		{
			// TODO
			if (template.getBindType() == BindType.ON_EQUIP)
			{
				if (!isBinded())
				{
					if (showMessage)
						player.sendMessage("Вы не связали с собой итем.");

					return false;
				}
				else if (!getOwnerName().equals(character.getName()))
				{
					if (showMessage)
						player.sendMessage("Этот итем связан с другим игроком.");

					return false;
				}
			}

			// если не подходящий класс
			if (!template.checkClass(player))
			{
				if (showMessage)
					player.sendMessage(MessageType.THAT_ITEM_IS_UNAVAILABLE_TO_YOUR_CLASS);

				return false;
			}

			// если уровень итема выше уровня игрока
			if (template.getRequiredLevel() > player.getLevel())
			{
				if (showMessage)
					player.sendMessage(MessageType.YOU_MUST_BE_A_HIGHER_LEVEL_TO_USE_THAT);

				return false;
			}
		}

		return true;
	}

	@Override
	public void finalyze()
	{
		super.finalyze();

		ownerName = Strings.EMPTY;

		if (crystals != null)
			crystals.finalyze();
	}

	@Override
	public CrystalList getCrystals()
	{
		return crystals;
	}

	@Override
	public String getOwnerName()
	{
		return ownerName;
	}

	@Override
	public boolean isBinded()
	{
		return ownerName != Strings.EMPTY;
	}

	@Override
	public void setObjectId(int objectId)
	{
		super.setObjectId(objectId);

		if (crystals != null)
			crystals.setObjectId(objectId);
	}

	@Override
	public void setOwnerName(String ownerName)
	{
		if (ownerName.isEmpty())
			ownerName = Strings.EMPTY;

		this.ownerName = ownerName;
	}

	@Override
	public final void setEnchantLevel(int enchantLevel)
	{
		super.setEnchantLevel(enchantLevel);

		updateEnchantStats();
	}

	/**
	 * Обновление статов завязаных на зачаровании.
	 */
	protected void updateEnchantStats()
	{

	}
}
