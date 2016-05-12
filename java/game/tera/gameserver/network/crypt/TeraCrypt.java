package tera.gameserver.network.crypt;

import rlib.network.GameCrypt;
import rlib.util.array.Arrays;

/**
 * Криптор/декриптор пакетов Tera-Online.
 *
 * @author Ronn
 */
public final class TeraCrypt implements GameCrypt
{
	/** декриптор */
	private Crypt decrypt;
	/** криптор */
	private Crypt encrypt;

	/** временный массивы байтов */
	private byte[][] temps;

	/** статус криптора */
	private CryptorState state;

	public TeraCrypt()
	{
		decrypt = new Crypt();
		encrypt = new Crypt();

		temps = new byte[4][];

		state = CryptorState.WAIT_FIRST_CLIENT_KEY;
	}

	/**
	 * Очистка криптора для переиспользования.
	 */
	public void clear()
	{
		state = CryptorState.WAIT_FIRST_CLIENT_KEY;

		decrypt = new Crypt();
		encrypt = new Crypt();
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void decrypt(byte[] data, int offset, int length)
	{
		switch(state)
		{
			case READY_TO_WORK: decrypt.applyCryptor(data, length); break;
			case WAIT_FIRST_CLIENT_KEY:
			{
				if(length == 128)
				{
					temps[0] = Arrays.copyOf(data, 128);
					state = CryptorState.WAIT_FIRST_SERVER_KEY;
				}

				break;
			}
			case WAIT_SECOND_CLIENT_KEY:
			{
				if(length == 128)
				{
					temps[2] = Arrays.copyOf(data, 128);
					state = CryptorState.WAIT_SECOND_SERCER_KEY;
				}
			}
		}
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void encrypt(byte[] data, int offset, int length)
	{
		switch(state)
		{
			case READY_TO_WORK: encrypt.applyCryptor(data, length); break;
			case WAIT_FIRST_SERVER_KEY:
			{
				if(length == 128)
				{
					temps[1] = Arrays.copyOf(data, 128);
					state = CryptorState.WAIT_SECOND_CLIENT_KEY;
				}

				break;
			}
			case WAIT_SECOND_SERCER_KEY:
			{
				if(length == 128)
				{
					temps[3] = Arrays.copyOf(data, 128);

					byte[] firstTemp = new byte[128];
					byte[] secondTemp = new byte[128];
					byte[] cryptKey = new byte[128];

					Crypt.shiftKey(temps[1], firstTemp, 31, true);
					Crypt.xorKey(firstTemp, temps[0], secondTemp);
					Crypt.shiftKey(temps[2], firstTemp, 17, false);
					Crypt.xorKey(firstTemp, secondTemp, cryptKey);

					decrypt.generateKey(cryptKey);

					Crypt.shiftKey(temps[3], firstTemp, 79, true);

					decrypt.applyCryptor(firstTemp, 128);

					encrypt.generateKey(firstTemp);

					Arrays.clear(temps);

					state = CryptorState.READY_TO_WORK;
				}
			}
		}
	}

	/**
	 * @return статус криптора.
	 */
	public CryptorState getState()
	{
		return state;
	}

	/**
	 * @param state статус криптора.
	 */
	public void setState(CryptorState state)
	{
		this.state = state;
	}

	@Override
	public String toString()
	{
		return "TeraCrypt decrypt = " + decrypt + ", encrypt = " + encrypt;
	}
}