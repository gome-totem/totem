package org.z.web.common;

import org.z.global.dict.Global.BizType;
import org.z.global.environment.Business.ClassName;
import org.z.global.factory.ModuleFactory;
import org.z.global.interfaces.ModuleIntf;
import org.z.web.interfaces.ServiceIntf;

import com.mongodb.BasicDBList;

public class ServiceFactory extends ModuleFactory {

	public static ServiceIntf byClass(ClassName className, BasicDBList classParams) {
		switch (className) {
//		case Bill:
//			return bill();
//		case Call:
//			return call();
//		case Lookup:
//			return lookup();
//		case Recommend:
//			return recommend();
//		case User:
//			return user();
//		case Product:
//			return product();
//		case Talk:
//			return talk();
//		case Sync:
//			break;
//		case Topic:
//			return topic();
//		case Activity:
//			int bizType = Integer.parseInt(classParams.get(0).toString());
//			if (bizType == 0)
//				return activity();
//			return activityBy(bizType);
//		case Appoint:
//			return appoint();
//		case Booking:
//			return booking();
//		case Calendar:
//			return calendar();
//		case Comment:
//			return comment();
//		case DataTable:
//			break;
//		case Order:
//			return order();
//		case Tuiba:
//			return push();
//		case SMS:
//			return sms();
//		case Root:
//			return root();
//		case TripOrder:
//			return tripOrder();
//		case AirTicketOrder:
//			return airTicketOrder();
//		case Module:
//			String moduleName = classParams.get(0).toString();
//			return (ServiceIntf) ServiceFactory.moduleInstanceBy(moduleName);
		default:
			break;
		}
		return null;
	}

//	public static ModuleTripOrder tripOrder() {
//		return (ModuleTripOrder) moduleInstanceBy("triporder");
//	}
//	private static ModuleAirTicket airTicketOrder() {
//		return (ModuleAirTicket) moduleInstanceBy("airticket");
//	}
//
//	public static ModulePush push() {
//		ModuleIntf instance = moduleInstanceBy("push");
//		return (ModulePush) instance;
//	}
//
//	public static ModuleAppDict appDict() {
//		ModuleIntf instance = moduleInstanceBy("appdict");
//		return (ModuleAppDict) instance;
//	}
//
//	public static ModuleTopic topic() {
//		ModuleIntf instance = moduleInstanceBy("topic");
//		return (ModuleTopic) instance;
//	}
//
//	public static ModuleComment comment() {
//		ModuleIntf instance = moduleInstanceBy("comment");
//		return (ModuleComment) instance;
//	}
//
//	public static ModuleRecommend recommend() {
//		ModuleIntf instance = moduleInstanceBy("recommend");
//		return (ModuleRecommend) instance;
//	}
//
//	public static ModuleActivity activity() {
//		ModuleIntf instance = moduleInstanceBy("activity");
//		return (ModuleActivity) instance;
//	}
//
//	public static ModuleUser user() {
//		ModuleIntf instance = moduleInstanceBy("user");
//		return (ModuleUser) instance;
//	}
//
//	public static ModuleImage image() {
//		ModuleIntf instance = moduleInstanceBy("image");
//		return (ModuleImage) instance;
//	}
//
//	public static ModuleMail mail() {
//		ModuleIntf instance = moduleInstanceBy("mail");
//		return (ModuleMail) instance;
//	}
//
//	public static ModuleSMS sms() {
//		ModuleIntf instance = moduleInstanceBy("sms");
//		return (ModuleSMS) instance;
//	}
//
//	public static ModuleRoot root() {
//		ModuleIntf instance = moduleInstanceBy("root");
//		return (ModuleRoot) instance;
//	}
//
//	public static ModuleTag product() {
//		ModuleIntf instance = moduleInstanceBy("product");
//		return (ModuleTag) instance;
//	}
//
//	public static ModuleTalk talk() {
//		ModuleIntf instance = moduleInstanceBy("talk");
//		return (ModuleTalk) instance;
//	}
//
//	public static ModuleTag tag() {
//		ModuleIntf instance = moduleInstanceBy("tag");
//		return (ModuleTag) instance;
//	}
//
//	public static ModuleCall call() {
//		ModuleIntf instance = moduleInstanceBy("call");
//		return (ModuleCall) instance;
//	}
//
//	public static ModuleBill bill() {
//		ModuleIntf instance = moduleInstanceBy("bill");
//		return (ModuleBill) instance;
//	}
//
//	public static ModuleAppoint appoint() {
//		ModuleIntf instance = moduleInstanceBy("appoint");
//		return (ModuleAppoint) instance;
//	}
//
//	public static ModuleAppoint booking() {
//		ModuleIntf instance = moduleInstanceBy("booking");
//		return (ModuleAppoint) instance;
//	}
//
//	public static ModuleActivity activityBy(int type) {
//		BizType bType = BizType.values()[type];
//		ModuleIntf instance = moduleInstanceBy(bType.name());
//		return (ModuleActivity) instance;
//	}
//
//	public static ModuleBooking order() {
//		ModuleIntf instance = moduleInstanceBy("order");
//		return (ModuleBooking) instance;
//	}
//
//	public static ModuleLookup lookup() {
//		ModuleIntf instance = moduleInstanceBy("lookup");
//		return (ModuleLookup) instance;
//	}
//
//	public static ModuleCalendar calendar() {
//		ModuleIntf instance = moduleInstanceBy("calendar");
//		return (ModuleCalendar) instance;
//	}

}
