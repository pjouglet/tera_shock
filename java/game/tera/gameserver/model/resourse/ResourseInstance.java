package tera.gameserver.model.resourse;

import rlib.geom.Coords;
import rlib.util.Rnd;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.Config;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.Party;
import tera.gameserver.model.TObject;
import tera.gameserver.model.drop.ResourseDrop;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.DeleteResourse;
import tera.gameserver.network.serverpackets.MessageAddedItem;
import tera.gameserver.network.serverpackets.ResourseInfo;
import tera.gameserver.templates.ResourseTemplate;
import tera.util.LocalObjects;

/**
 * Базовая модель ресурсов.
 *
 * @author Ronn
 */
public abstract class ResourseInstance extends TObject
{
	/** список сборщиков ресурса */
	protected final Array<Character> collectors;

	/** темплейт инстанса */
	protected final ResourseTemplate template;

	/** спавн ресурса */
	protected ResourseSpawn spawn;

	/** группа, которая начала собирать */
	protected volatile Party party;

	/** заблокирован ли сбор */
	protected volatile boolean lock;

	public ResourseInstance(int objectId, ResourseTemplate template)
	{
		super(objectId);

		this.collectors = Arrays.toArray(Character.class);

		this.template = template;
	}

	@Override
	public void addMe(Player player)
	{
		player.sendPacket(ResourseInfo.getInstance(this), true);
	}

	/**
	 * @return подходит ли игрок для сбора.
	 */
	public boolean checkCondition(Player collector)
	{
		return true;
	}

	/**
	 * Собрать ресурс указанным персонажем.
	 *
	 * @param actor соберающий персонаж.
	 */
	public void collectMe(Character actor)
	{
		// если это не игрок, выходим
		if(!actor.isPlayer())
			return;

		// получаем игрока
		Player collector = actor.getPlayer();

		// если условия не выполнены, выходим
		if(!checkCondition(collector))
		{
			collector.sendMessage(MessageType.YOU_CANT_DO_THAT_RIGHT_NOW_TRY_AGAINT_IN_A_MOMENT);
			return;
		}

		synchronized(this)
		{
			if(isLock())
				return;

			// получаем активную группу
			Party party = getParty();

			// если активной пати нет, но есть уже соберающий, выходим
			if(party == null && !collectors.isEmpty())
			{
				actor.sendMessage(MessageType.ANOTHER_PLAYER_IS_ALREADY_GATHERING_THAT);
				return;
			}

			// если есть активная пати, и персонаж не из нее, то выходим
			if(party != null && actor.getParty() != party)
			{
				actor.sendMessage(MessageType.ANOTHER_PLAYER_IS_ALREADY_GATHERING_THAT);
				return;
			}

			// если персонаж уже есть в списке соберающих, выходим
			if(collectors.contains(actor))
			{
				actor.sendMessage(MessageType.YOU_CANT_DO_THAT_RIGHT_NOW_TRY_AGAINT_IN_A_MOMENT);
				return;
			}

			// если активной пати нет
			if(party == null)
				// заносим пати персонажа
				setParty(actor.getParty());
		}

		// добавляем в список соберальщиков
		collectors.add(actor);

		// запускаем сбор
		actor.doCollect(this);
	}

	@Override
	public void deleteMe()
	{
		// очищаем список сборщиков
		collectors.clear();

		// удаляем из мира
		super.deleteMe();

		// отдаем спавну
		spawn.onCollected(this);
	}

	/**
	 * Расчет шанса сбора для указанного игрока.
	 *
	 * @param player собирающий игрок.
	 * @return шанс сбора.
	 */
	public int getChanceFor(Player player)
	{
		return 80;
	}

	/**
	 * @return список собирающих персонажей.
	 */
	public final Array<Character> getCollectors()
	{
		return collectors;
	}

	/**
	 * @return собирающая группа.
	 */
	public final Party getParty()
	{
		return party;
	}

	@Override
	public ResourseInstance getResourse()
	{
		return this;
	}

	/**
	 * @return спавн ресурса.
	 */
	public final ResourseSpawn getSpawn()
	{
		return spawn;
	}

	@Override
	public int getSubId()
	{
		return Config.SERVER_RESOURSE_SUB_ID;
	}

	/**
	 * @return темплейт ресурса.
	 */
	public ResourseTemplate getTemplate()
	{
		return template;
	}

	@Override
	public int getTemplateId()
	{
		return template.getId();
	}

	/**
	 * Увеличение навыка сбора ресурса.
	 *
	 * @param player игрок ,которому надо увеличить навык.
	 */
	public void increaseReq(Player player)
	{
		log.warning(this, new Exception("unsupported method"));
	}

	/**
	 * @return заблокирован ли сбор.
	 */
	public boolean isLock()
	{
		return lock;
	}

	@Override
	public boolean isResourse()
	{
		return true;
	}

	/**
	 * Завершение сбора ресурса.
	 *
	 * @param collector тот кто завершает сбор.
	 * @param cancel отмена ли это.
	 */
	public void onCollected(Player collector, boolean cancel)
	{
		if(isDeleted())
			return;

		// блокируем след. попытки сбора
		setLock(true);

		// нужно ли удалять
		boolean finish = false;

		synchronized(this)
		{
			// если уже пустой список ,выходим
			if(collectors.isEmpty())
			{
				log.warning(this, new Exception("found incorrect finish collect."));
				return;
			}

			// удаляем из соберающих
			collectors.fastRemove(collector);

			// определяем, закончился ли сбор
			finish = collectors.isEmpty();
		}

		// если сбор успешен
		if(!cancel)
		{
			collector.addExp(template.getExp(), null, getName());

			// получаем темплейт ресурса
			ResourseTemplate template = getTemplate();

			// получаем дроп ресурса
			ResourseDrop drop = template.getDrop();

			// если дроп есть
			if(drop != null)
			{
				// получаем локальные объекты
				LocalObjects local = LocalObjects.get();

				// получаем список итемов
				Array<ItemInstance> items = local.getNextItemList();

				// рассчитываем полученный дроп
				drop.addDrop(items, this, collector);

				// если он есть
				if(!items.isEmpty())
				{
					// получаем инвентарь игрока
					Inventory inventory = collector.getInventory();

					// если инвенторя нет, уведомляем
					if(inventory == null)
						log.warning(this, new Exception("not found inventiry"));
					else
					{
						// получаем массив итемов
						ItemInstance[] array = items.array();

						// перебираем итемы
						for(int i = 0, length = items.size(); i < length; i++)
						{
							// получаем итем
							ItemInstance item = array[i];

							// если не удалось положить в инвентакрь
							if(!inventory.putItem(item))
							{
								// сообщаем об заполнености его
								collector.sendMessage(MessageType.INVENTORY_IS_FULL);

								// устанавливаем континент, на котором расположен итем
								item.setContinentId(collector.getContinentId());

								// расчитываем направление дропа
								int heading = Rnd.nextInt(0, 32000) + collector.getHeading();

								// рамчитываем дистанцию
								int dist = Rnd.nextInt(30, 60);

								// определяем новые координаты
								float x = Coords.calcX(collector.getX(), dist, heading);
								float y = Coords.calcY(collector.getY(), dist, heading);

								// запоминаем владельца
								item.setTempOwner(collector);
								// запоминаем того, кто выбрасывает
								item.setDropper(collector);

								// выбрасываем итем на землю
								item.spawnMe(x, y, collector.getZ(), 0);
							}
							else
							{
								// отправляем пакет о том, что итем положился
								collector.sendPacket(MessageAddedItem.getInstance(collector.getName(), item.getItemId(), (int) item.getItemCount()), true);

								// если итем без владельца
								if(!item.hasOwner())
									// удаляем его
									item.deleteMe();
							}
						}
					}
				}
			}

			// увеличиваем навык сбора
			increaseReq(collector);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// уведомляем о сборе
			eventManager.notifyCollect(this, collector);

			//TODO баф от ресурса
		}

		//TODO если это был последний активный сборщик и он не отменил сбор
		if(finish)
			// удаляем ресурс
			deleteMe();
	}

	@Override
	public void removeMe(Player player, int type)
	{
		player.sendPacket(DeleteResourse.getInstance(this, type), true);
	}

	/**
	 * @param lock блокирован ли сбор.
	 */
	public void setLock(boolean lock)
	{
		this.lock = lock;
	}

	/**
	 * @param party собирающая группа.
	 */
	public final void setParty(Party party)
	{
		this.party = party;
	}

	/**
	 * @param spawn спавн ресурса.
	 */
	public final void setSpawn(ResourseSpawn spawn)
	{
		this.spawn = spawn;
	}

	@Override
	public void spawnMe()
	{
		// убираем блок сбора
		setLock(lock);
		// спавним
		super.spawnMe();
	}

	@Override
	public String toString()
	{
		return "ResourseInstance [getTemplateId()=" + getTemplateId() + "]";
	}
}
