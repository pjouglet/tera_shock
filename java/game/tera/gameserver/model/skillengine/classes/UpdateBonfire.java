package tera.gameserver.model.skillengine.classes;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.MessageType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.worldobject.BonfireObject;
import tera.gameserver.templates.SkillTemplate;
import tera.util.LocalObjects;

/**
 * Модель перезапускающего костра скила.
 *
 * @author Ronn
 */
public class UpdateBonfire extends AbstractSkill
{
	/** целевой костер */
	private BonfireObject bonfire;

	/**
	 * @param template темплейт скила.
	 */
	public UpdateBonfire(SkillTemplate template)
	{
		super(template);
	}

	@Override
	public boolean checkCondition(Character attacker, float targetX, float targetY, float targetZ)
	{
		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// ищем костры вокруг
		Array<TObject> objects = World.getAround(BonfireObject.class, local.getNextObjectList(), attacker, 30 + attacker.getGeomRadius());

		// если костров рядом нет, выходим
		if(objects.isEmpty())
		{
			attacker.sendMessage(MessageType.YOU_CANT_USE_FIREWOOD_RIGHT_NOW);
			return false;
		}

		// запоминаем найденный костер
		setBonfire((BonfireObject) objects.first());

		return super.checkCondition(attacker, targetX, targetY, targetZ);
	}

	private BonfireObject getBonfire()
	{
		return bonfire;
	}

	private void setBonfire(BonfireObject bonfire)
	{
		this.bonfire = bonfire;
	}

	@Override
	public void startSkill(Character attacker, float targetX, float targetY, float targetZ)
	{
		// получаем костер
		BonfireObject bonfire = getBonfire();

		// если он есть
		if(bonfire != null)
			// разворачиваем кастера к костру
			attacker.setHeading(attacker.calcHeading(bonfire.getX(), bonfire.getY()));

		super.startSkill(attacker, targetX, targetY, targetZ);
	}

	@Override
	public void useSkill(Character character, float targetX, float targetY, float targetZ)
	{
		// получаем костер
		BonfireObject bonfire = getBonfire();

		// если его нет, выходим
		if(bonfire == null)
			return;

		// зануляем костер
		setBonfire(null);

		// перезапускаем
		bonfire.restart();
	}
}
