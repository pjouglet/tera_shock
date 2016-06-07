package tera.gameserver.model.items;

import java.util.concurrent.ScheduledFuture;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.SafeTask;
import rlib.util.Strings;
import rlib.util.pools.Foldable;
import tera.Config;
import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.manager.GameLogManager;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.Party;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.equipment.SlotType;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.CharPickUpItem;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.network.serverpackets.DeleteItem;
import tera.gameserver.network.serverpackets.ItemInfo;
import tera.gameserver.network.serverpackets.MessageAddedItem;
import tera.gameserver.network.serverpackets.ServerPacket;
import tera.gameserver.network.serverpackets.SystemMessage;
import tera.gameserver.templates.ItemTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель итема.
 * 
 * @author Ronn
 */
public abstract class ItemInstance extends TObject implements Foldable
{
	private static final Logger log = Loggers.getLogger(ItemInstance.class);

	/** кол-во итемов */
	protected long itemCount;

	protected int masterworked;

	/** отспавненное время */
	protected long spawnTime;

	/** ид владельца */
	protected int ownerId;
	/** ид бонуса к итему */
	protected int bonusId;
	/** уровень заточки */
	protected int enchantLevel;
	/** положение итема */
	protected int index;

	/** местоположение итема */
	protected ItemLocation location;

	/** автор итема */
	protected String autor;

	/** темплейт итема */
	protected ItemTemplate template;

	/** тот кто выронил итем */
	protected TObject dropper;
	/** тот ко выбил итем */
	protected TObject tempOwner;

	/** пати, которая выбила итем */
	protected Party tempOwnerParty;

	/** скилы итема */
	protected Skill[] skills;

	/** таск жизни итема */
	protected SafeTask lifeTask;
	/** таск блокировки итема */
	protected SafeTask blockTask;

	/** ссылка на таск жизни в мире итема */
	protected ScheduledFuture<SafeTask> lifeSchedule;
	/** ссылка на таск блока итема */
	protected ScheduledFuture<SafeTask> blockSchedule;

	/**
	 * @param objectId уникальный ид итема.
	 * @param template темплейт итема.
	 */
	public ItemInstance(int objectId, ItemTemplate template)
	{
		super(objectId);

		this.template = template;
		this.ownerId = 0;
		this.itemCount = 1;
		this.masterworked = 0;
		this.bonusId = 0;
		this.enchantLevel = 0;
		this.index = 0;
		this.autor = Strings.EMPTY;

		this.location = ItemLocation.NONE;

		SkillTemplate[] templates = template.getSkills();

		this.skills = new Skill[templates.length];

		for (int i = 0, length = templates.length; i < length; i++)
			skills[i] = templates[i].newInstance();

		this.lifeTask = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				if (System.currentTimeMillis() - spawnTime < (Config.WORLD_LIFE_TIME_DROP_ITEM * 100 - 1000))
					log.warning(this, new Exception("it's fast despawn"));

				deleteMe();
			}
		};

		this.blockTask = new SafeTask()
		{
			@Override
			protected void runImpl()
			{
				tempOwner = null;
				tempOwnerParty = null;
			}
		};
	}

	/**
	 * Выдача владельцу итема скилы итема.
	 *
	 * @param owner владелец итема.
	 */
	public final void addFuncsTo(Character owner)
	{
		// если персонажа нет
		if (owner == null)
			return;

		// добавляем функции итема
		template.addFuncsTo(owner);

		// получаем список кристалов в итеме
		CrystalList crystals = getCrystals();

		// если их нет ,выходим
		if (crystals == null || crystals.isEmpty())
			return;

		// добавляем функции кристалов
		crystals.addFuncs(owner);
	}

	/**
	 * @param count кол-во которое нужно добавить.
	 */
	public final void addItemCount(long count)
	{
		itemCount += count;
	}

	@Override
	public void addMe(Player player)
	{
		player.sendPacket(ItemInfo.getInstance(this), true);
	}

	/**
	 * Проверка на совместимость игрока с итемом.
	 *
	 * @param player проверяемый игрок.
	 * @return совместим ли.
	 */
	public boolean checkClass(Player player)
	{
		return template.checkClass(player);
	}

	/**
	 * Проверка, можно ли вставить крист в итем.
	 *
	 * @param crystal вставляемый кристал.
	 */
	public boolean checkCrystal(CrystalInstance crystal)
	{
		return false;
	}

	@Override
	public void deleteMe()
	{
		super.deleteMe();

		synchronized (this)
		{
			// если есть таск жизни, отменяем и зануляем
			if (lifeSchedule != null)
			{
				lifeSchedule.cancel(false);
				lifeSchedule = null;
			}

			// если есть таск блокировки, отменяем и зануляем
			if (blockSchedule != null)
			{
				blockSchedule.cancel(false);
				blockSchedule = null;
			}

			if (isDeleted())
				return;

			// складируем в пул
			template.put(this);
		}
	}

	/**
	 * Проверка на возможность одевания итема персонажем.
	 *
	 * @param character персонаж.
	 * @return можно ли.
	 */
	public boolean equipmentd(Character character, boolean showMessage)
	{
		return true;
	}

	@Override
	public void finalyze()
	{
		tempOwner = null;
		dropper = null;
		tempOwnerParty = null;
		autor = Strings.EMPTY;
		bonusId = 0;
		dropper = null;
		enchantLevel = 0;
		itemCount = 1;
		masterworked = 0;
		ownerId = 0;
		objectId = 0;
		index = 0;
		location = ItemLocation.NONE;
	}

	/**
	 * @return активный скил итема.
	 */
	public Skill getActiveSkill()
	{
		return null;
	}

	/**
	 * @return экземпляр брони.
	 */
	public ArmorInstance getArmor()
	{
		return null;
	}

	/**
	 * @return бонус к атаке.
	 */
	public int getAttack()
	{
		return template.getAttack();
	}

	/**
	 * @return имя автора итема.
	 */
	public final String getAutor()
	{
		return autor;
	}

	/**
	 * @return бонус к балансу.
	 */
	public int getBalance()
	{
		return template.getBalance();
	}

	/**
	 * @return ид бонуса.
	 */
	public final int getBonusId()
	{
		return bonusId;
	}

	/**
	 * @return тип боундинга итема.
	 */
	public BindType getBoundType()
	{
		return template.getBindType();
	}

	/**
	 * @return стоимость покупки итема.
	 */
	public final int getBuyPrice()
	{
		return template.getBuyPrice();
	}

	/**
	 * @return ид класса скилов итема.
	 */
	public int getClassIdItemSkill()
	{
		return template.getClassIdItemSkill();
	}

	/**
	 * @return экземпляр обычного итема.
	 */
	public CommonInstance getCommon()
	{
		return null;
	}

	/**
	 * @return экземпляр кристала.
	 */
	public CrystalInstance getCrystal()
	{
		return null;
	}

	/**
	 * @return вставленные кристалы в итем.
	 */
	public CrystalList getCrystals()
	{
		return null;
	}

	/**
	 * @return бонус к защите.
	 */
	public int getDefence()
	{
		return template.getDefence();
	}

	/**
	 * @return выравниший объект.
	 */
	public final TObject getDropper()
	{
		return dropper;
	}

	/**
	 * @return уровень заточки.
	 */
	public final int getEnchantLevel()
	{
		return enchantLevel;
	}

	/**
	 * @return уровень экстракта.
	 */
	public int getExtractable()
	{
		return template.getExtractable();
	}

	/**
	 * @return бонус к опрокидыванию.
	 */
	public int getImpact()
	{
		return template.getImpact();
	}

	/**
	 * @return индекс ячейки, в которой лежит итем.
	 */
	public final int getIndex()
	{
		return index;
	}

	/**
	 * @return класс итема.
	 */
	public final ItemClass getItemClass()
	{
		return template.getItemClass();
	}

	/**
	 * @return кол-во итемов.
	 */
	public final long getItemCount()
	{
		return itemCount;
	}

	/**
	 * @return masterworked value
	 */
	public final int getMasterworked() { return this.masterworked; }

	/**
	 * @return ид темплейта итема.
	 */
	public final int getItemId()
	{
		return template.getItemId();
	}

	/**
	 * @return уровень итема.
	 */
	public final int getItemLevel()
	{
		return template.getItemLevel();
	}

	/**
	 * @return местоположение итема.
	 */
	public final ItemLocation getLocation()
	{
		return location;
	}

	/**
	 * @return местоположение итема.
	 */
	public final int getLocationId()
	{
		return location.ordinal();
	}

	/**
	 * @return название итема.
	 */
	@Override
	public final String getName()
	{
		return template.getName();
	}

	/**
	 * @return ид владельца.
	 */
	public final int getOwnerId()
	{
		return ownerId;
	}

	/**
	 * @return имя владельца итема.
	 */
	public String getOwnerName()
	{
		return Strings.EMPTY;
	}

	/**
	 * @return ранк итема.
	 */
	public Rank getRank()
	{
		return template.getRank();
	}

	/**
	 * @return минимальный уровень для одевания.
	 */
	public int getRequiredLevel()
	{
		return template.getRequiredLevel();
	}

	/**
	 * @return стоимость продажи.
	 */
	public final int getSellPrice()
	{
		return template.getSellPrice();
	}

	/**
	 * @return список скилов.
	 */
	public final SkillTemplate[] getSkills()
	{
		return template.getSkills();
	}

	/**
	 * @return тип экиперуемого слота.
	 */
	public final SlotType getSlotType()
	{
		return template.getSlotType();
	}

	/**
	 * @return кол-во сокетов под кристалы.
	 */
	public int getSockets()
	{
		return template.getSockets();
	}

	@Override
	public final int getSubId()
	{
		return Config.SERVER_ITEM_SUB_ID;
	}

	/**
	 * @return темплейт итема.
	 */
	public ItemTemplate getTemplate()
	{
		return template;
	}

	/**
	 * @return выбивший итем.
	 */
	public final TObject getTempOwner()
	{
		return tempOwner;
	}

	/**
	 * @return выбившая пати итем.
	 */
	public final Party getTempOwnerParty()
	{
		return tempOwnerParty;
	}

	/**
	 * @return тип итема.
	 */
	public Enum<?> getType()
	{
		return template.getType();
	}

	/**
	 * @return экземпляр оружия.
	 */
	public WeaponInstance getWeapon()
	{
		return null;
	}

	/**
	 * @return вставлен ли в итем хоть 1 кристал.
	 */
	public boolean hasCrystals()
	{
		// получаем список кристалов
		CrystalList crystals = getCrystals();

		// проверяемпуст ли он
		return crystals != null && !crystals.isEmpty();
	}

	/**
	 * @return есть ли владелец у итема.
	 */
	public boolean hasOwner()
	{
		return ownerId > 0;
	}

	/**
	 * @return является ли итем броней.
	 */
	public boolean isArmor()
	{
		return false;
	}

	/**
	 * @return можно ли ложить в банк итем.
	 */
	public final boolean isBank()
	{
		return template.isBank();
	}

	/**
	 * @return забинден ли итем.
	 */
	public boolean isBinded()
	{
		return false;
	}

	/**
	 * @return является ли итем обычным.
	 */
	public boolean isCommon()
	{
		return false;
	}

	/**
	 * @return является ли итем кристалом.
	 */
	public boolean isCrystal()
	{
		return false;
	}

	/**
	 * @return можно ли удалять итем.
	 */
	public final boolean isDeletable()
	{
		return template.isDeletable();
	}

	/**
	 * @return можно ли точить итем.
	 */
	public boolean isEnchantable()
	{
		return template.isEnchantable();
	}

	/**
	 * @return можно ли ложить итем в гильдию банка.
	 */
	public final boolean isGuildBank()
	{
		return template.isGuildBank();
	}

	/**
	 * @return является ли итем хербом.
	 */
	public boolean isHerb()
	{
		return false;
	}

	@Override
	public final boolean isItem()
	{
		return true;
	}

	/**
	 * @return можно ли ремоделировать итем.
	 */
	public boolean isRemodelable()
	{
		return template.isRemodelable();
	}

	/**
	 * @return можно ли продавать итем.
	 */
	public final boolean isSellable()
	{
		return template.isSellable();
	}

	/**
	 * @return стакуемый ли итем.
	 */
	public boolean isStackable()
	{
		return template.isStackable();
	}

	/**
	 * @return можно ли передовать итем.
	 */
	public final boolean isTradable()
	{
		return template.isTradable();
	}

	/**
	 * @return является ли итем оружием.
	 */
	public boolean isWeapon()
	{
		return false;
	}

	@Override
	public boolean pickUpMe(TObject target)
	{
		// если объект не видим, выходим
		if (!isVisible())
			return false;

		// если поднимающего нет, выходим
		if (target == null)
		{
			log.warning(this, new Exception("not found target"));
			return false;
		}

		// пробуем получить персонажа
		Character character = target.getCharacter();

		// если его нет, выходим
		if (character == null)
		{
			log.warning(this, new Exception("not found character"));
			return false;
		}

		// если персонаж в движении
		if (character.isMoving())
		{
			character.sendMessage("Нельзя поднимать во время движения.");
			return false;
		}

		// получаем группу
		Party party = character.getParty();

		// был ли поднят итем
		boolean pickUped = false;

		synchronized (this)
		{
			// если объект не видим, выходим
			if (!isVisible())
				return false;

			try
			{
				// если группа есть
				if (party != null)
					// передаем группе
					pickUped = party.pickUpItem(this, character);
				else
				{
					// получаем инвентарь персонажа
					Inventory inventory = character.getInventory();

					// если его нет, выходим
					if (inventory == null)
					{
						log.warning(this, new Exception("not found inventory"));
						return false;
					}

					// если объект не видим, выходим
					if (!isVisible())
						return false;

					long itemCount = getItemCount();

					// пробуем положить итем в инвентарь
					if (inventory.putItem(this))
					{
						// получаем логгер игровых событий
						GameLogManager gameLogger = GameLogManager.getInstance();

						// записываем событие выдачи итема
						gameLogger.writeItemLog(character.getName() + " pick up item [id = " + getItemId() + ", count = " + itemCount + ", name = " + template.getName() + "]");

						// ссылка на пакет сообщения
						ServerPacket packet = null;

						// если это деньги
						if (template.getItemId() != Inventory.MONEY_ITEM_ID)
							packet = MessageAddedItem.getInstance(character.getName(), template.getItemId(), (int) itemCount);
						else
						{
							packet = SystemMessage.getInstance(MessageType.ADD_MONEY).addMoney(character.getName(), (int) itemCount);
						}

						// отправляем сообщение
						character.sendPacket(packet, true);

						// ставим флаг поднятости итема
						pickUped = true;

						return true;
					}
					else if (character.isPlayer())
						character.sendMessage(MessageType.INVENTORY_IS_FULL);
				}
			}
			finally
			{
				if (pickUped)
				{
					// получаем менеджер событий
					ObjectEventManager eventManager = ObjectEventManager.getInstance();

					// обновляем инвентарь
					eventManager.notifyInventoryChanged(character);
					// увндомляем о подъеме итема
					eventManager.notifyPickUpItem(character, this);

					// отображаем подъем итема
					character.broadcastPacket(CharPickUpItem.getInstance(character, this));

					// если итем сам добавился в инвентарь
					if (hasOwner())
						// удаляем отображение итема в мире
						decayMe(DeleteCharacter.DISAPPEARS);
					else
						// иначе удаляем итем из мира
						deleteMe();

					// останавливаем таск жизни
					if (lifeSchedule != null)
					{
						lifeSchedule.cancel(true);
						lifeSchedule = null;
					}

					// останавливаем таск блока
					if (blockSchedule != null)
					{
						blockSchedule.cancel(true);
						blockSchedule = null;
					}

					// зануляем владельцев
					tempOwner = null;
					tempOwnerParty = null;
				}
			}
		}

		return false;
	}

	@Override
	public void reinit()
	{
	}

	/**
	 * Удаление скилов итема у персонажа.
	 *
	 * @param owner персонаж.
	 */
	public final void removeFuncsTo(Character owner)
	{
		// если персонажа нет
		if (owner == null)
			return;

		// удаляем функции итема
		template.removeFuncsTo(owner);

		// получаем список кристалов
		CrystalList crystals = getCrystals();

		// если их нет, выходим
		if (crystals == null || crystals.isEmpty())
			return;

		// удаляем функции кристалов
		crystals.removeFuncs(owner);
	}

	@Override
	public void removeMe(Player player, int type)
	{
		player.sendPacket(DeleteItem.getInstance(this), true);
	}

	/**
	 * @param autor автор итема.
	 */
	public final void setAutor(String autor)
	{
		this.autor = autor;
	}

	/**
	 * @param bonusId ид бонуса.
	 */
	public final void setBonusId(int bonusId)
	{
		this.bonusId = bonusId;
	}

	/**
	 * @param dropper выравнивший итем.
	 */
	public final void setDropper(TObject dropper)
	{
		this.dropper = dropper;
	}

	/**
	 * @param enchantLevel уровень заточки.
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		this.enchantLevel = enchantLevel;
	}

	/**
	 * @param index the index to set
	 */
	public final void setIndex(int index)
	{
		this.index = index;
	}

	/**
	 * @param itemCount кол-во итемов.
	 */
	public final void setItemCount(long itemCount)
	{
		this.itemCount = itemCount;
	}

	/**
	 * @param masterworked masterworked value
	 */
	public final void setMasterworked(int masterworked){
		this.masterworked = masterworked;
	}

	/**
	 * @param location the location to set
	 */
	public final void setLocation(ItemLocation location)
	{
		this.location = location;
	}

	/**
	 * @param objectId уникальный ид итема.
	 */
	@Override
	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @param ownerId ид владельца.
	 */
	public final void setOwnerId(int ownerId)
	{
		this.ownerId = ownerId;
	}

	/**
	 * @param ownerName имя владельца итема.
	 */
	public void setOwnerName(String ownerName)
	{
	}

	/**
	 * @param tempOwner выбивший итем.
	 */
	public final void setTempOwner(TObject tempOwner)
	{
		this.tempOwner = tempOwner;
	}

	/**
	 * @param tempOwnerParty выбивший итем.
	 */
	public final void setTempOwnerParty(Party tempOwnerParty)
	{
		this.tempOwnerParty = tempOwnerParty;
	}

	@Override
	public void spawnMe()
	{
		synchronized (this)
		{
			if (isVisible())
				return;

			// получаем время спавна
			spawnTime = System.currentTimeMillis();

			// увеличиваем счетчик дропнутых итемов
			World.addDroppedItems();

			// получаем исполнительного менеджера
			ExecutorManager executor = ExecutorManager.getInstance();

			// запускаем таск жизни
			this.lifeSchedule = executor.scheduleGeneral(lifeTask, Config.WORLD_LIFE_TIME_DROP_ITEM * 1000);

			// если есть владелец, блокируем итем
			if (tempOwner != null || tempOwnerParty != null)
				this.blockSchedule = executor.scheduleGeneral(blockTask, Config.WORLD_BLOCK_TIME_DROP_ITEM * 1000);
		}

		super.spawnMe();
	}

	/**
	 * @param count кол-во отнимаемых итемов.
	 */
	public final void subItemCount(long count)
	{
		itemCount -= count;

		if (itemCount < 0)
			itemCount = 0;
	}

	@Override
	public String toString()
	{
		return "ItemInstance type = " + template.getType() + ", name = " + template.getName() + ", itemCount = " + itemCount + ", ownerId = " + ownerId + ", objectId = " + objectId + ", location = " + location;
	}
}