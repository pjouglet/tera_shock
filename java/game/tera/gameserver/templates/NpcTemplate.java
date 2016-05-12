 package tera.gameserver.templates;

import rlib.util.Strings;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.IdFactory;
import tera.gameserver.model.Character;
import tera.gameserver.model.MinionData;
import tera.gameserver.model.drop.NpcDrop;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.NpcType;
import tera.gameserver.model.npc.interaction.DialogData;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestData;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.tables.NpcTable;
import tera.gameserver.tables.SkillTable;

/**
 * Темплейт описывающий базовые параметры НПС.
 *
 * @author Ronn
 */
public final class NpcTemplate extends CharTemplate
{
	/** имя нпс */
	protected String name;
	/** титул нпс */
	protected String title;
	/** фракция нпс*/
	protected String factionId;

	/** дроп в нпс */
	protected NpcDrop drop;
	/** информация о минионах */
	protected MinionData minions;
	/** диалог с нпс */
	protected DialogData dialog;
	/** инфа об квестах для нпс */
	protected QuestData quests;

	/** конструктор */
	protected NpcType npcType;

	/** таблица всех скилов нпс */
	protected SkillTemplate[][] skills;

	/** ширина модели нпс */
	protected float geomRadius;
	/** высота модели нпс */
	protected float geomHeight;

	/** ид нпс */
	protected int id;
	/** ид для отображающего пакета */
	protected int iconId;
	/** тип нпс */
	protected int type;
	/** ид модели нпс */
	protected int modelId;
	/** опыт за убийство нпс */
	protected int exp;
	/** атака нпс */
	protected int attack;
	/** защита нпс */
	protected int defense;
	/** шанс опрокинуть */
	protected int impact;
	/** защита от опрокидывания */
	protected int balance;
	/** уровень нпс */
	protected int level;
	/** аггро ренж */
	protected int aggro;
	/** радиус фракции */
	protected int factionRange;
	/** дистангция опрокидывания */
	protected int owerturnDist;
	/** время опрокидывания */
	protected int owerturnTime;

	/** является ли РБ */
	protected boolean isRaid;
	/** имеет ли этот нпс дроп */
	protected boolean canDrop;
	/** есть ли иммунитет к опрокидыванию */
	protected boolean owerturnImmunity;

	/**
	 * @param vars таблица параметров.
	 */
	public NpcTemplate(VarTable vars, Func[] funcs)
	{
		super(vars, funcs);

		id = vars.getInteger("id");
		iconId = vars.getInteger("iconId", id);
		modelId = vars.getInteger("modelId", iconId);
		exp = vars.getInteger("exp", 0);
		attack = vars.getInteger("attack");
		defense = vars.getInteger("defense");
		impact = vars.getInteger("impact");
		balance = vars.getInteger("balance");
		level = vars.getInteger("level");
		type = vars.getInteger("type");
		owerturnDist = vars.getInteger("owerturnDist", 50);
		owerturnTime = vars.getInteger("owerturnTime", 4500);

		name = vars.getString("name");
		title = vars.getString("title", "");
		factionId = vars.getString("fractionId", Strings.EMPTY);

		geomRadius = vars.getFloat("geomRadius", 40F);
		geomHeight = vars.getFloat("geomHeight", 60F);

		aggro = vars.getShort("aggro", (short) 120);
		factionRange = vars.getShort("fractionRange", (short) 0);

		isRaid = vars.getBoolean("isRaid", false);
		owerturnImmunity = vars.getBoolean("owerturnImmunity", false);

		npcType = NpcType.valueOf(vars.getString("class"));

		quests = new QuestData();

		skills = new SkillTemplate[SkillGroup.length][];

		setSkills(SkillTable.parseSkills(vars.getString("skills", ""), NpcTable.NPC_SKILL_CLASS_ID));
	}

	/**
	 * @param quest добавляемый квест.
	 */
	public final void addQuest(Quest quest)
	{
		quests.addQuest(quest);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;

		if(obj == null)
			return false;

		if(getClass() != obj.getClass())
			return false;

		NpcTemplate other = (NpcTemplate) obj;

		if(id != other.id)
			return false;

		if(type != other.type)
			return false;

		return true;
	}

	/**
	 * @return аггро ренж нпс.
	 */
	public final int getAggro()
	{
		return aggro;
	}

	/**
	 * @return атака нпс.
	 */
	public final int getAttack()
	{
		return attack;
	}

	/**
	 * @return защита от опракидывания.
	 */
	public final int getBalance()
	{
		return balance;
	}

	/**
	 * @return защита нпс.
	 */
	public final int getDefense()
	{
		return defense;
	}

	/**
	 * @return данные о диалогах.
	 */
	public final DialogData getDialog()
	{
		return dialog;
	}

	/**
	 * Рассчет выпавших итемов.
	 *
	 * @param items список итемов.
	 * @param npc убитый нпс.
	 * @param attacker убийца нпс.
	 * @return список выповших итемов.
	 */
	public final Array<ItemInstance> getDrop(Array<ItemInstance> items, Npc npc, Character attacker)
	{
		// рассчитываем и добавляем дропнутые итемы
		drop.addDrop(items, npc, attacker);

		// возвращаем список
		return items;
	}

	/**
	 * @return опыт с нпс.
	 */
	public final int getExp()
	{
		return exp;
	}

	/**
	 * @return ид фракции.
	 */
	public final String getFactionId()
	{
		return factionId;
	}

	/**
	 * @return радиус фракции.
	 */
	public final int getFactionRange()
	{
		return factionRange;
	}

	/**
	 * @return высота модели.
	 */
	public final float getGeomHeight()
	{
		return geomHeight;
	}

	/**
	 * @return радиус модели.
	 */
	public final float getGeomRadius()
	{
		return geomRadius;
	}

	/**
	 * @return ид для отображающего пакета.
	 */
	public final int getIconId()
	{
		return iconId;
	}

	/**
	 * @return сила опрокидывания.
	 */
	public final int getImpact()
	{
		return impact;
	}

	/**
	 * @return уровень нпс.
	 */
	public final int getLevel()
	{
		return level;
	}

	/**
	 * @return данные о миньенах.
	 */
	public final MinionData getMinions()
	{
		return minions;
	}

	@Override
	public int getModelId()
	{
		return modelId;
	}

	/**
	 * @return имя нпс.
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * @return тип нпс.
	 */
	public final NpcType getNpcType()
	{
		return npcType;
	}


	/**
	 * @return дистанция опракидывания.
	 */
	public final int getOwerturnDist()
	{
		return owerturnDist;
	}

	/**
	 * @return время опрокидывания.
	 */
	public final int getOwerturnTime()
	{
		return owerturnTime;
	}

	/**
	 * @return список квестов от нпс.
	 */
	public final QuestData getQuests()
	{
		return quests;
	}

	/**
	 * @return таблица скилов нпс.
	 */
	public final SkillTemplate[][] getSkills()
	{
		return skills;
	}

	@Override
	public int getTemplateId()
	{
		return id;
	}

	@Override
	public int getTemplateType()
	{
		return type;
	}

	/**
	 * @return титул нпс.
	 */
	public final String getTitle()
	{
		return title;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;

		int result = 1;

		result = prime * result + id;
		result = prime * result + type;

		return result;
	}

	/**
	 * @return имеет ли нпс дроп.
	 */
	public final boolean isCanDrop()
	{
		return canDrop;
	}

	/**
	 * @return есть ли иммунитет к опрокидывани.
	 */
	public boolean isOwerturnImmunity()
	{
		return owerturnImmunity;
	}

	/**
	 * @return является ли РБ.
	 */
	public final boolean isRaid()
	{
		return isRaid;
	}

	/**
	 * @return новый эксемпляр нпс.
	 */
	public final Npc newInstance()
	{
		// получаем фабрику ИД
		IdFactory idFactory = IdFactory.getInstance();

		// создаем НПС с новым уникальным ид
		return newInstance(idFactory.getNextNpcId());
	}

	/**
	 * @return новый эксемпляр нпс.
	 */
	public final Npc newInstance(int objectId)
	{
		return npcType.newInstance(objectId, this);
	}

	/**
	 * @param canDrop имеет ли нпс дроп.
	 */
	public final void setCanDrop(boolean canDrop)
	{
		this.canDrop = canDrop;
	}

	/**
	 * @param dialog данные о диалоге.
	 */
	public final void setDialog(DialogData dialog)
	{
		this.dialog = dialog;
	}

	/**
	 * @param drop данные о дропе.
	 */
	public final void setDrop(NpcDrop drop)
	{
		this.drop = drop;
	}

	/**
	 * @param minions данныве о минионах.
	 */
	public final void setMinions(MinionData minions)
	{
		this.minions = minions;
	}

	/**
	 * @param newSkills список новых скилов.
	 */
	public void setSkills(Array<SkillTemplate> newSkills)
	{
		// перебираем все скилы
		for(SkillTemplate skill : newSkills)
		{
			// получаем группу скилов
			SkillGroup group = skill.getSkillGroup();

			// если скил не имеет группы, выходим
			if(group == SkillGroup.NONE)
				continue;

			// вносим в таблицу
			skills[group.ordinal()] = Arrays.addToArray(skills[group.ordinal()], skill, SkillTemplate.class);
		}
	}

	@Override
	public String toString()
	{
		return "NpcTemplate npcId = " + id + ", exp = " + exp + ", attack = " + attack + ", defense = " + defense + ", owerAttack = " + impact + ", owerDefense = " + balance + ", level = " + level + ", aggro = " + aggro + ", factionRange = " + factionRange + ", name = " + name + ", title = " + title + ", factionId = " + factionId + ", type = " + type + ", isRaid = " + isRaid
				+ ", canDrop = " + canDrop + ", drop = " + drop + ", minions = " + minions + ", dialog = " + dialog + ", geomRadius = " + geomRadius + ", geomHeight = " + geomHeight + ", npcType = " + npcType;
	}
}