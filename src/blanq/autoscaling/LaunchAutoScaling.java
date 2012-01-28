package blanq.autoscaling;

import blanq.parameters.AutoScalingParameters;

/*
 * git push -u origin production
 */

public class LaunchAutoScaling {

	public static void main(String[] args) {
		AutoScalingParameters autoScalingParameters = AutoScalingParameters
				.getInstance();

		AutoScaling autoScaling = new AutoScaling(autoScalingParameters);
//		autoScaling.scale();
		autoScaling.delete();
	}

}
