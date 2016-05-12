package tera.gameserver.model.skillengine.funcs.chance;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.VarTable;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.parser.FuncParser;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.templates.SkillTemplate;

/**
 * Базовая модель шансовых функций.
 *
 * @author Ronn
 */
public abstract class AbstractChanceFunc implements ChanceFunc
{
	protected static final Logger log = Loggers.getLogger(ChanceFunc.class);

	/** скил */
	protected Skill skill;

	/** условие */
	protected Condition cond;

	/** ид скила */
	protected int id;
	/** класс скила */
	protected int classId;
	/** шанс срабатывания */
	protected int chance;

	/** срабатывать ли при атаке */
	protected boolean onAttack;
	/** срабатывать ли при крит атаке */
	protected boolean onCritAttack;
	/** срабатывать под атакой */
	protected boolean onAttacked;
	/** срабатывать под крит атакой */
	protected boolean onCritAttacked;
	/** срабатывать при опрокидывании */
	protected boolean onOwerturn;
	/** срабатывать ли при опрокидывании */
	protected boolean onOwerturned;
	/** срабатывать ли при блокировке */
	protected boolean onShieldBlocked;

	public AbstractChanceFunc(VarTable vars, Condition cond)
	{
		this.id = vars.getInteger("id");
		this.classId = vars.getInteger("class");
		this.chance = vars.getInteger("chance");
		this.cond = cond;
		this.onAttack = vars.getBoolean("attack", false);
		this.onCritAttack = vars.getBoolean("critAttack", false);
		this.onAttacked = vars.getBoolean("attacked", false);
		this.onCritAttacked = vars.getBoolean("critAttacked", false);
		this.onOwerturn = vars.getBoolean("owerturn", false);
		this.onOwerturned = vars.getBoolean("owerturned", false);
		this.onShieldBlocked = vars.getBoolean("shieldBlocked", false);

		FuncParser funcParser = FuncParser.getInstance();

		funcParser.addChanceFunc(this);
	}

	@Override
	public void addFuncTo(Character owner)
	{
		owner.addChanceFunc(this);
	}

	@Override
	public boolean apply(Character attacker, Character attacked, Skill skill)
	{
		return cond == null || cond.test(attacker, attacked, skill, 0);
	}

	@Override
	public int getChance()
	{
		return chance;
	}

	@Override
	public Skill getSkill()
	{
		return skill;
	}

	@Override
	public boolean isOnAttack()
	{
		return onAttack;
	}

	@Override
	public boolean isOnAttacked()
	{
		return onAttacked;
	}

	@Override
	public boolean isOnCritAttack()
	{
		return onCritAttack;
	}

	@Override
	public boolean isOnCritAttacked()
	{
		return onCritAttacked;
	}

	@Override
	public boolean isOnOwerturn()
	{
		return onOwerturn;
	}

	@Override
	public boolean isOnOwerturned()
	{
		return onOwerturned;
	}

	@Override
	public boolean isOnShieldBlocked()
	{
		return onShieldBlocked;
	}

	@Override
	public void prepare()
	{
		// получаем таблицу скилов
		SkillTable skillTable = SkillTable.getInstance();

		// получаем шаблон нужного скила
		SkillTemplate template = skillTable.getSkill(classId, id);

		// если шаблона нет, уведомляем
		if(template == null)
			log.warning(this, "not foun template for " + classId + " - " + id);

		// создаем экземпляр скила
		skill = template.newInstance();
	}

	@Override
	public void removeFuncTo(Character owner)
	{
		owner.removeChanceFunc(this);
	}
}
