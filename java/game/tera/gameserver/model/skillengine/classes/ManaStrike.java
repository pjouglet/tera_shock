package tera.gameserver.model.skillengine.classes;

import rlib.util.VarTable;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.templates.SkillTemplate;

/**
 * Реализация удара, при котором восстанавливается процентно МП.
 *
 * @author Ronn
 */
public class ManaStrike extends Strike
{
	/** кол-во получаемого МП */
	private int gainMp;

	/**
	 * @param template темплейт скила.
	 */
	public ManaStrike(SkillTemplate template)
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
		// получаем результат удара
		AttackInfo info = super.applySkill(attacker, target);

		// рассчитываем % восстанавливаемого МП
		int abs = (int) attacker.calcStat(StatType.ABSORPTION_MP_ON_MAX, 0, attacker, null);

		// определяем итоговое кол-во восстанавливаемого МП
		abs = gainMp + gainMp * abs / 100;

		// если такое есть
		if(abs > 0)
		{
			// добавляем МП
			attacker.setCurrentMp(attacker.getCurrentMp() + abs);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// уведомляем об изменении МП
			eventManager.notifyMpChanged(attacker);
		}

		return info;
	}
}
