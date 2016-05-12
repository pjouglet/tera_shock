package tera.gameserver.model.skillengine.effects;

import tera.gameserver.manager.ExecutorManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.listeners.DieListener;
import tera.gameserver.network.serverpackets.CharDead;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Баф для самовоскрешения.
 *
 * @author Ronn
 */
public class Pheonix extends AbstractEffect implements DieListener, Runnable
{
	public Pheonix(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	public boolean onActionTime()
	{
		return true;
	}

	@Override
	public void onDie(Character killer, Character killed)
	{
		// получаем исполнительного менеджера
		ExecutorManager executor = ExecutorManager.getInstance();

		try
		{
    		// ставим фул хп
			killed.setCurrentHp(killed.getMaxHp());

    		// обновляем статы
			killed.updateHp();

    		// отправляем
			killed.broadcastPacket(CharDead.getInstance(killed, false));
		}
		finally
		{
			executor.execute(this);
		}
	}

	@Override
	public void onExit()
	{
		Character effected = getEffected();

		if(effected != null)
			// удаляем прослушку смерти
			effected.removeDieListener(this);

		super.onExit();
	}

	@Override
	public void onStart()
	{
		Character effected = getEffected();

		if(effected != null)
			// добавляем прослушку смерти
			effected.addDieListener(this);

		super.onStart();
	}

	@Override
	public void run()
	{
		// тот, на ком висит эффект
		Character effector = getEffector();

		// если его нет, выходим
		if(effector == null)
			return;

		// эффект лист, в котором находится эффект
		EffectList effectList = effector.getEffectList();

		effectList.lock();
		try
		{
			// завершаемся
			exit();
		}
		finally
		{
			effectList.unlock();
		}
	}
}
