package tera.gameserver.model.skillengine.effects;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель эффекта, который наносит промежуточно урон.
 *
 * @author Ronn
 */
public class AuraManaDamOverTime extends AbstractAura
{
	/**
	 * @param template темплейт эффекта.
	 * @param effector тот кто наложил эффект.
	 * @param effected тот на кого наложили эффект.
	 * @param skill скил, которым наложили.
	 */
	public AuraManaDamOverTime(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	public boolean onActionTime()
	{
		// получаем наложившего эффект
		Character effector = getEffector();

		// если он мертв, выходим с завершением
		if(effector.isDead())
			return false;

		// получаем список эффектов эффектора
		EffectList effectList = effector.getEffectList();

		// если списка нет либо эффекта в нем нет, выходим с завершением
		if(effectList == null || !effectList.contains(this))
			return false;

		// получаем того, на ком наложен эффект
		Character effected = getEffected();

		// если его нет, выходим с завершением
		if(effected == null)
			return false;

		// проверка на присутствие в пати с бафеом
		if(effected != effector && effected.getParty() == null || effected.getParty() != effector.getParty())
			return false;

		// если эффект на том, кто его наложил
		if(effected == effector)
		{
			// получаем потребление МП на ауру
			int cost = template.getPower();

			// если у кастера нет МП, выходим с завершением
			if(effected.getCurrentMp() < cost)
				return false;

			// забираем часть мп
			effector.setCurrentMp(effector.getCurrentMp() - cost);

			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// обновляем состояние ХП
			eventManager.notifyMpChanged(effector);
		}

		// если эффектор слишком далеко от кастера
		else if(!effector.isInRange(effected, 1400))
			return false;

		return true;
	}
}
