package tera.gameserver.model.npc;

import tera.gameserver.templates.NpcTemplate;

/**
 * Модель социального монстра.
 * 
 * @author Ronn
 */
public class SocialMonster extends Monster
{
	public SocialMonster(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public int getKarmaMod()
	{
		return 0;
	}
}
