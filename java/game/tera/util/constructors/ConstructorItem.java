package tera.util.constructors;

import tera.gameserver.model.items.ArmorInstance;
import tera.gameserver.model.items.CommonInstance;
import tera.gameserver.model.items.CrystalInstance;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.items.WeaponInstance;
import tera.gameserver.templates.ItemTemplate;

/**
 * Интерфейс для реализации конструктора итема.
 *
 * @author Ronn
 * @created 14.04.2012
 */
public interface ConstructorItem
{
	public static final ConstructorItem WEAPON = new ConstructorItem()
	{
		@Override
		public ItemInstance newInstance(int objectId, ItemTemplate template)
		{
			return new WeaponInstance(objectId, template);
		}
	};
	
	public static final ConstructorItem ARMOR = new ConstructorItem()
	{
		@Override
		public ItemInstance newInstance(int objectId, ItemTemplate template)
		{
			return new ArmorInstance(objectId, template);
		}
	};
	
	public static final ConstructorItem COMMON = new ConstructorItem()
	{
		@Override
		public ItemInstance newInstance(int objectId, ItemTemplate template)
		{
			return new CommonInstance(objectId, template);
		}
	};
	
	public static final ConstructorItem CRYSTAL = new ConstructorItem()
	{
		@Override
		public ItemInstance newInstance(int objectId, ItemTemplate template)
		{
			return new CrystalInstance(objectId, template);
		}
	};
	
	/**
	 * @param objectId объектный ид итема.
	 * @param template базовые параметры итема.
	 * @return новый итем.
	 */
	public ItemInstance newInstance(int objectId, ItemTemplate template);
}
