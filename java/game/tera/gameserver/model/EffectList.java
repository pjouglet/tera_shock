package tera.gameserver.model;

import java.util.concurrent.locks.Lock;

import rlib.concurrent.Locks;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Synchronized;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.array.Search;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.templates.EffectTemplate;

/**
 * Модель контейнера эффектов персонажа.
 *
 * @author Ronn
 */
public final class EffectList implements Foldable, Synchronized
{
	private static final Logger log = Loggers.getLogger(EffectList.class);

	private static final FoldablePool<EffectList> pool = Pools.newConcurrentFoldablePool(EffectList.class);

	/** поисковик эффекта с таким же стак типом */
	private static final Search<Effect> SEARCH_STACK_TYPE = new Search<Effect>()
	{
		@Override
		public boolean compare(Effect required, Effect target)
		{
			if(target == null || required == target)
				return false;

			return required.getStackType().equals(target.getStackType());
		}
	};

	/** поисковик однотипного эффекта */
	private static final Search<Effect> SEARCH_EQUALS_EFFECT = new Search<Effect>()
	{
		@Override
		public boolean compare(Effect required, Effect target)
		{
			if(target == null)
				return false;

			return target.getTemplate() == required.getTemplate();
		}
	};

	/** поисковик однотипного эффекта */
	private static final Search<Effect> SEARCH_EQUALS_SKILL = new Search<Effect>()
	{
		@Override
		public boolean compare(Effect required, Effect target)
		{
			if(target == null || required == target)
				return false;

			return target.getSkillTemplate() == required.getSkillTemplate();
		}
	};

	/**
	 * Получение нового эффект листа для указанного персонажа
	 *
	 * @param owner целевой персонаж.
	 * @return новый эффект лист.
	 */
	public static EffectList newInstance(Character owner)
	{
		EffectList list = pool.take();

		if(list == null)
			list = new EffectList(owner);
		else
			list.owner = owner;

		return list;
	}

	/** синхронизатор */
	private final Lock lock;

	/** владелец эффект листа. */
	private Character owner;

	/** список всех эффектов */
	private Array<Effect> effects;

	/** кол-во уникальных эффектов */
	private volatile int size;

	/**
	 * @param owner владелец эффект листа.
	 */
	private EffectList(Character owner)
	{
		this.owner = owner;
		this.lock = Locks.newLock();
		this.effects = Arrays.toArray(Effect.class, 2);
	}

	/**
	 * Метод добавления нового эффекта в эффект лист.
	 *
	 * @param effect новый эффект.
	 */
	public boolean addEffect(Effect newEffect)
	{
		// если эффекта нет, сообщаем и выходим
		if(newEffect == null)
		{
			log.warning(new Exception("not found effect."));
			return false;
		}

		// если эффект уже используется, сообщаем и выходим
		if(newEffect.getEffectList() != null)
		{
			log.warning("found effect list to " + newEffect);
			return false;
		}

		// получаем текущий список эффектов
		Array<Effect> effects = getEffects();

		lock();
		try
		{
			// если этот же эффект уже есть
			if(effects.contains(newEffect))
				return false;

			// поиск однотипного эффекта
			Effect old = effects.search(newEffect, SEARCH_EQUALS_EFFECT);

			// если такой нашелся
			if(old != null)
			{
				// если эффект аура - выключаем
				if(newEffect.isAura())
				{
					old.exit();
					return false;
				}
				// если новый эффект короче старого - выходим
				else if(newEffect.getTimeEnd() < old.getTimeEnd())
					return false;

				// если новый эффект лучше, старый останавливаем
				old.exit();
			}
			// если эффект имеет стак тип
			else if(newEffect.hasStackType())
			{
				// ищем одностактиповый эффект
				old = effects.search(newEffect, SEARCH_STACK_TYPE);

				// если такой нашли
				if(old != null)
				{
					// если эффект аура - выключаем
					if(newEffect.isAura() && newEffect.getSkillTemplate() == old.getSkillTemplate())
					{
						old.exit();
						return false;
					}
					// если новый эффект короче старого - выходим
					else if(newEffect.getTimeEnd() < old.getTimeEnd())
						return false;

					// выключаем старый эффект
					old.exit();
				}
			}

			// ищем эффект от того же скила
			old = effects.search(newEffect, SEARCH_EQUALS_SKILL);

			// если такого нет, увеличиваем размер эффект листа
			if(old == null)
				increaseSize();

			// добавляем новый эффект
			effects.add(newEffect);

			// запоминаем эффект лист эффекта
			newEffect.setEffectList(this);

			// запускаем эффект
			newEffect.setInUse(true);

			// запускаем эффект
			newEffect.scheduleEffect();

			return true;
		}
		finally
		{
			unlock();
		}
	}

	/**
	 * Очистка эффект листа.
	 */
	public void clear()
	{
		// если эффектов нет, выходим
		if(effects.isEmpty())
			return;

		lock();
		try
		{
			// получаем массив эффектов
			Effect[] array = effects.array();

			// перебираем эффекты
			for(int i = 0, length = effects.size(); i < length; i++)
			{
				// получаем эффект
				Effect effect = array[i];

				// если эффекта нет, выходим
				if(effect == null)
					continue;

				// запускаем остановку эффекта
				effect.exit();

				// обновляем счетчики
				i--;
				length--;
			}

			// доочищаем список
			effects.clear();
		}
		finally
		{
			unlock();
		}
	}

	/**
	 * Содержится ли указанный эффект в эффект листе.
	 *
	 * @param effect искомый эффект.
	 * @return содержится ли.
	 */
	public boolean contains(Effect effect)
	{
		// если эффекта нет
		if(effect == null)
		{
			log.warning(new Exception("check contains null effect"));
			return false;
		}

		lock();
		try
		{
			return effects.search(effect, SEARCH_EQUALS_EFFECT) != null;
		}
		finally
		{
			unlock();
		}
	}

	/**
	 * Содержит ли эффекты с указанного скила.
	 *
	 * @param skill скил с эффектами.
	 * @return содержит ли.
	 */
	public boolean contains(Skill skill)
	{
		EffectTemplate[] temps = skill.getEffectTemplates();

		// получаем список эффектов
		Array<Effect> effects = getEffects();

		lock();
		try
		{
			Effect[] array = effects.array();

			// перебираем эффекты персонажа
			for(int i = 0, length = effects.size(); i < length; i++)
				// смотрим есть ли шаблон эффекта среди шаблонов эффектов скила
				if(Arrays.contains(temps, array[i].getTemplate()))
					return true;

			return false;
		}
		finally
		{
			unlock();
		}
	}

	/**
	 * Уменьшение размера листа на 1.
	 */
	protected void decreaseSize()
	{
		size -= 1;
	}

	/**
	 * Завершение эффектов, завершающихся под атакой.
	 */
	public void exitNoAttackedEffects()
	{
		if(effects.isEmpty())
			return;

		lock();
		try
		{
			// получаем массив эффектов
			Effect[] array = effects.array();

			// перебираем эффекты
			for(int i = 0, length = effects.size(); i < length; i++)
			{
				// получаем следующий эффект
				Effect effect = array[i];

				// если эффект есть и он нужный
				if(effect != null && effect.isNoAttacked())
				{
					// останавливаем
					effect.exit();

					// обновляем счетчики
					i--;
					length--;
				}
			}
		}
		finally
		{
			unlock();
		}
	}

	/**
	 * Завершение эффектов, завершающихся при атаке кого-то.
	 */
	public void exitNoAttackEffects()
	{
		if(effects.isEmpty())
			return;

		lock();
		try
		{
			// получаем массив эффектов
			Effect[] array = effects.array();

			// перебираем эффекты
			for(int i = 0, length = effects.size(); i < length; i++)
			{
				// получаем следующий эффект
				Effect effect = array[i];

				// если эффект есть и он нужный
				if(effect != null && effect.isNoAttack())
				{
					// останавливаем
					effect.exit();

					// обновляем счетчики
					i--;
					length--;
				}
			}
		}
		finally
		{
			unlock();
		}
	}

	/**
	 * Остановка всех эффектов, сбрасываемых при опрокидывании.
	 */
	public void exitNoOwerturnEffects()
	{
		if(effects.isEmpty())
			return;

		lock();
		try
		{
			// получаем массив эффектов
			Effect[] array = effects.array();

			// перебираем эффекты
			for(int i = 0, length = effects.size(); i < length; i++)
			{
				// получаем следующий эффект
				Effect effect = array[i];

				// если эффект есть и он нужный
				if(effect != null && effect.isNoOwerturn())
				{
					// останавливаем
					effect.exit();

					// обновляем счетчики
					i--;
					length--;
				}
			}
		}
		finally
		{
			unlock();
		}
	}

	@Override
	public void finalyze()
	{
		owner = null;
	}

	/**
	 * Складировать в пул.
	 */
	public void fold()
	{
		// очищаем лист
		clear();

		// ложим в пул
		pool.put(this);
	}

	/**
	 * @return получение массива эффектов.
	 */
	public Effect[] getArrayEffects()
	{
		return effects.array();
	}

	/**
	 * @return список активных эффектов.
	 */
	public Array<Effect> getEffects()
	{
		return effects;
	}

	/**
	 * @return владелец эффект листа.
	 */
	public Character getOwner()
	{
		return owner;
	}

	/**
	 * Увеличение размера листа на 1.
	 */
	protected void increaseSize()
	{
		size += 1;
	}

	@Override
	public void lock()
	{
		lock.lock();
	}

	@Override
	public void reinit()
	{
		size = 0;
	}

	/**
	 * Удаляет эффект из листа.
	 *
	 * @param effect старый эффект.
	 */
	public void removeEffect(Effect effect)
	{
		lock.lock();
		try
		{
			// если эффекта нет либо его нет в списке эффектов
			if(effect == null || !effects.fastRemove(effect))
			{
				log.warning(new Exception("incorrect remove effect"));
				return;
			}

			// ищем эффект от того же скила
			Effect equals = effects.search(effect, SEARCH_EQUALS_SKILL);

			// если нету, уменьшаем счетчик эффектов
			if(equals == null)
				decreaseSize();

			// получаем владельца
			Character owner = getOwner();

			// если вдалельца нет, выходим
			if(owner == null)
			{
				log.warning(new Exception("not found owner"));
				return;
			}

			// обновляем информацию ему
			owner.updateInfo();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * @return кол-во эффектов.
	 */
	public int size()
	{
		return size;
	}

	@Override
	public void unlock()
	{
		lock.unlock();
	}
}
