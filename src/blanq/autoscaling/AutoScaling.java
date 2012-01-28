package blanq.autoscaling;

import java.util.Arrays;
import java.util.Collection;

import blanq.parameters.AutoScalingParameters;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DeleteLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeletePolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.DeleteAlarmsRequest;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;

public class AutoScaling {

	private AutoScalingParameters autoScalingParameters;

	public AutoScaling(AutoScalingParameters autoScalingParameters) {
		this.autoScalingParameters = autoScalingParameters;
	}

	public static void main(String[] args) {

	}

	public void delete() {
		deleteAlarm();
		deletePolicy();
		deleteAutoScalingGroup();
		deleteLaunchConfiguration();
	}

	private void deleteAlarm() {
		AmazonCloudWatchClient cloudWatch = this.autoScalingParameters
				.getAmazonCloudWatchClient();
		try {
			DeleteAlarmsRequest deleteAlarmsRequest = new DeleteAlarmsRequest();
			Collection<String> alarmNames = Arrays
					.asList(new String[] { this.autoScalingParameters
							.getAlarmMetricName() });
			deleteAlarmsRequest.setAlarmNames(alarmNames);
			cloudWatch.deleteAlarms(deleteAlarmsRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	private void deletePolicy() {
		AmazonAutoScalingClient autoScaling = this.autoScalingParameters
				.getAmazonAutoScalingClient();
		try {
			DeletePolicyRequest deletePolicyRequest = new DeletePolicyRequest();
			deletePolicyRequest.setPolicyName(this.autoScalingParameters
					.getPolicyName());
			deletePolicyRequest
					.setAutoScalingGroupName(this.autoScalingParameters
							.getAutoScalingGroupName());
			autoScaling.deletePolicy(deletePolicyRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	private void deleteAutoScalingGroup() {
		AmazonAutoScalingClient autoScaling = this.autoScalingParameters
				.getAmazonAutoScalingClient();
		try {
			DeleteAutoScalingGroupRequest deleteAutoScalingGroupRequest = new DeleteAutoScalingGroupRequest();
			deleteAutoScalingGroupRequest
					.setAutoScalingGroupName(this.autoScalingParameters
							.getAutoScalingGroupName());
			deleteAutoScalingGroupRequest.setForceDelete(true);
			autoScaling.deleteAutoScalingGroup(deleteAutoScalingGroupRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	private void deleteLaunchConfiguration() {
		AmazonAutoScalingClient autoScaling = this.autoScalingParameters
				.getAmazonAutoScalingClient();
		try {
			DeleteLaunchConfigurationRequest deleteLaunchConfigurationRequest = new DeleteLaunchConfigurationRequest();
			deleteLaunchConfigurationRequest
					.setLaunchConfigurationName(this.autoScalingParameters
							.getLaunchConfigurationName());
			autoScaling
					.deleteLaunchConfiguration(deleteLaunchConfigurationRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	private void createLaunchConfiguration() {
		AmazonAutoScalingClient autoScaling = this.autoScalingParameters
				.getAmazonAutoScalingClient();
		// as-create-launch-config my_autoscale_config --image-id ami-XXXXXXXX --instance-type m1.small --group "My Security Group Name"
		CreateLaunchConfigurationRequest createLaunchConfigurationRequest = new CreateLaunchConfigurationRequest();
		createLaunchConfigurationRequest.setImageId(this.autoScalingParameters
				.getAmi());
		createLaunchConfigurationRequest
				.setInstanceType(this.autoScalingParameters.getInstanceType());
		Collection<String> securityGroups = Arrays
				.asList(new String[] { this.autoScalingParameters
						.getSecurityGroup() });
		createLaunchConfigurationRequest.setSecurityGroups(securityGroups);
		createLaunchConfigurationRequest.setKeyName(this.autoScalingParameters
				.getKeyPair());
		createLaunchConfigurationRequest
				.setLaunchConfigurationName(this.autoScalingParameters
						.getLaunchConfigurationName());
		createLaunchConfigurationRequest.setUserData(this.autoScalingParameters
				.getUserData());
		autoScaling.createLaunchConfiguration(createLaunchConfigurationRequest);
	}

	private void createAutoScalingGroup() {
		AmazonAutoScalingClient autoScaling = this.autoScalingParameters
				.getAmazonAutoScalingClient();
		// as-create-auto-scaling-group my_autoscale_group --availability-zones us-east-1X --launch-configuration my_autoscale_config --min-size 1 --max-size 3 --load-balancers my_load_balancer_name --health-check-type ELB --grace-period 300
		CreateAutoScalingGroupRequest createAutoScalingGroupRequest = new CreateAutoScalingGroupRequest();
		createAutoScalingGroupRequest
				.setAutoScalingGroupName(this.autoScalingParameters
						.getAutoScalingGroupName());
		createAutoScalingGroupRequest
				.setLaunchConfigurationName(this.autoScalingParameters
						.getLaunchConfigurationName());
		Collection<String> availabilityZones = Arrays
				.asList(new String[] { this.autoScalingParameters
						.getAvailabilityZone() });
		createAutoScalingGroupRequest.setAvailabilityZones(availabilityZones);
		createAutoScalingGroupRequest.setMinSize(this.autoScalingParameters
				.getAutoScalingGroupMin());
		createAutoScalingGroupRequest.setMaxSize(this.autoScalingParameters
				.getAutoScalingGroupMax());
		createAutoScalingGroupRequest
				.setDefaultCooldown(this.autoScalingParameters
						.getAutoScalingGroupCoolDown());
		Collection<String> loadBalancerNames = Arrays
				.asList(new String[] { this.autoScalingParameters.getElbName() });
		createAutoScalingGroupRequest.setLoadBalancerNames(loadBalancerNames);
		autoScaling.createAutoScalingGroup(createAutoScalingGroupRequest);
	}

	private PutScalingPolicyResult putScalingPolicy() {
		AmazonAutoScalingClient autoScaling = this.autoScalingParameters
				.getAmazonAutoScalingClient();
		// as-put-scaling-policy ScaleUp --auto-scaling-group my_autoscale_group --adjustment=1 --type ChangeInCapacity
		PutScalingPolicyRequest putScalingPolicyRequest = new PutScalingPolicyRequest();
		putScalingPolicyRequest.setPolicyName(this.autoScalingParameters
				.getPolicyName());
		putScalingPolicyRequest.setScalingAdjustment(this.autoScalingParameters
				.getPolicyScalingAdjustment());
		putScalingPolicyRequest.setAdjustmentType(this.autoScalingParameters
				.getPolicyAdjustmentType());
		putScalingPolicyRequest.setCooldown(this.autoScalingParameters
				.getAutoScalingGroupCoolDown());
		putScalingPolicyRequest
				.setAutoScalingGroupName(this.autoScalingParameters
						.getAutoScalingGroupName());
		return autoScaling.putScalingPolicy(putScalingPolicyRequest);
	}

	private void putAlarmRequest(PutScalingPolicyResult putScalingPolicyResult) {
		AmazonCloudWatchClient cloudWatch = this.autoScalingParameters
				.getAmazonCloudWatchClient();

		PutMetricAlarmRequest putMetricAlarmRequest = new PutMetricAlarmRequest();
		putMetricAlarmRequest.setAlarmName(this.autoScalingParameters
				.getAlarmMetricName());
		Collection<String> alarmActions = Arrays
				.asList(new String[] { putScalingPolicyResult.getPolicyARN() });
		putMetricAlarmRequest.setAlarmActions(alarmActions);
		putMetricAlarmRequest.setMetricName(this.autoScalingParameters
				.getAlarmMetricName());
		putMetricAlarmRequest.setNamespace(this.autoScalingParameters
				.getAlarmNamespace());
		putMetricAlarmRequest.setStatistic(this.autoScalingParameters
				.getAlarmStatistic());
		putMetricAlarmRequest.setPeriod(this.autoScalingParameters
				.getAlarmPeriod()); 
		putMetricAlarmRequest.setEvaluationPeriods(this.autoScalingParameters
				.getAlarmEvaluationPeriods());
		putMetricAlarmRequest.setThreshold(this.autoScalingParameters
				.getAlarmThreshold());
		/* http://docs.amazonwebservices.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudwatch/model/PutMetricAlarmRequest.html#getComparisonOperator() */
		putMetricAlarmRequest.setComparisonOperator(this.autoScalingParameters
				.getAlarmComparisonOperator());
		Dimension autoScalingGroupDimesion = new Dimension();
		autoScalingGroupDimesion.setName(this.autoScalingParameters
				.getAlarmGroupDimesionName());
		autoScalingGroupDimesion.setValue(this.autoScalingParameters
				.getAutoScalingGroupName());
		Collection<Dimension> dimensions = Arrays
				.asList(new Dimension[] { autoScalingGroupDimesion });
		putMetricAlarmRequest.setDimensions(dimensions);

		cloudWatch.putMetricAlarm(putMetricAlarmRequest);
	}

	// Dividir em métodos o código macarronico
	public void scale() {
		createLaunchConfiguration();
		createAutoScalingGroup();
		PutScalingPolicyResult putScalingPolicyResult = putScalingPolicy();
		putAlarmRequest(putScalingPolicyResult);
	}
}
