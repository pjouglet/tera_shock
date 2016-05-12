package tera.gameserver.model.skillengine.effects;

import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.Party;
import tera.gameserver.model.skillengine.EffectState;
import tera.gameserver.network.serverpackets.AppledCharmEffect;
import tera.gameserver.network.serverpackets.CancelCharmEffect;
import tera.gameserver.taskmanager.EffectTaskManager;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Простой баф, служит для применения статов без какой-нибудь логики.
 *
 * @author Ronn
 */
public class CharmBuff extends AbstractEffect
{
	public CharmBuff(EffectTemplate template, Character effector, Character effected, SkillTemplate skill)
	{
		super(template, effector, effected, skill);
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
					effected.broadcastPacket(AppledCharmEffect.getInstance(effected, this));

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
					// если еще остались приминения
					if(count > 0)
					{
						// уменьшаем на 1
						count--;

						// применяем
						if(onActionTime() && count > 0)
							break;
					}

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
					effected.sendPacket(CancelCharmEffect.getInstance(getEffectId()), true);

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
