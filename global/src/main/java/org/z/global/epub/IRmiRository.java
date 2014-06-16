package org.z.global.epub;

import java.rmi.Remote;

public interface IRmiRository extends Remote {

	public void oFiles(String componentQualifiedName, String propertiesName, Object propertiesValue) throws Exception;

	public void oMethod(String componentQualifiedName, String methodName) throws Exception;

	public Object oMethodReturnValue(String componentQualifiedName, String methodName, Class<?>[] parameterTypes, Object[] params) throws Exception;

}
