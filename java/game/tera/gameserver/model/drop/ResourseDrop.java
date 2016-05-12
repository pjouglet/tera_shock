package tera.gameserver.model.drop;

import tera.gameserver.model.Character;
import tera.gameserver.model.TObject;

/**
 * Модель дропа ресурсов.
 * 
 * @author Ronn
 */
public final class ResourseDrop extends AbstractDrop
{

	public ResourseDrop(int templateId, DropGroup[] groups)
	{
		super(templateId, groups);
	}

	@Override
	protected boolean checkCondition(TObject creator, Character owner)
	{
		if(!creator.isResourse() || !owner.isPlayer())
			return false;
		
		return true;
	}

	@Override
	public int getTemplateType()
	{
		return -1;
	}
}
