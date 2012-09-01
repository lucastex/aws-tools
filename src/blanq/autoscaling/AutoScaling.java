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

	public void delete() {
		deleteAlarm();
		deletePolicy();
		deleteAutoScalingGroup();
		deleteLaunchConfiguration();
	}

	private void deleteAlarm() {
		AmazonCloudWatchClient cloudWatch = autoScalingParameters
				.getAmazonCloudWatchClient();
		try {
			DeleteAlarmsRequest deleteAlarmsRequest = new DeleteAlarmsRequest();
			Collection<String> alarmNames = Arrays
					.asList(new String[] { autoScalingParameters
							.getAlarmMetricName() });
			deleteAlarmsRequest.setAlarmNames(alarmNames);
			cloudWatch.deleteAlarms(deleteAlarmsRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	private void deletePolicy() {
		AmazonAutoScalingClient autoScaling = autoScalingParameters
				.getAmazonAutoScalingClient();
		try {
			DeletePolicyRequest deletePolicyRequest = new DeletePolicyRequest();
			deletePolicyRequest.setPolicyName(autoScalingParameters
					.getPolicyName());
			deletePolicyRequest.setAutoScalingGroupName(autoScalingParameters
					.getAutoScalingGroupName());
			autoScaling.deletePolicy(deletePolicyRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	private void deleteAutoScalingGroup() {
		AmazonAutoScalingClient autoScaling = autoScalingParameters
				.getAmazonAutoScalingClient();
		try {
			DeleteAutoScalingGroupRequest deleteAutoScalingGroupRequest = new DeleteAutoScalingGroupRequest();
			deleteAutoScalingGroupRequest
					.setAutoScalingGroupName(autoScalingParameters
							.getAutoScalingGroupName());
			deleteAutoScalingGroupRequest.setForceDelete(true);
			autoScaling.deleteAutoScalingGroup(deleteAutoScalingGroupRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	private void deleteLaunchConfiguration() {
		AmazonAutoScalingClient autoScaling = autoScalingParameters
				.getAmazonAutoScalingClient();
		try {
			DeleteLaunchConfigurationRequest deleteLaunchConfigurationRequest = new DeleteLaunchConfigurationRequest();
			deleteLaunchConfigurationRequest
					.setLaunchConfigurationName(autoScalingParameters
							.getLaunchConfigurationName());
			autoScaling
					.deleteLaunchConfiguration(deleteLaunchConfigurationRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	private void createLaunchConfiguration() {
		AmazonAutoScalingClient autoScaling = autoScalingParameters
				.getAmazonAutoScalingClient();

		// as-create-launch-config my_autoscale_config --image-id ami-XXXXXXXX --instance-type m1.small --group "My Security Group Name"
		CreateLaunchConfigurationRequest launchConfigurationRequest = new CreateLaunchConfigurationRequest();

		// configuration name
		launchConfigurationRequest
				.setLaunchConfigurationName(autoScalingParameters
						.getLaunchConfigurationName());

		// image to be initialized
		launchConfigurationRequest.setImageId(autoScalingParameters.getAmi());

		// instance-type (available types micro, small, medium, large, extra large)
		launchConfigurationRequest.setInstanceType(autoScalingParameters
				.getInstanceType());

		// security group (http:80, https:443, ssh:22)
		Collection<String> securityGroups = Arrays
				.asList(new String[] { autoScalingParameters.getSecurityGroup() });
		launchConfigurationRequest.setSecurityGroups(securityGroups);

		// keyParName
		launchConfigurationRequest.setKeyName(autoScalingParameters
				.getKeyPair());

		// initialization script 
		launchConfigurationRequest.setUserData(autoScalingParameters
				.getUserData());

		autoScaling.createLaunchConfiguration(launchConfigurationRequest);
	}

	private void createAutoScalingGroup() {
		AmazonAutoScalingClient autoScaling = autoScalingParameters
				.getAmazonAutoScalingClient();

		// as-create-auto-scaling-group my_autoscale_group --availability-zones us-east-1X --launch-configuration my_autoscale_config --min-size 1 --max-size 3 --load-balancers my_load_balancer_name --health-check-type ELB --grace-period 300
		CreateAutoScalingGroupRequest autoScalingGroupRequest = new CreateAutoScalingGroupRequest();

		// group name
		autoScalingGroupRequest.setAutoScalingGroupName(autoScalingParameters
				.getAutoScalingGroupName());
		// launch config name
		autoScalingGroupRequest
				.setLaunchConfigurationName(autoScalingParameters
						.getLaunchConfigurationName());

		// zone to be deployed
		Collection<String> availabilityZones = Arrays
				.asList(new String[] { autoScalingParameters
						.getAvailabilityZone() });
		autoScalingGroupRequest.setAvailabilityZones(availabilityZones);

		// minimum number of running instances
		autoScalingGroupRequest.setMinSize(autoScalingParameters
				.getAutoScalingGroupMin());

		// maximum number of running instances
		autoScalingGroupRequest.setMaxSize(autoScalingParameters
				.getAutoScalingGroupMax());

		// amount of time, in seconds, after a scaling activity completes before any further trigger-related scaling activities can start
		autoScalingGroupRequest.setDefaultCooldown(autoScalingParameters
				.getAutoScalingGroupCoolDown());

		// loadbalance to be used
		Collection<String> loadBalancerNames = Arrays
				.asList(new String[] { autoScalingParameters.getElbName() });
		autoScalingGroupRequest.setLoadBalancerNames(loadBalancerNames);

		autoScaling.createAutoScalingGroup(autoScalingGroupRequest);
	}

	private PutScalingPolicyResult putScalingPolicy() {
		AmazonAutoScalingClient autoScaling = autoScalingParameters
				.getAmazonAutoScalingClient();

		// as-put-scaling-policy ScaleUp --auto-scaling-group my_autoscale_group --adjustment=1 --type ChangeInCapacity
		PutScalingPolicyRequest putScalingPolicyRequest = new PutScalingPolicyRequest();

		putScalingPolicyRequest.setPolicyName(autoScalingParameters
				.getPolicyName());

		putScalingPolicyRequest.setScalingAdjustment(autoScalingParameters
				.getPolicyScalingAdjustment());

		putScalingPolicyRequest.setAdjustmentType(autoScalingParameters
				.getPolicyAdjustmentType());

		putScalingPolicyRequest.setCooldown(autoScalingParameters
				.getAutoScalingGroupCoolDown());

		putScalingPolicyRequest.setAutoScalingGroupName(autoScalingParameters
				.getAutoScalingGroupName());

		return autoScaling.putScalingPolicy(putScalingPolicyRequest);
	}

	private void putAlarmRequest(PutScalingPolicyResult putScalingPolicyResult) {
		AmazonCloudWatchClient cloudWatch = autoScalingParameters
				.getAmazonCloudWatchClient();

		PutMetricAlarmRequest putMetricAlarmRequest = new PutMetricAlarmRequest();

		putMetricAlarmRequest.setAlarmName(autoScalingParameters
				.getAlarmMetricName());

		putMetricAlarmRequest.setComparisonOperator(autoScalingParameters
				.getAlarmComparisonOperator());

		putMetricAlarmRequest.setEvaluationPeriods(autoScalingParameters
				.getAlarmEvaluationPeriods());

		putMetricAlarmRequest.setMetricName(autoScalingParameters
				.getAlarmMetricName());

		putMetricAlarmRequest.setNamespace(autoScalingParameters
				.getAlarmNamespace());

		putMetricAlarmRequest.setPeriod(autoScalingParameters.getAlarmPeriod());

		putMetricAlarmRequest.setStatistic(autoScalingParameters
				.getAlarmStatistic());

		putMetricAlarmRequest.setThreshold(autoScalingParameters
				.getAlarmThreshold());

		Collection<String> alarmActions = Arrays
				.asList(new String[] { putScalingPolicyResult.getPolicyARN() });

		putMetricAlarmRequest.setAlarmActions(alarmActions);

		Dimension autoScalingGroupDimesion = new Dimension();
		autoScalingGroupDimesion.setName(autoScalingParameters
				.getAlarmGroupDimesionName());

		autoScalingGroupDimesion.setValue(autoScalingParameters
				.getAutoScalingGroupName());

		Collection<Dimension> dimensions = Arrays
				.asList(new Dimension[] { autoScalingGroupDimesion });

		putMetricAlarmRequest.setDimensions(dimensions);

		/* mon-put-metric-alarm AlarmName  --comparison-operator  value  --evaluation-periods  value
		--metric-name  value  --namespace  value  --period  value  --statistic 
		value  --threshold  value [--actions-enabled  value ] [--alarm-actions 
		value[,value...] ] [--alarm-description  value ] [--dimensions 
		"key1=value1,key2=value2..." ] [--ok-actions  value[,value...] ] [--unit 
		value ] [--insufficient-data-actions  value[,value...] ]  [General Options]
		*/
		
		cloudWatch.putMetricAlarm(putMetricAlarmRequest);
	}

	public void scale() {
		createLaunchConfiguration();
		createAutoScalingGroup();
		PutScalingPolicyResult putScalingPolicyResult = putScalingPolicy();
		putAlarmRequest(putScalingPolicyResult);
	}
}
