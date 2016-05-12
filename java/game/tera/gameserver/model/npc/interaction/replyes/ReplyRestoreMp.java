package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Обработка ссылки на восстановление мп.
 *
 * @author Ronn
 */
public class ReplyRestoreMp extends AbstractReply
{
	/** стоимость восстановления */
	private int price;

	/**
	 * @param node
	 */
	public ReplyRestoreMp(Node node)
	{
		super(node);

		this.price = VarTable.newInstance(node).getInteger("price");
	}

	@Override
	public void reply(Npc npc, Player player, Link link)
	{
		// если всястамина есть, выходим
		if(player.getCurrentMp() >= player.getMaxMp())
			return;

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// если у игрока нет денег, выходим
		if(inventory.getMoney() < price)
		{
			player.sendMessage(MessageType.YOU_DONT_HAVE_ENOUGH_GOLD);
			return;
		}

		// уменьшаем кол-во денег
		inventory.subMoney(price);

		// отображаем оплату
		PacketManager.showPaidGold(player, price);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем инвентарь
		eventManager.notifyInventoryChanged(player);

		// восстанавливаем хп
		player.effectHealMp(player.getMaxMp(), player);
	}
}
