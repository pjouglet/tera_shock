package tera.gameserver.model.skillengine.classes;

import rlib.util.VarTable;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель удара с получнием МП.
 *
 * @author Ronn
 */
public class ManaGainStrike extends Strike
{
	/** кол-во получаемого МП */
	private int gainMp;

	public ManaGainStrike(SkillTemplate template)
	{
		super(template);

		// получаем таблицу переменных
		VarTable vars = template.getVars();

		// получаем кол-во рестора МП
		gainMp = vars.getInteger("gainMp", 0);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		AttackInfo info = super.applySkill(attacker, target);

		// если есть восстановление МП
		if(gainMp > 0)
		{
			// восстанавливаем МП
			attacker.setCurrentMp(attacker.getCurrentMp() + gainMp);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// уведомляем об изменении МП
			eventManager.notifyMpChanged(attacker);
		}

		return info;
	}
}
