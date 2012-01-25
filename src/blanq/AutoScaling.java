package blanq;

import java.util.Arrays;
import java.util.Collection;

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

	private DeployParameters deployParameters;

	public AutoScaling(DeployParameters deployParameters) {
		this.deployParameters = deployParameters;
	}

	// Dividir em métodos o código macarronico

	private void deletePreviousScale(AmazonAutoScalingClient autoScaling,
			AmazonCloudWatchClient cloudWatch) {
		try {
			DeleteAlarmsRequest deleteAlarmsRequest = new DeleteAlarmsRequest();
			Collection<String> alarmNames = Arrays
					.asList(new String[] { this.deployParameters
							.getMetricAlarmName() });
			deleteAlarmsRequest.setAlarmNames(alarmNames);
			cloudWatch.deleteAlarms(deleteAlarmsRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}

		try {
			DeletePolicyRequest deletePolicyRequest = new DeletePolicyRequest();
			deletePolicyRequest.setPolicyName(this.deployParameters
					.getPolicyName());
			deletePolicyRequest.setAutoScalingGroupName(this.deployParameters
					.getAutoScalingGroupName());
			autoScaling.deletePolicy(deletePolicyRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}

		try {
			DeleteAutoScalingGroupRequest deleteAutoScalingGroupRequest = new DeleteAutoScalingGroupRequest();
			deleteAutoScalingGroupRequest
					.setAutoScalingGroupName(this.deployParameters
							.getAutoScalingGroupName());
			deleteAutoScalingGroupRequest.setForceDelete(true);
			autoScaling.deleteAutoScalingGroup(deleteAutoScalingGroupRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
		try {
			DeleteLaunchConfigurationRequest deleteLaunchConfigurationRequest = new DeleteLaunchConfigurationRequest();
			deleteLaunchConfigurationRequest
					.setLaunchConfigurationName(this.deployParameters
							.getLaunchConfigurationName());
			autoScaling
					.deleteLaunchConfiguration(deleteLaunchConfigurationRequest);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
		}
	}

	// Dividir em métodos o código macarronico
	public void scale() {

		// http://www.caseylabs.com/how-to-setup-auto-scaling-on-amazon-ec2
		AmazonAutoScalingClient autoScaling = this.deployParameters
				.getAmazonAutoScalingClient();

		AmazonCloudWatchClient cloudWatch = this.deployParameters
				.getAmazonCloudWatchClient();

		deletePreviousScale(autoScaling, cloudWatch);

		// aguarda para garantir que foram apagadas com sucesso (2 = magic number)
		Util.sleepFor(2);

		// as-create-launch-config my_autoscale_config --image-id ami-XXXXXXXX --instance-type m1.small --group "My Security Group Name"
		CreateLaunchConfigurationRequest createLaunchConfigurationRequest = new CreateLaunchConfigurationRequest();
		createLaunchConfigurationRequest.setImageId(this.deployParameters
				.getAmi());
		createLaunchConfigurationRequest.setInstanceType(this.deployParameters
				.getInstanceType());
		Collection<String> securityGroups = Arrays
				.asList(new String[] { this.deployParameters.getSecurityGroup() });
		createLaunchConfigurationRequest.setSecurityGroups(securityGroups);
		createLaunchConfigurationRequest.setKeyName(this.deployParameters
				.getKeyPair());
		createLaunchConfigurationRequest
				.setLaunchConfigurationName(this.deployParameters
						.getLaunchConfigurationName());
		createLaunchConfigurationRequest.setUserData(this.deployParameters
				.getUserData());
		autoScaling.createLaunchConfiguration(createLaunchConfigurationRequest);

		// as-create-auto-scaling-group my_autoscale_group --availability-zones us-east-1X --launch-configuration my_autoscale_config --min-size 1 --max-size 3 --load-balancers my_load_balancer_name --health-check-type ELB --grace-period 300
		int autoScalingGroupMin = 2;
		int autoScalingGroupMax = 4;
		int autoScalingGroupCollDown = 600;
		CreateAutoScalingGroupRequest createAutoScalingGroupRequest = new CreateAutoScalingGroupRequest();
		createAutoScalingGroupRequest
				.setAutoScalingGroupName(this.deployParameters
						.getAutoScalingGroupName());
		createAutoScalingGroupRequest
				.setLaunchConfigurationName(this.deployParameters
						.getLaunchConfigurationName());
		Collection<String> availabilityZones = Arrays
				.asList(new String[] { this.deployParameters
						.getAvailabilityZone() });
		createAutoScalingGroupRequest.setAvailabilityZones(availabilityZones);
		createAutoScalingGroupRequest.setMinSize(autoScalingGroupMin);
		createAutoScalingGroupRequest.setMaxSize(autoScalingGroupMax);
		createAutoScalingGroupRequest
				.setDefaultCooldown(autoScalingGroupCollDown);
		Collection<String> loadBalancerNames = Arrays
				.asList(new String[] { this.deployParameters.getElbName() });
		createAutoScalingGroupRequest.setLoadBalancerNames(loadBalancerNames);
		autoScaling.createAutoScalingGroup(createAutoScalingGroupRequest);

		// as-put-scaling-policy ScaleUp --auto-scaling-group my_autoscale_group --adjustment=1 --type ChangeInCapacity
		PutScalingPolicyRequest putScalingPolicyRequest = new PutScalingPolicyRequest();
		putScalingPolicyRequest.setPolicyName(this.deployParameters
				.getPolicyName());
		putScalingPolicyRequest.setScalingAdjustment(1);
		putScalingPolicyRequest.setAdjustmentType("ChangeInCapacity");
		putScalingPolicyRequest.setCooldown(600);
		putScalingPolicyRequest.setAutoScalingGroupName(this.deployParameters
				.getAutoScalingGroupName());
		PutScalingPolicyResult putScalingPolicyResult = autoScaling
				.putScalingPolicy(putScalingPolicyRequest);

		PutMetricAlarmRequest putMetricAlarmRequest = new PutMetricAlarmRequest();
		putMetricAlarmRequest.setAlarmName(this.deployParameters
				.getMetricAlarmName());
		Collection<String> alarmActions = Arrays
				.asList(new String[] { putScalingPolicyResult.getPolicyARN() });
		putMetricAlarmRequest.setAlarmActions(alarmActions);
		putMetricAlarmRequest.setMetricName("Latency");
		putMetricAlarmRequest.setNamespace("AWS/ELB");
		putMetricAlarmRequest.setStatistic("Average");
		putMetricAlarmRequest.setPeriod(300); /* 5 minutos */
		putMetricAlarmRequest.setEvaluationPeriods(1);
		putMetricAlarmRequest.setThreshold(4.0);
		/* http://docs.amazonwebservices.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/cloudwatch/model/PutMetricAlarmRequest.html#getComparisonOperator() */
		putMetricAlarmRequest
				.setComparisonOperator("GreaterThanOrEqualToThreshold");
		Dimension autoScalingGroupDimesion = new Dimension();
		autoScalingGroupDimesion.setName("AutoScalingGroupName");
		autoScalingGroupDimesion.setValue(this.deployParameters
				.getAutoScalingGroupName());
		Collection<Dimension> dimensions = Arrays
				.asList(new Dimension[] { autoScalingGroupDimesion });
		putMetricAlarmRequest.setDimensions(dimensions);
	}
}
