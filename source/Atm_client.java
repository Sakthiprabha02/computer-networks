import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.net.*;
import java.security.Key;
import java.util.Base64;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class Atm_client implements ActionListener
{
	private static Socket socket=null;
	private static BufferedReader breader=null;
	private static BufferedWriter bwriter=null;
	static String name,balance;
	static String accno;
	
	JFrame frame=new JFrame();
	JButton cd=new JButton("Cash deposit");
	JButton cw=new JButton("Cash withdrawal");
	JButton cb=new JButton("View Balance");
	JButton tr=new JButton("Last Transaction");
	JLabel welcomelabel=new JLabel();
	static JLabel welcomelabel1=new JLabel();
	static JLabel welcomelabel2=new JLabel();
	JLabel balancelabel=new JLabel();
	JLabel balancelabel2=new JLabel();
	JLabel balancelabel3=new JLabel();
	JLabel messagelabel=new JLabel();
	JTextField amt=new JTextField();
	JLabel amtlabel=new JLabel();
	static String skey="AHGDU284992HFSHD";
	static Key key=new SecretKeySpec(skey.getBytes(),"AES");
	
		
	public static void getConnection()
	{
		try
		{
			socket=new Socket("localhost",2754);
			System.out.print("\nConnected to "+socket.getInetAddress().getHostName());
			breader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bwriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}
		catch(Exception e)
		{
			System.out.print("\nError in Client.....");
			e.printStackTrace();
			closeEverything(socket,breader,bwriter);
		}
	}
	
	
	public Atm_client(String userid,String name)
	{
		cd.setBounds(325,350,150,25);
		cw.setBounds(125,350,150,25);
		cb.setBounds(125,400,150,25);
		tr.setBounds(325,400,150,25);
		balancelabel.setBounds(75,200,400,25);
		balancelabel.setFont(new Font(null,Font.BOLD,20));
		balancelabel2.setBounds(75,225,300,25);
		balancelabel2.setFont(new Font(null,Font.BOLD,18));
		balancelabel3.setBounds(75,250,300,25);
		balancelabel3.setFont(new Font(null,Font.BOLD,18));
		messagelabel.setBounds(150,500,300,35);
		messagelabel.setFont(new Font(null,Font.BOLD,25));
		
		amtlabel.setText("Amount:");
		amt.setBounds(155,150,150,25);
		amtlabel.setBounds(75,150,50,25);
		welcomelabel.setBounds(75,50,400,25);
		welcomelabel1.setBounds(75,90,300,25);
		welcomelabel2.setBounds(75,115,300,25);
		welcomelabel.setText("Welcome to our KYC ATM");
		welcomelabel.setFont(new Font(null,Font.BOLD,30));
		welcomelabel1.setFont(new Font(null,Font.BOLD,15));
		welcomelabel2.setFont(new Font(null,Font.BOLD,15));
		cd.setFocusable(false);
		cw.setFocusable(false);
		cb.setFocusable(false);
		tr.setFocusable(false);
		
		frame.add(amt);
		frame.add(amtlabel);
		frame.add(cd);
		frame.add(cw);
		frame.add(cb);
		frame.add(tr);
		frame.add(welcomelabel);
		frame.add(welcomelabel1);
		frame.add(balancelabel);
		frame.add(balancelabel2);
		frame.add(balancelabel3);
		frame.add(messagelabel);
		frame.setTitle("Transaction Window");
		frame.add(welcomelabel2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(650,650);
		frame.setLayout(null);
		frame.setVisible(true);
		frame.setResizable(false);
		cd.addActionListener(this);
		cw.addActionListener(this);
		tr.addActionListener(this);
		cb.addActionListener(this);
		welcomelabel1.setText("Name:"+name);
		welcomelabel2.setText("Acccount Number:"+userid);
		
	}
	public Atm_client()
	{
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==cd)
		{
			sendtoserver(Character.toString('d'));
			String amount = amt.getText();
			sendtoserver(amount);
			balance=receivefromserver();
			balancelabel.setText("Balance:"+balance);
			messagelabel.setText("Transaction Successful!");
			JOptionPane.showMessageDialog(frame,"Cash deposited sucessfully");
			frame.dispose();
			new endpage();
		}
		if(e.getSource()==cw)
		{
			sendtoserver(Character.toString('w'));
			String amount = amt.getText();
			sendtoserver(amount);
			balance=receivefromserver();
			if(balance.equals("Invalid Balance"))
			{
				messagelabel.setText("Transaction Failed!");
				JOptionPane.showMessageDialog(frame,"Cash not dispersed","Alert",JOptionPane.WARNING_MESSAGE);
				
			}
			else
			{
			balancelabel.setText("Balance:"+balance);
			messagelabel.setText("Transaction Successful!");
			JOptionPane.showMessageDialog(frame,"Cash withdrawed sucessfully");
			frame.dispose();
			new endpage();
			}
		}
		if(e.getSource()==cb)
		{
			sendtoserver(Character.toString('b'));
			balance=receivefromserver();
			balancelabel.setText("Balance:"+balance);
			
		}
		if(e.getSource()==tr)
		{
			sendtoserver(Character.toString('t'));
			String trt=receivefromserver();
			String tramt=receivefromserver();
			String date=receivefromserver();
			if(trt.equals(Character.toString('w')))
			{
			balancelabel.setText("Last Transaction type:Withdrawl");
			balancelabel2.setText("Last Transaction Amount:"+tramt);
			balancelabel3.setText("Last Transaction Date:"+date);
			}
			else 
			{
				balancelabel.setText("Last Transaction type:Deposit");
				balancelabel2.setText("Last Transaction Amount:"+tramt);
				balancelabel3.setText("Last Transaction Date:"+date);
			}
			}
				
	}
	public static String getdata(String uaccno,String pin) throws Exception
	{
		
		sendtoserver(uaccno);
		sendtoserver(pin);
		String p=receivefromserver();
		if(p.equals("valid"))
		{
			return "valid";	
		}
		else
		{			
			closeEverything(socket,breader,bwriter);	
			return "0";
		}
	} 

	private static void sendtoserver(String data)
	{
		try {
			data=(data);
			System.out.println("......"+data);
			bwriter.write(data);
			bwriter.newLine();
			bwriter.flush();
		} catch (Exception e) {
			
			System.out.print("\nError in sending message......");
			e.printStackTrace();
			closeEverything(socket,breader,bwriter);
		}
	}
	
	public static String receivefromserver()
	{
		String recmsg;
    	try {
			recmsg=breader.readLine();
			System.out.println("????"+recmsg);
			recmsg=(recmsg);
			
			return recmsg;
		} catch (Exception e) {
			System.out.print("\nError in reading message...");
			e.printStackTrace();
			closeEverything(socket,breader,bwriter);
			return null;
		}
	}
	
	private static void closeEverything(Socket s,BufferedReader br,BufferedWriter bw)
	{
		try {
		if(s!=null)
			s.close();
		if(br!=null)
			br.close();
		if(bw!=null)
			bw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static String decrypt(String pt,Key key) throws Exception 
	{
	Cipher c=Cipher.getInstance("AES");
	c.init(Cipher.DECRYPT_MODE,key);
	byte[] dec=Base64.getDecoder().decode(pt);
	byte[] decval=c.doFinal(dec);
	String decstring =new String(decval);
	return decstring;
	}
	
	public static String encrypt(String ct,Key key) throws Exception 
	{
	Cipher c=Cipher.getInstance("AES");
	c.init(Cipher.ENCRYPT_MODE,key);
	byte[] encval=c.doFinal(ct.getBytes());
	String encstring=Base64.getEncoder().encodeToString(encval);
	return encstring;
	}
}

class endpage
{
	JFrame frame1=new JFrame();
	JLabel label=new JLabel();
	JLabel label2=new JLabel();
	JLabel label3=new JLabel();
	
	public endpage()
	{
		label.setText("Thank you for using KYC ATM services");
		label.setFont(new Font(null,Font.BOLD,27));
		label.setBounds(75,50,500,35);
		label2.setBounds(75,500,500,35);
		label2.setFont(new Font(null,Font.BOLD,18));
		label2.setText("For more details contact Queries@KYCbank.com");
		label3.setBounds(150,550,300,35);
		label3.setFont(new Font(null,Font.BOLD,18));
		label3.setText("CONTACT NUMBER:9651123457");
		frame1.setTitle("KYC Bank");
		frame1.add(label);
		frame1.add(label2);
		frame1.add(label3);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame1.setSize(650,650);
		frame1.setLayout(null);
		frame1.setVisible(true);
		frame1.setResizable(false);
		
	}
}
