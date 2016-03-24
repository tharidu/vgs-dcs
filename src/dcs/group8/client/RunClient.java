package dcs.group8.client;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import dcs.group8.messaging.ClientRemoteMessaging;
import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.JobCompletionMessage;
import dcs.group8.messaging.JobMessage;
import dcs.group8.models.Job;
import dcs.group8.utils.PropertiesUtil;

/**
 * 
 * @author greg
 * An example Client trying to connect to a random
 * gridscheduler to submit a job to the DCS
 */

public class RunClient implements ClientRemoteMessaging{
	
	private HashMap<String,String> gsAddressesMap;
	private InputStream inputstream;
	private static Properties properties;
	private String propFile;
	private UUID myUUID;
	private static String myIpAddress;
	
	public String gsToClientMessage(JobCompletionMessage jcm){
		return "Thank you grid scheduler";
	}
	
	private void setUpRegistry(){
		
		try{
			ClientRemoteMessaging crm_stub = (ClientRemoteMessaging) UnicastRemoteObject.exportObject(this,0);
			Registry registry = LocateRegistry.getRegistry();
			registry.bind(ClientRemoteMessaging.registry, crm_stub);
			System.out.println("Client registry is properly set up");
		}
		catch(Exception e){
			System.err.println("Client registry wasn't set up: "+e.toString());
			e.printStackTrace();
		}
	}
	
	//create a client with the path to the properties file
	public RunClient(){
		this.gsAddressesMap = new HashMap<String, String>();
		this.myUUID = UUID.randomUUID();
	}

	private String getRandomGs(String[] gsaddr){
		String randomaddr = gsaddr[new Random().nextInt(gsaddr.length)];
		return randomaddr;
	}

	private String getRandomGsAddress(){
		Random random = new Random();
		List<String> keys = new ArrayList<String>(gsAddressesMap.keySet());
		String randomGS = keys.get(random.nextInt(keys.size()));
		return gsAddressesMap.get(randomGS);
		
	}
	
	public static void main(String[] args){
		//RunClient cl = new RunClient("resources/gridschedulers.properties");
		RunClient cl = new RunClient();
		try{
		myIpAddress = InetAddress.getLocalHost().getHostAddress();
		}
		catch(UnknownHostException ue){
			System.err.println("Unknown host: "+ue.toString());
		}
		try{
			properties = PropertiesUtil.getProperties("dcs.group8.client.RunClient","gridschedulers.properties");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		String[] gsarr = properties.getProperty("gsaddr").split(";");
		String gsaddr = cl.getRandomGs(gsarr);
		//set up clients registry
		
		
		cl.setUpRegistry();
		String randomGs = cl.getRandomGsAddress();

		//create a new JobMessage and pass it to the gridscheduler
		Job job = new Job(UUID.randomUUID(), 20, cl.myUUID,myIpAddress);
		JobMessage jb = new JobMessage(job);
		//System.out.println("Address of gs1 :"+cl.gsAddressesMap.get("gs1"));
		
		try{
			Registry registry = LocateRegistry.getRegistry(randomGs);
			GridSchedulerRemoteMessaging clgs_stub = (GridSchedulerRemoteMessaging) registry.lookup("GridSchedulerRemoteMessaging");
			String ack = clgs_stub.clientToGsMessage(jb);
			System.out.println("Response from the gridScheduler was: "+ack);
		}
		catch (Exception e ){
			System.err.println("Exception on the client: "+e.toString());
			e.printStackTrace();
		}
		
	}
}
