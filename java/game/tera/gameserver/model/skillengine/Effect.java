package tera.gameserver.model.skillengine;

import rlib.util.pools.Foldable;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Интерфейс для реализации эффекта.
 *
 * @author Ronn
 * @created 13.04.2012
 */
public interface Effect extends Foldable
{
	/**
	 * Завершение эффекта.
	 */
	public void exit();

	/**
	 * Складировать в пул.
	 */
	public void fold();

	/**
	 * @return шанс наложения эффекта.
	 */
	public int getChance();

	/**
	 * @return кол-во приминений эффекта.
	 */
	public int getCount();

	/**
	 * @return тот, на кого накладывают эффект.
	 */
	public Character getEffected();

	/**
	 * @return id эффекта.
	 */
	public int getEffectId();

	/**
	 * @return effectList эффект лист, в котором находится этот эффект.
	 */
	public EffectList getEffectList();

	/**
	 * @return тот, кто накладывает эффект.
	 */
	public Character getEffector();

	/**
	 * @return тип эффекта.
	 */
	public EffectType getEffectType();

	/**
	 * @return функции эффекта.
	 */
	public Func[] getFuncs();

	/**
	 * @return порядок в темплейте скила.
	 */
	public int getOrder();

	/**
	 * @return период эффекта.
	 */
	public int getPeriod();

	/**
	 * @return тип ресиста эффекта.
	 */
	public ResistType getResistType();

	/**
	 * @return класс ид скила эффекта.
	 */
	public int getSkillClassId();

	/**
	 * @return ид скила эффекта.
	 */
	public int getSkillId();

	/**
	 * @return скил эффекта.
	 */
	public SkillTemplate getSkillTemplate();

	/**
	 * @return стак тип эффекта.
	 */
	public String getStackType();

	/**
	 * @return время запуска эффекта.
	 */
	public long getStartTime();

	/**
	 * @return состояние эффекта.
	 */
	public EffectState getState();

	/**
	 * @return темплейт эффекта.
	 */
	public EffectTemplate getTemplate();

	/**
	 * @return сколько уже секунд висит.
	 */
	public int getTime();

	/**
	 * @return сколько времени осталось.
	 */
	public int getTimeEnd();

	/**
	 * @return время для пакета.
	 */
	public int getTimeForPacket();

	/**
	 * @return общее время эффекта.
	 */
	public int getTotalTime();

	/**
	 * @return сколько приминенний уже было.
	 */
	public int getUsingCount();

	/**
	 * @return имеет ли стак тип.
	 */
	public boolean hasStackType();

	/**
	 * @return является ли аурой.
	 */
	public boolean isAura();

	/**
	 * @return дебаф ли эффект.
	 */
	public boolean isDebuff();

	/**
	 * @return является ли эффектом.
	 */
	public boolean isEffect();

	/**
	 * @return завершается ли скил.
	 */
	public boolean isEnded();

	/**
	 * @return завершен ли эффект.
	 */
	public boolean isFinished();

	/**
	 * @return использован ли эффект.
	 */
	public boolean isInUse();

	/**
	 * @return снимается ли при атаке.
	 */
	public boolean isNoAttack();

	/**
	 * @return снимается ли при получении уроеа.
	 */
	public boolean isNoAttacked();

	/**
	 * @return снимается ли при опрокидывании.
	 */
	public boolean isNoOwerturn();

	/**
	 * Действие при завершении периода эффекта.
	 *
	 * @return нужно ли продолжать.
	 */
	public boolean onActionTime();

	/**
	 * Завершение эффекта.
	 */
	public void onExit();

	/**
	 * Подготовка к старту эффект.
	 */
	public void onStart();

	/**
	 * Обработка работы эффекта.
	 */
	public void scheduleEffect();

	/**
	 * @param count кол-во приминений.
	 */
	public void setCount(int count);

	/**
	 * @param effected тот, на кого накладывают эффект.
	 */
	public void setEffected(Character effected);

	/**
	 * @param effectList эффект лист, в котором находится этот эффект.
	 */
	public void setEffectList(EffectList effectList);

	/**
	 * @param effector тот, кто накладывает эффект.
	 */
	public void setEffector(Character effector);

	/**
	 * @param value запустить ли скил.
	 */
	public void setInUse(boolean value);

	/**
	 * @param period перидо эффекта.
	 */
	public void setPeriod(int period);

	/**
	 * @param startTime стартовое время эффекта.
	 */
	public void setStartTime(long startTime);

	/**
	 * @param state состояние эффекта.
	 */
	public void setState(EffectState state);

	/**
	 * @return динамический ли каунтер эффекта.
	 */
	public boolean isDynamicCount();

	/**
	 * @return динамическое ли время эффекта.
	 */
	public boolean isDynamicTime();
}
