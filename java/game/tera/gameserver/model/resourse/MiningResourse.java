package tera.gameserver.model.resourse;

import tera.Config;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.network.serverpackets.ResourseIncreaseLevel;
import tera.gameserver.templates.ResourseTemplate;

/**
 * Модель руды ресурсов.
 *
 * @author Ronn
 */
public class MiningResourse extends ResourseInstance
{
	public MiningResourse(int objectId, ResourseTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getChanceFor(Player player)
	{
		// получаем формулы
		Formulas formulas = Formulas.getInstance();

		// рассчитываем шанс сбора
		return formulas.getChanceCollect(getTemplate().getReq(), player.getMiningLevel());
	}

	@Override
	public void increaseReq(Player player)
	{
		if(player.getMiningLevel() >= Config.WORLD_MAX_COLLECT_LEVEL)
			return;

		// увеличиваем уровень сбора
		player.setMiningLevel(player.getMiningLevel() + 1);

		// отправляю сообщение
		player.sendPacket(ResourseIncreaseLevel.getInstance(getTemplate().getType(), player.getMiningLevel()), true);

		// обновляю статы
		player.updateInfo();
	}
}
