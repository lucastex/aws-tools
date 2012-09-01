package blanq.parameters;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;

public class DeployParameters extends AbstractParameters {

	private static DeployParameters instance;
	private AmazonEC2Client ec2;

	private AmazonElasticLoadBalancingClient elb;

	@NotBlank
	private String ec2Endpoint;

	@NotBlank
	private String elbEndpoint;

	@Min(0)
	private int qty;

	public AmazonElasticLoadBalancingClient getAmazonElasticLoadBalancingClient() {
		if (elb == null) {
			elb = new AmazonElasticLoadBalancingClient(this.getCredentials());
			if (elbEndpoint != null) {
				elb.setEndpoint(elbEndpoint);
			}
		}
		return elb;
	}

	public AmazonEC2Client getAmazonEC2Client() {
		if (ec2 == null) {
			ec2 = new AmazonEC2Client(this.getCredentials());
			if (ec2Endpoint != null) {
				ec2.setEndpoint(ec2Endpoint);
			}
		}
		return ec2;
	}

	public static DeployParameters getInstance() {
		if (instance == null) {
			instance = (DeployParameters) load(DeployParameters.class);
			instance.validateParameters();
		}
		return instance;
	}

	public String getEc2Endpoint() {
		return ec2Endpoint;
	}

	public void setEc2Endpoint(String ec2Endpoint) {
		this.ec2Endpoint = ec2Endpoint;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getElbEndpoint() {
		return elbEndpoint;
	}

	public void setElbEndpoint(String elbEndpoint) {
		this.elbEndpoint = elbEndpoint;
	}
}
