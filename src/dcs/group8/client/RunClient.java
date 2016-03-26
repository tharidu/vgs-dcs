package dcs.group8.client;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dcs.group8.messaging.ClientRemoteMessaging;
import dcs.group8.messaging.GridSchedulerRemoteMessaging;
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
	
	public static Logger logger;
	
	private HashMap<String,String> gsAddressesMap;
	private InputStream inputstream;
	private static Properties properties;
	private String propFile;
	private UUID myUUID;
	private static String myIpAddress;
	
	public void gsToClientMessage(JobMessage jcm){
		System.out.println("My job with id: "+jcm.job.getJobId().toString()
				+" was succesfully completed by Cluster: "+jcm.job.getClientId().toString());
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
		logger.info("Initializing client's data structures");
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
		//set in the system properties the file where the client should log info
		System.setProperty("logfileclient", "client@"+myIpAddress);
		logger = LogManager.getLogger(RunClient.class);
		logger.info("Creatind a new client");
		RunClient cl = new RunClient();
		
		try{
		myIpAddress = InetAddress.getLocalHost().getHostAddress();
		System.out.println(myIpAddress);
		}
		catch(UnknownHostException ue){
			logger.error("Could not retrieve my ip address "+ue.toString());
			ue.printStackTrace();
		}
		try{
			properties = PropertiesUtil.getProperties("dcs.group8.client.RunClient","gridschedulers.properties");
		}
		catch (Exception e){
			logger.error("Could not read the properties file for the gridscheduler addresses");
		}
		String[] gsarr = properties.getProperty("gsaddr").split(";");
		String gsaddr = "172.20.92.32";
		logger.info("Creating jobs to submit to the Distributed System");
		cl.setUpRegistry();
		Job job = new Job(UUID.randomUUID(), 10000, cl.myUUID,myIpAddress);
		Job job1 = new Job(UUID.randomUUID(), 10000, cl.myUUID, myIpAddress);
		JobMessage jb = new JobMessage(job);
		JobMessage jb1 = new JobMessage(job1);
		//System.out.println("Address of gs1 :"+cl.gsAddressesMap.get("gs1"));
		
		try{
			Registry registry = LocateRegistry.getRegistry(gsaddr);
			GridSchedulerRemoteMessaging clgs_stub = (GridSchedulerRemoteMessaging) registry.lookup("GridSchedulerRemoteMessaging");
			logger.info("[+] Submitting"+jb.toString()+" to gs@"+gsaddr);
			String ack = clgs_stub.clientToGsMessage(jb);
			logger.info("[+]Response from gs@"+gsaddr+":"+ack);
			
			logger.info("[+] Submitting"+jb.toString()+" to gs@"+gsaddr);
			String ack1 =clgs_stub.clientToGsMessage(jb1);
			logger.info("[+]Response from gs@"+gsaddr+":"+ack1);
			/*try{
				Thread.sleep(2000);
				String ack1 =clgs_stub.clientToGsMessage(jb1);
			}
			catch(Exception e){
				e.printStackTrace();
			}*/
		}
		catch (Exception e ){
			logger.error("Rmi exception occured could not submit jobs to gs@"+gsaddr);
			e.printStackTrace();
		}
		
	}
}
