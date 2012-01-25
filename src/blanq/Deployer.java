package blanq;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

public class Deployer {

	private static final Log log = LogFactory.getLog(Deployer.class);

	private DeployParameters deployParameters;

	public Deployer(DeployParameters deployParameters) {

		super();

		this.deployParameters = deployParameters;

		log.info("AWS Deployer initialized [APP: "
				+ this.deployParameters.getAppName() + " / Version: "
				+ this.deployParameters.getAppVersion() + "]");
	}

	public void deploy() {

		log.info("Connecting to AWS Services [EC2+ELB]");

		//criando conectividade com AWS
		AmazonEC2Client ec2 = this.deployParameters.getAmazonEC2Client();
		AmazonElasticLoadBalancingClient elb = this.deployParameters
				.getAmazonElasticLoadBalancingClient();

		log.info("Retrieving instances that is currently serving "
				+ this.deployParameters.getAppName() + " app");

		//lista de instancias antigas que estavam no ELB
		List<String> oldInstanceIds = new ArrayList<String>();
		oldInstanceIds = getOldInstancesAttachedInELB(elb,
				this.deployParameters.getElbName());

		log.info("Instances serving app: "
				+ StringUtils.join(oldInstanceIds, ", "));

		//lista de agentes
		List<DeployInstanceAgent> deployAgents = new ArrayList<DeployInstanceAgent>();

		//Contador de instancias
		Integer maxInstances = Integer.parseInt(this.deployParameters.getQty());
		for (int counter = 1; counter <= maxInstances; counter++) {

			String instanceId = startInstance();

			log.info("Starting instance " + counter + ": " + instanceId);

			DeployInstanceAgent agent = new DeployInstanceAgent(counter,
					this.deployParameters.getElbName(),
					this.deployParameters.getAppName(),
					this.deployParameters.getAppVersion(), instanceId, ec2, elb);
			Thread threadAgent = new Thread(agent);
			threadAgent.start();

			deployAgents.add(agent);
		}

		//Aguarda 5 segundos para iniciar a verificacao
		Util.sleepFor(5);

		//remove instancias antigas do elb
		if (allInstancesAlreadyInService(elb,
				this.deployParameters.getElbName(), deployAgents)) {

			log.info("All new instances already serving newer application version, will remove old instances from ELB now.");
			terminateAndRemoveFromElbInstances(ec2, elb,
					this.deployParameters.getElbName(), oldInstanceIds);

		}
	}

	private void terminateAndRemoveFromElbInstances(AmazonEC2Client ec2,
			AmazonElasticLoadBalancingClient elb, String elbName,
			List<String> oldInstanceIds) {

		List<com.amazonaws.services.elasticloadbalancing.model.Instance> instancesToDeregister = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
		for (String instance : oldInstanceIds) {
			instancesToDeregister
					.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(
							instance));
		}

		DeregisterInstancesFromLoadBalancerRequest deregisterInstancesFromLoadBalancerRequest = new DeregisterInstancesFromLoadBalancerRequest();
		deregisterInstancesFromLoadBalancerRequest.setLoadBalancerName(elbName);
		deregisterInstancesFromLoadBalancerRequest
				.setInstances(instancesToDeregister);

		elb.deregisterInstancesFromLoadBalancer(deregisterInstancesFromLoadBalancerRequest);

		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
		terminateInstancesRequest.setInstanceIds(oldInstanceIds);

		ec2.terminateInstances(terminateInstancesRequest);
	}

	private boolean allInstancesAlreadyInService(
			AmazonElasticLoadBalancingClient elb, String elbName,
			List<DeployInstanceAgent> deployAgents) {

		List<String> instancesIdsLeftToCheck = new ArrayList<String>();
		List<com.amazonaws.services.elasticloadbalancing.model.Instance> allInstances = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
		for (DeployInstanceAgent deployAgent : deployAgents) {
			instancesIdsLeftToCheck.add(deployAgent.getInstanceId());
			allInstances
					.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(
							deployAgent.getInstanceId()));
		}

		log.info("Will check if all " + instancesIdsLeftToCheck.size()
				+ " are already serving the app.");

		while (instancesIdsLeftToCheck.size() > 0) {

			DescribeInstanceHealthRequest describeInstanceHealthRequest = new DescribeInstanceHealthRequest();
			describeInstanceHealthRequest.setLoadBalancerName(elbName);
			describeInstanceHealthRequest.setInstances(allInstances);

			DescribeInstanceHealthResult describeInstanceHealthResult = elb
					.describeInstanceHealth(describeInstanceHealthRequest);
			List<InstanceState> instanceStates = describeInstanceHealthResult
					.getInstanceStates();

			for (InstanceState instanceState : instanceStates) {

				if (instancesIdsLeftToCheck.contains(instanceState
						.getInstanceId())) {

					log.info("Instance " + instanceState.getInstanceId()
							+ " state is [" + instanceState.getState() + "]");
					if (instanceState.getState().equals("InService")) {
						instancesIdsLeftToCheck.remove(instanceState
								.getInstanceId());
						log.info("Instance " + instanceState.getInstanceId()
								+ " already InService. "
								+ instancesIdsLeftToCheck.size()
								+ " instances left");
					}

				}
			}

			//Aguarda 10 segundos pra checar novamente
			Util.sleepFor(10);

		}

		return true;
	}

	private List<String> getOldInstancesAttachedInELB(
			AmazonElasticLoadBalancingClient elb, String elbName) {

		List<String> oldInstancesInElb = new ArrayList<String>();

		DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest();
		describeLoadBalancersRequest.withLoadBalancerNames(elbName);

		DescribeLoadBalancersResult describeLoadBalancersResult = elb
				.describeLoadBalancers(describeLoadBalancersRequest);
		List<LoadBalancerDescription> loadBalancerDescriptions = describeLoadBalancersResult
				.getLoadBalancerDescriptions();

		for (LoadBalancerDescription loadBalancerDescription : loadBalancerDescriptions) {

			List<com.amazonaws.services.elasticloadbalancing.model.Instance> instances = loadBalancerDescription
					.getInstances();
			for (com.amazonaws.services.elasticloadbalancing.model.Instance instance : instances) {
				oldInstancesInElb.add(instance.getInstanceId());
			}
		}

		return oldInstancesInElb;
	}

	private String startInstance() {

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest.setKeyName(this.deployParameters.getKeyPair());
		runInstancesRequest.setImageId(this.deployParameters.getAmi());
		runInstancesRequest.setMinCount(1);
		runInstancesRequest.setMaxCount(1);
		runInstancesRequest.setUserData(this.deployParameters.getUserData());
		runInstancesRequest.setPlacement(new Placement(this.deployParameters
				.getAvailabilityZone()));
		runInstancesRequest.setInstanceType(this.deployParameters
				.getInstanceType());

		List<String> securityGroups = new ArrayList<String>();
		securityGroups.add(this.deployParameters.getSecurityGroup());

		runInstancesRequest.setSecurityGroups(securityGroups);
		runInstancesRequest.setDisableApiTermination(false);

		RunInstancesResult runInstancesResult = this.deployParameters
				.getAmazonEC2Client().runInstances(runInstancesRequest);
		List<Instance> instances = runInstancesResult.getReservation()
				.getInstances();
		if (instances.size() == 1) {

			Instance instance = instances.get(0);
			String instanceId = instance.getInstanceId();

			//aguarda 2 segundos para garantir que a inst‰ncia foi registrada com sucesso na amazon
			Util.sleepFor(2);

			//retorna o id da inst‰ncia
			return instanceId;

		} else {
			System.err
					.println("Erro ao iniciar inst‰ncia. Total de inst‰ncias iniciadas: "
							+ instances.size());
			System.exit(1);
		}

		return null;
	}

}