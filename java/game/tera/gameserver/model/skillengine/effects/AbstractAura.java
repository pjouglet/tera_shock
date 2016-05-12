package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.Party;
import tera.gameserver.model.skillengine.EffectState;
import tera.gameserver.network.serverpackets.AppledEffect;
import tera.gameserver.network.serverpackets.CancelEffect;
import tera.gameserver.taskmanager.EffectTaskManager;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Базовая модель ауры.
 *
 * @author Ronn
 */
public abstract class AbstractAura extends AbstractEffect
{
	/**
	 * @param effectTemplate темплейт эффекта.
	 * @param effector тот, кто наложил эффект.
	 * @param effected тот, на кого наложили эффект,
	 * @param skill скил, которым был наложен эффект.
	 */
	public AbstractAura(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
	}

	@Override
	public boolean isAura()
	{
		return true;
	}

	@Override
	public boolean isEffect()
	{
		return false;
	}

	@Override
	public void scheduleEffect()
	{
		// получаем того, на кого повесили эффект
		Character effected = getEffected();
		// получаем того, кто повесил эффект
		Character effector = getEffector();

		// если нет эффктеда, выходим
		if(effected == null)
		{
			LOGGER.warning(this, new Exception("not found effected"));
			return;
		}

		// если нет эффектора, выходим
		if(effector == null)
		{
			LOGGER.warning(this, new Exception("not found effector"));
			return;
		}

		// получаем эффект лист, в котором находится эффект
		EffectList effectList = getEffectList();

		// если его нет, выходим
		if(effectList == null)
		{
			LOGGER.warning(this, new Exception("not found effect list."));
			return;
		}

		effectList.lock();
		try
		{
			switch(getState())
			{
				// стадия запуска эффекта
				case CREATED:
				{
					// запускаем эффект
					onStart();

					// ставим активную фазу
					setState(EffectState.ACTING);

					// отправляем пакет отображения бафа
					effected.broadcastPacket(AppledEffect.getInstance(effector, effected, this));

					// получаем пати эффектеда
					Party party = effected.getParty();

					// если пати есть
					if(party != null)
						// обновляем в ней эффект лист
						party.updateEffects(effected.getPlayer());

					// получаем менеджера эффетков
					EffectTaskManager effectManager = EffectTaskManager.getInstance();

					// добавляем на обработку
					effectManager.addTask(this, period);

					break;
				}
				// стадия работы эффекта
				case ACTING:
				{
					if(onActionTime())
						break;

					// если коунтер кончился, меняем стадию на звершение
					setState(EffectState.FINISHING);

					break;
				}
				// если стадия завершения
				case FINISHING:
				{
					// ставим стадию завершенности
					setState(EffectState.FINISHED);

					// флаг не использования
					setInUse(false);

					// выполняем метод выхода
					onExit();

					// удаляем с эффект листа
					effected.removeEffect(this);

					// отправляем пакет
					effected.broadcastPacket(CancelEffect.getInstance(effected, getEffectId()));

					// получаем пати эффектеда
					Party party = effected.getParty();

					// если пати есть
					if(party != null)
						// обновляем в ней эффект лист
						party.updateEffects(effected.getPlayer());

					break;
				}
				default:
					LOGGER.warning(this, new Exception("incorrect effect state " + state));
			}
		}
		finally
		{
			effectList.unlock();
		}
	}
}
