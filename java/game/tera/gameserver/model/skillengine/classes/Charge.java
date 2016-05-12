package tera.gameserver.model.skillengine.classes;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.RequestSkillStart;
import tera.gameserver.network.serverpackets.SkillEnd;
import tera.gameserver.network.serverpackets.SkillStart;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель зарядки скила.
 *
 * @author Ronn
 */
public class Charge extends AbstractSkill
{
	/** степень зарядки */
	private int charge;

	/**
	 * @param template темплейт скила.
	 */
	public Charge(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void endSkill(Character attacker, float targetX, float targetY, float targetZ, boolean force)
	{
		template.removeCastFuncs(attacker);

		// если принудительное завершение
		if(force || attacker.isAttackBlocking() || attacker.isOwerturned())
		{
			// отображаем пакет завершения каста
			attacker.broadcastPacket(SkillEnd.getInstance(attacker, castId, template.getId()));
			// зануляем заряжаемый скил
			attacker.setChargeSkill(null);
			return;
		}

		attacker.setCastId(castId);
		attacker.setChargeLevel(getCharge());
		attacker.sendPacket(RequestSkillStart.getInstance(template.getId() + template.getOffsetId()), true);
	}

	@Override
	public boolean isWaitable()
	{
		return false;
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		super.startSkill(attacker, targetX, targetY, targetZ);

		// указываем стартовый заряд
		setCharge(template.getStartState());
		// указываем текущий заряжаемый скил
		attacker.setChargeSkill(this);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		setCharge(getCharge() + 1);

		Skill skill = character.getSkill(template.getId() + getCharge());

		if(skill == null || !skill.checkCondition(character, targetX, targetY, targetZ))
		{
			character.abortCast(false);
			return;
		}

		character.broadcastPacket(SkillStart.getInstance(character, template.getId(), castId, getCharge()));

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		//есть ли мп потребление у скила
		if(skill.getMpConsume() > 0)
		{
			//потребляем мп
			int resultMp = character.getCurrentMp() - skill.getMpConsume();

			character.setCurrentMp(resultMp);

			//обновляем мп игроку
			eventManager.notifyMpChanged(character);
		}

		//есть ли хп потребление у скила
		if(skill.getHpConsume() > 0)
		{
			//потребляем хп
			int resultHp = character.getCurrentHp() - skill.getHpConsume();

			character.setCurrentHp(resultHp);

			//обновляем хп игроку
			eventManager.notifyHpChanged(character);
		}
	}

	/**
	 * @return уровень зарядки.
	 */
	public int getCharge()
	{
		return charge;
	}

	/**
	 * @param charge уровень зарядки.
	 */
	public void setCharge(int charge)
	{
		this.charge = charge;
	}
}
