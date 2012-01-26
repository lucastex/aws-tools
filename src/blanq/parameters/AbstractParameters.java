package blanq.parameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public abstract class AbstractParameters {

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
	private AmazonElasticLoadBalancingClient elb;
	private String elbEndpoint;

	public AmazonElasticLoadBalancingClient getAmazonElasticLoadBalancingClient() {
		if (elb == null) {
			elb = new AmazonElasticLoadBalancingClient(this.getCredentials());
			if (elbEndpoint != null) {
				//				elbEndpoint = "elasticloadbalancing.sa-east-1.amazonaws.com";
				elb.setEndpoint(elbEndpoint);
			}
		}
		return elb;
	}

	public String getUserData() {
		URL thisURL = AbstractParameters.class.getResource("/user-data.txt");
		File thisFile = new File(thisURL.getFile());
		try {
			return new String(Base64.encodeBase64(FileUtils
					.readFileToByteArray(thisFile)));
		} catch (IOException e) {
			System.err.printf("Erro ao abrir o user-data.txt. Erro msg: %s",
					e.getMessage());
			System.exit(1);
		}
		return StringUtils.EMPTY;
	}

	protected void validateParameters() {
		//validando variaveis
		if (!StringUtils.isNotBlank(this.getAccessKey())) {
			System.err
					.println("Voc� deve fornecer a sua accessKey de acesso a AWS na vari�vel de ambiente \"aws.accessKey\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getSecretKey())) {
			System.err
					.println("Voc� deve fornecer a sua secretKey de acesso a AWS na vari�vel de ambiente \"aws.secretKey\".");
			System.exit(1);
		}
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
			System.err.printf("Erro ao carregar o arquivo: %s. Erro msg: %s",
					thisURL.getFile(), e.getMessage());
			System.exit(1);
		}
	}

	protected static Object load() {
		InputStream is = DeployParameters.class.getResourceAsStream("/"
				+ DeployParameters.class.getSimpleName() + ".json");
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.alias(DeployParameters.class.getSimpleName(),
				DeployParameters.class);
		Object fromJSON = xstream.fromXML(is);
		return fromJSON;
	}

	public BasicAWSCredentials getBasicAWSCredentials() {
		if (this.credentials == null) {
			this.credentials = new BasicAWSCredentials(this.accessKey,
					this.secretKey);
		}
		return this.credentials;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
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
}
