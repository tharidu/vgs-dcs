package dcs.group8.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
import org.uncommons.maths.random.ExponentialGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import dcs.group8.messaging.ClientRemoteMessaging;
import dcs.group8.messaging.GridSchedulerRemoteMessaging;
import dcs.group8.messaging.JobMessage;
import dcs.group8.models.Job;
import dcs.group8.models.JobFactory;
import dcs.group8.utils.PropertiesUtil;

/**
 * 
 * RunClient is the process representing the client that picks at random a GS
 * from the distributed system and assigns jobs to it
 * 
 */

public class RunClient implements ClientRemoteMessaging {

	public static Logger logger;
	public static int numberOfJobs;
	private static Properties properties;
	private static String myIpAddress;
	private static int index = 0;

	private HashMap<String, String> gsAddressesMap;
	private InputStream inputstream;
	private String propFile;
	private UUID myUUID;

	/**
	 * This method is called from a GS to inform a client about the completion
	 * of a job that he submitted
	 * 
	 * @param jcm
	 *            JobMessage containing the job
	 * 
	 */
	public void gsToClientMessage(JobMessage jcm) {
		logger.info("My job with id: " + jcm.job.getJobNo() + " was succesfully completed by cluster@: "
				+ jcm.job.getClusterId().toString());
	}

	/**
	 * 
	 * Constructor method of the client where it initializes the GS addresses
	 * HashMap and assigns an id to himself
	 * 
	 */
	public RunClient() {
		logger.info("Initializing client's data structures");
		this.gsAddressesMap = new HashMap<String, String>();
		this.myUUID = UUID.randomUUID();
	}

	/**
	 * 
	 * @param gsaddr
	 *            An array of string url addresses of the GS
	 * @return String a round robin selected address of a GS in the DCS
	 */
	private String getRoundRobinGs(String[] gsaddr) {
		if (index >= gsaddr.length) {
			index = 0;

		}
		return gsaddr[index++];
	}

	private String getRandomGsAddress() {
		Random random = new Random();
		List<String> keys = new ArrayList<String>(gsAddressesMap.keySet());
		String randomGS = keys.get(random.nextInt(keys.size()));
		return gsAddressesMap.get(randomGS);

	}

	public static void gwa(RunClient cl, String[] gsarr, String fileName) throws Exception {
		ArrayList<GwaStruct> jobs = new ArrayList<GwaStruct>();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				String[] params = line.split(";");
				jobs.add(new GwaStruct(Integer.valueOf(params[0]), Integer.valueOf(params[1])));
			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String gsaddr = "";
		JobFactory jobFactory = new JobFactory(cl.myUUID, 22000, 1000, myIpAddress, 1);
		int previousSubmitTime = 0;
		for (int i = 0; i < jobs.size(); i++) {
			GwaStruct gwaStruct = jobs.get(i);
			Thread.sleep((gwaStruct.submitTime - previousSubmitTime) * 1000);
			gsaddr = cl.getRoundRobinGs(gsarr);
			Registry registry = LocateRegistry.getRegistry(gsaddr);
			GridSchedulerRemoteMessaging clgs_stub = (GridSchedulerRemoteMessaging) registry
					.lookup("GridSchedulerRemoteMessaging");
			Job job = jobFactory.createJob(gwaStruct.runTime);
			logger.info("[+] Submitting " + job.toString() + " to gs@" + gsaddr);

			String ack = clgs_stub.clientToGsMessage(JobFactory.createMessage(job));

			logger.info("[+]Response from gs@" + gsaddr + ":" + ack);
			previousSubmitTime = gwaStruct.submitTime;
		}
	}

	/**
	 * 
	 * Main method of the client jobs are created and submitted to the DCS
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.println("The number of jobs to be submitted by the client must be provided as an argument");
			System.exit(-1);
		}

		try {
			myIpAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ue) {
			ue.printStackTrace();
		}

		System.setProperty("logfileclient", "client@" + myIpAddress);
		logger = LogManager.getLogger(RunClient.class);
		logger.info("Creating a new client");

		RunClient cl = new RunClient();

		try {
			properties = PropertiesUtil.getProperties("dcs.group8.client.RunClient", "gridschedulers.properties");
		} catch (Exception e) {
			logger.error("Could not read the properties file for the gridscheduler addresses");
		}
		String[] gsarr = properties.getProperty("gsaddr").split(";");

		logger.info("Creating jobs to submit to the Distributed System");
		cl.setUpRegistry();

		boolean gwa = false;
		try {
			numberOfJobs = Integer.parseInt(args[0]);
		} catch (Exception e) {
			gwa = true;
		}

		if (!gwa) {

			/***
			 * Create a number of jobs here and add them to a list to submit
			 * them in the DCS
			 ***/
			ArrayList<Job> jlist = new ArrayList<Job>();

			/***
			 * CHANGE CLIENT NUMBER (1) TO BE INSERTED AS ARGUMENT FOR THE
			 * CLIENT
			 ***/
			JobFactory jobFactory = new JobFactory(cl.myUUID, 22000, 1000, myIpAddress, 1);
			for (int i = 0; i < numberOfJobs; i++) {
				jlist.add(jobFactory.createJob());
			}
			String gsaddr = "";
			try {
				// One minute
				final long timePeriod = 60000;
				Random rng = new MersenneTwisterRNG();

				// Generate events at an average rate of 20 per minute.
				ExponentialGenerator gen = new ExponentialGenerator(20, rng);

				for (int i = 0; i < jlist.size(); i++) {
					Job job = jlist.get(i);
					if (i % 40 == 0) {
						Thread.sleep(20000);
					}

					gsaddr = cl.getRoundRobinGs(gsarr);
					Registry registry = LocateRegistry.getRegistry(gsaddr);
					GridSchedulerRemoteMessaging clgs_stub = (GridSchedulerRemoteMessaging) registry
							.lookup("GridSchedulerRemoteMessaging");

					logger.info("[+] Submitting " + job.toString() + " to gs@" + gsaddr);

					String ack = clgs_stub.clientToGsMessage(JobFactory.createMessage(job));

					logger.info("[+]Response from gs@" + gsaddr + ":" + ack);
				}
			}

			/*
			 * for (Job job : jlist){ // Thread.sleep(1000);
			 * 
			 * long interval = Math.round(gen.nextValue() * timePeriod);
			 * Thread.sleep(interval);
			 * 
			 * gsaddr = cl.getRoundRobinGs(gsarr); Registry registry =
			 * LocateRegistry.getRegistry(gsaddr); GridSchedulerRemoteMessaging
			 * clgs_stub = (GridSchedulerRemoteMessaging)
			 * registry.lookup("GridSchedulerRemoteMessaging");
			 * 
			 * logger.info("[+] Submitting "+job.toString()+" to gs@"+gsaddr);
			 * 
			 * String ack =
			 * clgs_stub.clientToGsMessage(JobFactory.createMessage(job));
			 * 
			 * logger.info("[+]Response from gs@"+gsaddr+":"+ack); } }
			 */
			catch (Exception e) {
				logger.error("Rmi exception occured could not submit jobs to gs@" + gsaddr);
				e.printStackTrace();
			}
		} else {
			try {
				// Grid workload archive
				gwa(cl, gsarr, args[0]);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void setUpRegistry() {

		try {
			ClientRemoteMessaging crm_stub = (ClientRemoteMessaging) UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.bind(ClientRemoteMessaging.registry, crm_stub);
			logger.info("Client registry is properly set up");
		} catch (Exception e) {
			logger.error("Client registry wasn't set up: " + e.toString());
			e.printStackTrace();
		}
	}
}

class GwaStruct {
	int submitTime;

	public GwaStruct(int submitTime, int runTime) {
		super();
		this.submitTime = submitTime;
		this.runTime = runTime;
	}

	int runTime;
}
