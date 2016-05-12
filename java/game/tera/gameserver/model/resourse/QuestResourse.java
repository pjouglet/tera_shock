package tera.gameserver.model.resourse;

import tera.gameserver.model.playable.Player;
import tera.gameserver.templates.ResourseTemplate;

/**
 * Модель квестовых ресурсов.
 * 
 * @author Ronn
 */
public class QuestResourse extends ResourseInstance
{
	public QuestResourse(int objectId, ResourseTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean checkCondition(Player collector)
	{
		return true;
	}
	
	@Override
	public int getChanceFor(Player player)
	{
		return 100;
	}
	
	@Override
	public void increaseReq(Player player){}
}
