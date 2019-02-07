import java.util.*;
import java.security.*;

public class Add128 implements SymCipher
{
	byte[] key;
	
	/**
       * CONSTRUCTORS
       */
	public Add128()
	{
		key = new byte[128];
		SecureRandom random = new SecureRandom();
		random.nextBytes(key);
	}
	
	public Add128(byte[] key)
	{
		this.key = key;
	}
	
	public byte[] getKey()
	{
		return key;
	}
	
	/**
       * Encodes the string using the key and returns the result as an array of
	   * bytes. String input is converted to an array of bytes prior to encrypting it.  
	   * Note that String S could have an arbitrary length, so the cipher
	   * will "wrap" when encrypting if necessary.
       */
	public byte[] encode(String S)
	{
		byte[] message = S.getBytes();
		byte[] messageEnc = new byte[message.length];
		for (int i = 0; i < message.length; i++)
		{
			//add the key's byte value at i to the message's value at i
			//message length remains the same, key value at i is modulo length
			//so that it will continue to loop around the 128bit key
			messageEnc[i] = (byte)(message[i] + key[i % key.length]);
		}
		return messageEnc;
	}
	
	/**
       * Decrypts an array of bytes and generates and returns the corresponding String.
	   * Inverses the addition operation with the key performed by the encode method.
       */
	public String decode(byte [] bytes)
	{
		byte[] message = new byte[bytes.length]; //decoded message
		for (int i = 0; i < bytes.length; i++)
		{
			message[i] = (byte)(bytes[i] - key[i % key.length]);
		}
		return new String(message);
	}
}