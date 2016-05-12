package tera.gameserver.model.actions.classes;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.EmotionType;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.actions.ActionType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.AppledAction;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель акшена для приглашения игрока в пати.
 *
 * @author Ronn
 * @created 06.03.2012
 */
public class BindItemAction extends AbstractAction<ItemInstance>
{
	@Override
	public void assent(Player player)
	{
		// получам инициатора
		Player actor = getActor();
		// получаем цель
		ItemInstance target = getTarget();

		super.assent(player);

		if(!test(actor, target))
			return;

		// синхронизируем итем
		synchronized(target)
		{
			// если уже забинден, выходим
			if(target.isBinded())
				return;

			// биндим
			target.setOwnerName(actor.getName());

			// получаем менеджера БД
			DataBaseManager dbManager = DataBaseManager.getInstance();

			// обновляем в базе
			dbManager.updateDataItem(target);

			// показываем эмоцию
			PacketManager.showEmotion(actor, EmotionType.BOASTING);
			// обновляем инвентарь
			PacketManager.updateInventory(actor);
		}
	}

	@Override
	public synchronized void cancel(Player player)
	{
		// получаем инициатора
		Player actor = getActor();

		// если актора нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// зануляем акшен
		actor.setLastAction(null);

		super.cancel(player);
	}

	@Override
	public ActionType getType()
	{
		return ActionType.BIND_ITEM;
	}

	@Override
	public void init(Player actor, String name)
	{
		this.actor = actor;

		// получаем ид итема
		int objectId = Integer.parseInt(name);

		// получаем инвентарь игрока
		Inventory inventory = actor.getInventory();

		// если его нет, выходим
		if(inventory == null)
		{
			log.warning(this, new Exception("not found inventory"));
			return;
		}

		// ищем нужный итем
		ItemInstance item = inventory.getItemForObjectId(objectId);

		// если не нашли, выходим
		if(item == null)
		{
			log.warning(this, new Exception("not found item " + target));
			return;
		}

		// запоминаем итем
		this.target = item;
	}

	@Override
	public synchronized void invite()
	{
		// получаем инициатора
		Player actor = getActor();

		// если кого-то из них нету, выходим
		if(actor == null || target == null || target.isBinded() || actor.isOnMount() || actor.isFlyingPegas())
			return;

		// запоминаем у игрока акшен
		actor.setLastAction(this);

		ActionType type = getType();

		// отправляем соответсвующие пакеты
		actor.sendPacket(AppledAction.newInstance(actor, null, type.ordinal(), objectId), true);

		// показываем эмоцию
		PacketManager.showEmotion(actor, EmotionType.CAST);

		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		// запускаем новый таск
		setSchedule(executor.scheduleGeneral(this, 5000));
	}

	@Override
	protected final void runImpl()
	{
		assent(null);
	}

	@Override
	public boolean test(Player actor, ItemInstance target)
	{
		// если кого-то нет, выходим
		if(target == null || actor == null || actor.isOnMount() || actor.isFlyingPegas())
			return false;

		// если итем уже забинден
		if(target.isBinded())
		{
			actor.sendMessage(MessageType.THAT_ITEM_IS_SOULBOUND);
			return false;
		}

		// получаем темплейт игрока
		ItemTemplate template = target.getTemplate();

		// если не подходящий класс
		if(!template.checkClass(actor))
		{
			actor.sendMessage(MessageType.THAT_ITEM_IS_UNAVAILABLE_TO_YOUR_CLASS);
			return false;
		}

		// если уровень итема выше уровня игрока
		if(template.getRequiredLevel() > actor.getLevel())
		{
			actor.sendMessage(MessageType.YOU_MUST_BE_A_HIGHER_LEVEL_TO_USE_THAT);
			return false;
		}

		return true;
	}
}
