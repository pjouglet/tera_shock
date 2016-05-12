package tera.gameserver.model.items;

import tera.gameserver.model.Character;
import tera.gameserver.model.TObject;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.serverpackets.CharPickUpItem;
import tera.gameserver.templates.CommonTemplate;
import tera.gameserver.templates.ItemTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Модель обычных итемов.
 *
 * @author Ronn
 */
public final class CommonInstance extends ItemInstance
{
	/** активный скил итема */
	private Skill activeSkill;

	/**
	 * @param objectId уник ид итема.
	 * @param template темплейт итема.
	 */
	public CommonInstance(int objectId, ItemTemplate template)
	{
		super(objectId, template);

		SkillTemplate temp = getTemplate().getActiveSkill();

		if(temp != null)
			activeSkill = temp.newInstance();
	}

	@Override
	public Skill getActiveSkill()
	{
		return activeSkill;
	}

	@Override
	public CommonInstance getCommon()
	{
		return this;
	}

	@Override
	public CommonTemplate getTemplate()
	{
		return (CommonTemplate) template;
	}

	@Override
	public boolean isCommon()
	{
		return true;
	}

	@Override
	public boolean isHerb()
	{
		return template.getType() == CommonType.HERB;
	}

	@Override
	public boolean pickUpMe(TObject target)
	{
		if(!isHerb())
			return super.pickUpMe(target);
		else
		{
			if(target == null)
				return false;

			Character character = target.getCharacter();

			if(character == null)
				return false;

			character.broadcastPacket(CharPickUpItem.getInstance(character, this));

			deleteMe();

			character.getAI().startUseItem(this, character.getHeading(), true);

			return true;
		}
	}
}
