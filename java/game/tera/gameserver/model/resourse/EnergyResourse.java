package tera.gameserver.model.resourse;

import tera.Config;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Formulas;
import tera.gameserver.network.serverpackets.ResourseIncreaseLevel;
import tera.gameserver.templates.ResourseTemplate;

/**
 * Модель кристалов ресурсов.
 *
 * @author Ronn
 */
public class EnergyResourse extends ResourseInstance
{
	public EnergyResourse(int objectId, ResourseTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getChanceFor(Player player)
	{
		// получаем формулы
		Formulas formulas = Formulas.getInstance();

		// рассчитываем шанс сбора
		return formulas.getChanceCollect(getTemplate().getReq(), player.getEnergyLevel());
	}

	@Override
	public void increaseReq(Player player)
	{
		if(player.getEnergyLevel() >= Config.WORLD_MAX_COLLECT_LEVEL)
			return;

		// увеличиваем уровень сбора
		player.setEnergyLevel(player.getEnergyLevel() + 1);

		// отправляю сообщение
		player.sendPacket(ResourseIncreaseLevel.getInstance(getTemplate().getType(), player.getEnergyLevel()), true);

		// обновляю статы
		player.updateInfo();
	}
}
