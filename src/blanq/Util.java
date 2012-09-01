package blanq;

import org.apache.commons.logging.LogFactory;

public class Util {

	public static void sleepFor(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException ie) {
			System.err.println("Erro ao aguardar thread: " + ie.getMessage());
			System.exit(1);
		}
	}

	public static void configureLogging() {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}

}
