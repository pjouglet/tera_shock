package tera.gameserver.model.items;

import tera.gameserver.templates.CrystalTemplate;
import tera.gameserver.templates.ItemTemplate;

/**
 * Модель кристала.
 * 
 * @author Ronn
 */
public class CrystalInstance extends ItemInstance
{
	public CrystalInstance(int objectId, ItemTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public CrystalInstance getCrystal()
	{
		return this;
	}

	/**
	 * @return тсэк тип кристала.
	 */
	public StackType getStackType()
	{
		return getTemplate().getStackType();
	}
	
	@Override
	public CrystalTemplate getTemplate()
	{
		return (CrystalTemplate) template;
	}
	
	@Override
	public CrystalType getType()
	{
		return (CrystalType) template.getType();
	}
	
	@Override
	public boolean isCrystal()
	{
		return true;
	}
	
	/**
	 * @return запрощено ли больше 1 однотипного кристала.
	 */
	public boolean isNoStack()
	{
		return getTemplate().isNoStack();
	}
}
