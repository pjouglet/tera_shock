package tera.gameserver.model.skillengine;

/**
 * Перечисление тип работ скилов
 * 
 * @author Ronn
 */
public enum OperateType
{
	/** пассивный */
	PASSIVE,
	/** активный */
	ACTIVE,
	/** заряжающийся */
	CHARGE,
	/** активиремые скилы */
	ACTIVATE,
	/** кастовый для итема */
	CAST_ITEM,
	/** без каста для итема */
	NO_CAST_ITEM,
	/** лок он скил */
	LOCK_ON;
}
