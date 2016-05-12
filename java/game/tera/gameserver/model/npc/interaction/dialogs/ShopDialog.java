package tera.gameserver.model.npc.interaction.dialogs;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.array.FuncElement;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.Config;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.BuyableItem;
import tera.gameserver.model.SellableItem;
import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ShopReplyPacket;
import tera.gameserver.network.serverpackets.ShopTradePacket;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель окна магазина.
 *
 * @author Ronn
 */
public final class ShopDialog extends AbstractDialog
{
	private static final FuncElement<BuyableItem> BUYABLE_ITEM_FOLD = new FuncElement<BuyableItem>()
	{
		@Override
		public void apply(BuyableItem item)
		{
			item.fold();
		}
	};

	private static final FuncElement<SellableItem> SELLABLE_ITEM_FOLD = new FuncElement<SellableItem>()
	{
		@Override
		public void apply(SellableItem item)
		{
			item.fold();
		}
	};

	/**
	 * @param npc нпс.
	 * @param sections секции с итемами.
	 * @param availableItems доступные итемы.
	 * @param player игрок.
	 * @param sectionId ид первой секции.
	 * @return новый диалог.
	 */
	public static final ShopDialog newInstance(Npc npc, ItemTemplate[][] sections, Table<IntKey, ItemTemplate> availableItems, Player player, Bank bank, int sectionId, float resultTax)
	{
		ShopDialog dialog = (ShopDialog) DialogType.SHOP_WINDOW.newInstance();

		dialog.availableItems = availableItems;
		dialog.npc = npc;
		dialog.player = player;
		dialog.bank = bank;
		dialog.sections = sections;
		dialog.sectionId =  sectionId;
		dialog.resultTax = resultTax;

		return dialog;
	}

	/** массив секций с итемами */
	private ItemTemplate[][] sections;

	/** доступные для продажи итемы */
	private Table<IntKey, ItemTemplate> availableItems;

	/** внесеные на покупку итиемы */
	private Array<BuyableItem> buyItems;

	/** внесенные на продажу итемы */
	private Array<SellableItem> sellItems;

	/** банк для отчислений */
	private Bank bank;

	/** ид первой секции */
	private int sectionId;

	/** налон на товар */
	private float resultTax;

	/**
	 * @param npc нпс.
	 * @param sections секции с итемами.
	 * @param availableItems доступные итемы.
	 * @param player игрок.
	 * @param sectionId ид первой секции.
	 */
	protected ShopDialog()
	{
		this.buyItems = Arrays.toArray(BuyableItem.class, 8);
		this.sellItems = Arrays.toArray(SellableItem.class, 8);
	}

	/**
	 * Добавляет в покупаемые итемы итем с указанным ид и кол-вом.
	 *
	 * @param itemId ид итема.
	 * @param count кол-во итемов.
	 * @return добавлен ли итем.
	 */
	public synchronized boolean addBuyItem(int itemId, long count)
	{
		if(count < 1)
			return false;

		if(Arrays.contains(Config.WORLD_DONATE_ITEMS, itemId))
		{
			log.warning(this, "not added donate item for " + itemId);
			return false;
		}

		// темплейт добавляемого итема
		ItemTemplate template = availableItems.get(itemId);

		// если такого нет - выходим
		if(template == null)
		{
			log.warning(this, new Exception("not found template"));
			return false;
		}

		// получаем список продаваемых магазинов
		Array<BuyableItem> buyItems = getBuyItems();

		// если итем не стакуем, и мы не уперлись в лимит
		if(!template.isStackable() && buyItems.size() < 8)
		{
			// добавляем его
			buyItems.add(BuyableItem.newInstance(template, 1));
			return true;
		}

		// если итем стакуем
		if(template.isStackable())
		{
			BuyableItem[] array = buyItems.array();

			// ищем однотипные итем
			for(int i = 0, length = buyItems.size(); i < length; i++)
			{
				BuyableItem item = array[i];

				// если итем однотипный
				if(item.getItemId() == itemId)
				{
					// увеличиваем его кол-во
					item.addCount(count);
					return true;
				}
			}
			// если однотипного не нашли и не уперлись в лимит
			if(buyItems.size() < 8)
			{
				// добавляем его
				buyItems.add(BuyableItem.newInstance(template, count));
				return true;
			}
		}

		return false;
	}

	/**
	 * Добавление итемов с инвенторя на продажу.
	 *
	 * @param itemId ид итема.
	 * @param count кол-во итемов.
	 * @param index индекс ячейки в инвенторе.
	 * @return добавлен ли.
	 */
	public synchronized boolean addSellItem(int itemId, int count, int index)
	{
		// если кол-во нулевое, выходим
		if(count < 1)
			return false;

		// получаем игрока
		Player player = getPlayer();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		inventory.lock();
		try
		{
			// получаем ячеяку нужного индекса
			Cell cell = inventory.getCell(index);

			// если ее нет, выходим
			if(cell == null)
			{
				log.warning(this, new Exception("not found cell"));
				return false;
			}

			// получаем итем в этой ячейке
			ItemInstance old = cell.getItem();

			// если его нету или он неправильного ид, выходим
			if(old == null || old.getItemId() != itemId)
				return false;

			// если он не продаваемый
			if(!old.isSellable())
			{
				// сообщаем об этом и выходим
				player.sendMessage("This item can not be sold.");
				return false;
			}

			// получаем список продаваемых итемов
			Array<SellableItem> sellItems = getSellItems();

			// если не стаукуемый
			if(!old.isStackable())
			{
				// если нет свободной ячейки, выходим
				if(sellItems.size() > 7 || sellItems.contains(old))
					return false;

				// добавляем в продаваемые
				sellItems.add(SellableItem.newInstance(old, inventory, 1));

				return true;
			}

			// если кол-во не соответвует действительности, выходим
			if(count > old.getItemCount())
				return false;

			// ищем однотипные итемы
			int i = sellItems.indexOf(old);

			// если не нашли
			if(i < 0)
			{
				// если упираемся в лимит, выходим
				if(sellItems.size() > 7)
					return false;

				// добавляем
				sellItems.add(SellableItem.newInstance(old, inventory, count));

				return true;
			}
			else
			{
				// получаем контейнер
				SellableItem sell = sellItems.get(i);

				// если кол-во не сходится, выходим
				if(sell.getCount() + count > old.getItemCount())
					return false;

				// увеличиваем кол-во
				sell.addCount(count);

				return true;
			}
		}
		finally
		{
			inventory.unlock();
		}
	}

	@Override
	public synchronized boolean apply()
	{
		// получаем игрка
		Player player = getPlayer();

		// если его нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// получаем инвентарь игрока
		Inventory inventory = player.getInventory();

		// получаем список продаваемых предметов
		Array<SellableItem> sellItems = getSellItems();

		// получаем список покупаемых итемов
		Array<BuyableItem> buyItems = getBuyItems();

		// получаем банк для отчислений
		Bank bank = getBank();

		// получаем налоговую ставку
		float tax = getResultTax();

		// получаем логера игровых событий
		GameLogManager gameLogger = GameLogManager.getInstance();

		lock(bank);
		try
		{
			inventory.lock();
			try
			{
				// доступные деньги
				long availableMoney = inventory.getMoney();

				// список продаваемых итемов
				SellableItem[] sellArray = sellItems.array();
				// список покупаемых итемов
				BuyableItem[] buyArray = buyItems.array();

				// если есть продаваемые
				if(!sellItems.isEmpty())
				{
					// проверяем на коррекцию
					for(int i = 0, length = sellItems.size(); i < length; i++)
						if(!sellArray[i].check())
							return false;

					// сумируем получаемые деньги
					for(int i = 0, length = sellItems.size(); i < length; i++)
						availableMoney += sellArray[i].getSellPrice();
				}

				// необходимые деньги для покупки покупаемых итемов
				long neededMoney = 0;

				// итог по рассчету
				long result = 0;

				// если есть покупаемые
				if(!buyItems.isEmpty())
					for(int i = 0, length = buyItems.size(); i < length; i++)
						// сумируем необходимые деньги
						neededMoney += (buyArray[i].getBuyPrice() + tax);

				// если нужных денег больше чем ИНТ или больше чем будет у нас денег
				if(neededMoney > Integer.MAX_VALUE || neededMoney > availableMoney)
					return false;

				// если есть продаваемы итемы
				if(!sellItems.isEmpty())
					for(int i = 0, length = sellItems.size(); i < length; i++)
					{
						SellableItem sell = sellArray[i];

						// получаем кол-во зарабатываемых денег за продажу
						long money = sell.getSellPrice();

						// получаем продаваемый итем
						ItemInstance item = sell.getItem();

						// получаем кол-во продаваемых итемов
						long count = sell.getCount();

						// выдаем получаемые деньги
						inventory.addMoney(money);

						// прибавляем
						result += money;

						// удаляем итем с инвенторя
						sell.deleteItem();

						// ложим оболочку в пул
						sell.fold();

						// записываем в лог о итоге продажи итема
						gameLogger.writeItemLog(player.getName() + " sell item [id = " + item.getItemId() + ", count = " + count + ", name = " + item.getName() + "] for " + money + " gold");
					}

				// если есть покупаемые итемы
				if(!buyItems.isEmpty())
					for(int i = 0, length = buyItems.size(); i < length; i++)
					{
						BuyableItem buy = buyArray[i];

						// получаем продаваемый итем
						ItemTemplate item = buy.getItem();

						// получаем кол-во покупаемых итемов
						long count = buy.getCount();

						// получаем итоговую цену
						long price = (long) (buy.getBuyPrice() * tax);

						// выдает покупаемый итем
						if(inventory.addItem(buy.getItemId(), buy.getCount(), "Merchant"))
						{
							// если итем выдался, забираем деньги
							inventory.subMoney(price);

							// записываем в лог о итоге покупки итема
							gameLogger.writeItemLog(player.getName() + " buy item [id = " + item.getItemId() + ", count = " + count + ", name = " + item.getName() + "] for " + price + " gold");
						}

						// если есть банк для отчислений
						if(bank != null)
							// выдаем деньги
							bank.addMoney(price - buy.getBuyPrice());

						// прибавляем
						result -= price;

						// ложим оболочку в пул
						buy.fold();
					}

				// очищаем список покупаемых итемов
				buyItems.clear();

				// очищаем список продаваемых итемов
				sellItems.clear();

				if(result < 0)
					PacketManager.showPaidGold(player, (int) -result);
				else if(result > 0)
					PacketManager.showAddGold(player, (int) result);

				// отправляем новое окно
				PacketManager.showShopDialog(player, this);

				// получаем менеджера событий
				ObjectEventManager eventManager = ObjectEventManager.getInstance();

				// обновляем инвентарь
				eventManager.notifyInventoryChanged(player);

				return true;
			}
			finally
			{
				inventory.unlock();
			}
		}
		finally
		{
			unlock(bank);
		}
	}

	/**
	 * @return банк для отчислений.
	 */
	public Bank getBank()
	{
		return bank;
	}

	@Override
	public void finalyze()
	{
		// получаем список покупаемых итемов
		Array<BuyableItem> buyItems = getBuyItems();

		// складируем все элементы
		buyItems.apply(BUYABLE_ITEM_FOLD);

		// очищаем список
		buyItems.clear();

		// получаеим список продаваемых итемов
		Array<SellableItem> sellItems = getSellItems();

		// складируем все элементы
		sellItems.apply(SELLABLE_ITEM_FOLD);

		// очищаем список
		sellItems.clear();

		super.finalyze();
	}

	/**
	 * @return список покупаемых итемов.
	 */
	public Array<BuyableItem> getBuyItems()
	{
		return buyItems;
	}

	/**
	 * @return общая сумма покупаемых вещей.
	 */
	public synchronized long getBuyPrice()
	{
		// итоговая сумма
		long price = 0;

		// получаем массив продаваемых итемов
		BuyableItem[] array = buyItems.array();

		// получаем налоговую ставку
		float tax = getResultTax();

		// подсчитываем итоговую сумму
		for(int i = 0, length = buyItems.size(); i < length; i++)
			price += (array[i].getBuyPrice() * tax);

		return price;
	}

	/**
	 * @return список продаваемых вещей.
	 */
	public Array<SellableItem> getSellItems()
	{
		return sellItems;
	}

	/**
	 * @return общая сумма продаваемых вещей.
	 */
	public synchronized long getSellPrice()
	{
		// итоговое кол-во денег
		long price = 0;

		// получаем список
		SellableItem[] array = sellItems.array();

		// подсчитываем выручаемые средства
		for(int i = 0, length = sellItems.size(); i < length; i++)
			price += array[i].getSellPrice();

		return price;
	}

	/**
	 * @return итоговый налог.
	 */
	public float getResultTax()
	{
		return resultTax;
	}

	@Override
	public DialogType getType()
	{
		return DialogType.SHOP_WINDOW;
	}

	@Override
	public synchronized boolean init()
	{
		if(!super.init())
			return false;

		Player player = getPlayer();

		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		player.sendPacket(ShopReplyPacket.getInstance(sections, player, sectionId), true);
		player.sendPacket(ShopTradePacket.getInstance(this), true);

		return true;
	}

	/**
	 * @param player покупатель.
	 */
	public void setPlayer(Player player)
	{
		this.player = player;
	}

	/**
	 * Удаляет из пакупаемых итемов итем с указанным ид и кол-вом.
	 *
	 * @param itemId ид итема.
	 * @param count кол-во итемов.
	 * @return убран ли итем.
	 */
	public synchronized boolean subBuyItem(int itemId, int count)
	{
		if(count < 1)
			return false;

		// получаем список покупаемых итемов
		Array<BuyableItem> buyItems = getBuyItems();

		// получаем массив итемов
		BuyableItem[] array = buyItems.array();

		// перебираем итемы
		for(int i = 0, length = buyItems.size(); i < length; i++)
		{
			// получаем покупаемый итем
			BuyableItem item = array[i];

			// если это искомый
			if(item.getItemId() == itemId)
			{
				// уменьшаем кол-во итемов
				item.subCount(count);

				// если они уже отсутствуют
				if(item.getCount() < 1)
				{
					// удаляем из списка
					buyItems.fastRemove(i);

					// складируем контейнер
					item.fold();
				}

				// выходим
				return true;
			}
		}

		return false;
	}

	/**
	 * Удалят продаваемый итем.
	 *
	 * @param itemId ид итема.
	 * @param count кол-во итемов.
	 * @param objectId уникальный ид итема.
	 * @return успешно ли убран.
	 */
	public synchronized boolean subSellItem(int itemId, int count, int objectId)
	{
		if(count < 1)
			return false;

		//получаем список продаваемых итемов
		Array<SellableItem> sellItems = getSellItems();

		// получаем массив итемов
		SellableItem[] array = sellItems.array();

		// перебираем итемы
		for(int i = 0, length = sellItems.size(); i < length; i++)
		{
			// получаем продаваемый итем
			SellableItem sell = array[i];

			// если это искомый
			if(sell.getObjectId() == objectId)
			{
				// уменьшаем его кол-во
				sell.subCount(count);

				// если итемы кончились
				if(sell.getCount() < 1)
				{
					// удаляем из списка
					sellItems.fastRemove(i);

					// складируем в пул
					sell.fold();
				}

				// выходим
				return true;
			}
		}

		return false;
	}

	/**
	 * @param bank блокируемый банк.
	 */
	public void lock(Bank bank)
	{
		if(bank != null)
			bank.lock();
	}

	/**
	 * @param bank разблокируемый банк.
	 */
	public void unlock(Bank bank)
	{
		if(bank != null)
			bank.unlock();
	}
}
