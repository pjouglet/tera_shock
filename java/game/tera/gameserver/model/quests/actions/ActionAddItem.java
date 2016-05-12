package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.Config;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.network.serverpackets.MessageAddedItem;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;

/**
 * Акшен для выдачи итема.
 *
 * @author Ronn
 */
public class ActionAddItem extends AbstractQuestAction
{
	/** ид итема */
	private int itemId;
	/** кол-во итемов */
	private int itemCount;

	/** является ли это наградой */
	private boolean reward;

	public ActionAddItem(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.reward = vars.getBoolean("reward", false);
			this.itemId = vars.getInteger("id");
			this.itemCount = (int) (vars.getInteger("count") * (isReward()? Config.SERVER_RATE_QUEST_REWARD : 1));
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @return ялвяется ли наградой.
	 */
	public boolean isReward()
	{
		return reward;
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// если его нет, выходим
		if(inventory == null)
		{
			log.warning(this, "not found inventory");
			return;
		}

		// получаем таблицу итемов
		ItemTable itemTable = ItemTable.getInstance();

		// получаем темплейт итема
		ItemTemplate template = itemTable.getItem(itemId);

		// если его нет, выходим
		if(template == null)
		{
			log.warning(this, "not found item for " + itemId);
			return;
		}

		int itemCount = getItemCount();

		if(Config.ACCOUNT_PREMIUM_QUEST && player.hasPremium())
			itemCount *= Config.ACCOUNT_PREMIUM_QUEST_RATE;

		if(!template.isStackable())
			itemCount = 1;

		// добавляем итем
		if(!inventory.forceAddItem(itemId, itemCount, quest.getName()))
			return;

		// если это не деньги
		if(itemId != Inventory.MONEY_ITEM_ID)
			// отправляем пакет о выдачи итема
			player.sendPacket(MessageAddedItem.getInstance(player.getName(), itemId, itemCount), true);
		else
			PacketManager.showAddGold(player, itemCount);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// получаем логер игровых событий
		GameLogManager gameLogger = GameLogManager.getInstance();

		// записываем событие выдачи итема
		gameLogger.writeItemLog("Quest [id = " + quest.getId() + ", name = " + quest.getName() + "] added item [id = " + itemId + ", count = " + itemCount + ", name = " + template.getName() + "] to " + player.getName());

		// обновляем инвентарь
		eventManager.notifyInventoryChanged(player);
	}

	/**
	 * @return кол-во итемов.
	 */
	public int getItemCount()
	{
		return itemCount;
	}

	/**
	 * @return ид итема.
	 */
	public int getItemId()
	{
		return itemId;
	}

	@Override
	public String toString()
	{
		return "AddItem itemId = " + itemId + ", itemCount = " + itemCount;
	}
}
