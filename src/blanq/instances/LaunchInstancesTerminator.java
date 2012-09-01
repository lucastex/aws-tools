package blanq.instances;

import java.util.List;

import blanq.parameters.DeployParameters;

public class LaunchInstancesTerminator {
	public static void main(String[] args) {
		DeployParameters deployParameters = DeployParameters.getInstance();
		InstancesTerminator instanceTerminator = new InstancesTerminator(
				deployParameters.getAmazonEC2Client(),
				deployParameters.getAmazonElasticLoadBalancingClient());
		List<String> oldInstanceIds = instanceTerminator
				.getInstancesAttachedInELB(deployParameters.getElbName());

		instanceTerminator.terminateAndRemoveFromElbInstances(
				deployParameters.getElbName(), oldInstanceIds);
	}
}
