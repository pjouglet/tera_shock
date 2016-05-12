package tera.gameserver.model.drop;

import rlib.logging.Loggers;
import rlib.util.array.Arrays;
import rlib.util.random.Random;
import tera.Config;
import tera.gameserver.manager.RandomManager;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель группы дропа итемов.
 *
 * @author Ronn
 */
public final class DropGroup
{
	/** список итемов группы */
	private DropInfo[] items;

	/** ид группы */
	private int id;
	/** шанс группы */
	private int chance;
	/** кол-во вызовов группы */
	private int count;

	/** является ли эта группа денежной */
	private boolean money;

	/**
	 * @param id ид группы.
	 * @param chance шанс группы.
	 * @param count кол-во вызовов группы.
	 */
	public DropGroup(int id, int chance, int count, DropInfo[] items)
	{
		this.id = id;
		this.chance = chance;
		this.items = items;
		this.count = count;
		this.money = true;

		// определяем, является ли группа группой денег
		for(DropInfo item : items)
			// если хоть 1 итем не деньги
			if(item.getItemId() != Inventory.MONEY_ITEM_ID)
			{
				// ставим флаг что это не денежная группа
				money = false;
				break;
			}
	}

	/**
	 * @return шанс группы.
	 */
	public final int getChance()
	{
		return chance;
	}

	/**
	 * @return кол-во вызовов группы.
	 */
	public final int getCount()
	{
		return count;
	}

	/**
	 * @return ид группы.
	 */
	public final int getId()
	{
		return id;
	}

	/**
	 * @return выпавший итем из группы.
	 */
	public ItemInstance getItem()
	{
		// получаем менеджер рандома
		RandomManager randManager = RandomManager.getInstance();

		Random rand = randManager.getDropRandom();

		// если шанс не выпал, выходим
		if(!rand.chance(chance))
			return null;

		// получаем итемы группы
		DropInfo[] items = getItems();

		// перебираем итемы группы
		for(int i = 0, length = items.length; i < length; i++)
		{
			// получаем инфу о итеме
			DropInfo data = items[i];

			// если он не выпадает, пропускаем
			if(rand.nextInt(0, 100000) > data.getChance())
				continue;

			// рассчитываем кол-во его
			int count = rand.nextInt(data.getMinCount(), data.getMaxCount());

			// если вышло нулевое, пропускаем
			if(count < 1)
				continue;

			// получаем темплейт итема
			ItemTemplate template = data.getItem();

			// если его нет, пропускаем
			if(template == null)
				continue;

			// проверяем отношение итема к донату
			if(Arrays.contains(Config.WORLD_DONATE_ITEMS, template.getItemId()))
			{
				Loggers.warning(this, new Exception("not create donate item for id " + template.getItemId()));
				continue;
			}

			// создаем новый инстанс итема
			ItemInstance item = template.newInstance();

			// если не получилось создать, пропускаем
			if(item == null)
				continue;

			// если итем не стакуемый
			if(template.isStackable())
				// вставляем в него новое кол-во
				item.setItemCount(count);

			// возвращаем итем
			return item;
		}

		return null;
	}

	/**
	 * @return список дропа.
	 */
	public final DropInfo[] getItems()
	{
		return items;
	}

	/**
	 * @return группа денег ли.
	 */
	public final boolean isMoney()
	{
		return money;
	}
}
