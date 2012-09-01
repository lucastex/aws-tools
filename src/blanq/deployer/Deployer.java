package blanq.deployer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import blanq.instances.InstancesTerminator;
import blanq.parameters.DeployParameters;
import blanq.util.Util;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;

public class Deployer {

	private static final Log log = LogFactory.getLog(Deployer.class);

	private DeployParameters deployParameters;

	public Deployer(DeployParameters deployParameters) {

		super();

		this.deployParameters = deployParameters;

		log.info("AWS Deployer initialized [APP: "
				+ deployParameters.getAppName() + " / Version: "
				+ deployParameters.getAppVersion() + "]");
	}

	public void deploy() {

		log.info("Connecting to AWS Services [EC2+ELB]");

		// criando conectividade com AWS
		AmazonEC2Client ec2 = deployParameters.getAmazonEC2Client();
		AmazonElasticLoadBalancingClient elb = deployParameters
				.getAmazonElasticLoadBalancingClient();

		log.info("Retrieving instances that is currently serving "
				+ deployParameters.getAppName() + " app");

		// lista de agentes
		List<DeployInstanceAgent> deployAgents = new ArrayList<DeployInstanceAgent>();

		// contador de instancias
		Integer maxInstances = deployParameters.getQty();
		for (int counter = 1; counter <= maxInstances; counter++) {

			String instanceId = startInstance();

			log.info("Starting instance " + counter + ": " + instanceId);

			DeployInstanceAgent agent = new DeployInstanceAgent(counter,
					deployParameters.getElbName(),
					deployParameters.getAppName(),
					deployParameters.getAppVersion(), instanceId, ec2, elb);
			Thread threadAgent = new Thread(agent);
			threadAgent.start();

			deployAgents.add(agent);
		}

		// aguarda 5 segundos para iniciar a verificacao
		Util.sleepFor(5);

		//remove instancias antigas do elb
		if (allInstancesAlreadyInService(elb, deployParameters.getElbName(),
				deployAgents)) {

			InstancesTerminator instanceTerminator = new InstancesTerminator(
					deployParameters.getAmazonEC2Client(),
					deployParameters.getAmazonElasticLoadBalancingClient());

			// lista de instancias antigas que estavam no ELB
			List<String> oldInstanceIds = new ArrayList<String>();
			oldInstanceIds = instanceTerminator
					.getInstancesAttachedInELB(deployParameters.getElbName());

			log.info("Instances serving app: "
					+ StringUtils.join(oldInstanceIds, ", "));

			log.info("All new instances already serving newer application version, will remove old instances from ELB now.");
			instanceTerminator.terminateAndRemoveFromElbInstances(
					deployParameters.getElbName(), oldInstanceIds);

		}
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

			// aguarda 10 segundos pra checar novamente
			Util.sleepFor(10);

		}

		return true;
	}

	private String startInstance() {

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		runInstancesRequest.setKeyName(deployParameters.getKeyPair());
		runInstancesRequest.setImageId(deployParameters.getAmi());
		runInstancesRequest.setMinCount(1);
		runInstancesRequest.setMaxCount(1);
		runInstancesRequest.setUserData(deployParameters.getUserData());
		runInstancesRequest.setPlacement(new Placement(deployParameters
				.getAvailabilityZone()));
		runInstancesRequest.setInstanceType(deployParameters.getInstanceType());

		List<String> securityGroups = new ArrayList<String>();
		securityGroups.add(deployParameters.getSecurityGroup());

		runInstancesRequest.setSecurityGroups(securityGroups);
		runInstancesRequest.setDisableApiTermination(false);

		RunInstancesResult runInstancesResult = deployParameters
				.getAmazonEC2Client().runInstances(runInstancesRequest);
		List<Instance> instances = runInstancesResult.getReservation()
				.getInstances();
		if (instances.size() == 1) {

			Instance instance = instances.get(0);
			String instanceId = instance.getInstanceId();

			// aguarda 2 segundos para garantir que a inst‰ncia foi registrada com sucesso na amazon
			Util.sleepFor(2);

			// retorna o id da inst‰ncia
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