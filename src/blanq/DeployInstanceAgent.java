package blanq;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;

public class DeployInstanceAgent implements Runnable {	

	private Integer counter;
	private String elbName;
	private String appName;
	private String appVersion;
	private String instanceId;
	private AmazonEC2Client ec2;
	private AmazonElasticLoadBalancingClient elb;
	
	private static final Log log = LogFactory.getLog(DeployInstanceAgent.class);
	
	public DeployInstanceAgent(Integer counter, String elbName, String appName, String appVersion, String instanceId, AmazonEC2Client ec2, AmazonElasticLoadBalancingClient elb) {

		this.ec2        = ec2;
		this.elb        = elb;
		this.counter    = counter;
		this.elbName    = elbName;
		this.appName    = appName;
		this.appVersion = appVersion;
		this.instanceId = instanceId;
	}
	
	@Override
	public void run() {
	
		if (instanceAlreadyRunning(ec2, instanceId)) {
			
			List<Tag> tags = new ArrayList<Tag>();
			tags.add(new Tag("Name",    appName+"-"+appVersion+"-"+counter));
			tags.add(new Tag("app",     appName));
			tags.add(new Tag("version", "v"+appVersion));			
			tagInstance(ec2, instanceId, tags);
		}
		
		attachInstanceToElb(elb, elbName, instanceId);
		
	}
	
	private static void attachInstanceToElb(AmazonElasticLoadBalancingClient elb, String elbName, String instanceId) {
		
		log.info("Attaching instance "+instanceId+" in ELB "+elbName);

		List<com.amazonaws.services.elasticloadbalancing.model.Instance> instanceIdsToAttach = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
		instanceIdsToAttach.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(instanceId));
		
		DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest();
		describeLoadBalancersRequest.withLoadBalancerNames(elbName);
		
		RegisterInstancesWithLoadBalancerRequest registerInstancesWithLoadBalancerRequest = new RegisterInstancesWithLoadBalancerRequest(elbName, instanceIdsToAttach);
		elb.registerInstancesWithLoadBalancer(registerInstancesWithLoadBalancerRequest);
		
	}

	private static void tagInstance(AmazonEC2Client ec2, String instanceId, List<Tag> tags) {
		
		log.info("Tagging instance "+instanceId+" with tags: "+tags.toString());
		
		List<String> instanceIdsToTag = new ArrayList<String>();
		instanceIdsToTag.add(instanceId);

		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.setResources(instanceIdsToTag);
		createTagsRequest.setTags(tags);
		
		ec2.createTags(createTagsRequest);
	}

	private static boolean instanceAlreadyRunning(AmazonEC2Client ec2, String instanceId) {

		List<String> instanceIdsToCheck = new ArrayList<String>();
		instanceIdsToCheck.add(instanceId);
		
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		describeInstancesRequest.setInstanceIds(instanceIdsToCheck);

		while (true) {
			
			log.info("Checking if instance "+instanceId+" is running...");
			
			DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);
			List<Reservation> reservations = describeInstancesResult.getReservations();
			for (Reservation reservation : reservations) {
			
				List<Instance> instances = reservation.getInstances();
				for (Instance instance : instances) {
				
					//encontrou a instancia solicitada
					if (instance.getInstanceId() == instanceId) {
					
						//certifica que est‡ rodando
						if (instance.getState().getCode() == 16) {
							System.out.println("Instance "+instanceId+" is running.");
							break;
						}
					}
				}
			}
		
			//aguarda 2 segundos para voltar a checar novamente
			Util.sleepFor(2);
			
			return true;
		}
	}

	public String getInstanceId() {
		return instanceId;
	}
}
