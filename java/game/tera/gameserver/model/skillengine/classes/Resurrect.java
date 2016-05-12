package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.CharDead;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель скила, служащего только для активирования бафа.
 *
 * @author Ronn
 */
public class Resurrect extends AbstractSkill
{
	/**
	 * @param template темплейт скила.
	 */
	public Resurrect(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем пустой список целей
		Array<Character> targets = local.getNextCharList();

		// вносим в список цели
		addTargets(targets, character, targetX, targetY, targetZ);

		// получаем их массив
		Character[] array = targets.array();

		for(int i = 0, length = targets.size(); i < length; i++)
    	{
			// получаем цель
			Character target = array[i];

			// если она не мертва, пропускаем
    		if(!target.isDead())
    			continue;

    		// получаем игрока
    		Player player = target.getPlayer();

    		// если игрока нет или его нельзя воскрешать, пропускаем
    		if(player == null || !player.isResurrected())
    			continue;

    		// уменьшаем стамину
    		player.setStamina(player.getStamina() - 10);

    		// ставим фул хп
    		player.setCurrentHp(player.getMaxHp());

    		// обновляем статы в пати
    		player.updateHp();

    		// отправляем
    		player.broadcastPacket(CharDead.getInstance(target, false));
    	}
	}
}
