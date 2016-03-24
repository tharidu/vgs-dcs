package dcs.group8.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import dcs.group8.messaging.ClientRemoteMessaging;
import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.JobCompletionMessage;
import dcs.group8.messaging.JobMessage;

/**
 * 
 * @author greg
 * An example Client trying to connect to a random
 * gridscheduler to submit a job to the DCS
 */

public class RunClient implements ClientRemoteMessaging{
	
	private HashMap<String,String> gsAddressesMap;
	private InputStream inputstream;
	private Properties properties;
	private String propFile;
	private UUID myUUID;
	
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
	public RunClient(String f){
		this.propFile = f;
		this.gsAddressesMap = new HashMap<String, String>();
		this.myUUID = UUID.randomUUID();
	}
	
	private void getGsAddresses(){
		try{
			properties = new Properties();
			inputstream = new FileInputStream(propFile);
			if (inputstream!=null){
				properties.load(inputstream);
			}
			else{
				throw new FileNotFoundException("The property file :"+propFile+" could not be found or does not exist");
			}
			
			Set<String> keyset = properties.stringPropertyNames();
			for (String key : keyset){
				String value = properties.getProperty(key);
				gsAddressesMap.put(key, value);
			}
			inputstream.close();
		}
		
		catch(Exception e){
			System.out.println("Exception occured: " + e.toString());
			e.printStackTrace();
		}
	}
	
	private String getRandomGsAddress(){
		Random random = new Random();
		List<String> keys = new ArrayList<String>(gsAddressesMap.keySet());
		String randomGS = keys.get(random.nextInt(keys.size()));
		return gsAddressesMap.get(randomGS);
		
	}
	
	public static void main(String[] args){
		RunClient cl = new RunClient("resources/gridschedulers.properties");
		cl.getGsAddresses();
		cl.setUpRegistry();
		String randomGs = cl.getRandomGsAddress();
		//System.out.println(randomGs);
		//create a new JobMessage and pass it to the gridscheduler
		JobMessage jb = new JobMessage();
		jb.setClient_id(cl.myUUID);
		jb.setJob_duration(20);
		jb.setJob_id(UUID.randomUUID());
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
