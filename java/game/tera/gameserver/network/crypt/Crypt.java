package tera.gameserver.network.crypt;

import rlib.util.array.Arrays;
import rlib.util.sha160.Sha160;


/**
 * Модель криптора.
 */
public final class Crypt
{
	public static final byte EMPTY_BYTE_ARRAY[] = new byte[0];

    /**
	 * Конвектирование байтов в беззнаковый инт.
	 *
	 * @param bytes исходный массив байтов.
	 * @param offset отступ в массиве.
	 */
    private static long bytesToUInts(byte bytes[], int offset)
    {
    	// получаем знаковый ИНТ
        int i = (0xFF & bytes[offset + 3]) << 24 | (0xFF & bytes[offset + 2]) << 16 | (0xFF & bytes[offset + 1]) << 8 | 0xFF & bytes[offset];

        // убераем знак
        return (long) i & 0xFFFFFFFFL;
    }

    /**
     * Запись беззнакового инта в массив байтов
     *
     * @param containerInt массив байтов.
     * @param unsignedInt само ковектируемое число.
     * @param offset отступ в массиве.
     */
    private static void intToBytes(byte containerInt[], long unsignedInt, int offset)
    {
        containerInt[offset + 3] = (byte)(int)((unsignedInt & 0xff000000L) >> 24);
        containerInt[offset + 2] = (byte)(int)((unsignedInt & 0xff0000L) >> 16);
        containerInt[offset + 1] = (byte)(int)((unsignedInt & 65280L) >> 8);
        containerInt[offset] = (byte)(int)(unsignedInt & 255L);
    }

    public static void shiftKey(byte[] src, byte[] dest, int n, boolean direction)
	{
		byte[] tmp = new byte[128];

		for(int i = 0; i < 128; i++)
		{
			if(direction)
				tmp[(i + n) % 128] = src[i];
			else
				tmp[i] = src[(i + n) % 128];
		}
		for(int i = 0; i < 128; i++)
			dest[i] = tmp[i];
	}

    /**
     * Извлечение подмассива байтов
     *
     * @param buffer исходный массив.
     * @param offset отступ.
     * @param end конец.
     */
    public static byte[] subarray(byte buffer[], int offset, int end)
    {
        if(buffer == null)
            return null;

        if(offset < 0)
            offset = 0;

        if(end > buffer.length)
            end = buffer.length;

        int newSize = end - offset;

        if(newSize <= 0)
            return EMPTY_BYTE_ARRAY;
        else
        {
        	// создаем новый массив
            byte subarray[] = new byte[newSize];

            // переносим данные в новый массив
            System.arraycopy(buffer, offset, subarray, 0, newSize);

            // возвращаем его
            return subarray;
        }
    }

    public static void xorKey(byte[] src1, byte[] src2, byte[] dst)
	{
		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 16; j++)
				dst[i * 16 + j] = (byte) ((src1[i * 16 + j] & 0xff) ^ (src2[i * 16 + j] & 0xff));
	}

    private int changeData;
    private int changeLenght;

    /** массив ключей */
    private CryptKey[] keys;

    private CryptKey first;
    private CryptKey second;
    private CryptKey thrid;

    /** байтовый контейнер ИНТ */
    private byte[] containerInt;

    public Crypt()
    {
        this.first = new CryptKey(31, 55);
        this.second = new CryptKey(50, 57);
        this.thrid = new CryptKey(39, 58);

        this.keys = Arrays.toGenericArray(first, second, thrid);

        this.containerInt = new byte[4];
    }

    /**
     * Приминение криптора.
     *
     * @param buffer юуфер данных
     * @param size длинна обрабатываемых данных.
     */
    public void applyCryptor(byte buffer[], int size)
    {
    	int changeLenght = getChangeLenght();
    	int changeData = getChangeData();

        int pre = size >= changeLenght ? changeLenght : size;

        // получаем контейнер для инта
        byte[] containerInt = getContainerInt();

        if(pre != 0)
        {
            if(pre > 0)
            {
            	intToBytes(containerInt, changeData, 0);

                // применяем изменения
                for(int j = 0; j < pre; j++)
                    buffer[j] ^= containerInt[(4 - changeLenght) + j];
            }

            changeLenght -= pre;
            size -= pre;
        }

        int offset = pre;

        // получаем массив ключей
        CryptKey[] keys = getKeys();

        // поучаем прямые ссылки на ключи
        CryptKey first = getFirst();
        CryptKey second = getSecond();
        CryptKey thrid = getThrid();

        for(int i = 0; i < size / 4; i++)
        {
            int result = first.getKey() & second.getKey() | thrid.getKey() & (first.getKey() | second.getKey());

            // перебираем ключи
            for(int j = 0; j < 3; j++)
            {
            	// получаем ключ
                CryptKey key = keys[j];

                // если ключ совпадает
                if(result == key.getKey())
                {
                    long t1 = bytesToUInts(key.getBuffer(), key.getFirstPos() * 4);
                    long t2 = bytesToUInts(key.getBuffer(), key.getSecondPos() * 4);

                    long t3 = t1 > t2 ? t2 : t1;

                    long sum = t1 + t2;

                    sum = sum > 0xFFFFFFFFL? (long)((int) t1 + (int) t2) & 0xFFFFFFFFL : sum;

                    key.setSum(sum);

                    key.setKey(t3 <= sum ? 0 : 1);

                    key.setFirstPos((key.getFirstPos() + 1) % key.getSize());
                    key.setSecondPos((key.getSecondPos() + 1) % key.getSize());
                }

                long unsBuf = bytesToUInts(buffer, offset + i * 4) ^ key.getSum();

                intToBytes(buffer, unsBuf, offset + i * 4);
            }
        }

        int remain = size & 3;

        if(remain != 0)
        {
            int result = first.getKey() & second.getKey() | thrid.getKey() & (first.getKey() | second.getKey());

            changeData = 0;

            for(int j = 0; j < 3; j++)
            {
                // получаем ключ
                CryptKey key = keys[j];

                // если ключ совпадает
                if(result == key.getKey())
                {
                    long t1 = bytesToUInts(key.getBuffer(), key.getFirstPos() * 4);
                    long t2 = bytesToUInts(key.getBuffer(), key.getSecondPos() * 4);

                    long t3 = t1 > t2 ? t2 : t1;

                    long sum = t1 + t2;

                    sum = sum > 0xFFFFFFFFL? (long)((int) t1 + (int) t2) & 0xFFFFFFFFL : sum;

                    key.setSum(sum);

                    key.setKey(t3 <= sum ? 0 : 1);

                    key.setFirstPos((key.getFirstPos() + 1) % key.getSize());
                    key.setSecondPos((key.getSecondPos() + 1) % key.getSize());
                }

                changeData ^= key.getSum();
            }

            intToBytes(containerInt, changeData, 0);

            for(int j = 0; j < remain; j++)
                buffer[((size + pre) - remain) + j] ^= containerInt[j];

            changeLenght = 4 - remain;
        }

        setChangeData(changeData);
        setChangeLenght(changeLenght);
    }

    private void fillKey(byte src[], byte dst[])
    {
        for(int i = 0; i < 680; i++)
            dst[i] = src[i % 128];

        dst[0] = -128;
    }

    /**
     * Генерация итогового ключа по полученному.
     *
     * @param source исходный ключ.
     */
    public void generateKey(byte source[])
    {
        byte buffer[] = new byte[680];

        fillKey(source, buffer);

        Sha160 sha160 = new Sha160();

        for(int i = 0; i < 680; i += 20)
        {
            sha160.update(buffer, 0, 680);

            byte digest2[] = sha160.digest();

            int j = i;

            for(int l = 0; j < i + 20; l++)
            {
                buffer[j] = digest2[l];
                j++;
            }
        }

        first.setBuffer(subarray(buffer, 0, 220));
        second.setBuffer(subarray(buffer, 220, 448));
        thrid.setBuffer(subarray(buffer, 448, 680));
    }

    /**
     * @return изменение данных.
     */
    public int getChangeData()
	{
		return changeData;
	}

    /**
     * @return длинна изменений.
     */
    public int getChangeLenght()
	{
		return changeLenght;
	}

    /**
     * @return байтовый контейнер инта.
     */
    public byte[] getContainerInt()
	{
		return containerInt;
	}

    /**
     * @return первый ключ.
     */
    public CryptKey getFirst()
	{
		return first;
	}

    /**
     * @return массив ключей.
     */
    public CryptKey[] getKeys()
	{
		return keys;
	}

    /**
     * @return второй ключ.
     */
    public CryptKey getSecond()
	{
		return second;
	}

    /**
     * @return третий ключ.
     */
    public CryptKey getThrid()
	{
		return thrid;
	}

    /**
     * @param changeData изменение данных.
     */
    public void setChangeData(int changeData)
	{
		this.changeData = changeData;
	}

    /**
     * @param changeLenght
     */
    public void setChangeLenght(int changeLenght)
	{
		this.changeLenght = changeLenght;
	}
}