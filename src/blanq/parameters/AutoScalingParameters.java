package blanq.parameters;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;

public class AutoScalingParameters extends AbstractParameters {
	private static AutoScalingParameters instance;
	private AmazonAutoScalingClient autoScaling;
	private AmazonCloudWatchClient cloudWatch;
	private String policyName;
	private String autoScalingGroupName;
	private String launchConfigurationName;
	private String autoScalingEndpoint;
	private String cloudWatchEndpoint;
	private int autoScalingGroupMin; /* = 2; */
	private int autoScalingGroupMax; /* = 4; */
	private int autoScalingGroupCoolDown; /* = 600; */
	private int policyScalingAdjustment;
	private String policyAdjustmentType;
	private String alarmMetricName;
	private String alarmNamespace;
	private String alarmStatistic;
	private int alarmPeriod;
	private int alarmEvaluationPeriods;
	private double alarmThreshold;
	private String alarmComparisonOperator;
	private String alarmGroupDimesionName;

	private AutoScalingParameters() {
	}

	public static AutoScalingParameters getInstance() {
		if (instance == null) {
			instance = (AutoScalingParameters) load(AutoScalingParameters.class);
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
		if (!StringUtils.isNotBlank(this.getAlarmMetricName())) {
			System.err
					.println("Você deve fornecer a sua getMetricAlarmName de acesso a AWS na variável de ambiente \"aws.secretKey\".");
			System.exit(1);
		}
	}

	public AmazonAutoScalingClient getAmazonAutoScalingClient() {
		if (autoScaling == null) {
			autoScaling = new AmazonAutoScalingClient(this.getCredentials());
			if (autoScalingEndpoint != null) {
				autoScaling.setEndpoint(autoScalingEndpoint);
			}
		}
		return autoScaling;
	}

	public AmazonCloudWatchClient getAmazonCloudWatchClient() {
		if (cloudWatch == null) {
			cloudWatch = new AmazonCloudWatchClient(this.getCredentials());
			if (cloudWatchEndpoint != null) {
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

	public AmazonAutoScalingClient getAutoScaling() {
		return autoScaling;
	}

	public void setAutoScaling(AmazonAutoScalingClient autoScaling) {
		this.autoScaling = autoScaling;
	}

	public String getAutoScalingEndpoint() {
		return autoScalingEndpoint;
	}

	public void setAutoScalingEndpoint(String autoScalingEndpoint) {
		this.autoScalingEndpoint = autoScalingEndpoint;
	}

	public AmazonCloudWatchClient getCloudWatch() {
		return cloudWatch;
	}

	public void setCloudWatch(AmazonCloudWatchClient cloudWatch) {
		this.cloudWatch = cloudWatch;
	}

	public String getCloudWatchEndpoint() {
		return cloudWatchEndpoint;
	}

	public void setCloudWatchEndpoint(String cloudWatchEndpoint) {
		this.cloudWatchEndpoint = cloudWatchEndpoint;
	}

	public int getPolicyScalingAdjustment() {
		return policyScalingAdjustment;
	}

	public void setPolicyScalingAdjustment(int policyScalingAdjustment) {
		this.policyScalingAdjustment = policyScalingAdjustment;
	}

	public String getPolicyAdjustmentType() {
		return policyAdjustmentType;
	}

	public void setPolicyAdjustmentType(String policyAdjustmentType) {
		this.policyAdjustmentType = policyAdjustmentType;
	}

	public String getAlarmMetricName() {
		return alarmMetricName;
	}

	public void setAlarmMetricName(String alarmMetricName) {
		this.alarmMetricName = alarmMetricName;
	}

	public String getAlarmNamespace() {
		return alarmNamespace;
	}

	public void setAlarmNamespace(String alarmNamespace) {
		this.alarmNamespace = alarmNamespace;
	}

	public int getAlarmPeriod() {
		return alarmPeriod;
	}

	public void setAlarmPeriod(int alarmPeriod) {
		this.alarmPeriod = alarmPeriod;
	}

	public int getAlarmEvaluationPeriods() {
		return alarmEvaluationPeriods;
	}

	public void setAlarmEvaluationPeriods(int alarmEvaluationPeriods) {
		this.alarmEvaluationPeriods = alarmEvaluationPeriods;
	}

	public double getAlarmThreshold() {
		return alarmThreshold;
	}

	public void setAlarmThreshold(double alarmThreshold) {
		this.alarmThreshold = alarmThreshold;
	}

	public String getAlarmComparisonOperator() {
		return alarmComparisonOperator;
	}

	public void setAlarmComparisonOperator(String alarmComparisonOperator) {
		this.alarmComparisonOperator = alarmComparisonOperator;
	}

	public String getAlarmGroupDimesionName() {
		return alarmGroupDimesionName;
	}

	public void setAlarmGroupDimesionName(String alarmGroupDimesionName) {
		this.alarmGroupDimesionName = alarmGroupDimesionName;
	}

	public String getAlarmStatistic() {
		return alarmStatistic;
	}

	public void setAlarmStatistic(String alarmStatistic) {
		this.alarmStatistic = alarmStatistic;
	}

}
