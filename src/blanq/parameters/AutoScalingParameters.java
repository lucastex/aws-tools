package blanq.parameters;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;

public class AutoScalingParameters extends AbstractParameters {
	private static AutoScalingParameters instance;
	private String policyName;
	private String autoScalingGroupName;
	private String launchConfigurationName;
	private String metricAlarmName;
	private AmazonAutoScalingClient autoScaling;
	private String autoScalingEndpoint;
	private AmazonCloudWatchClient cloudWatch;
	private String cloudWatchEndpoint;
	private int autoScalingGroupMin; /* = 2; */
	private int autoScalingGroupMax; /* = 4; */
	private int autoScalingGroupCoolDown; /* = 600; */

	private AutoScalingParameters() {

	}

	public static AutoScalingParameters getInstance() {
		if (instance == null) {
			instance = (AutoScalingParameters) load();
			instance.validateParameters();
		}
		return instance;
	}

	protected void validateParameters() {
		//validando variaveis
		if (!StringUtils.isNotBlank(this.getPolicyName())) {
			System.err
					.println("Você deve fornecer a sua getPolicyName de acesso a AWS na variável de ambiente \"aws.accessKey\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getAutoScalingGroupName())) {
			System.err
					.println("Você deve fornecer a sua getAutoScalingGroupName de acesso a AWS na variável de ambiente \"aws.secretKey\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getLaunchConfigurationName())) {
			System.err
					.println("Você deve fornecer a sua getLaunchConfigurationName de acesso a AWS na variável de ambiente \"aws.secretKey\".");
			System.exit(1);
		}
		if (!StringUtils.isNotBlank(this.getMetricAlarmName())) {
			System.err
					.println("Você deve fornecer a sua getMetricAlarmName de acesso a AWS na variável de ambiente \"aws.secretKey\".");
			System.exit(1);
		}
	}

	public AmazonAutoScalingClient getAmazonAutoScalingClient() {
		if (autoScaling == null) {
			autoScaling = new AmazonAutoScalingClient(this.getCredentials());
			if (autoScalingEndpoint != null) {
				//				autoScalingEndpoint = "autoscaling.sa-east-1.amazonaws.com";
				autoScaling.setEndpoint(autoScalingEndpoint);
			}
		}
		return autoScaling;
	}

	public AmazonCloudWatchClient getAmazonCloudWatchClient() {
		if (cloudWatch == null) {
			cloudWatch = new AmazonCloudWatchClient(this.getCredentials());
			if (cloudWatchEndpoint != null) {
				//				cloudWatchEndpoint = "monitoring.sa-east-1.amazonaws.com";
				cloudWatch.setEndpoint(cloudWatchEndpoint);
			}
		}
		return cloudWatch;
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

	public int getAutoScalingGroupMin() {
		return autoScalingGroupMin;
	}

	public void setAutoScalingGroupMin(int autoScalingGroupMin) {
		this.autoScalingGroupMin = autoScalingGroupMin;
	}

	public int getAutoScalingGroupMax() {
		return autoScalingGroupMax;
	}

	public void setAutoScalingGroupMax(int autoScalingGroupMax) {
		this.autoScalingGroupMax = autoScalingGroupMax;
	}

	public int getAutoScalingGroupCoolDown() {
		return autoScalingGroupCoolDown;
	}

	public void setAutoScalingGroupCoolDown(int autoScalingGroupCoolDown) {
		this.autoScalingGroupCoolDown = autoScalingGroupCoolDown;
	}

}
