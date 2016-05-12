package tera.gameserver.model.npc.interaction.dialogs;

import rlib.util.array.Array;
import tera.gameserver.model.Party;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.GuildInputName;

/**
 * Моделт диалога создания гильдии.
 *
 * @author Ronn
 */
public final class CreateGuildDialog extends AbstractDialog
{
	/**
	 * Создание нового диалога.
	 *
	 * @param npc нпс, с которым говорим.
	 * @param player игрок, который говорит.
	 * @param price цена создания клана.
	 * @param minLevel минимальный уровень.
	 * @return новый диалог.
	 */
	public static CreateGuildDialog newInstance(Npc npc, Player player, int price, int minLevel)
	{
		CreateGuildDialog dialog = (CreateGuildDialog) DialogType.GUILD_CREATE.newInstance();

		dialog.player = player;
		dialog.npc = npc;
		dialog.price = price;
		dialog.minLevel = minLevel;

		return dialog;
	}

	/** стоимость */
	private int price;
	/** минимальный уровень */
	private int minLevel;

	@Override
	public DialogType getType()
	{
		return DialogType.GUILD_CREATE;
	}

	@Override
	public synchronized boolean init()
	{
		// базовая инициализация
		if(!super.init())
			// если была неуспешной, выходим
			return false;

		// игрок
		Player player = getPlayer();

		// если игрока нет ,выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// проверяем уровень
		if(player.getLevel() < minLevel)
		{
			// сообщаем и выходим
			player.sendMessage("You must be level 8 for create a Guild.");
			return false;
		}

		// проверяем наличие гильдии
		if(player.hasGuild())
		{
			// сообщаем и выходим
			player.sendMessage("You already have a Guild.");
			return false;
		}

		// получаем группу игрока
		Party party = player.getParty();

		// если ее нету
		if(party == null)
		{
			// сообщаем и выходим
			player.sendMessage("You must be in party.");
			return false;
		}

		// набор соппартийцов
		Array<Player> members = party.getMembers();

		// блокируем список
		members.readLock();
		try
		{
			Player[] array = members.array();

			// перебираем
			for(int i = 0, length = members.size(); i < length; i++)
				// если кто-то уже состоит в группе
				if(array[i].hasGuild())
				{
					/// сообщаем и выходим
					player.sendMessage("A member of you party have already a Guild.");
					return false;
				}
		}
		finally
		{
			members.readUnlock();
		}

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// смотрим наличие необъодимой суммы денег
		if(inventory.getMoney() < price)
		{
			// если ее нету, сообщаем и выходим
			player.sendMessage("You don't have enough money.");
			return false;
		}

		// отправляем окно ввода названия гильдии
		player.sendPacket(GuildInputName.getInstance(), true);

		return true;
	}
}
