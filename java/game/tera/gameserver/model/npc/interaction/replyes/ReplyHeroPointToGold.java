package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import tera.Config;
import tera.gameserver.events.EventConstant;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Модель для перевода очков славы в деньги.
 *
 * @author Ronn
 */
public class ReplyHeroPointToGold extends AbstractReply
{
	public ReplyHeroPointToGold(Node node)
	{
		super(node);
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		synchronized(player)
		{
			// получаем текущее кол-во очков
			int points = player.getVar(EventConstant.VAR_NANE_HERO_POINT, 0);

			// если их нет, выходим
			if(points < 1)
			{
				player.sendMessage("You don't have Fame points.");
				return;
			}

			// забираем 1 очко
			player.setVar(EventConstant.VAR_NANE_HERO_POINT, points - 1);

			// получаем менеджера БД
			DataBaseManager dbManager = DataBaseManager.getInstance();

			// на всякий случай сохраняем в БД
			dbManager.updatePlayerVar(player.getObjectId(), EventConstant.VAR_NANE_HERO_POINT, String.valueOf(points - 1));
		}

		// рассчитываем итоговое кол-во денег
		int reward = (int) (1 * Config.EVENT_HERO_POINT_TO_GOLD * Config.SERVER_RATE_MONEY);

		// если награды нет, выходим
		if(reward < 1)
			return;

		// получаем инвентарь
		Inventory inventory = player.getInventory();

		// выдаем деньги
		inventory.addMoney(reward);

		// отображаем выдачу денег
		PacketManager.showAddGold(player, reward);
	}
}
