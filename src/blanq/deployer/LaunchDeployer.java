package blanq.deployer;

import blanq.parameters.DeployParameters;

public class LaunchDeployer {
	public static void main(String[] args) {
		DeployParameters deployParameters = DeployParameters.getInstance();
		Deployer deployer = new Deployer(deployParameters);
		deployer.deploy();
	}
}
