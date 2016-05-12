package tera.gameserver.model.skillengine.classes;

import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.wraps.Wrap;
import rlib.util.wraps.WrapType;
import rlib.util.wraps.Wraps;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.network.serverpackets.RequestSkillStart;
import tera.gameserver.network.serverpackets.SkillEnd;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель поглощателя хп врагов.
 *
 * @author Ronn
 */
public class AbsorptionHp extends PrepareNextSkill
{
	/** кол-во поглощенного хп */
	private int absHp;

	public AbsorptionHp(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		AttackInfo info = super.applySkill(attacker, target);

		if(!info.isBlocked())
			absHp += info.getDamage();

		return info;
	}

	@Override
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force)
	{
		// дулаяем функции
		template.removeCastFuncs(attacker);

		// зануляем активироваемый скил
		attacker.setActivateSkill(null);

		// если нет смысла в запуске восстановителя мп, просто завершаем каст
		if(absHp < 1 || force || attacker.isAttackBlocking() || attacker.isOwerturned())
		{
			attacker.broadcastPacket(SkillEnd.getInstance(attacker, castId, template.getId()));
			return;
		}

		// определяем ид скила для восстановления мп
		int attackId = template.getId() + template.getOffsetId();

		// получаем переменные скилов персонажа
		Table<IntKey, Wrap> variables = attacker.getSkillVariables();

		// получаем переменную этого скила
		Wrap wrap = variables.get(attackId);

		// если она есть, обновляем ее значение
		if(!(wrap == null || wrap.getWrapType() != WrapType.INTEGER))
			wrap.setInt(absHp);
		else
			// иначе вносим ее
			variables.put(attackId, Wraps.newIntegerWrap(absHp, true));

		// запоминаем каст ид
		attacker.setCastId(castId);
		// отправляем запрос на запуск скила
		attacker.sendPacket(RequestSkillStart.getInstance(attackId), true);
	}

	@Override
	public int getPower()
	{
		return super.getPower() * getCastCount();
	}

	@Override
	public boolean isCanceable()
	{
		return applyOrder > 0;
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);

		absHp = 0;
	}
}
