package blanq.instances;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

public class InstancesTerminator {

	private AmazonEC2Client ec2;
	private AmazonElasticLoadBalancingClient elb;

	public InstancesTerminator(AmazonEC2Client ec2,
			AmazonElasticLoadBalancingClient elb) {
		this.ec2 = ec2;
		this.elb = elb;
	}

	public void terminateAndRemoveFromElbInstances(String elbName,
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

	public List<String> getInstancesAttachedInELB(String elbName) {

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
}
