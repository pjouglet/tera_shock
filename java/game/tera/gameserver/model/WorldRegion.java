package tera.gameserver.model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import rlib.concurrent.Locks;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.ai.CharacterAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.model.traps.Trap;
import tera.gameserver.network.serverpackets.ServerPacket;

/**
 * Класс описывающий ячеку в мире, в которой находятся объекты.
 *
 * @author Ronn
 * @created 24.02.2012
 */
public final class WorldRegion
{
	/** локер на запись */
	private final Lock writeLock;
	/** локер на чтение */
	private final Lock readLock;

	/** координаты региона */
	private final int tileX;
	private final int tileY;
	private final int tileZ;

	/** ид континента */
	private final int continentId;

	/** массив объектов находящихся в регионе */
	private volatile TObject[] objects;

	/** массив регионов этот + окружающие */
	private volatile WorldRegion[] neighbors;

	/** положение свободной ячейки в массиве */
	private volatile int ordinal;
	/** всего объектов */
	private volatile int sizeAll;
	/** всего нпс */
	private volatile int sizeNpcs;
	/** всего игроков */
	private volatile int sizePlayers;

	/** активен ли регион */
	private volatile boolean active;

	/** территории, входящие в этот регион */
	private Territory[] territories;
	/** зоны, которые находятся в этом регионе */
	private WorldZone[] zones;
	/** рабочие ловушки в регионе */
	private Array<Trap> traps;

	/**
	 * @param continentId ид континента.
	 * @param tileX индекс в массиве мира.
	 * @param tileY индекс в массиве мира.
	 * @param tileZ индекс в массиве мира.
	 */
	public WorldRegion(int continentId, int tileX, int tileY, int tileZ)
	{
		this.continentId = continentId;
		this.tileX = tileX;
		this.tileY = tileY;
		this.tileZ = tileZ;
		this.sizeAll = 0;
		this.sizeNpcs = 0;
		this.sizePlayers = 0;
		this.ordinal = 0;
		this.active = false;
		this.traps = Arrays.toArray(Trap.class);

		ReadWriteLock readWriteLock = Locks.newRWLock();

		this.writeLock = readWriteLock.writeLock();
		this.readLock = readWriteLock.readLock();
	}

	/**
	 * Обработка активаций ловушек.
	 *
	 * @param object движущийся объект.
	 */
	public void activateTrap(TObject object)
	{
		// если объект не персонаж или ловушек нет, выходим
		if(!object.isCharacter() || traps.isEmpty())
			return;

		readLock.lock();
		try
		{
			// получаем массив ловушек
			Trap[] array = traps.array();

			// пробуем активировать на персонаж
			for(int i = 0, length = traps.size(); i < length; i++)
				array[i].activate(object);
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Получение списка потенциальных препядствий для движения во время каста скила.
	 *
	 * @param array список объектов.
	 * @param caster кастующий персонаж.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата z.
	 * @param distance перемещения.
	 */
	public void addBarriers(Array<Character> array, Character caster, float x, float y, float z, float distance)
	{
		// если объектов нет, выходим
		if(objects == null || sizeAll == 0)
			return;

		readLock.lock();
		try
		{
			// получаем объекты региона
			TObject[] objects = getObjects();

			// перебираем объекты
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// пытаемся получить персонажа из объекта
				Character target = objects[i].getCharacter();

				// проверяем на потенциальность препграды
				if(target == null || target == caster || target.getGeomDistance(x, y) > distance || Math.abs(z - target.getZ()) > 90F)
					continue;

				// добавляем в список
				array.add(target);
			}
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Добавление в коллекцию объектов указанного класса.
	 *
	 * @param container массив объектов.
	 * @param type тип искомых объектов.
	 * @param exclude исключаемый объект.
	 * @param subId саб ид исключаемого.
	 * @return список искомых объектов.
	 */
	public <V extends T, T extends TObject> Array<T> addObject(Array<T> container, Class<V> type, int exclude, int subId)
	{
		// если объектов нет, выходим
		if(objects == null || sizeAll == 0)
			return container;

		readLock.lock();
		try
		{
			// получаем объекты региона
			TObject[] objects = getObjects();

			// перебираем объекты
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// получаем объект
				TObject object = objects[i];

				// если объект подходит, добавляем в контейнер
				if(!(object.getObjectId() == exclude && object.getSubId() == subId) && type.isInstance(object))
					container.add(type.cast(object));
			}

			return container;
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Добавляет объект в регион.
	 *
	 * @param object новый объект в мире.
	 */
	public void addObject(TObject object)
	{
		// если объекта нет, выходим
		if(object == null)
			return;

		// обновлять ли статус региона
		boolean changeStatus = false;

		writeLock.lock();
		try
		{
			// если это ловушка, добавляем в список ловушек
			if(object.isTrap())
				traps.add(object.getTrap());

			// если массива нету, создаем
			if(objects == null)
				objects = new TObject[10];

			// вносим новый объект
			objects[ordinal] = object;
			// смешаем орден на свободную ячейку
			ordinal += 1;

			// если массив заполнен, увеличиваем на 10
			if(ordinal == objects.length)
				objects = Arrays.copyOf(objects, 10);

			// запоминаем текущее кол-во игроков
			int oldPlayer = sizePlayers;

			// увеличиваем счетчики
			if(object.isPlayer())
				sizePlayers += 1;
			else if(object.isNpc())
				sizeNpcs += 1;

			sizeAll += 1;

			// если кол-во игроков увеличилось, а раньше их небыло, ставим флаг тру
			changeStatus = oldPlayer < 1 && sizePlayers > 0;
		}
		finally
		{
			writeLock.unlock();
		}

		// если надо обновлять статус, обновляем
		if(changeStatus)
			changeStatus();
	}

	/**
	 * Добавление в список объектов в рамках региона.
	 *
	 * @param container контейнер объектов.
	 * @param type тип нужных объектов.
	 */
	public <T extends TObject> void addObjects(Array<T> container, Class<T> type)
	{
		// если объектов нет, выходим
		if(objects == null || sizeAll == 0)
			return;

		readLock.lock();
		try
		{
			// получаем объекты региона
			TObject[] objects = getObjects();

			// перебираем их
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// получаем объект
				TObject object = objects[i];

				// если объект не подходит, пропускаем
				if(!type.isInstance(object))
					continue;

				// добавляем в контайнер
				container.add(type.cast(object));
			}
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Получение списка объектов с указанным классом в указанный лист вокруг точки исключая указанный ид.
	 *
	 * @param container список объектов.
	 * @param type искомый тип объектов.
	 * @param exclude исключаемый объект.
	 * @param subId саб ид исключаемого.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @param radius радиус.
	 * @return список искомых объектов.
	 */
	public <V extends T, T extends TObject> Array<T> addObjects(Array<T> container, Class<V> type, int exclude, int subId, float x, float y, float z, float radius)
	{
		// если объектов нет, выходим
		if(objects == null || sizeAll == 0)
			return container;

		readLock.lock();
		try
		{
			// получаем объекты региона
			TObject[] objects = getObjects();

			// перебираем их
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// получаем объект
				TObject object = objects[i];

				// если объект не подходит, пропускаем
				if(object.getObjectId() == exclude && object.getSubId() == subId || !type.isInstance(object) || !object.isInRange(x, y, z, radius))
					continue;

				// добавляем в контайнер
				container.add(type.cast(object));
			}

			return container;
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Получение кол-во искомых объектов.
	 *
	 * @param type искомый тип объектов.
	 * @param exclude исключаемый объект.
	 * @param subId саб ид исключаемого.
	 * @param x координата.
	 * @param y координата.
	 * @param z координата.
	 * @param radius радиус.
	 * @return кол-во искомых объектов.
	 */
	public <V extends T, T extends TObject> int getObjectCount(Class<V> type, int exclude, int subId, float x, float y, float z, float radius)
	{
		// если объектов нет, выходим
		if(objects == null || sizeAll == 0)
			return 0;

		int counter = 0;

		readLock.lock();
		try
		{
			// получаем объекты региона
			TObject[] objects = getObjects();

			// перебираем их
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// получаем объект
				TObject object = objects[i];

				// если объект не подходит, пропускаем
				if(object.getObjectId() == exclude && object.getSubId() == subId || !type.isInstance(object) || !object.isInRange(x, y, z, radius))
					continue;

				counter++;
			}

			return counter;
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Добавляет объект для игроков.
	 *
	 * @param object новый объект.
	 */
	public void addToPlayers(TObject object)
	{
		// если объектов нет, выходим
		if(objects == null || sizeAll < 1)
			return;

		readLock.lock();
		try
		{
			// пробуем получить с объекта игрока
			Player player = object.getPlayer();

			// объекты региона
			TObject[] objects = getObjects();

			// если получить игрока удалось
			if(player != null)
			{
				// отобпажем ему окружающие объекты
				for(int i = 0, length = ordinal; i < length; i++)
					player.addVisibleObject(objects[i]);
			}

			// показываем окружающим объектам добавляемый объект
			for(int i = 0, length = ordinal; i < length; i++)
			{
				Player target = objects[i].getPlayer();

				if(target == null || target == object)
					continue;

				target.addVisibleObject(object);
			}
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Отправка пакета всем игрокав в этом регионе.
	 *
	 * @param sender отправитель пакета.
	 * @param packet отправляемый пакет.
	 */
	public void calcSendCount(TObject sender, ServerPacket packet)
	{
		// если игроков тут нет, выходим
		if(objects == null || sizePlayers == 0)
			return;

		//TODO
		readLock.lock();
		try
		{
			// получаем объекты региона
			TObject[] objects = getObjects();

			// перебираем все объекты в регионе
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// пробуем получить игрока из объекта
				Player target = objects[i].getPlayer();

				// если получить не удалось или этот игрок и есть отправитель, пропускаем
				if(target == null || target == sender)
					continue;

				// увеличивает счетчик отправок
				packet.increaseSends();
			}
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Обновляет статус активности близлежащих регионов.
	 */
	private void changeStatus()
	{
		// получаем соседние регионы
		WorldRegion[] regions = getNeighbors();

		// вызываем у них метод обновления активности
		for(int i = 0, length = regions.length; i < length; i++)
			regions[i].updateActive();
	}

	@Override
	public boolean equals(Object target)
	{
		return this == target;
	}

	/**
	 * @return ид континента.
	 */
	public int getContinentId()
	{
		return continentId;
	}

	public String getName()
	{
		return "(" + tileX + ", " + tileY + ", " + tileZ + ")";
	}

	/**
	 * @return список соседних регионов.
	 */
	public WorldRegion[] getNeighbors()
	{
		// если массив соседей не сформирован
		if(neighbors == null)
		{
			// синхронизируемся
			synchronized(this)
			{
				// если досихпор не сформирован
				if(neighbors == null)
					// формируем
					neighbors = World.getNeighbors(continentId, tileX, tileY, tileZ);
			}
		}

		return neighbors;
	}

	/**
	 * Получение объекта с указанным уник ид находящегося в этом регионе.
	 *
	 * @param objectId уникальный ид.
	 * @param subId саб ид объекта.
	 * @return искомый объект.
	 */
	public TObject getObject(int objectId, int subId)
	{
		// если объектов нет, выходим
		if(objects == null)
			return null;

		readLock.lock();
		try
		{
			// получаем объекты в регионе
			TObject[] objects = getObjects();

			// перебираем объекты
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// получаем объект
				TObject object = objects[i];

				// если это нужный, возвращаем его
				if(object.getObjectId() == objectId && object.getSubId() == subId)
					return object;
			}

			return null;
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Получение объекта с указанным именем находящегося в этом регионе.
	 *
	 * @param name имя объекта.
	 * @return искомый объект.
	 */
	public TObject getObject(String name)
	{
		// если объектов нет или имени, выходим
		if(name == null || objects == null)
			return null;

		readLock.lock();
		try
		{
			// получаем объекты в регионе
			TObject[] objects = getObjects();

			// перебираем их
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// получаем объект
				TObject object = objects[i];

				// сравниваем имя
				if(name.equals(object.getName()))
					// если совпадают, возвращаем
					return object;
			}

			return null;
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * @return массив объектов в регионе.
	 */
	private final TObject[] getObjects()
	{
		return objects;
	}

	/**
	 * @return кол-во всех объектов в регионе.
	 */
	public int getSizeAll()
	{
		return sizeAll;
	}

	/**
	 * @return кол-во всех нпс.
	 */
	public int getSizeNpcs()
	{
		return sizeNpcs;
	}

	/**
	 * @return кол-во игроков.
	 */
	public int getSizePlayers()
	{
		return sizePlayers;
	}

	/**
	 * @return территории, которые есть в регионе.
	 */
	public final Territory[] getTerritories()
	{
		return territories;
	}

	/**
	 * @return ид зоны, в которой находится объект.
	 */
	public final int getZoneId(TObject object)
	{
		WorldZone[] zones = getZones();

		// если зон нету, выходим
		if(zones == null)
			return object.getContinentId() + 1;

		// если только одна, сразу смотрим ее без цикла
		if(zones.length == 1)
		{
			// получаем единственную зону
			WorldZone zone = zones[0];

			// если объект в ее входит
			//todo : check cette merde (TP kanstria)
			return zone.getZoneId();
			/*if(zone.contains((int) object.getX(), (int) object.getY(), (int) object.getZ()))
				// возвращаем ее ид
				return zone.getZoneId();*/
		}
		// иначе перебираем все зоны
		else
		{
			for(int i = 0, length = zones.length; i < length; i++)
			{
				// получаем зону
				WorldZone zone = zones[i];

				// если объект входит в эту зону
				if(zone.contains((int) object.getX(), (int) object.getY(), (int) object.getZ()))
					// возвращаем ее ид
					return zone.getZoneId();
			}
		}

		return -1;
	}

	/**
	 * @return массив зон, пересекающихся с этим регионом.
	 */
	public final WorldZone[] getZones()
	{
		return zones;
	}

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;

		result = prime * result + tileX;
		result = prime * result + tileY;
		result = prime * result + tileZ;

		return result;
	}

	/**
	 * @return есть ли в регионе территории.
	 */
	public final boolean hasTerritories()
	{
		return territories != null;
	}

	/**
	 * Активность региона, подразумивает наличие в нем игрока.
	 *
	 * @return активный ли регион.
	 */
	public boolean isActive()
	{
		return active;
	}

	/**
	 * @return пустой ли регион.
	 */
	public boolean isEmpty()
	{
		return sizeAll < 1;
	}

	/**
	 * Удаляем объект для игроков.
	 *
	 * @param object удаляесый объект.
	 */
	public void removeFromPlayers(TObject object, int type)
	{
		// если объектов нет, выходим
		if(objects == null || sizeAll == 0)
			return;

		readLock.lock();
		try
		{
			// пытаемся получить игрока из объекта
			Player player = object.getPlayer();

			// получаем объекты региона
			TObject[] objects = getObjects();

			// если игрока получили
			if(player != null)
			{
				// перебираем окружающие объекты
				for(int i = 0, length = ordinal; i < length; i++)
				{
					// получаем объект
					TObject target = objects[i];

					// если его нет или это удаляемый, то пропускаем
					if(target == null || target == object)
						continue;

					// удаляем его у удаляемого игрока
					player.removeVisibleObject(target, type);
				}
			}

			// перебираем окружающие объекты
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// получаем с объекта игрока
				Player target = objects[i].getPlayer();

				// если игрока смогли получить, удаляем у него удаляемый объект
				if(target != null && target != object)
					target.removeVisibleObject(object, type);
			}
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Удаляет объект из региона.
	 *
	 * @param object удаляемый объект.
	 */
	public void removeObject(TObject object)
	{
		// если объекта нет или объектов в регионе, то выходим
		if(object == null || objects == null)
			return;

		// флаг, нужно ли обновлять статус региона
		boolean changeStatus = false;

		writeLock.lock();
		try
		{
			// если удаляемый объект ловушка
			if(object.isTrap())
				// удаляем из списка ловушек
				traps.fastRemove(object);

			// флаг с результатом удаления объекта
			boolean removed = false;

			// получаем локальный список объектов
			TObject[] objects = getObjects();

			// перебираем объекты
			for(int i = 0, length = objects.length; i < length; i++)
				// если это искомый объект
				if(objects[i] == object)
				{
					// заменяем его на последний в списке
					objects[i] = objects[--ordinal];
					// зануляем последнюю занятую
					objects[ordinal] = null;

					// ставим флаг что удаление произведено
					removed = true;

					break;
				}

			// если объект был удален
			if(removed)
			{
				// уменьшаем общий счетчик
				sizeAll -= 1;

				// получаем старое кол-во игроков в регионе
				int oldPlayers = sizePlayers;

				// уменьшаем счетчик
				if(object.isPlayer())
					sizePlayers -= 1;
				else if(object.isNpc())
					sizeNpcs -= 1;

				// если был удален последний игрок, стави флаг требования обновления статуса
				changeStatus = oldPlayers > 0 && sizePlayers <= 0;
			}
		}
		finally
		{
			writeLock.unlock();
		}

		// если надо обновить статус
		if(changeStatus)
			// обновляем статус
			changeStatus();
	}

	/**
	 * Отправка пакета всем игрокав в этом регионе.
	 *
	 * @param sender отправитель пакета.
	 * @param packet отправляемый пакет.
	 */
	public void sendPacket(TObject sender, ServerPacket packet)
	{
		// если игроков тут нет, выходим
		if(objects == null || sizePlayers == 0)
			return;

		readLock.lock();
		try
		{
			// получаем объекты региона
			TObject[] objects = getObjects();

			// перебираем все объекты в регионе
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// пробуем получить игрока из объекта
				Player target = objects[i].getPlayer();

				// если получить не удалось или этот игрок и есть отправитель, пропускаем
				if(target == null || target == sender)
					continue;

				// добавляем на отправку пакет игроку
				target.sendPacket(packet, true);
			}
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Установка активности региона.
	 */
	private synchronized void setActive(boolean newActive)
	{
		// применяем новый статус
		active = newActive;

		// если он стал активным
		if(newActive)
		{
			// запускаем всем АИ
			startAI();
			// добавляемся в активные регионы
			World.addActiveRegion(this);
		}
		else
		{
			// останавливаем все АИ
			stopAI();
			// удаляемся из активных регионов
			World.removeActiveRegion(this);
		}
	}

	/**
	 * @param territories набор территорий в регионе.
	 */
	public void setTerritories(Territory[] territories)
	{
		this.territories = territories;
	}

	/**
	 * @param zones зоны, которые находятся в этом регионе.
	 */
	public void setZones(WorldZone[] zones)
	{
		this.zones = zones;
	}

	/**
	 * Запуск обычных АИ у нпс.
	 */
	public void startAI()
	{
		// если объектов нет, выходим
		if(objects == null || sizeNpcs < 1)
			return;

		readLock.lock();
		try
		{
			TObject[] objects = getObjects();

    		// перебираем объекты в регионе
    		for(int i = 0, length = ordinal; i < length; i++)
    		{
    			// пытаемся получить нпс
    			Npc npc = objects[i].getNpc();

    			// если нет, пропускаем
    			if(npc == null)
    				continue;

    			// получаем АИ нпс
				CharacterAI ai = npc.getAI();

				// если АИ глобальное, пропускаем
				if(ai.isGlobalAI())
					continue;

				// запускаем АИ
				ai.startAITask();
				// запускаем авто эмоции
				npc.startEmotions();
			}
		}
		finally
		{
			readLock.unlock();
		}
	}

	/**
	 * Остановка обычных АИ у нпс.
	 */
	public void stopAI()
	{
		// если объектов нет, выходим
		if(objects == null || sizeNpcs < 1)
			return;

		readLock.lock();
		try
		{
			TObject[] objects = getObjects();

			// перебираем объекты в регионе
			for(int i = 0, length = ordinal; i < length; i++)
			{
				// пытаемся получить нпс
				Npc npc = objects[i].getNpc();

				// если нет, пропускаем
				if(npc == null)
					continue;

				// получаем АИ нпс
				CharacterAI ai = npc.getAI();

				// если он глобальный, пропускаем
				if(ai.isGlobalAI())
					continue;

				// останавливаем АИ
				ai.stopAITask();
				// останавливаем автоэмоции
				npc.stopEmotions();
			}
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public String toString()
	{
		return "WorldRegion sizeAll = " + sizeAll + ", sizeNpcs = " + sizeNpcs + ", sizePlayers = " + sizePlayers + ", continentId = " + continentId + ", tileX = " + tileX + ", tileY = " + tileY + ", tileZ = " + tileZ + ", active = " + active;
	}

	/**
	 * Обновление состояние активности региона.
	 */
	private void updateActive()
	{
		// флаг для определения текущего состояния
		boolean current = false;

		// получаем соседние регионы
		WorldRegion[] around = getNeighbors();

		// перебираем регионы
		for(int i = 0, length = around.length; i < length; i++)
		{
			// получаем регион
			WorldRegion region = around[i];

			// если хоть у одного региона есть игрок
			if(region.getSizePlayers() > 0)
			{
				// ставим флаг текущей активности
				current = true;
				break;
			}
		}

		// если реальное состояние не совпадает с текущим в регионе, то изменяем
		if(current != active)
			setActive(current);
	}
}