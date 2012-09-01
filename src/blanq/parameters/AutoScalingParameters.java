package blanq.parameters;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;

public class AutoScalingParameters extends AbstractParameters {
	private static AutoScalingParameters instance;

	private AmazonAutoScalingClient autoScaling;
	private AmazonCloudWatchClient cloudWatch;

	@NotBlank
	private String policyName;
	@NotBlank
	private String autoScalingGroupName;
	@NotBlank
	private String launchConfigurationName;
	@NotBlank
	private String autoScalingEndpoint;
	@NotBlank
	private String cloudWatchEndpoint;
	@Min(0)
	private int autoScalingGroupMin;
	@Min(0)
	private int autoScalingGroupMax;
	@Min(0)
	private int autoScalingGroupCoolDown;
	@Min(0)
	private int policyScalingAdjustment;
	@NotBlank
	private String policyAdjustmentType;
	@NotBlank
	private String alarmMetricName;
	@NotBlank
	private String alarmNamespace;
	@NotBlank
	private String alarmStatistic;
	@Min(0)
	private int alarmPeriod;
	@Min(0)
	private int alarmEvaluationPeriods;
	@Min(0)
	private double alarmThreshold;
	@NotBlank
	private String alarmComparisonOperator;
	@NotBlank
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
