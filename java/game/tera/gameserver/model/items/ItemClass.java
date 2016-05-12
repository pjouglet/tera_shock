package tera.gameserver.model.items;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rlib.logging.Loggers;
import rlib.util.VarTable;
import tera.gameserver.IdFactory;
import tera.gameserver.templates.ArmorTemplate;
import tera.gameserver.templates.CommonTemplate;
import tera.gameserver.templates.CrystalTemplate;
import tera.gameserver.templates.ItemTemplate;
import tera.gameserver.templates.WeaponTemplate;
import tera.util.constructors.ConstructorItem;

/**
 * Перечисление классов итемов.
 *
 * @author Ronn
 */
public enum ItemClass
{
	/** броня */
	ARMOR("armor", ArmorTemplate.class, ConstructorItem.ARMOR, ArmorType.class),
	/** оружие */
	WEAPON("weapon", WeaponTemplate.class, ConstructorItem.WEAPON, WeaponType.class),
	/** другие итемы */
	COMMON_ITEM("common", CommonTemplate.class, ConstructorItem.COMMON, CommonType.class),
	/** крристалы */
	CRYSTAL("crystal", CrystalTemplate.class, ConstructorItem.CRYSTAL, CrystalType.class);

	/**
	 * Получение нужного класса по названию в xml.
	 *
	 * @param name название в xml.
	 * @return соответствующий класс.
	 */
	public static ItemClass valueOfXml(String name)
	{
		for(ItemClass type : values())
			if(type.getXmlName().equals(name))
				return type;

		return null;
	}

	/** название в хмл */
	private String xmlName;

	/** тип темплейта */
	private Constructor<?> templateConstructor;

	/** экземпляр соответствующего типа */
	private ConstructorItem constructor;

	/** тип енума типа итема */
	private Method getType;

	/**
	 * @param constructor конструктор итема.
	 */
	private ItemClass(String xmlName, Class<? extends ItemTemplate> templateClass, ConstructorItem constructor, Class<? extends Enum<?>> itemType)
	{
		this.xmlName = xmlName;
		this.constructor = constructor;

		try
		{
			this.templateConstructor = templateClass.getConstructors()[0];
			this.getType = itemType.getMethod("valueOfXml", String.class);
		}
		catch(SecurityException | NoSuchMethodException e)
		{
			Loggers.warning(this, e);
		}
	}

	/**
	 * @return xml название.
	 */
	public final String getXmlName()
	{
		return xmlName;
	}

	/**
	 * @param objectId уник ид итема.
	 * @param template темплейт итема.
	 * @return новый экземпляр.
	 */
	public ItemInstance newInstance(int objectId, ItemTemplate template)
	{
		return constructor.newInstance(objectId, template);
	}

	/**
	 * @param template темплейт итема.
	 * @return новый экземпляр.
	 */
	public ItemInstance newInstance(ItemTemplate template)
	{
		// получаеим фабрику ид
		IdFactory idFactory = IdFactory.getInstance();

		return constructor.newInstance(idFactory.getNextItemId(), template);
	}

	/**
	 * Создание нового темплейта.
	 *
	 * @param vars набор параметров.
	 * @param funcs набор функций.
	 * @return новый темплейт.
	 */
	public ItemTemplate newTemplate(VarTable vars)
	{
		try
		{
			return (ItemTemplate) templateConstructor.newInstance(getType.invoke(null, vars.getString("type")), vars);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Loggers.warning(this, e);

			System.out.println(vars);
		}

		return null;
	}
}
