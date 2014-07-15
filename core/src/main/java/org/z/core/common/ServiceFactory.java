package org.z.core.common;


import org.z.core.interfaces.ServiceIntf;
import org.z.core.module.ModuleActivity;
import org.z.core.module.ModuleAppDict;
import org.z.core.module.ModuleBill;
import org.z.core.module.ModuleComment;
import org.z.core.module.ModuleImage;
import org.z.core.module.ModuleMail;
import org.z.core.module.ModuleRecommend;
import org.z.core.module.ModuleSMS;
import org.z.core.module.ModuleUser;
import org.z.global.dict.Global.BizType;
import org.z.global.environment.Business.ClassName;
import org.z.global.factory.ModuleFactory;
import org.z.global.interfaces.ModuleIntf;

import com.mongodb.BasicDBList;

public class ServiceFactory extends ModuleFactory {

	public static ServiceIntf byClass(ClassName className, BasicDBList classParams) {
		switch (className) {
		case Bill:
			return bill();
//		case Module:
//			String moduleName = classParams.get(0).toString();
//			return (ServiceIntf) ServiceFactory.moduleInstanceBy(moduleName);
		default:
			break;
		}
		return null;
	}


	public static ModuleAppDict appDict() {
		ModuleIntf instance = moduleInstanceBy("appdict");
		return (ModuleAppDict) instance;
	}


	public static ModuleComment comment() {
		ModuleIntf instance = moduleInstanceBy("comment");
		return (ModuleComment) instance;
	}

	public static ModuleRecommend recommend() {
		ModuleIntf instance = moduleInstanceBy("recommend");
		return (ModuleRecommend) instance;
	}

	public static ModuleActivity activity() {
		ModuleIntf instance = moduleInstanceBy("activity");
		return (ModuleActivity) instance;
	}

	public static ModuleUser user() {
		ModuleIntf instance = moduleInstanceBy("user");
		return (ModuleUser) instance;
	}

	public static ModuleImage image() {
		ModuleIntf instance = moduleInstanceBy("image");
		return (ModuleImage) instance;
	}

	public static ModuleMail mail() {
		ModuleIntf instance = moduleInstanceBy("mail");
		return (ModuleMail) instance;
	}

	public static ModuleSMS sms() {
		ModuleIntf instance = moduleInstanceBy("sms");
		return (ModuleSMS) instance;
	}

	public static ModuleBill bill() {
		ModuleIntf instance = moduleInstanceBy("bill");
		return (ModuleBill) instance;
	}


	public static ModuleActivity activityBy(int type) {
		BizType bType = BizType.values()[type];
		ModuleIntf instance = moduleInstanceBy(bType.name());
		return (ModuleActivity) instance;
	}


}
