package blanq.parameters;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.ec2.AmazonEC2Client;

public class DeployParameters extends AbstractParameters {

	private static DeployParameters instance;
	private AmazonEC2Client ec2;
	private String ec2Endpoint;

	public static void main(String[] args) {
		DeployParameters auto = new DeployParameters();

		auto.setAccessKey("AccessKey");
		auto.setSecretKey("SecretKey");
		auto.setKeyPair("deliveria");
		auto.setAppName("Deliveria");
		auto.setAppVersion("1");
		auto.setSecurityGroup("deliveria");
		auto.setInstanceType("c1.medium");
		auto.setElbName("Deliveria");
		auto.setAvailabilityZone("sa-east-1b");
		auto.setAmi("ami-66538c7b");
		auto.setEc2Endpoint("ec2.sa-east-1.amazonaws.com");

		auto.save(DeployParameters.class);
	}

	private DeployParameters() {
	}

	public AmazonEC2Client getAmazonEC2Client() {
		if (ec2 == null) {
			ec2 = new AmazonEC2Client(this.getCredentials());
			if (ec2Endpoint != null) {
				if (ec2Endpoint != null) {
					ec2Endpoint = "ec2.sa-east-1.amazonaws.com";
					ec2.setEndpoint(ec2Endpoint);
				}
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

	protected void validateParameters() {
		super.validateParameters();
		//validando variaveis
		if (!StringUtils.isNotBlank(this.getAmi())) {
			System.err
					.println("Você deve indicar a imagem das instâncias (ami-id) na variável de ambiente \"aws.ami\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getQty())) {
			System.err
					.println("Você deve indicar a quantidade de instâncias para iniciar na variável de ambiente \"aws.qty\".");
			System.exit(1);
		}
		if (!StringUtils.isNumeric(this.getQty())) {
			System.err
					.println("A quantidade de instâncias deve ser um valor numérico.");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getElbName())) {
			System.err
					.println("Você deve indicar em qual loadbalancer as instâncias serão plugadas na variável de ambiente \"aws.elb\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getKeyPair())) {
			System.err
					.println("Você deve indicar qual o keypair que será atribuído as instâncias na variável de ambiente \"aws.keypair\"");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getAppName())) {
			System.err
					.println("Você deve definir o nome da aplicação que está publicando na variável de ambiente \"aws.appname\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getAppVersion())) {
			System.err
					.println("Você deve definir o número da versão da aplicação que está publicando na variável de ambiente \"aws.appversion\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getInstanceType())) {
			System.err
					.println("Você deve definir o tipo de instância na variável de ambiente \"aws.type\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getSecurityGroup())) {
			System.err
					.println("Você deve definir qual o security group das instâncias na variável de ambiente \"aws.group\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getAvailabilityZone())) {
			System.err
					.println("Você deve definir a zona onde as intâncias serão criadas na variável de ambiente \"aws.zone\".");
			System.exit(1);
		}
	}

	public String getEc2Endpoint() {
		return ec2Endpoint;
	}

	public void setEc2Endpoint(String ec2Endpoint) {
		this.ec2Endpoint = ec2Endpoint;
	}

}
