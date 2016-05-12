package tera.gameserver.model.actions.dialogs;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.TradeItem;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ServerPacket;
import tera.gameserver.network.serverpackets.ShowTrade;
import tera.gameserver.network.serverpackets.SystemMessage;

/**
 * Модель диалога трейда.
 *
 * @author Ronn
 */
public class TradeDialog extends AbstractActionDialog
{
	/** максимальное кол-во итемов для передачи */
	public static final int MAX_ITEMS = 18;

	/**
	 * Создание нового диалога.
	 *
	 * @param actor инициатор диалога.
	 * @param enemy опонент диалога.
	 * @return новый диалог.
	 */
	public static final TradeDialog newInstance(Player actor, Player enemy)
	{
		TradeDialog dialog = (TradeDialog) ActionDialogType.TRADE_DIALOG.newInstance();

		// получаем фабрику ИД
		IdFactory idFactory = IdFactory.getInstance();

		dialog.actor = actor;
		dialog.enemy = enemy;
		dialog.objectId = idFactory.getNextActionId();

		return dialog;
	}

	/** передаваемые инициатором итемы */
	private Array<TradeItem> actorItems;
	/** передываемые апонентом итемы */
	private Array<TradeItem> enemyItems;

	/** передаваемые инициатором деньги */
	private long actorMoney;
	/** передаваемые деньги опонентом */
	private long enemyMoney;

	/** заблокирование изминения инициатором */
	private boolean actorLock;
	/** заблокирование изминения опонентом */
	private boolean enemyLock;

	/** завершен ли трейд */
	private boolean done;

	public TradeDialog()
	{
		this.actorItems = Arrays.toArray(TradeItem.class, MAX_ITEMS);
		this.enemyItems = Arrays.toArray(TradeItem.class, MAX_ITEMS);
	}

	/**
	 * Добавление итема на передачу.
	 *
	 * @param player игрок, который добавляет.
	 * @param count кол-во итемов.
	 * @param index индекс ячейки инвенторя.
	 */
	public synchronized void addItem(Player player, int count, int index)
	{
		// если уже кто-то заблокировал, выходим
		if(isActorLock() || isEnemyLock())
			return;

		// список предложенных итемов
		Array<TradeItem> items;

		// инвентарь игрока
		Inventory inventory = player.getInventory();

		// если инициатор
		if(player == getActor())
			items = getActorItems();
		else
			items = getEnemyItems();

		inventory.lock();
		try
		{
			// получаем ячейку инвенторя
			Cell cell = inventory.getCell(index);

			// если ее нет или она пуста, выходим
			if(cell == null || cell.isEmpty())
				return;

			// получаем итем
			ItemInstance item = cell.getItem();

			// если он не передается
			if(!item.isTradable())
			{
				// создаем сообщение
				SystemMessage packet = SystemMessage.getInstance(MessageType.YOU_CANT_TRADE);
				// добавляем указание о итеме
				packet.addItemName(item.getItemId());
				// отправляем пакет
				player.sendPacket(packet, true);
				return;
			}

			// если итема больше чем есть, выходим
			if(count > item.getItemCount())
				return;

			// получаем позицию итема в текущем списке
			int order = items.indexOf(item);

			// если он уже добавлен в список, значит это стакуемый
			if(order > -1)
			{
				// получаем его аналог в положенных
				TradeItem tradeItem = items.get(order);

				// если по кол-ву не сходится, выходим
				if(tradeItem.getCount() + count > item.getItemCount())
					return;

				// увеличиваем кол-вд
				tradeItem.addCount(count);

				// обновляем диалог
				updateDialog();
			}
			// если еще не переполнен
			else if(items.size() < MAX_ITEMS)
			{
				// добалвяем новый итем
				items.add(TradeItem.newInstance(item, count));
				// обновляем диалог
				updateDialog();
			}
		}
		finally
		{
			inventory.unlock();
		}
	}

	/**
	 * Добавление денег на передачу.
	 *
	 * @param player игрок, который добавляет.
	 * @param money кол-во добавляемых денег.
	 */
	public synchronized void addMoney(Player player, long money)
	{
		// если уже кто-то заблокировал, выходим
		if(isActorLock() || isEnemyLock())
			return;

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// определяем кто это
		boolean isActor = player == getActor();

		// если у инициатора есть столько денег
		if(isActor && money + actorMoney <= inventory.getMoney())
		{
			// добавляем
			actorMoney += money;

			// обновляем окно
			updateDialog();
		}
		// если у опонента есть столько денег
		else if(money + enemyMoney <= inventory.getMoney())
		{
			// добавляем
			enemyMoney += money;

			// обновляем окно
			updateDialog();
		}
	}

	@Override
	public synchronized boolean apply()
	{
		// если уже был выполнен, выходим
		if(isDone())
			return false;

		// ставим флаг выполнения
		setDone(true);

		// инициатор трейда
		Player actor = getActor();
		// опонент
		Player enemy = getEnemy();

		// если кого-то из них нету, выходим
		if(actor == null || enemy == null)
		{
			log.warning(this, new Exception("not found actor or enemy,"));
			return false;
		}

		// получаем инвентори обоих участников
		Inventory actorInventory = actor.getInventory();
		Inventory enemyInventory = enemy.getInventory();

		// если чьего-то инвенторя нету, выходим
		if(actorInventory == null || enemyInventory == null)
			return false;

		actorInventory.lock();
		try
		{
			enemyInventory.lock();
			try
			{
				// если инициатор пробует передать больше, чем у него есть, зануляем
				if(actorMoney > actorInventory.getMoney())
					actorMoney = 0;

				// получаем список передаваемых инициатором итемов
				TradeItem[] array = actorItems.array();

				// перебираем их
				for(int i = 0, length = actorItems.size(); i < length; i++)
				{
					// получаем передаваемый итем
					TradeItem tradeItem = array[i];

					// смотрим, есть ли он у него
					ItemInstance item = actorInventory.getItemForObjectId(tradeItem.getObjectId());

					// если его нет или недостаточное кол-во
					if(item == null || item.getItemCount() < tradeItem.getCount())
					{
						// удаляем из списка
						actorItems.fastRemove(i--);
						length--;
					}
				}

				// если опонент пробует передать больше, чем у него есть, зануляем
				if(enemyMoney > enemyInventory.getMoney())
					enemyMoney = 0;

				// получаем список передаваемых итемов опонентом
				array = enemyItems.array();

				// перебираем их
				for(int i = 0, length = enemyItems.size(); i < length; i++)
				{
					// получаем передаваемый итем
					TradeItem tradeItem = array[i];

					// смотрим, есть ли он у него
					ItemInstance item = enemyInventory.getItemForObjectId(tradeItem.getObjectId());

					// если его нет или недостаточное кол-во
					if(item == null || item.getItemCount() < tradeItem.getCount())
					{
						// удаляем из списка
						enemyItems.fastRemove(i--);
						length--;
					}
				}

				// передаем деньги
				actorInventory.addMoney(enemyMoney);
				actorInventory.subMoney(actorMoney);
				enemyInventory.addMoney(actorMoney);
				enemyInventory.subMoney(enemyMoney);

				// получаем логера игровых событий
				GameLogManager gameLogger = GameLogManager.getInstance();

				// записываем событие о передаче денег
				gameLogger.writeItemLog(actor.getName() + " add " + actorMoney + " money to " + enemy.getName());
				// записываем событие о передаче денег
				gameLogger.writeItemLog(enemy.getName() + " add " + enemyMoney + " money to " + actor.getName());

				// получаем список передаваемых итемов инициатором
				array = actorItems.array();

				// перебираем их
				for(int i = 0, length = actorItems.size(); i < length; i++)
				{
					// получаем передаваемый итем
					TradeItem trade = array[i];

					// получаем кол-во передаваемых итемов
					long count = trade.getCount();

					// получаем сам итем
					ItemInstance item = trade.getItem();

					// если итем не стакуемый
					if(!trade.isStackable())
						// то переносим его
						enemyInventory.moveItem(item, actorInventory);
					else // иначе
					{
						// добавляем опоненту нужное кол-во нового итема
						if(enemyInventory.addItem(trade.getItemId(), trade.getCount(), actor.getName()))
							// и удаляем это кол-во у инициатора
							actorInventory.removeItem(trade.getItemId(), trade.getCount());
					}

					// записываем событие передачи итема
					gameLogger.writeItemLog(actor.getName() + " trade item [id = " + item.getItemId() + ", count = " + count + ", name = " + item.getName() + "] to " + enemy.getName());
				}

				// получаем список передаваемых итемов опонентом
				array = enemyItems.array();

				// перебираем их
				for(int i = 0, length = enemyItems.size(); i < length; i++)
				{
					// получаем передаваемый итем
					TradeItem trade = array[i];

					// получаем кол-во передаваемых итемов
					long count = trade.getCount();
					// получаем сам итем
					ItemInstance item = trade.getItem();

					// если итем не стакуемый
					if(!trade.isStackable())
						// то переносим его
						actorInventory.moveItem(trade.getItem(), enemyInventory);
					else // иначе
					{
						// добавляем инициатору нужное кол-во нового итема
						if(actorInventory.addItem(trade.getItemId(), trade.getCount(), actor.getName()))
							// и удаляем это кол-во у опонента
							enemyInventory.removeItem(trade.getItemId(), trade.getCount());
					}

					// записываем событие передачи итема
					gameLogger.writeItemLog(enemy.getName() + " trade item [id = " + item.getItemId() + ", count = " + count + ", name = " + item.getName() + "] to " + actor.getName());
				}

				// получаем менеджера событий
				ObjectEventManager eventManager = ObjectEventManager.getInstance();

				// обновляемся
				eventManager.notifyInventoryChanged(actor);
				eventManager.notifyInventoryChanged(enemy);
			}
			finally
			{
				enemyInventory.unlock();
			}
		}
		finally
		{
			actorInventory.unlock();
		}

		actor.sendMessage(MessageType.TRADE_COMPLETED);
		enemy.sendMessage(MessageType.TRADE_COMPLETED);

		return true;
	}

	@Override
	public synchronized void cancel(Player player)
	{
		// если кто-то закрыл, значит была отмена
		if(player != null)
		{
			// получаем инициатора
			Player actor = getActor();
			// получаем цель его
			Player enemy = getEnemy();

			if(player == actor)
			{
				actor.sendMessage(MessageType.TRADE_CANCELED);
				enemy.sendPacket(SystemMessage.getInstance(MessageType.OPPONENT_CANCELED_THE_TRADE).addOpponent(actor.getName()), true);
			}
			else if(player == enemy)
			{
				enemy.sendMessage(MessageType.TRADE_CANCELED);
				actor.sendPacket(SystemMessage.getInstance(MessageType.OPPONENT_CANCELED_THE_TRADE).addOpponent(enemy.getName()), true);
			}
		}

		super.cancel(player);
	}

	@Override
	public void finalyze()
	{
		TradeItem[] array = actorItems.array();

		for(int i = 0, length = actorItems.size(); i < length; i++)
			array[i].fold();

		actorItems.clear();

		array = enemyItems.array();

		for(int i = 0, length = enemyItems.size(); i < length; i++)
			array[i].fold();

		enemyItems.clear();

		actorMoney = 0;
		enemyMoney = 0;

		actorLock = false;
		enemyLock = false;
		done = false;

		super.finalyze();
	}

	/**
	 * @return список передоваемых итемов инициатором.
	 */
	protected final Array<TradeItem> getActorItems()
	{
		return actorItems;
	}

	/**
	 * @return кол-во передоваемых денег инициатором.
	 */
	protected final long getActorMoney()
	{
		return actorMoney;
	}

	/**
	 * @return список передоваемых итемов опонентом.
	 */
	protected Array<TradeItem> getEnemyItems()
	{
		return enemyItems;
	}

	/**
	 * @return кол-во передаваемых денег опонентом.
	 */
	protected final long getEnemyMoney()
	{
		return enemyMoney;
	}

	/**
	 * Кол-во выставленных итемов у указанного игрока.
	 *
	 * @param player игрок.
	 * @return сколько видов итемов выставил указанный итем.
	 */
	public int getItemCount(Player player)
	{
		if(player == actor)
			return actorItems.size();
		else if(player == enemy)
			return enemyItems.size();

		return 0;
	}

	/**
	 * Получаем список положенных итемов.
	 *
	 * @param player игрок.
	 * @return список положенных итемов.
	 */
	public Array<TradeItem> getItems(Player player)
	{
		if(player == actor)
			return actorItems;
		else if(player == enemy)
			return enemyItems;

		return null;
	}

	/**
	 * Кол-во выставленных на передачу денег у указанного игрока.
	 *
	 * @param player игрок.
	 * @return сколько денег указанный игрок выставил на передачу.
	 */
	public long getMoney(Player player)
	{
		if(player == actor)
			return getActorMoney();
		else if(player == enemy)
			return getEnemyMoney();

		return 0;
	}

	@Override
	public ActionDialogType getType()
	{
		return ActionDialogType.TRADE_DIALOG;
	}

	@Override
	public synchronized boolean init()
	{
		if(super.init())
		{
			Player actor = getActor();
			Player enemy = getEnemy();

			PacketManager.updateInventory(actor);
			PacketManager.updateInventory(enemy);

			updateDialog();

			actor.sendMessage(MessageType.TRADE_HAS_BEGUN);
			enemy.sendMessage(MessageType.TRADE_HAS_BEGUN);

			return true;
		}

		return false;
	}

	/**
	 * @return заблокировал ли трейд инициатор.
	 */
	protected final boolean isActorLock()
	{
		return actorLock;
	}

	/**
	 * @return завершена ли передача.
	 */
	protected final boolean isDone()
	{
		return done;
	}

	/**
	 * @return заблокировал ли трейд опонент.
	 */
	protected final boolean isEnemyLock()
	{
		return enemyLock;
	}

	/**
	 * Заблокирован ли трейд у указанного игрока.
	 *
	 * @param player игрок.
	 * @return заблокирован ли.
	 */
	public boolean isLock(Player player)
	{
		if(player == actor)
			return isActorLock();
		else if(player == enemy)
			return isEnemyLock();

		return false;
	}

	/**
	 * Блокировка трейда.
	 *
	 * @param player игрок.
	 */
	public synchronized void lock(Player player)
	{
		// если это инициатор
		if(actor == player)
			// блочим у него
			setActorLock(true);
		// если это опонент
		else if(enemy == player)
			// блочим у опонента
			setEnemyLock(true);

		// нсли у обоих диалог заблокирован
		if(isActorLock() && isEnemyLock())
		{
			// применяем
			apply();
			// закрываем окно
			cancel(null);
			return;
		}

		// обновляем диалог
		updateDialog();
	}

	protected final void setActorItems(Array<TradeItem> actorItems)
	{
		this.actorItems = actorItems;
	}

	protected final void setActorLock(boolean actorLock)
	{
		this.actorLock = actorLock;
	}

	protected final void setDone(boolean done)
	{
		this.done = done;
	}

	protected final void setEnemyLock(boolean enemyLock)
	{
		this.enemyLock = enemyLock;
	}

	/**
	 * Обновление отображения диалога.
	 */
	protected void updateDialog()
	{
		Player actor = getActor();
		Player enemy = getEnemy();

		if(actor == null || enemy == null)
			return;

		// создаем новый пакет
		ServerPacket packet = ShowTrade.getInstance(actor, enemy, objectId, this);

		packet.increaseSends();
		packet.increaseSends();

		// отправляем
		actor.sendPacket(packet, false);
		enemy.sendPacket(packet, false);
	}
}
