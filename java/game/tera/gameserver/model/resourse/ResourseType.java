package tera.gameserver.model.resourse;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import rlib.logging.Loggers;
import tera.gameserver.templates.ResourseTemplate;


/**
 * Перечисление типов ресурсов.
 * 
 * @author Ronn
 */
public enum ResourseType
{
	/** квестовые ресурсы */
	QUEST(QuestResourse.class),
	/** растения */
	PLANT(PlantResourse.class),
	/** руда */
	MINING(MiningResourse.class),
	/** какой-то буг хутинг */
	UNKNOWN(QuestResourse.class),
	/** кристалы */
	ENERGY(EnergyResourse.class);
	
	/** конструктор инстансов */
	private Constructor<? extends ResourseInstance> constructor;
	
	private ResourseType(Class<? extends ResourseInstance> type)
	{
		try
		{
			this.constructor = type.getConstructor(int.class, ResourseTemplate.class);
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			Loggers.warning(this, e);
		}
	}
	/**
	 * Получение нового экземпляра инстанса ресурса.
	 * 
	 * @param template темплейт ресурса.
	 * @return новый инстанс.
	 */
	public ResourseInstance newInstance(ResourseTemplate template)
	{
		try
		{
			return constructor.newInstance(0, template);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Loggers.warning(this, e);
		}
		
		return null;
	}
}
