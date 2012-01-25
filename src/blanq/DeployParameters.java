package blanq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class DeployParameters {

	private String accessKey;
	private String secretKey;
	private String ami;
	private String qty;
	private String elbName;
	private String keyPair;
	private String appName;
	private String appVersion;
	private String instanceType;
	private String securityGroup;
	private String availabilityZone;
	@XStreamOmitField
	private BasicAWSCredentials credentials;
	private String policyName;
	private String autoScalingGroupName;
	private String launchConfigurationName;
	private String metricAlarmName;

	private static DeployParameters instance;

	private DeployParameters() {
	}

	public static void main(String[] args) {
		DeployParameters a = new DeployParameters();
		a.save();
		DeployParameters b = DeployParameters.getInstance();
		System.out.println(b.getAppName());
	}

	public String getUserData() {
		URL thisURL = DeployParameters.class.getResource("/user-data.txt");
		File thisFile = new File(thisURL.getFile());
		try {
			return new String(Base64.encodeBase64(FileUtils
					.readFileToByteArray(thisFile)));
			//			return FileUtils.readFileToString(thisFile, "base64");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return StringUtils.EMPTY;
	}

	public void save() {
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.setMode(XStream.NO_REFERENCES);
		xstream.alias(DeployParameters.class.getSimpleName(),
				DeployParameters.class);
		URL thisURL = DeployParameters.class.getResource("/"
				+ DeployParameters.class.getSimpleName() + ".json");
		File thisFile = new File(thisURL.getFile());
		try {
			FileUtils.writeStringToFile(thisFile, xstream.toXML(this));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static DeployParameters getInstance() {
		if (DeployParameters.instance != null) {
			return instance;
		}
		InputStream is = DeployParameters.class.getResourceAsStream("/"
				+ DeployParameters.class.getSimpleName() + ".json");
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.alias(DeployParameters.class.getSimpleName(),
				DeployParameters.class);
		DeployParameters.instance = (DeployParameters) xstream.fromXML(is);
		DeployParameters.instance.validateParameters();
		return DeployParameters.instance;
	}

	private void validateParameters() {
		//validando variaveis
		if (!StringUtils.isNotBlank(this.accessKey)) {
			System.err
					.println("Você deve fornecer a sua accessKey de acesso a AWS na variável de ambiente \"aws.accessKey\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.secretKey)) {
			System.err
					.println("Você deve fornecer a sua secretKey de acesso a AWS na variável de ambiente \"aws.secretKey\".");
			System.exit(1);
		}

		if (!StringUtils.isNotBlank(this.ami)) {
			System.err
					.println("Você deve indicar a imagem das instâncias (ami-id) na variável de ambiente \"aws.ami\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.qty)) {
			System.err
					.println("Você deve indicar a quantidade de instâncias para iniciar na variável de ambiente \"aws.qty\".");
			System.exit(1);
		}
		if (!StringUtils.isNumeric(this.qty)) {
			System.err
					.println("A quantidade de instâncias deve ser um valor numérico.");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.elbName)) {
			System.err
					.println("Você deve indicar em qual loadbalancer as instâncias serão plugadas na variável de ambiente \"aws.elb\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.keyPair)) {
			System.err
					.println("Você deve indicar qual o keypair que será atribuído as instâncias na variável de ambiente \"aws.keypair\"");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.appName)) {
			System.err
					.println("Você deve definir o nome da aplicação que está publicando na variável de ambiente \"aws.appname\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.appVersion)) {
			System.err
					.println("Você deve definir o número da versão da aplicação que está publicando na variável de ambiente \"aws.appversion\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.instanceType)) {
			System.err
					.println("Você deve definir o tipo de instância na variável de ambiente \"aws.type\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.securityGroup)) {
			System.err
					.println("Você deve definir qual o security group das instâncias na variável de ambiente \"aws.group\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.availabilityZone)) {
			System.err
					.println("Você deve definir a zona onde as intâncias serão criadas na variável de ambiente \"aws.zone\".");
			System.exit(1);
		}
	}

	public BasicAWSCredentials getBasicAWSCredentials() {
		if (this.credentials == null) {
			this.credentials = new BasicAWSCredentials(this.accessKey,
					this.secretKey);
		}
		return this.credentials;
	}

	public AmazonAutoScalingClient getAmazonAutoScalingClient() {
		AmazonAutoScalingClient autoScaling = new AmazonAutoScalingClient(
				this.credentials);
		autoScaling.setEndpoint("autoscaling.sa-east-1.amazonaws.com");
		return autoScaling;
	}

	public AmazonCloudWatchClient getAmazonCloudWatchClient() {

		AmazonCloudWatchClient cloudWatch = new AmazonCloudWatchClient(
				this.credentials);
		cloudWatch.setEndpoint("monitoring.sa-east-1.amazonaws.com");
		return cloudWatch;
	}

	public AmazonEC2Client getAmazonEC2Client() {

		AmazonEC2Client ec2 = new AmazonEC2Client(this.getBasicAWSCredentials());
		ec2.setEndpoint("ec2.sa-east-1.amazonaws.com");
		return ec2;
	}

	public AmazonElasticLoadBalancingClient getAmazonElasticLoadBalancingClient() {

		AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(
				this.credentials);
		elb.setEndpoint("elasticloadbalancing.sa-east-1.amazonaws.com");
		return elb;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.credentials = null;
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.credentials = null;
		this.secretKey = secretKey;
	}

	public String getAmi() {
		return ami;
	}

	public void setAmi(String ami) {
		this.ami = ami;
	}

	public String getQty() {
		return qty;
	}

	public void setQty(String qty) {
		this.qty = qty;
	}

	public String getElbName() {
		return elbName;
	}

	public void setElbName(String elbName) {
		this.elbName = elbName;
	}

	public String getKeyPair() {
		return keyPair;
	}

	public void setKeyPair(String keyPair) {
		this.keyPair = keyPair;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getSecurityGroup() {
		return securityGroup;
	}

	public void setSecurityGroup(String securityGroup) {
		this.securityGroup = securityGroup;
	}

	public String getAvailabilityZone() {
		return availabilityZone;
	}

	public void setAvailabilityZone(String availabilityZone) {
		this.availabilityZone = availabilityZone;
	}

	public BasicAWSCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(BasicAWSCredentials credentials) {
		this.credentials = credentials;
	}

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public String getAutoScalingGroupName() {
		return autoScalingGroupName;
	}

	public void setAutoScalingGroupName(String autoScalingGroupName) {
		this.autoScalingGroupName = autoScalingGroupName;
	}

	public String getLaunchConfigurationName() {
		return launchConfigurationName;
	}

	public void setLaunchConfigurationName(String launchConfigurationName) {
		this.launchConfigurationName = launchConfigurationName;
	}

	public String getMetricAlarmName() {
		return metricAlarmName;
	}

	public void setMetricAlarmName(String metricAlarmName) {
		this.metricAlarmName = metricAlarmName;
	}

}
