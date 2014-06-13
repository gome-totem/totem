package org.x.cloud.epub;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * <pre>
 * 需要配置host
 * sit	10.57.4.13 		S1TA05
 * uat	10.58.13.21		atg-uat-buildserver-vm
 * prelive	10.58.8.23	S1PA15
 * live	10.58.22.17		S1PA57
 * </pre>
 * 
 * @author 蒋礼俊
 * @version 2013-6-2 上午10:48:10
 */
public class RmiRositoryImpl {

	public static final String local = "10.57.41.199:8860";
	public static final String sit = "10.126.53.119:8860";
	public static final String uat = "10.126.45.39:8860";
	public static final String prelive = "10.58.44.51:8862";
	public static final String live = "10.58.22.17:8860";

	private String serverIp;
	private String rmiComponent = "/com/epub/RmiRositoryImpl";
	private String targetComponent = "/com/gome/cloud/index/IndexManager";
	private String currentEnv;

	public void init() {
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getRmiComponent() {
		return rmiComponent;
	}

	public void setRmiComponent(String rmiComponent) {
		this.rmiComponent = rmiComponent;
	}

	public String getTargetComponent() {
		return targetComponent;
	}

	public void setTargetComponent(String targetComponent) {
		this.targetComponent = targetComponent;
	}

	public String getCurrentEnv() {
		return currentEnv;
	}

	public void setCurrentEnv(String currentEnv) {
		this.currentEnv = currentEnv;
		if (currentEnv.equalsIgnoreCase("local")) {
			setServerIp(local);
		} else if (currentEnv.equalsIgnoreCase("sit")) {
			setServerIp(sit);
		} else if (currentEnv.equalsIgnoreCase("uat")) {
			setServerIp(uat);
		} else if (currentEnv.equalsIgnoreCase("prelive")) {
			setServerIp(prelive);
		} else if (currentEnv.equalsIgnoreCase("live")) {
			setServerIp(live);
		} else {
			System.exit(-1);
		}
	}

	public Object getObj(String pUrl, String pCompent) throws RemoteException, NotBoundException, IOException {
		Object o = null;
		try {
			o = (Object) Naming.lookup("rmi://" + pUrl + pCompent);
			if (o != null)
				return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return o;
	}

	public void invokeFiled(String propertiesName, Object proValue) {
		try {
			IRmiRository ormi = (IRmiRository) this.getObj(this.getServerIp(), getRmiComponent());
			ormi.oFiles(targetComponent, propertiesName, proValue);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void invokeMethod(String methodName) {
		try {
			IRmiRository ormi = (IRmiRository) this.getObj(this.getServerIp(), getRmiComponent());
			ormi.oMethod(targetComponent, methodName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object invokeMethod(String methodName, Class<?>[] parameterTypes, Object[] params) {
		try {
			IRmiRository ormi = (IRmiRository) this.getObj(this.getServerIp(), getRmiComponent());
			return ormi.oMethodReturnValue(targetComponent, methodName, parameterTypes, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		int server = 1;
		RmiRositoryImpl ri = new RmiRositoryImpl();
		switch (server) {
		case 1:
			ri.setServerIp(sit);
			break;
		case 2:
			ri.setServerIp(uat);
			break;
		case 3:
			ri.setServerIp(prelive);
			break;
		case 4:
			ri.setServerIp(live);
			break;
		case 5:
			ri.setServerIp(local);
			break;
		}
		ri.setRmiComponent("/com/epub/RmiRositoryImpl");
		ri.setTargetComponent("/com/gome/cloud/index/IndexManager");
		ri.invokeFiled("loggingDebug", "true");
		ri.invokeFiled("loggingError", "true");
		ri.invokeFiled("loggingInfo", "true");
		int mode = 3;
		switch (mode) {
		case 1:
			ri.invokeMethod("doStartService");
			break;
		case 2:
			ri.invokeMethod("startFullCategory");
			break;
		case 3:
			ri.invokeFiled("threadSize", "50");
			ri.invokeMethod("startFullProduct");
			break;
		case 4:
			Object object = ri.invokeMethod("rmiIncrement", new Class[] { String.class, String.class, boolean.class }, new Object[] { "product", "A0000821930", true });
			System.out.println(object);
			break;
		}

	}

}
