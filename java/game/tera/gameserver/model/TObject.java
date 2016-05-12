package tera.gameserver.model;

import rlib.gamemodel.GameObject;
import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.Config;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.ai.AI;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.model.traps.Trap;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.tables.ItemTable;
import tera.util.Location;

/**
 * Класс описывающий фундамент всем игровым объектам.
 *
 * @author Ronn
 * @created 05.03.2012
 */
public abstract class TObject implements GameObject
{
	protected static final Logger log = Loggers.getLogger(TObject.class);

	protected static final float HEADINGS_IN_PI = 10430.378350470452724949566316381F;

	/** пустой массив объектов */
	public static final TObject[] EMTY_OBJECTS = new TObject[0];

	/** пустой массив объектов */
	public static final Array<TObject> EMPTY_ARRAY = Arrays.toArray(TObject.class, 0);

	/** уникальный ид объекта */
	protected int objectId;
	/** разворот объекта */
	protected int heading;
	/** ид континента, на котором находится объект */
	protected int continentId;

	/** текущее x */
	protected float x;
	/** текущее y */
	protected float y;
	/** текущее z */
	protected float z;

	/** видим ли объект */
	protected volatile boolean visible;
	/** удаленный объект */
	protected volatile boolean deleted;

	/** текущий регион */
	protected WorldRegion currentRegion;

	/**
	 * @param objectId уникальный ид объекта.
	 */
	public TObject(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * Добавляет в отображение себя указанному игроку.
	 *
	 * @param player игрок.
	 */
	public void addMe(Player player){}

	@Override
	public final int compareTo(GameObject object)
	{
		return objectId - object.getObjectId();
	}

	/**
	 * Удаляет объект из региона и делает его невидимым для окружающийх.
	 *
	 * @param type тип исчезновения.
	 */
	public void decayMe(int type)
	{
		synchronized(this)
		{
			// если уже убран из отображениы в мире, выходим
			if(!isVisible())
				return;

			// ставим флаг невидимости
			setVisible(false);
		}

		// удаляем из мира
		World.removeVisibleObject(this, type);
	}

	/**
	 * Удаляет объект из мира вообще.
	 */
	public void deleteMe()
	{
		if(isDeleted())
			return;

		// удаляем из мира
		deleteMe(DeleteCharacter.DISAPPEARS);

		// ставим флаг удаленности
		setDeleted(true);

		// получаем менеджер событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем об этом
		eventManager.notifyDelete(this);
	}

	/**
	 * Удаляет объект из мира с указаным типом пакета.
	 *
	 * @param type тип исчезновения.
	 */
	public void deleteMe(int type)
	{
		decayMe(type);
	}

	/**
	 * @return АИ объекта.
	 */
	public AI getAI()
	{
		return null;
	}

	/**
	 * @return персонаж.
	 */
	public Character getCharacter()
	{
		return null;
	}

	/**
	 * @return ид континента.
	 */
	public final int getContinentId()
	{
		return continentId;
	}

	/**
	 * @return текущий регион.
	 */
	public final WorldRegion getCurrentRegion()
	{
		return currentRegion;
	}

	/**
	 * Рассчет 2Д дистанции от объекта до указанной точки.
	 *
	 * @param targetX координата точки.
	 * @param targetY координата точки.
	 * @return расстояние до точки.
	 */
	public final float getDistance(float targetX, float targetY)
	{
		float dx = targetX - x;
		float dy = targetY - y;

		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Рассчет 3Д дистанции от объекта до указанной точки.
	 *
	 * @param targetX координата точки.
	 * @param targetY координата точки.
	 * @param targetZ координата точки.
	 * @return расстояние до точки.
	 */
	public final float getDistance(float targetX, float targetY, float targetZ)
	{
		float dx = targetX - x;
		float dy = targetY - y;
		float dz = targetZ - z;

		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Рассчет 2Д дистанции от объекта до указанного объекта.
	 *
	 * @param object объект.
	 * @return расстояние до одбъекта.
	 */
	public final float getDistance(TObject object)
	{
		if(object == null)
			return 0;

		float dx = object.x - x;
		float dy = object.y - y;

		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Рассчет 3Д дистанции от объекта до указанного объекта.
	 *
	 * @param object объект.
	 * @return расстояние до одбъекта.
	 */
	public final float getDistance3D(TObject object)
	{
		if(object == null)
			return 0;

		float dx = object.x - x;
		float dy = object.y - y;
		float dz = object.z - z;

		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Рассчет расстояния до модели объекта.
	 *
	 * @param targetX координата.
	 * @param targetY координата.
	 * @return расстояние до этой точки.
	 */
	public float getGeomDistance(float targetX, float targetY)
	{
		float dx = targetX - x;
		float dy = targetY - y;

		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public final int getHeading()
	{
		if(heading > 65536)
			heading -= 65536;

		return heading;
	}

	/**
	 * Рассчет разворота относительно указанной точки.
	 *
	 * @param targetX целевая кооридната.
	 * @param  targetY целевая кооридната.
	 * @return направление.
	 */
	public final int getHeadingTo(float targetX, float targetY)
	{
		float dx = targetX - x;
		float dy = targetY - y;

		int heading = (int) (Math.atan2(-dy, -dx) * HEADINGS_IN_PI + 32768);

		heading = getHeading() - heading;

		if(heading < 0)
			heading = heading + 1 + Integer.MAX_VALUE & 0xFFFF;
		else if(heading > 0xFFFF)
			heading &= 0xFFFF;

		return heading;
	}

	/**
	 * Рассчет разворота относительно указанного объекта.
	 *
	 * @param target целевой объект.
	 * @param toChar является ли объект персонажем.
	 * @return направление.
	 */
	public final int getHeadingTo(TObject target, boolean toChar)
	{
		if(target == null || target == this)
			return -1;

		float dx = target.x - x;
		float dy = target.y - y;

		int heading = (int) (Math.atan2(-dy, -dx) * HEADINGS_IN_PI + 32768);

		heading = toChar ? target.getHeading() - heading : getHeading() - heading;

		if(heading < 0)
			heading = heading + 1 + Integer.MAX_VALUE & 0xFFFF;
		else if(heading > 0xFFFF)
			heading &= 0xFFFF;

		return heading;
	}

	/**
	 * @return итем.
	 */
	public ItemInstance getItem()
	{
		return null;
	}

	/**
	 * @return точка, в которой находится персонаж.
	 */
	public final Location getLoc()
	{
		return getLoc(new Location(x, y, z, heading, continentId));
	}

	/**
	 * @return точка, в которой находится персонаж.
	 */
	public final Location getLoc(Location loc)
	{
		return loc.setXYZH(x, y, z, heading);
	}

	/**
	 * @return name имя объекта.
	 */
	public String getName()
	{
		return getClass().getSimpleName() + ":" + objectId;
	}

	/**
	 * @return нпс.
	 */
	public Npc getNpc()
	{
		return null;
	}

	/**
	 * @return уникальный ид объекта.
	 */
	public final int getObjectId()
	{
		return objectId;
	}

	/**
	 * @return игрок.
	 */
	public Player getPlayer()
	{
		return null;
	}

	/**
	 * @return ресурс.
	 */
	public ResourseInstance getResourse()
	{
		return null;
	}

	/**
	 * Рассчет квадрата дистанции от объекта до точки.
	 *
	 * @param targetX координата.
	 * @param targetY координата.
	 * @return квадрат расстояния.
	 */
	public final float getSquareDistance(float targetX, float targetY)
	{
		float dx = targetX - x;
		float dy = targetY - y;

		return dx * dx + dy * dy;
	}

	/**
	 * Рассчет квадрата дистанции от объекта до точки.
	 *
	 * @param targetX координата.
	 * @param targetY координата.
	 * @param targetZ координата.
	 * @return квадрат расстояния.
	 */
	public final float getSquareDistance(float targetX, float targetY, float targetZ)
	{
		float dx = targetX - x;
		float dy = targetY - y;
		float dz = targetZ - z;

		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * @return под ид объекта.
	 */
	public int getSubId()
	{
		return 0;
	}

	/**
	 * @return ид темплейта.
	 */
	public int getTemplateId()
	{
		return -1;
	}

	/**
	 * @return тип темплейта.
	 */
	public int getTemplateType()
	{
		return -1;
	}

	/**
	 * @return список территорий, в которых находится игрок.
	 */
	public Array<Territory> getTerritories()
	{
		return null;
	}

	/**
	 * @return получение ловушки.
	 */
	public Trap getTrap()
	{
		return null;
	}

	@Override
	public final float getX()
	{
		return x;
	}

	@Override
	public final float getY()
	{
		return y;
	}

	@Override
	public final float getZ()
	{
		return z;
	}

	/**
	 * @return есть ли у объекта АИ.
	 */
	public boolean hasAI()
	{
		return false;
	}

	@Override
	public final int hashCode()
	{
		return objectId;
	}

	/**
	 * @return является ли объект персонажем.
	 */
	public boolean isCharacter()
	{
		return false;
	}

	/**
	 * @return удален ли объект.
	 */
	public final boolean isDeleted()
	{
		return deleted;
	}

	/**
	 * Определяет, входят ли указанные координаты в модель персонажа.
	 *
	 * @param startX начало луча.
	 * @param startY начало луча.
	 * @param startZ начало луча.
	 * @param endX конец луча.
	 * @param endY конец луча.
	 * @param endZ конец луча.
	 * @param radius радиус луча.
	 * @return проходит ли луч через модель объекта.
	 */
	public boolean isHit(float startX, float startY, float startZ, float endX, float endY, float endZ, float radius)
	{
		return this.isHit(startX, startY, startZ, endX, endY, endZ, radius, true);
	}

	/**
	 * Определяет, входят ли указанные координаты в модель персонажа.
	 *
	 * @param startX начало луча.
	 * @param startY начало луча.
	 * @param startZ начало луча.
	 * @param endX конец луча.
	 * @param endY конец луча.
	 * @param endZ конец луча.
	 * @param radius радиус луча.
	 * @param checkHeight проверять ли высоту.
	 * @return проходит ли луч через модель объекта.
	 */
	public boolean isHit(float startX, float startY, float startZ, float endX, float endY, float endZ, float radius, boolean checkHeight)
	{
		return false;
	}

	/**
	 * Находится ли объект в радиусе от точки.
	 *
	 * @param targetX координаты.
	 * @param targetY координаты.
	 * @param targetZ координаты.
	 * @param range радиус.
	 * @return находится ли.
	 */
	public boolean isInRange(float targetX, float targetY, float targetZ, float range)
	{
		return getSquareDistance(targetX, targetY, targetZ) <= (range * range);
	}

	/**
	 * Находится ли объект в радиусе от точки.
	 *
	 * @param targetX координаты.
	 * @param targetY координаты.
	 * @param targetZ координаты.
	 * @param range радиус.
	 * @return находится ли.
	 */
	public boolean isInRange(float targetX, float targetY, float targetZ, int range)
	{
		return getSquareDistance(targetX, targetY, targetZ) <= (range * range);
	}

	/**
	 * Находится ли объект в радиусе от точки.
	 *
	 * @param targetX координаты.
	 * @param targetY координаты.
	 * @param range радиус.
	 * @return находится ли.
	 */
	public final boolean isInRange(float targetX, float targetY, int range)
	{
		return getSquareDistance(targetX, targetY) <= (range * range);
	}

	/**
	 * Находится ли объект в радиусе от точки.
	 *
	 * @param location точка.
	 * @param range радиус.
	 * @return находится ли.
	 */
	public final boolean isInRange(Location location, int range)
	{
		return getSquareDistance(location.getX(), location.getY()) <= (range * range);
	}

	/**
	 * Находится ли объект в радиусе от объект.
	 *
	 * @param object проверяемый объект.
	 * @param range расстояние.
	 * @return находится ли.
	 */
	public final boolean isInRange(TObject object, int range)
	{
		if(object == null)
			return false;

		float dx = Math.abs(object.x - x);

		if(dx > range)
			return false;

		float dy = Math.abs(object.y - y);

		if(dy > range)
			return false;

		float dz = Math.abs(object.z - z);

		return dz <= 1500 && dx * dx + dy * dy <= range * range;
	}

	/**
	 * Находится ли объект в радиусе от точки.
	 *
	 * @param location точка.
	 * @param range радиус.
	 * @return находится ли.
	 */
	public final boolean isInRangeZ(Location location, int range)
	{
		return getSquareDistance(location.getX(), location.getY(), location.getZ()) <= (range * range);
	}

	/**
	 * Находится ли объект в радиусе от объект.
	 *
	 * @param object проверяемый объект.
	 * @param range расстояние.
	 * @return находится ли.
	 */
	public final boolean isInRangeZ(TObject object, int range)
	{
		if(object == null)
			return false;

		float dx = Math.abs(object.x - x);

		if(dx > range)
			return false;

		float dy = Math.abs(object.y - y);

		if(dy > range)
			return false;

		float dz = Math.abs(object.z - z);

		return dz <= range && dx * dx + dy * dy + dz * dz <= range * range;
	}

	/**
	 * @return невидимый ли объект.
	 */
	public final boolean isInvisible()
	{
		return !visible;
	}

	/**
	 * Проверяет наличие объекта в мире.
	 *
	 * @return находится ли в мире.
	 */
	public final boolean isInWorld()
	{
		return currentRegion != null;
	}

	/**
	 * @return является ли объект итемом.
	 */
	public boolean isItem()
	{
		return false;
	}

	/**
	 * @return является ли объект НПС.
	 */
	public boolean isNpc()
	{
		return false;
	}

	/**
	 * @return является ли объект игроком.
	 */
	public boolean isPlayer()
	{
		return false;
	}

	/**
	 * @return является ли объект ресурсом.
	 */
	public boolean isResourse()
	{
		return false;
	}

	/**
	 * @return является ли объект суммоном.
	 */
	public boolean isSummon()
	{
		return false;
	}

	/**
	 * @return является ли объект ловушкой.
	 */
	public boolean isTrap()
	{
		return false;
	}

	@Override
	public final boolean isVisible()
	{
		return visible;
	}

	/**
	 * @return является ли мировым объектом.
	 */
	public boolean isWorldObject()
	{
		return false;
	}

	/**
	 * Обработка поднятия себя.
	 *
	 * @param target объект, который поднял.
	 */
	public boolean pickUpMe(TObject target)
	{
		return false;
	}

	/**
	 * Удаляет из отображение себя указанному игроку.
	 *
	 * @param player игрок.
	 * @param type тип удаления.
	 */
	public void removeMe(Player player, int type){}

	/**
	 * @param continentId ид континента.
	 */
	public final void setContinentId(int continentId)
	{
		this.continentId = continentId;
	}

	/**
	 * @param region текущий регион.
	 */
	public final void setCurrentRegion(WorldRegion region)
	{
		currentRegion = region;
	}

	/**
	 * @param deleted удален ли из мира объект
	 */
	protected final void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	/**
	 * @param heading разворот объекта.
	 */
	public final void setHeading(int heading)
	{
		this.heading = heading;
	}

	/**
	 * @param location текущая позиция.
	 */
	public void setLoc(Location location)
	{
		setContinentId(location.getContinentId());
		setXYZ(location.getX(), location.getY(), location.getZ());
	}

	/**
	 * @param objectId уникальный ид объекта.
	 */
	public void setObjectId(int objectId)
	{
		this.objectId = objectId;
	}

	/**
	 * @param territories список территорий.
	 */
	public void setTerritories(Array<Territory> territories)
	{
		log.warning(this, new Exception("unsupported method"));
	}

	/**
	 * @param visible видимый ли объект.
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	/**
	 * @param x координата.
	 */
	public void setX(float x)
	{
		this.x = x;
	}

	/**
	 * Установка новых координат объекта и обновление его размещения в мире.
	 *
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 */
	public void setXYZ(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;

		if((isSummon() || isPlayer()) && Config.DEVELOPER_DEBUG_MOVING_PLAYER)
			ItemTable.createItem(125, 1L).spawnMe(getLoc());
		else if(isNpc() && Config.DEVELOPER_DEBUG_MOVING_NPC)
			ItemTable.createItem(125, 1L).spawnMe(getLoc());

		World.addVisibleObject(this);
	}

	/**
	 * Установка новых координат для объекта.
	 *
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 */
	public void setXYZInvisible(float x, float y, float z)
	{
		//проверяем, не выходит ли за грани мира
		if(x > World.MAP_MAX_X)
			x = World.MAP_MAX_X;
		if(x < World.MAP_MIN_X)
			x = World.MAP_MIN_X;
		if(y > World.MAP_MAX_Y)
			y = World.MAP_MAX_Y;
		if(y < World.MAP_MIN_Y)
			y = World.MAP_MIN_Y;
		if(z < World.MAP_MIN_Z)
			z = World.MAP_MIN_Z;
		if(z > World.MAP_MAX_Z)
			z = World.MAP_MAX_Z;

		//применяем координаты
		this.x = x;
		this.y = y;
		this.z = z;

		//ставим флаг невидимости
		setVisible(false);
	}

	/**
	 * @param y координата.
	 */
	public void setY(float y)
	{
		this.y = y;
	}

	/**
	 * @param z координата.
	 */
	public void setZ(float z)
	{
		this.z = z;
	}

	/**
	 * Спавн объекта в мире.
	 */
	public void spawnMe()
	{
		synchronized(this)
		{
			// ставим флаг нахождения в мире
			setDeleted(false);

			// ставим флаг видимости
			setVisible(true);

			// добавляем в мир
			World.addVisibleObject(this);
		}

		// получаем менеджер событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// событие спавна
		eventManager.notifySpawn(this);
	}

	@Override
	public void spawnMe(float x, float y, float z, int heading)
	{
		if(x > World.MAP_MAX_X)
			x = World.MAP_MAX_X - 5000;
		if(x < World.MAP_MIN_X)
			x = World.MAP_MIN_X + 5000;
		if(y > World.MAP_MAX_Y)
			y = World.MAP_MAX_Y - 5000;
		if(y < World.MAP_MIN_Y)
			y = World.MAP_MIN_Y + 5000;

		this.x = x;
		this.y = y;
		this.z = z;

		if(heading > 0)
			setHeading(heading);

		spawnMe();
	}

	/**
	 * Спавн объекта в указаной точке.
	 *
	 * @param location новая точка.
	 */
	public void spawnMe(Location location)
	{
		// вносим ид континента
		setContinentId(location.getContinentId());
		// спавним
		spawnMe(location.getX(), location.getY(), location.getZ(), location.getHeading());
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " objectId = " + objectId + ", x = " + x + ", y = " + y + ", z = " + z + ", visible = " + visible + ", heading = " + heading + ", currentRegion = " + currentRegion;
	}

	/**
	 * Обновляет территории.
	 */
	public void updateTerritories()
	{
		log.warning(this, new Exception("unsupported method"));
	}

	/**
	 * Обновление ид зоны.
	 */
	public void updateZoneId(){}
}