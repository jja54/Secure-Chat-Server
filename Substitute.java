import java.util.*;
import java.security.*;

public class Substitute implements SymCipher
{
	byte[] key;

	public Substitute()
	{
		key = new byte[256];
		SecureRandom random = new SecureRandom();
		for (int i = 0; i < key.length; i++)
		{
			key[i] = (byte)i;
		}
		//now the values need to be shuffled, and create inverse mapping array
		for (int i = 0; i < key.length; i++)
		{
			int rand = random.nextInt(key.length);
			byte temp = key[i]; //temporarily save the byte at i so it can be overwritten
			key[i] = key[rand]; //swap indicies i and rand
			key[rand] = temp;
		}
	}
	
	public Substitute(byte[] key)
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
			messageEnc[i] = key[(int)(message[i] & 0xff)]; //swap indicies from the message to the key's value
			//byte is &'d with 0xFF so that it is rendered as unsigned
			//there are no primitive unsigned bytes in java, so it is cast as an int as well
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
		byte[] newKey = new byte[key.length];
		//first inverse the key 
		for (int i = 0; i < newKey.length; i++)
		{
			newKey[(int)(key[i] & 0xff)] = (byte)i;
		}
		//then decode message with inverse key
		for (int i = 0; i < message.length; i++)
		{
			message[i] = newKey[(int)(bytes[i] & 0xff)];
			//byte is &'d with 0xFF so that it is rendered as unsigned
			//there are no primitive unsigned bytes in java, so it is cast as an int as well
		}
		//System.out.println(message); //TEST TO SEE IF ITS WORKING
		return new String(message);
	}
}