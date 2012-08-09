package blanq.autoscaling;

import blanq.parameters.AutoScalingParameters;

public class DeleteAutoScaling {

	public static void main(String[] args) {
		AutoScalingParameters autoScalingParameters = AutoScalingParameters
				.getInstance();
		AutoScaling autoScaling = new AutoScaling(autoScalingParameters);
		autoScaling.delete();
	}
}
