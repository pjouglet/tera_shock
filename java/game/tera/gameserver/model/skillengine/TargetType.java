package tera.gameserver.model.skillengine;

import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import tera.Config;
import tera.gameserver.manager.GeoManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.World;
import tera.gameserver.model.skillengine.targethandler.AreOneTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AreaFractionTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AreaOwnerTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AreaPartyGuildTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AreaPartyTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AreaTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AuraFractionTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AuraOwnerTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AuraPartyTargetHandler;
import tera.gameserver.model.skillengine.targethandler.AuraTargetHandler;
import tera.gameserver.model.skillengine.targethandler.TargetHandler;
import tera.gameserver.model.skillengine.targethandler.NoneTargetHandler;
import tera.gameserver.model.skillengine.targethandler.SelfTargetHandler;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;
import tera.util.LocalObjects;
import tera.util.Location;

/**
 * Перечисление типов таргетов у скилов.
 *
 * @author Ronn
 */
public enum TargetType
{
	/** без таргета */
	TARGET_NONE(new NoneTargetHandler()),
	/** одиночный */
	TARGET_ONE(new NoneTargetHandler()),
	/** область дружественных мобов */
	TARGET_FRACTION_AREA(new AreaFractionTargetHandler()),
	/** облать дружественных мобов */
	TARGET_FRACTION_AURA(new AuraFractionTargetHandler()),
	/** одиночный в области */
	TARGET_ONE_AREA(new AreOneTargetHandler()),
	/** таргет на себя */
	TARGET_SELF(new SelfTargetHandler()),
	/** таргет вокруг цели */
	TARGET_AREA(new AreaTargetHandler()),
	/** таргет вокруг себя */
	TARGET_AURA(new AuraTargetHandler()),
	/** таргет впереди тебя */
	TARGET_FRONT,
	/** градусы вокруг тебя */
	TARGET_DEGREE_AURA,
	/** рельсовый ид */
	TARGET_RAIL,
	/** рельсовый ид */
	TARGET_BACK_RAIL,
	/** группа */
	TARGET_PARTY(new AuraPartyTargetHandler()),
	/** владелец и его друзья */
	TARGET_OWNER_AURA(new AuraOwnerTargetHandler()),
	/** владелец и его друзья */
	TARGET_OWNER_AREA(new AreaOwnerTargetHandler()),
	/** лок он по врагам */
	LOCK_ON_WAR,
	/** лон ок своих */
	LOCK_ON_FRIEND,
	/** зона вокруг трапа */
	TRAP_AURA,
	/** группа в области */
	TARGET_AREA_PARTY(new AreaPartyTargetHandler()),
	/** группа и гильдия в области */
	TARGET_AREA_PARTY_GUILD(new AreaPartyGuildTargetHandler());

	private final TargetHandler handler;

	private TargetType()
	{
		this.handler = null;
	}

	private TargetType(TargetHandler handler)
	{
		this.handler = handler;
	}

	/**
	 * Предварительная проверка на подходимость цели.
	 *
	 * @param caster кастующий скил.
	 * @param target цель скила.
	 * @return подходит ли.
	 */
	public boolean check(Character caster, Character target)
	{
		switch(this)
		{
			case LOCK_ON_FRIEND: return !caster.checkTarget(target);
			case LOCK_ON_WAR: return caster.checkTarget(target);
			default:
			{
				Loggers.warning(this, "incorrect check target type " + this);
				return false;
			}
		}
	}

	/**
	 * Рассчитывает таргеты в соответствии с типом таргета.
	 *
	 * @param targets список целей.
	 * @param skill кастуемый скил.
	 * @param targetX координата цели скила.
	 * @param targetY координата цели скила.
	 * @param targetZ координата цели скила.
	 * @param attacker кастующий скил.
	 */
	public void getTargets(Array<Character> targets, Skill skill, float targetX, float targetY, float targetZ, Character attacker)
	{
		skill.setImpactX(attacker.getX());
		skill.setImpactY(attacker.getY());
		skill.setImpactZ(attacker.getZ());

		if(handler != null)
			handler.addTargetsTo(targets, attacker, skill, targetX, targetY, targetZ);
		else
		{
			// получаем таблицу итемов
			ItemTable itemTable = ItemTable.getInstance();

			// получаем менеджера геодаты
			GeoManager geoManager = GeoManager.getInstance();

			switch(this)
			{
				case LOCK_ON_FRIEND:
				{
					Array<Character> prepare = attacker.getLockOnTargets();

					skill.setImpactX(attacker.getX());
					skill.setImpactY(attacker.getY());
					skill.setImpactZ(attacker.getZ());

					prepare.readLock();
					try
					{
						Character[] array = prepare.array();

						for(int i = 0, length = prepare.size(); i < length; i++)
						{
							Character target = array[i];

							if(!attacker.checkTarget(target))
								targets.add(target);
						}
					}
					finally
					{
						prepare.readUnlock();
					}

					prepare.clear();

					break;
				}
				case LOCK_ON_WAR:
				{
					Array<Character> prepare = attacker.getLockOnTargets();

					skill.setImpactX(attacker.getX());
					skill.setImpactY(attacker.getY());
					skill.setImpactZ(attacker.getZ());

					prepare.readLock();
					try
					{
						Character[] array = prepare.array();

						for(int i = 0, length = prepare.size(); i < length; i++)
						{
							Character target = array[i];

							if(attacker.checkTarget(target) && attacker.isInRange(target, skill.getRange()))
								targets.add(target);
						}
					}
					finally
					{
						prepare.readUnlock();
					}

					prepare.clear();

					break;
				}
				case TARGET_DEGREE_AURA:
				{
					int degree = skill.getDegree();
					int width = skill.getWidth();

					int min = degree - width;

					if(min < 0)
						min += 360;

					int max = degree + width;

					if(max < 0)
						max += 360;

					float radius = skill.getRadius();

					World.getAround(Character.class, targets, attacker, radius);

					if(Config.DEVELOPER_DEBUG_TARGET_TYPE)
					{
						Location[] locs = Coords.arcCoords(Location.class, attacker, (int) radius, 10, degree, width);

						ItemTemplate template = itemTable.getItem(8007);

						// перебираем точки
						for(int i = 0; i < 10; i++)
						{
							// получаем точку
							Location loc = locs[i];

							// вносим континент
							loc.setContinentId(attacker.getContinentId());

							// спавним
							template.newInstance().spawnMe(loc);
						}

					}

					float x = attacker.getX();
					float y = attacker.getY();
					float z = attacker.getZ();

					if(!targets.isEmpty())
					{
						Character[] array = targets.array();

						for(int i = 0, length = targets.size(); i < length; i++)
						{
							Character target = array[i];

							if(!attacker.checkTarget(target))
							{
								targets.fastRemove(i--);
								length--;
								continue;
							}

							if(!target.isHit(x, y, z, 100, radius))
							{
								targets.fastRemove(i--);
								length--;
								continue;
							}

							if(!attacker.isInDegree(target, degree, width))
							{
								targets.fastRemove(i--);
								length--;
							}
						}
					}

					break;
				}
				case TARGET_RAIL:
				{
					float radius = skill.getRadius();
					float range = skill.getRange();

					targets = World.getAround(Character.class, targets, attacker, range);

					float x = attacker.getX();
					float y = attacker.getY();
					float z = attacker.getZ();

					float radians = Angles.headingToRadians(attacker.getHeading() + skill.getHeading());

					targetX = Coords.calcX(x, skill.getRange(), radians);
					targetY = Coords.calcY(y, skill.getRange(), radians);

					targetZ = geoManager.getHeight(attacker.getContinentId(), targetX, targetY, z);

					if(Config.DEVELOPER_DEBUG_TARGET_TYPE)
					{
						Location[] locs = Coords.circularCoords(Location.class, targetX, targetY, z, (int) radius, 10);

						ItemTemplate template = itemTable.getItem(8007);

						// перебираем точки
						for(int i = 0; i < 10; i++)
						{
							// получаем точку
							Location loc = locs[i];

							// вносим континент
							loc.setContinentId(attacker.getContinentId());

							// спавним
							template.newInstance().spawnMe(loc);
						}

						template.newInstance().spawnMe(attacker.getLoc());
						template.newInstance().spawnMe(new Location(targetX, targetY, z, 0, attacker.getContinentId()));
					}

					if(!targets.isEmpty())
					{
						Character[] array = targets.array();

						for(int i = 0, length = targets.size(); i < length; i++)
						{
							Character target = array[i];

							if(!attacker.checkTarget(target))
							{
								targets.fastRemove(i--);
								length--;
								continue;
							}

							if(!target.isHit(x, y, z, targetX, targetY, targetZ, radius, false))
							{
								targets.fastRemove(i--);
								length--;
							}
						}
					}

					break;
				}
				case TARGET_BACK_RAIL:
				{
					float radius = skill.getRadius();
					float range = skill.getRange();

					targets = World.getAround(Character.class, targets, attacker, range);

					float x = attacker.getX();
					float y = attacker.getY();
					float z = attacker.getZ();

					float radians = Angles.degreeToRadians(Angles.headingToDegree(attacker.getHeading() + skill.getHeading()) - 180);

					targetX = Coords.calcX(x, skill.getRange(), radians);
					targetY = Coords.calcY(y, skill.getRange(), radians);

					targetZ = geoManager.getHeight(attacker.getContinentId(), targetX, targetY, z);

					if(Config.DEVELOPER_DEBUG_TARGET_TYPE)
					{
						Location[] locs = Coords.circularCoords(Location.class, targetX, targetY, z, (int) radius, 10);

						ItemTemplate template = itemTable.getItem(8007);

						// перебираем точки
						for(int i = 0; i < 10; i++)
						{
							// получаем точку
							Location loc = locs[i];

							// вносим континент
							loc.setContinentId(attacker.getContinentId());

							// спавним
							template.newInstance().spawnMe(loc);
						}

						template.newInstance().spawnMe(attacker.getLoc());
						template.newInstance().spawnMe(new Location(targetX, targetY, z, 0, attacker.getContinentId()));
					}

					if(!targets.isEmpty())
					{
						Character[] array = targets.array();

						for(int i = 0, length = targets.size(); i < length; i++)
						{
							Character target = array[i];

							if(!attacker.checkTarget(target))
							{
								targets.fastRemove(i--);
								length--;
								continue;
							}

							if(!target.isHit(x, y, z, targetX, targetY, targetZ, radius, false))
							{
								targets.fastRemove(i--);
								length--;
							}
						}
					}

					break;
				}
				case TRAP_AURA:
				{
					float radius = skill.getRadius();

					skill.setImpactX(targetX);
					skill.setImpactY(targetY);
					skill.setImpactZ(targetZ);

					if(Config.DEVELOPER_DEBUG_TARGET_TYPE)
					{
						Location[] locs = Coords.circularCoords(Location.class, targetX, targetY, targetZ, (int) radius, 10);

						ItemTemplate template = itemTable.getItem(8007);

						// перебираем точки
						for(int i = 0; i < 10; i++)
						{
							// получаем точку
							Location loc = locs[i];

							// вносим континент
							loc.setContinentId(attacker.getContinentId());

							// спавним
							template.newInstance().spawnMe(loc);
						}

						template.newInstance().spawnMe(new Location(targetX, targetY, targetZ));
					}

					targets = World.getAround(Character.class, targets, attacker.getContinentId(), targetX, targetY, attacker.getZ(), attacker.getObjectId(), attacker.getSubId(), radius);

					if(!targets.isEmpty())
					{
						Character[] array = targets.array();

						for(int i = 0, length = targets.size(); i < length; i++)
						{
							Character target = array[i];

							if(!attacker.checkTarget(target))
							{
								targets.fastRemove(i--);
								length--;
								continue;
							}

							if(!target.isHit(targetX, targetY, targetZ, 100, radius))
							{
								targets.fastRemove(i--);
								length--;
							}
						}
					}

					break;
				}
				default:
					break;
			}
		}

		// если скил на себя не кастуется
		if(skill.isNoCaster())
			// удаляем кастующего из списка целей
			targets.fastRemove(attacker);

		// нужно ли вообще начинать рассчет прикрытых таргетов
		if(!skill.isShieldIgnore() && targets.size() > 1)
		{
			// получаем локальные объекты
			LocalObjects local = LocalObjects.get();

			// дополнительный список для хранения прикрытых таргетов
			Array<Character> removed = local.getNextCharList();

			// массив целей
			Character[] array = targets.array();

			// точка удара скила
			float x = skill.getImpactX();
			float y = skill.getImpactY();
			float z = skill.getImpactZ();

			// радиус
			float radius = 0F;

			// градусы
			int angle = 0;
			int degree = 0;

			// получаем таблицу итемов
			ItemTable itemTable = ItemTable.getInstance();

			// получаем формулы
			Formulas formulas = Formulas.getInstance();

			for(int i = 0, length = targets.size(); i < length; i++)
			{
				Character target = array[i];

				// проверяем на возможность блокирования удара
				if(target.isDefenseStance() && formulas.calcDamageSkill(local.getNextAttackInfo(), skill, attacker, target).isBlocked() && !removed.contains(target))
				{
					// рассчет дистанции от точки удара до танка
					float distance = target.getGeomDistance(x, y);
					// рассчет ширины блока
					float width = target.getGeomRadius();
					// половина ширины блока
					float half = width / 2F;
					// рассчет длинны бедра
					float hip = (float) Math.sqrt(distance * distance + half * half);
					// рассчет площади треугольника
					float square = half * (float) Math.sqrt((hip + half) * (hip - half));

					// рассчет радиуса окружности вокруг треугольника
					radius = (hip * hip * width) / (4F * square);
					// расчет градус вершины
					angle = (int) Math.toDegrees(Math.asin(width / ( 2 * radius))) / 2;
					// направление от точки удара на танка
					degree = Angles.calcHeading(x, y, target.getX(), target.getY());

					// отображаем, если надо
					if(Config.DEVELOPER_DEBUG_TARGET_TYPE)
					{
						Location[] locs = Coords.arcCoords(Location.class, x, y, z, Angles.calcHeading(x, y, target.getX(), target.getY()), skill.getRadius(), 10, 0, angle * 2);

						ItemTemplate template = itemTable.getItem(10001);

						// перебираем точки
						for(int g = 0; g < 10; i++)
						{
							// получаем точку
							Location loc = locs[g];

							// вносим континент
							loc.setContinentId(attacker.getContinentId());

							// спавним
							template.newInstance().spawnMe(loc);
						}
					}

					radius = skill.getRadius();

					// перебираем потенциально прикрытые цели
					for(int j  = 0; j < length; j++)
					{
						Character covered = array[j];

						if(covered == target || removed.contains(covered) || covered.getGeomDistance(x, y) < distance)
							continue;

						if(!Angles.isInDegree(x, y, degree, covered.getX(), covered.getY(), angle))
							continue;

						// так как все проверки прошел, добавляем в список прикрытых
						removed.add(covered);
					}
				}
			}

			// если нашлись прикрытые, то удаляем из списка целей
			if(!removed.isEmpty())
			{
				array = removed.array();

				for(int i = 0, length = removed.size(); i < length; i++)
					targets.fastRemove(array[i]);
			}
		}

		// если есть лимит по кол-ву целей
		if(skill.getMaxTargets() > 0)
		{
			// смотрим разницу текущего кол-во с лимитом
			int diff = targets.size() - skill.getMaxTargets();

			// если текущее кол-во превышает лимит
			if(diff > 0)
				// откидываем лишние цели
				for(int i = diff; i > 0; i--)
					targets.pop();
		}
	}
}
