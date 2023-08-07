
import java.io.*;
import java.net.*;
import java.security.Key;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.*;
import javax.mail.internet.*;

class BankServer extends Thread {
	
	 ServerSocket serversocket=null;
	 Socket socket=null;
	 BufferedReader breader=null;
	 BufferedWriter bwriter=null;
	 Connection con=null;
	 Statement statement=null;
	 Scanner sc=null;
	 HashMap<Integer,String> logininfo=new HashMap<Integer,String>();
	 HashMap<Integer,String> nameinfo=new HashMap<Integer,String>();
	 HashMap<Integer,Float> balanceinfo=new HashMap<Integer,Float>();
	 HashMap<Integer,String> emailinfo=new HashMap<Integer,String>();
	 HashMap<Integer,String> ltrtinfo=new HashMap<Integer,String>();
	 HashMap<Integer,String> ltramtinfo=new HashMap<Integer,String>();
	 HashMap<Integer,String> dateinfo=new HashMap<Integer,String>();
	 Session newSession=null;
	 MimeMessage mimeMessage = null;
	 SimpleDateFormat day=new SimpleDateFormat("dd/MM/yyyy");
	 SimpleDateFormat time=new SimpleDateFormat("HH:mm");
	 Date date=new Date();
	 String skey="AHGDU284992HFSHD";
	 Key key=new SecretKeySpec(skey.getBytes(),"AES");
	 
	 public BankServer(Socket s,BufferedReader br,BufferedWriter bw)
	 {
		 this.socket=s; 
		 this.breader=br;
		 this.bwriter=bw;
	 }
	
	@Override
	public void run() 
	{
		
		int amt ;
    	int uaccno;
    	String upin="",upin2;
    	float newbalance;
    	String mode,s1;
    	managedb();
    	uaccno=Integer.parseInt(readfromclient());
    	System.out.print("\nAccount number:"+uaccno);
    	upin2=readfromclient(); 	
    	try {
			upin=encrypt(upin2,key);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	if(logininfo.containsKey(uaccno))
		{
    		if(logininfo.get(uaccno).equalsIgnoreCase(upin))
    		{
    		sendtoclient("valid");
    		String name=nameinfo.get(uaccno);
			sendtoclient(name);
    		while(true) {
			mode=readfromclient();
			setupServerProperties();
			if (mode.equalsIgnoreCase(Character.toString('w')))
			{
				amt=Integer.parseInt(readfromclient());
				Float balance=balanceinfo.get(uaccno);
				if(balance-amt>0)
				{
					newbalance=balance-amt;
					s1="UPDATE useraccounts SET Balance="+newbalance+" WHERE AccountNumber="+uaccno;
					String s2="UPDATE useraccounts SET ltransactiondate='"+day.format(date)+"',ltransactiontype='w',ltransactionamt='"+Integer.toString(amt)+"' WHERE AccountNumber="+uaccno;	
					try
					{
					statement=con.createStatement();
					statement.executeUpdate(s1);
					statement.executeUpdate(s2);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					sendtoclient(Float.toString(newbalance));
					draftEmail(uaccno,amt,mode,emailinfo.get(uaccno));
					try {
						sendEmail();
					} catch (MessagingException e) {
						e.printStackTrace();
					}
					break;
				}
				else
				{
						sendtoclient("Invalid Balance");
				}
			}
			else if(mode.equalsIgnoreCase(Character.toString('d')))
			{
				amt=Integer.parseInt(readfromclient());
				Float balance=balanceinfo.get(uaccno);
				newbalance=balance+amt;
				s1="UPDATE useraccounts SET Balance="+newbalance+" WHERE AccountNumber="+uaccno;
				String s2="UPDATE useraccounts SET ltransactiondate='"+day.format(date)+"',ltransactiontype='d',ltransactionamt='"+Integer.toString(amt)+"' WHERE AccountNumber="+uaccno;
				try
				{
				statement=con.createStatement();
				statement.executeUpdate(s1);
				statement.executeUpdate(s2);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				sendtoclient(Float.toString(newbalance));
				draftEmail(uaccno,amt,mode,emailinfo.get(uaccno));
				try {
					sendEmail();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				break;

			}
			else if(mode.equalsIgnoreCase(Character.toString('b')))
			{
			
				Float balance=balanceinfo.get(uaccno);
				sendtoclient(Float.toString(balance));
			}
			else if(mode.equalsIgnoreCase(Character.toString('t')))
			{
				String tr=ltrtinfo.get(uaccno);
				String tramt=ltramtinfo.get(uaccno);
				String date2=dateinfo.get(uaccno);
			    sendtoclient(tr);
			    sendtoclient(tramt);
			    sendtoclient(date2);
			}
    		}
    		}
			else
			{
				System.out.print("\nInvalid Account Number or Pin....");
				sendtoclient("Invalid");
			}
		}
		else
		{
			System.out.print("\nInvalid Account Number or Pin....!");
			sendtoclient("Invalid");
		}
	}
	
	private void sendEmail() throws MessagingException {
		String fromUser = "sakthiseetha2002@gmail.com";  		
		String fromUserPassword = "bgypzxhdytudrlmo";  		
		String emailHost = "smtp.gmail.com";
		Transport transport = newSession.getTransport("smtp");
		transport.connect(emailHost, fromUser, fromUserPassword);
		transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
		transport.close();
		System.out.println("\nEmail has been successfully sent!!!");
	}
	
	private MimeMessage draftEmail(int accno,int amt,String mode,String emailid)  {
		String emailReceipients =emailid; 				 
		String emailSubject = "KYC Bank \n\nAmount Transaction";
		String emailBody="";
		String emailBody1,emailBody2;
		
		if(mode.equals("w"))
		{
			emailBody1 = "Your AccountNumber "+accno+" is withdrawed for Rs."+amt+" from Thanjavur branch on "+day.format(date)+" at "+time.format(date);
			emailBody2="\n\n\n\n\nThis is a system generated mail,do not reply to this mail.";
			emailBody=emailBody1+emailBody2;
		}
		else
		{
			emailBody1= "Your AccountNumber "+accno+" is deposited for Rs."+amt+" to Thanjavur branch on "+day.format(date)+" at "+time.format(date);	
			emailBody2="\n\n\n\n\nThis is a system generated mail,do not reply to this mail.";
			emailBody=emailBody1+emailBody2;
		}
		mimeMessage = new MimeMessage(newSession);
		try {
		mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailReceipients));
		mimeMessage.setSubject(emailSubject);
		mimeMessage.setText(emailBody);
		 return mimeMessage;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private void setupServerProperties() {
		Properties properties = System.getProperties();
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		newSession = Session.getDefaultInstance(properties,null);
	}

	
	
	 public void sendtoclient(String data)
	    {
	    	try {
	    		
		    	bwriter.write(data);
				bwriter.newLine();
				bwriter.flush();
			} catch (Exception e) {
				
				System.out.print("\nError in sending message......");
				e.printStackTrace();
				CloseEverything(socket,breader,bwriter);
			}
	    	
	    }
	 public String readfromclient()
	    {
	    	String recmsg;
	    	try {
				recmsg=breader.readLine();
				System.out.println(recmsg);
				return recmsg;
			} 
	    	catch (Exception e) {
				System.out.print("\nError in reading message...");
				e.printStackTrace();
				CloseEverything(socket,breader,bwriter);
				return null;
				
			}
	    }

	 public void managedb() 
	    {
	    	String jdbcurl="jdbc:sqlite://D:\\sqlite\\sqlite-tools-win32-x86-3390400 (1)\\sqlite-tools-win32-x86-3390400\\AccountDetails.db";
			try{
				con =DriverManager.getConnection(jdbcurl);		
				String sql="SELECT * from useraccounts";
				statement=con.createStatement();
				ResultSet result=statement.executeQuery(sql);
				while(result.next())
				{
					String pin=result.getString("Pin");
					int accountno=result.getInt("AccountNumber");
					String name=result.getString("name");
					Float balance=result.getFloat("Balance");
					String email=result.getString("emailid");
					String ltrt=result.getString("ltransactiontype");
					String ltramt=result.getString("ltransactionamt");
					String date1=result.getString("ltransactiondate");
					logininfo.put(accountno,pin);
					nameinfo.put(accountno,name);
					balanceinfo.put(accountno,balance);
					emailinfo.put(accountno,email);
					ltrtinfo.put(accountno,ltrt);
					ltramtinfo.put(accountno, ltramt);
					dateinfo.put(accountno, date1);
				}		
			}
			catch(Exception e) {
			System.out.print("\nError in fetching account number and PIN....");
			e.printStackTrace();
			}
	    }
	 public void CloseEverything(Socket s,BufferedReader br,BufferedWriter bw)
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
	 
	 private String encrypt(String ct,Key key) throws Exception 
		{
		Cipher c=Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE,key);
		byte[] encval=c.doFinal(ct.getBytes());
		String encstring=Base64.getEncoder().encodeToString(encval);
		return encstring;
		}
	 
	 

}

public class Bank_server
{
	public static  void main(String args[]) throws IOException
	{
		Socket socket;
		ServerSocket ss=null;
		BufferedReader breader=null;
		BufferedWriter bwriter=null;
		ss=new ServerSocket(2754);
		System.out.print("\nWaiting for client.....");
		while(true)
		{
			try {
				socket=ss.accept();
				System.out.print("\nConnection has been  Established");
				System.out.print("\nConnected to "+socket.getInetAddress().getHostName());	
				breader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bwriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				BankServer b1=new BankServer(socket,breader,bwriter);
				b1.start();	
			}
			catch(IOException e)
			{
				System.out.print("\nError in creating thread....");
				e.printStackTrace();	
				
			}
		}
	}
}

