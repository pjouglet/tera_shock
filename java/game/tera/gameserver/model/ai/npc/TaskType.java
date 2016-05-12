package tera.gameserver.model.ai.npc;

/**
 * Типы заданий для АИ
 * @author Ronn
 */
public enum TaskType
{
	/** движение c обновлением разворота */
	MOVE_UPDATE_HEADING,
	/** движение без обновления разворота */
	MOVE_NOT_UPDATE_HEADING,
	/** движение для каста скила */
	MOVE_ON_CAST,
	/** каст скила */
	CAST,
	/** каст с применением указанного направления */
	CAST_ON_HEADING,
	/** наблюдение */
	NOTICE,
	/** ускоренный фокус */
	NOTICE_FAST,
}
