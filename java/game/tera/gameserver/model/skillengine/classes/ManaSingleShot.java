package tera.gameserver.model.skillengine.classes;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.AttackInfo;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель быстро стреляющего скила.
 *
 * @author Ronn
 */
public class ManaSingleShot extends SingleShot
{
	/**
	 * @param template темплейт скила.
	 */
	public ManaSingleShot(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public AttackInfo applySkill(Character attacker, Character target)
	{
		AttackInfo info = super.applySkill(attacker, target);

		if(!info.isBlocked())
		{
			int currentMp = attacker.getCurrentMp();

			if(currentMp < attacker.getMaxMp())
			{
				int mp = (int) attacker.calcStat(StatType.GAIN_MP, 0, target, this);

				if(mp > 0)
				{
					attacker.setCurrentMp(currentMp + mp);

					// получаем менеджера событий
					ObjectEventManager eventManager = ObjectEventManager.getInstance();

					eventManager.notifyMpChanged(attacker);
				}
			}
		}

		return info;
	}
}
