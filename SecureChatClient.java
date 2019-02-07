import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.math.BigInteger;

public class SecureChatClient extends JFrame implements Runnable, ActionListener {

    public static final int PORT = 8765;

    //BufferedReader myReader;
    //PrintWriter myWriter;
	ObjectOutputStream myWriter;
	ObjectInputStream myReader;
    JTextArea outputArea;
    JLabel prompt;
    JTextField inputField;
    String myName, serverName;
	Socket connection;
	
	SymCipher cipher;

    public SecureChatClient ()
    {
        try {
		//get server name
		myName = JOptionPane.showInputDialog(this, "Enter your user name: ");
        serverName = JOptionPane.showInputDialog(this, "Enter the server name: ");
		
        InetAddress addr =
                InetAddress.getByName(serverName);
        connection = new Socket(addr, PORT);   // Connect to server with new
                                               // Socket
											   
		myWriter = new ObjectOutputStream(connection.getOutputStream());
		myWriter.flush();
		
		myReader = new ObjectInputStream(connection.getInputStream());
		
		BigInteger E = (BigInteger)myReader.readObject(); //read server's public key
		System.out.println("E: "+E.toString());
		BigInteger N = (BigInteger)myReader.readObject(); //read server's public mod value
		System.out.println("N: "+N.toString());
		String cipherType = (String)myReader.readObject(); //read server's preferred symmetric cipher
		System.out.println("Symmetric encryption type: "+cipherType);
		
		if (cipherType.equals("Add"))
			cipher = new Add128();
		if (cipherType.equals("Sub"))
			cipher = new Substitute();
		
		BigInteger key = new BigInteger(1, cipher.getKey());
		//now encrypt the key via RSA and send it to the server:
		BigInteger keyEnc = key.modPow(E, N);
		System.out.println("Encryped symmetric key: "+keyEnc.toString());
		myWriter.writeObject(keyEnc);
		
		//encrypt username:
		byte[] encodedName = cipher.encode(myName);
        myWriter.writeObject(encodedName);   // Send name to Server.  Server will need
                                    // this to announce sign-on and sign-off
                                    // of clients
		myWriter.flush();

        this.setTitle(myName);      // Set title to identify chatter

        Box b = Box.createHorizontalBox();  // Set up graphical environment for
        outputArea = new JTextArea(8, 30);  // user
        outputArea.setEditable(false);
        b.add(new JScrollPane(outputArea));

        outputArea.append("Welcome to the Groupchat, " + myName + "\n");

        inputField = new JTextField("");  // This is where user will type input
        inputField.addActionListener(this);

        prompt = new JLabel("Type your messages below:");
        Container c = getContentPane();

        c.add(b, BorderLayout.NORTH);
        c.add(prompt, BorderLayout.CENTER);
        c.add(inputField, BorderLayout.SOUTH);

        Thread outputThread = new Thread(this);  // Thread is to receive strings
        outputThread.start();                    // from Server

		addWindowListener(
                new WindowAdapter()
                {
                    public void windowClosing(WindowEvent e)
                    {
						try
						{
							myWriter.writeObject(cipher.encode("CLIENT CLOSING"));
							myWriter.flush();
							System.exit(0);
						}
						catch (Exception ex)
						{
							System.out.println(ex + ", issue closing client");
						}
                    }
                }
            );

        setSize(500, 200);
        setVisible(true);

        }
        catch (Exception e)
        {
            System.out.println("Problem starting client!");
        }
    }

    public void run()
    {
        while (true)
        {
             try {
                byte[] initMsg = (byte[])myReader.readObject();
				String decodedMsg = cipher.decode(initMsg);
			    outputArea.append(decodedMsg+"\n");
				System.out.println("Received array of bytes: "+initMsg);
				System.out.println("Decoded array of bytes: "+decodedMsg.getBytes());
				System.out.println("Decoded message: "+decodedMsg);
             }
			 catch (EOFException ex)
			 {
				System.out.println("Session successfully terminated by user.");
				break;
			 }
             catch (Exception e)
             {
                System.out.println(e +  ", closing client!");
				e.printStackTrace();
                break;
             }
        }
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e)
    {
        String currMsg = e.getActionCommand();      // Get input value
        inputField.setText("");
		currMsg = (myName + ": " + currMsg);
		byte[] encodedMsg = cipher.encode(currMsg);
		System.out.println("Original message: "+currMsg);
		System.out.println("Message array of bytes: "+currMsg.getBytes());
		System.out.println("Encrypted array of bytes: "+encodedMsg);
		try
		{
			myWriter.writeObject(encodedMsg); // Add name and send it to Server
			myWriter.flush();
		}
		catch (Exception ex)
		{
			System.out.println(ex + ", issue sending message to Server!");
		}
    }

    public static void main(String [] args)
    {
         SecureChatClient JR = new SecureChatClient();
         JR.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
}