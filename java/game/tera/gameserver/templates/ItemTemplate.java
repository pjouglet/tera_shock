package tera.gameserver.templates;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Objects;
import rlib.util.Reloadable;
import rlib.util.VarTable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.IdFactory;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.equipment.SlotType;
import tera.gameserver.model.items.ArmorKind;
import tera.gameserver.model.items.BindType;
import tera.gameserver.model.items.ItemClass;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.Rank;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.funcs.Func;

/**
 * Базовая модель шаблона итемов.
 * 
 * @author Ronn
 */
public abstract class ItemTemplate implements Reloadable<ItemTemplate>
{
	protected static final Logger log = Loggers.getLogger(ItemTemplate.class);

	/** класс ид скилов для итемов */
	public static final byte CLASS_ID_ITEM_SKILL = -9;

	/** пул итемов с этим темплейтом */
	protected final FoldablePool<ItemInstance> itemPool;

	/** название итема */
	protected String name;

	/** ид итема */
	protected int itemId;
	/** уровень итема */
	protected int itemLevel;
	/** цена итема */
	protected int buyPrice;
	/** цена итема на прадажу */
	protected int sellPrice;

	/** тип одеваемого слота */
	protected SlotType slotType;
	/** тип ранка */
	protected Rank rank;
	/** класс итема */
	protected ItemClass itemClass;

	/** набор скилов, которые выдаются с итемом */
	protected SkillTemplate[] skills;

	/** набор функций */
	protected Func[] funcs;

	/** максимальное кол-во итемов в стопке */
	protected boolean stackable;
	/** продаваемый ли в магазин */
	protected boolean sellable;
	/** можно ли вещь ложить в свое хранилище */
	protected boolean bank;
	/** можно ли вещь хранить в хранилище гильдии */
	protected boolean guildBank;
	/** обменивающий ли */
	protected boolean tradable;
	/** можно ли удалять */
	protected boolean deletable;

	/** тип итема */
	protected Enum<?> type;

	public ItemTemplate(Enum<?> type, VarTable vars)
	{
		this.type = type;

		try
		{
			name = vars.getString("name");

			itemId = vars.getInteger("id");
			itemLevel = vars.getInteger("itemLevel", 1);
			buyPrice = vars.getInteger("buyPrice", 0);
			sellPrice = vars.getInteger("sellPrice", 0);

			rank = Rank.valueOfXml(vars.getString("rank", "common"));

			itemClass = vars.getEnum("class", ItemClass.class);

			itemPool = Pools.newConcurrentFoldablePool(ItemInstance.class);

			stackable = vars.getBoolean("stackable", true);
			sellable = vars.getBoolean("sellable", true);
			bank = vars.getBoolean("bank", true);
			tradable = vars.getBoolean("tradable", true);
			guildBank = vars.getBoolean("guildBank", true);
			deletable = vars.getBoolean("deletable", true);
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw e;
		}
	}

	/**
	 * Выдача функций персонажу.
	 * 
	 * @param character персонаж.
	 */
	public void addFuncsTo(Character character)
	{
		Func[] funcs = getFuncs();

		if(funcs.length < 1)
			return;

		for(int i = 0, length = funcs.length; i < length; i++)
			funcs[i].addFuncTo(character);
	}

	/**
	 * Проверка на одеваемость итема.
	 * 
	 * @param player игрок, который хочет одеть итем.
	 * @return можно ли игроку одеть этот итем.
	 */
	public boolean checkClass(Player player)
	{
		return true;
	}

	/**
	 * @return активный скил.
	 */
	public SkillTemplate getActiveSkill()
	{
		return null;
	}

	/**
	 * @return бонус к атаке.
	 */
	public int getAttack()
	{
		return 0;
	}

	/**
	 * @return баланс шмотки.
	 */
	public int getBalance()
	{
		return 0;
	}

	/**
	 * @return боунд.
	 */
	public BindType getBindType()
	{
		return BindType.NONE;
	}

	/**
	 * @return стоимость покупки.
	 */
	public final int getBuyPrice()
	{
		return buyPrice;
	}

	/**
	 * @return ид класса, по которому вытягиваются скилы.
	 */
	public int getClassIdItemSkill()
	{
		return CLASS_ID_ITEM_SKILL;
	}

	/**
	 * @return бонус к дефенсу.
	 */
	public int getDefence()
	{
		return 0;
	}

	/**
	 * @return уровень раборки тема на ресурсы.
	 */
	public int getExtractable()
	{
		return 0;
	}

	/**
	 * @return набор функций.
	 */
	public final Func[] getFuncs()
	{
		return funcs;
	}

	/**
	 * @return бонус к опрокидыванию.
	 */
	public int getImpact()
	{
		return 0;
	}

	/**
	 * @return класс итема.
	 */
	public final ItemClass getItemClass()
	{
		return itemClass;
	}

	/**
	 * @return ид итем.
	 */
	public final int getItemId()
	{
		return itemId;
	}

	/**
	 * @return уровень итема.
	 */
	public final int getItemLevel()
	{
		return itemLevel;
	}

	/**
	 * @return пул готовых экземпляров.
	 */
	public final FoldablePool<ItemInstance> getItemPool()
	{
		return itemPool;
	}

	/**
	 * @return вид брони.
	 */
	public ArmorKind getKind()
	{
		return ArmorKind.CLOTH;
	}

	/**
	 * @return название итема.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return ранг итема.
	 */
	public Rank getRank()
	{
		return rank;
	}

	/**
	 * @return минимальный уровень к экиперовке.
	 */
	public int getRequiredLevel()
	{
		return 0;
	}

	/**
	 * @return стоимость продажи.
	 */
	public final int getSellPrice()
	{
		return sellPrice;
	}

	/**
	 * @return скилы итемов.
	 */
	public final SkillTemplate[] getSkills()
	{
		return skills;
	}

	/**
	 * @return слот экиперовки.
	 */
	public final SlotType getSlotType()
	{
		return slotType;
	}

	/**
	 * @return лимит кристалов.
	 */
	public int getSockets()
	{
		return 0;
	}

	/**
	 * @return тип итема.
	 */
	public Enum<?> getType()
	{
		return type;
	}

	/**
	 * @return можно ли в хранилище ложить итем.
	 */
	public final boolean isBank()
	{
		return bank;
	}

	/**
	 * @return можно ли удалять итем.
	 */
	public final boolean isDeletable()
	{
		return deletable;
	}

	/**
	 * @return можно ли точить итем.
	 */
	public boolean isEnchantable()
	{
		return false;
	}

	/**
	 * @return можно ли итем ложить в хранилище гильдии.
	 */
	public final boolean isGuildBank()
	{
		return guildBank;
	}

	/**
	 * @return можно ли ремоделировать.
	 */
	public boolean isRemodelable()
	{
		return false;
	}

	/**
	 * @return можно ли продавать.
	 */
	public final boolean isSellable()
	{
		return sellable;
	}

	/**
	 * @return стакуем ли итем.
	 */
	public boolean isStackable()
	{
		return stackable;
	}

	/**
	 * @return можно ли передовать итем.
	 */
	public final boolean isTradable()
	{
		return tradable;
	}

	/**
	 * @return новый итем.
	 */
	public ItemInstance newInstance()
	{
		ItemInstance item = itemPool.take();

		IdFactory idFactory = IdFactory.getInstance();

		int objectId = idFactory.getNextItemId();

		if(item == null)
			item = itemClass.newInstance(objectId, this);

		item.setObjectId(idFactory.getNextItemId());

		DataBaseManager dbManager = DataBaseManager.getInstance();

		if(!dbManager.createItem(item))
			return null;

		return item;
	}

	/**
	 * @return новый итем.
	 */
	public ItemInstance newInstance(int objectId)
	{
		ItemInstance item = itemPool.take();

		if(item == null)
			item = itemClass.newInstance(objectId, this);

		item.setObjectId(objectId);

		return item;
	}

	/**
	 * Положить указанный итем в пул итемов.
	 * 
	 * @param item итем, который нужно положить.
	 */
	public void put(ItemInstance item)
	{
		itemPool.put(item);
	}

	@Override
	public void reload(ItemTemplate update)
	{
		if(getClass() != update.getClass())
			return;

		Objects.reload(this, update);
	}

	/**
	 * Удаление функций у персонажа.
	 * 
	 * @param character персонаж.
	 */
	public void removeFuncsTo(Character character)
	{
		Func[] funcs = getFuncs();

		if(funcs.length < 1)
			return;

		for(int i = 0, length = funcs.length; i < length; i++)
			funcs[i].removeFuncTo(character);
	}

	/**
	 * @param funcs набор функций итема.
	 */
	public final void setFuncs(Func[] funcs)
	{
		this.funcs = funcs;
	}

	/**
	 * @param sellPrice стоимость продажи итема.
	 */
	public void setSellPrice(int sellPrice)
	{
		this.sellPrice = sellPrice;
	}

	/**
	 * @param skills набор скилов итема.
	 */
	public final void setSkills(SkillTemplate[] skills)
	{
		this.skills = skills;
	}

	@Override
	public String toString()
	{
		return "ItemTemplate  name = " + name + ", itemId = " + itemId + ", buyPrice = " + buyPrice + ", sellPrice = " + sellPrice + ", type = " + type;
	}
}
