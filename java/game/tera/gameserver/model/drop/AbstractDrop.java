package tera.gameserver.model.drop;

import java.util.Arrays;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import tera.Config;
import tera.gameserver.model.Character;
import tera.gameserver.model.TObject;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;

/**
 * Базовая модель дропа.
 *
 * @author Ronn
 */
public abstract class AbstractDrop implements Drop
{
	protected static final Logger log = Loggers.getLogger(Drop.class);

	/** группы дропа */
	protected DropGroup[] groups;

	/** ид темплейта, к которому принадлежит дроп */
	protected int templateId;

	public AbstractDrop(int templateId, DropGroup[] groups)
	{
		this.templateId = templateId;
		this.groups = groups;
	}

	@Override
	public void addDrop(Array<ItemInstance> container, TObject creator, Character owner)
	{
		// если владельца нет, выходим
		if(owner == null)
		{
			log.warning(this, new Exception("not found owner"));
			return;
		}

		// если генератора нет, выходим
		if(creator == null)
		{
			log.warning(this, new Exception("not found creator"));
			return;
		}

		// если условия не выполняются, выходим
		if(!checkCondition(creator, owner))
			return;

		// пробуем получить игрока
		Player player = owner.getPlayer();

		// начало рассчета рейтов дропа
		float dropRate = Config.SERVER_RATE_DROP_ITEM;
		// начало рассчета рейтов денег
		float moneyRate = Config.SERVER_RATE_MONEY;

		// определяем, есть ли премиум у игрока
		boolean payed = player.hasPremium();

		// если активирован бонус премиума на деньги, и игрок с премиумом
		if(Config.ACCOUNT_PREMIUM_MONEY && payed)
			// увеличиваем соответсвенно рейты на деньги
			moneyRate *= Config.ACCOUNT_PREMIUM_MONEY_RATE;

		// если активирован бонус премиума на дроп, и игрок с премиумом
		if(Config.ACCOUNT_PREMIUM_DROP && payed)
			// увеличиваем соответсвенно рейты на дроп
			dropRate *= Config.ACCOUNT_PREMIUM_DROP_RATE;

		// получаем группы с дропом
		DropGroup[] groups = getGroups();

		// перебираем группы
		for(int i = 0, length = groups.length; i < length; i++)
		{
			// получаем группу
			DropGroup group = groups[i];

			// получаем кол-во проходов по группе
			float maxCount = group.getCount();

			// если это не деньги
			if(!group.isMoney())
				// то увеличиваем на рейты
				maxCount *= dropRate;

			// получаем итоговое кол-во проходов
			int count = Math.max((int) maxCount, 1);

			// начинаем проходы
			for(int g = 0; g < count; g++)
			{
				// пробуем получить итем из группы
				ItemInstance item = group.getItem();

				// если его нет, проход пропускаем
				if(item == null)
					continue;

				// если это деньги
				if(item.getItemId() == Inventory.MONEY_ITEM_ID)
				{
					// увеличиваем кол-во на рейт денег
					long newCount = (long) (item.getItemCount() * moneyRate);

					// если кол-во меньше 1 о_О
					if(newCount < 1)
					{
						// удаляем итем
						item.deleteMe();
						// идем дальше
						continue;
					}

					// устанавливаем новое кол-во
					item.setItemCount(newCount);
				}

				// добавляем в контейнер
				container.add(item);
			}
		}
	}

	/**
	 * @return нужно ли вообще рассчитывать для владельца дроп.
	 */
	protected abstract boolean checkCondition(TObject creator, Character owner);

	/**
	 * @return группы с дропом.
	 */
	protected final DropGroup[] getGroups()
	{
		return groups;
	}

	@Override
	public int getTemplateId()
	{
		return templateId;
	}

	@Override
	public String toString()
	{
		return "AbstractDrop groups = " + Arrays.toString(groups) + ", templateId = " + templateId;
	}
}
