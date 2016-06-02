package bacnet;

import org.dsa.iot.dslink.util.json.JsonArray;
import org.dsa.iot.dslink.util.json.JsonObject;

import com.serotonin.bacnet4j.*;
import com.serotonin.bacnet4j.base.BACnetUtils;
import com.serotonin.bacnet4j.enums.DayOfWeek;
import com.serotonin.bacnet4j.enums.Month;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.CalendarEntry;
import com.serotonin.bacnet4j.type.constructed.DailySchedule;
import com.serotonin.bacnet4j.type.constructed.DateRange;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.SpecialEvent;
import com.serotonin.bacnet4j.type.constructed.TimeValue;
import com.serotonin.bacnet4j.type.constructed.WeekNDay;
import com.serotonin.bacnet4j.type.constructed.WeekNDay.WeekOfMonth;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.BitString;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Date;
import com.serotonin.bacnet4j.type.primitive.Enumerated;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.OctetString;
import com.serotonin.bacnet4j.type.primitive.Primitive;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.SignedInteger;
import com.serotonin.bacnet4j.type.primitive.Time;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

/**
 * @author Samuel Grenier
 */
public class Utils {

    public static Address toAddress(int netNum, String mac) {
        mac = mac.trim();
        int colon = mac.indexOf(":");
        if (colon == -1) {
            OctetString os = new OctetString(BACnetUtils.dottedStringToBytes(mac));
            return new Address(netNum, os);
        } else {
            byte[] ip = BACnetUtils.dottedStringToBytes(mac.substring(0, colon));
            int port = Integer.parseInt(mac.substring(colon + 1));
            return IpNetworkUtils.toAddress(netNum, ip, port);
        }
    }

    /**
     * Returns the IP or MSTP mac for the given device.
     */
    public static String getMac(RemoteDevice remoteDevice) {
        try {
            return IpNetworkUtils.toIpPortString(remoteDevice.getAddress().getMacAddress());
        } catch (IllegalArgumentException ignore) {}
        return Byte.toString(remoteDevice.getAddress().getMacAddress().getBytes()[0]);
    }
    
    
    public static String datetimeToString(DateTime dt) {
    	String dat = dateToString(dt.getDate());
    	String tim = unparseTime(dt.getTime());
    	if (tim.equals("Unspecified")) return dat;
    	return dat + "T" + tim;
    }
    
    public static String dateToString(Date date) {
    	String str = (date.getYear() != Date.UNSPECIFIED_YEAR) ? Integer.toString(date.getCenturyYear()) : "????";
	    Month mon = date.getMonth();
	    if (mon != Month.UNSPECIFIED) {
	    	int id = mon.getId();
	    	str += (id < 10) ? "-0" + Integer.toString(id) : "-" + Integer.toString(id);
	    } else {
	    	str += "-??";
	    }
	    int day = date.getDay();
	    if (day != Date.UNSPECIFIED_DAY) {
	    	str += (day < 10) ? "-0" + Integer.toString(day) : "-" + Integer.toString(day);
	    } else {
	    	str += "-??";
	    }
	    return str;
    }
    
    public static Date dateFromString(String str) {
    	int yr; Month mon; int day;
    	try {
    		yr = Integer.parseInt(str.substring(0, 4));
    	} catch (NumberFormatException e) {
    		yr = Date.UNSPECIFIED_YEAR;
    	}
    	try {
    		mon = Month.valueOf(Integer.parseInt(str.substring(5, 7)));
    	} catch (NumberFormatException e) {
    		mon = Month.UNSPECIFIED;
    	}
    	try {
    		day = Integer.parseInt(str.substring(8, 10));
    	} catch (NumberFormatException e) {
    		day = Date.UNSPECIFIED_DAY;
    	}
    	return new Date(yr, mon, day, null);
    	
    }
    
    public static String unparseTime(Time time) {
    	if (time.isHourUnspecified()) return "Unspecified";
    	String str = "";
    	String part = ("00" + Integer.toString(time.getHour()));
    	str += part.substring(part.length() - 2) + ":";
    	part = (time.isMinuteUnspecified()) ? "000" : ("00" + Integer.toString(time.getMinute()));
    	str += part.substring(part.length() - 2) + ":";
    	part = (time.isSecondUnspecified()) ? "000" : ("00" + Integer.toString(time.getSecond()));
    	str += part.substring(part.length() - 2) + ".";
    	part = (time.isHundredthUnspecified()) ? "0000" : (Integer.toString(time.getHundredth()) + "000");
    	str += part.substring(0, 3);
    	return str;
    	
    }
    
    public static Time parseTime(String str) {
    	int hr = Integer.parseInt(str.substring(0, 2));
    	int min = Integer.parseInt(str.substring(3, 5));
    	int sec = Integer.parseInt(str.substring(6, 8));
    	int hund = Integer.parseInt(str.substring(9, 11));
    	return new Time(hr, min, sec, hund);
    }
    
    public static Object interpretPrimitive(Primitive p) {
    	if (p instanceof com.serotonin.bacnet4j.type.primitive.Boolean) return ((com.serotonin.bacnet4j.type.primitive.Boolean) p).booleanValue();
    	else if (p instanceof Real) return ((Real) p).floatValue();
    	else if (p instanceof SignedInteger) return ((SignedInteger) p).intValue();
    	else if (p instanceof com.serotonin.bacnet4j.type.primitive.Double) return ((com.serotonin.bacnet4j.type.primitive.Double) p).doubleValue();
    	else if (p instanceof Enumerated) return ((Enumerated) p).intValue();
    	else if (p instanceof UnsignedInteger) return ((UnsignedInteger) p).intValue();
    	else if (p instanceof Null) return null;
    	else if (p instanceof Date) return dateToString((Date) p);
    	else if (p instanceof Time) return unparseTime((Time) p);
    	else return p.toString();
    }
    
    public static Primitive parsePrimitive(Object o, byte typeid) {
    	if (o == null) return new Null();
    	if (o instanceof Boolean) return new com.serotonin.bacnet4j.type.primitive.Boolean(((Boolean) o).booleanValue());
    	if (o instanceof Number) {
    		if (typeid == SignedInteger.TYPE_ID) return new SignedInteger(((Number) o).intValue());
    		if (typeid == UnsignedInteger.TYPE_ID) return new UnsignedInteger(((Number) o).intValue());
    		if (typeid == Enumerated.TYPE_ID) return new Enumerated(((Number) o).intValue());
    		if (typeid == Real.TYPE_ID) return new Real(((Number) o).floatValue());
    		return new com.serotonin.bacnet4j.type.primitive.Double(((Number) o).doubleValue());
    	}
    	if (o instanceof String) {
    		if (typeid == Date.TYPE_ID) return dateFromString((String) o);
    		if (typeid == Time.TYPE_ID) return parseTime((String) o);
    		if (typeid == ObjectIdentifier.TYPE_ID) return oidFromString((String) o);
    		if (typeid == OctetString.TYPE_ID) return octetStrFromString((String) o);
    		if (typeid == BitString.TYPE_ID) return bitStrFromString((String) o);
    		return new CharacterString((String) o);
    	}
    	return null;
    }
    
    public static byte getPrimitiveType(Primitive p) {
    	if (p instanceof com.serotonin.bacnet4j.type.primitive.Boolean) return com.serotonin.bacnet4j.type.primitive.Boolean.TYPE_ID;
    	else if (p instanceof Real) return Real.TYPE_ID;
    	else if (p instanceof SignedInteger) return SignedInteger.TYPE_ID;
    	else if (p instanceof com.serotonin.bacnet4j.type.primitive.Double) return com.serotonin.bacnet4j.type.primitive.Double.TYPE_ID;
    	else if (p instanceof Enumerated) return Enumerated.TYPE_ID;
    	else if (p instanceof UnsignedInteger) return UnsignedInteger.TYPE_ID;
    	else if (p instanceof Null) return Null.TYPE_ID;
    	else if (p instanceof Date) return Date.TYPE_ID;
    	else if (p instanceof Time) return Time.TYPE_ID;
    	else if (p instanceof ObjectIdentifier) return ObjectIdentifier.TYPE_ID;
    	else if (p instanceof OctetString) return OctetString.TYPE_ID;
    	else if (p instanceof BitString) return BitString.TYPE_ID;
    	else return CharacterString.TYPE_ID;
    }
    
    public static ObjectIdentifier oidFromString(String str) {
    	String[] a = str.split(" ");
    	String numstr = a[a.length-1];
    	int num = Integer.parseInt(numstr);
    	ObjectType ot = DeviceFolder.parseObjectType(str.substring(0, str.length() - numstr.length() - 1));
    	return new ObjectIdentifier(ot, num);
    }
    
    public static OctetString octetStrFromString(String str) {
    	str = str.trim().substring(1, str.length()-1);
    	String[] strings = str.split(",");
    	byte[] bytes = new byte[strings.length];
    	for (int i=0; i<strings.length; i++) {
    		String s = strings[i];
    		bytes[i] = Byte.parseByte(s.trim());
    	}
    	return new OctetString(bytes);
    }
    
    public static BitString bitStrFromString(String str) {
    	str = str.trim().substring(1, str.length()-1);
    	String[] strings = str.split(",");
    	boolean[] bools = new boolean[strings.length];
    	for (int i=0; i<strings.length; i++) {
    		String s = strings[i];
    		bools[i] = Boolean.parseBoolean(s.trim());
    	}
    	return new BitString(bools);
    }
    
    public static JsonObject dateToJson(Date date) {
    	JsonObject dateObj = new JsonObject();
    	if (date.getYear() != Date.UNSPECIFIED_YEAR) dateObj.put("Year", date.getCenturyYear());
		if (date.getMonth() != Month.UNSPECIFIED) dateObj.put("Month", date.getMonth().toString());
		if (date.getDay() != Date.UNSPECIFIED_DAY) dateObj.put("Day", date.getDay());
		if (date.getDayOfWeek() != DayOfWeek.UNSPECIFIED) dateObj.put("Day of Week", date.getDayOfWeek().toString());
		return dateObj;
    }
    
    public static Date dateFromJson(JsonObject jobj) {
		Object y = jobj.get("Year");
		Object m = jobj.get("Month");
		Object d = jobj.get("Day");
		Object w = jobj.get("Day of Week");
		int yr; Month mon; int day; DayOfWeek dow;
		yr = (y instanceof Number) ? ((Number) y).intValue() : Date.UNSPECIFIED_YEAR;
		mon = (m instanceof String) ? Month.valueOf(((String) m)) : Month.UNSPECIFIED;
		day = (d instanceof Number) ? ((Number) d).intValue() : Date.UNSPECIFIED_DAY;
		dow = (w instanceof String) ? DayOfWeek.valueOf((String) w) : DayOfWeek.UNSPECIFIED;
		return new Date(yr, mon, day, dow);
	}
    
    public static JsonObject dateRangeToJson(DateRange dr) {
    	JsonObject jo = new JsonObject();
    	jo.put("Start Date", dateToJson(dr.getStartDate()));
    	jo.put("End Date", dateToJson(dr.getEndDate()));
    	return jo;
    }
    
    public static DateRange dateRangeFromJson(JsonObject jobj) {
		Object sobj = jobj.get("Start Date");
		Object eobj = jobj.get("End Date");
		if (sobj instanceof JsonObject && eobj instanceof JsonObject) {
			Date start = dateFromJson((JsonObject) sobj);
			Date end = dateFromJson((JsonObject) eobj);
			return new DateRange(start, end);
		}
		return null;
	}
    
    public static JsonObject weekNDayToJson(WeekNDay wd) {
    	JsonObject jo = new JsonObject();
    	if (wd.getMonth() != Month.UNSPECIFIED) jo.put("Month", wd.getMonth().toString());
    	if (wd.getWeekOfMonth() != WeekOfMonth.any) jo.put("Week of Month", wd.getWeekOfMonth().intValue());
    	if (wd.getDayOfWeek() != DayOfWeek.UNSPECIFIED) jo.put("Day of Week", wd.getDayOfWeek().toString());
    	return jo;
    }
    
    public static WeekNDay weekNDayFromJson(JsonObject jobj) {
		Object m = jobj.get("Month");
		Object w = jobj.get("Week of Month");
		Object d = jobj.get("Day of Week");
		Month mon; WeekOfMonth week; DayOfWeek day;
		mon = (m instanceof String) ? Month.valueOf(((String) m)) : Month.UNSPECIFIED;
		week = (w instanceof Number) ? WeekOfMonth.valueOf(((Number) w).byteValue()) : WeekOfMonth.any;
		day = (d instanceof String) ? DayOfWeek.valueOf((String) w) : DayOfWeek.UNSPECIFIED;
		return new WeekNDay(mon, week, day);
	}

	public static JsonObject calendarEntryToJson(CalendarEntry ce) {
		JsonObject jo = new JsonObject();
		if (ce.isDate()) {
			jo.put("Date", dateToJson(ce.getDate()));
		} else if (ce.isDateRange()) {
			jo.put("Date Range", dateRangeToJson(ce.getDateRange()));
		} else if (ce.isWeekNDay()) {
			jo.put("Week and Day", weekNDayToJson(ce.getWeekNDay()));
		}
		return jo;
	}
	
	public static CalendarEntry calendarEntryFromJson(JsonObject jobj) {
		Object dateobj = jobj.get("Date");
		if (dateobj instanceof JsonObject) {
			Date date = dateFromJson((JsonObject) dateobj);
			return new CalendarEntry(date);
		}
		Object rangeobj = jobj.get("Date Range");
		if (rangeobj instanceof JsonObject) {
			DateRange range = dateRangeFromJson((JsonObject) rangeobj);
			if (range != null) return new CalendarEntry(range);
		}
		Object wdobj = jobj.get("Week and Day");
		if (wdobj instanceof JsonObject) {
			WeekNDay wd = weekNDayFromJson((JsonObject) wdobj);
			return new CalendarEntry(wd);
		}
		return null;
	}

	public static JsonObject timeValueToJson(TimeValue tv) {
		JsonObject jo = new JsonObject();
		jo.put("Time", unparseTime(tv.getTime()));
		jo.put("Value", interpretPrimitive(tv.getValue()));
		return jo;
	}
	
	private static TimeValue timeValueFromJson(JsonObject jobj, byte typeid) {
		Object tim = jobj.get("Time");
		if (tim instanceof String) {
			Time time = parseTime((String) tim);
			Primitive val = parsePrimitive(jobj.get("Value"), typeid);
			return new TimeValue(time, val);
		}
		return null;
	}
	
	public static JsonObject specialEventToJson(SpecialEvent se) {
		JsonObject jo = new JsonObject();
		if (se.isCalendarReference()) {
			jo.put("Calendar Reference", se.getCalendarReference().toString());
		} else {
			jo.put("Calendar Entry", calendarEntryToJson(se.getCalendarEntry()));
		}
		JsonArray jarr = new JsonArray();
		for (TimeValue tv: se.getListOfTimeValues()) {
			jarr.add(timeValueToJson(tv));
		}
		jo.put("TimeValue List", jarr);
		jo.put("Event Priority", se.getEventPriority().intValue());
		return jo;
	}
	
	public static SpecialEvent specialEventFromJson(JsonObject jobj, byte typeid) {
		Object pobj = jobj.get("Event Priority");
		if (!(pobj instanceof Number)) return null;
		UnsignedInteger epriority = new UnsignedInteger(((Number) pobj).intValue());
		Object tvlobj =  jobj.get("TimeValue List");
		if (!(tvlobj instanceof JsonArray)) return null;
		SequenceOf<TimeValue> tvseq = new SequenceOf<TimeValue>();
		for (Object o: (JsonArray) tvlobj) {
			if (o instanceof JsonObject) {
				TimeValue tv = timeValueFromJson((JsonObject) o, typeid);
				if (tv != null) tvseq.add(tv);
			}
		}
		Object refobj = jobj.get("Calendar Reference");
		if (refobj instanceof String) {
			ObjectIdentifier ref = oidFromString((String) refobj);
			return new SpecialEvent(ref, tvseq, epriority);
		}
		Object calobj = jobj.get("Calendar Entry");
		if (!(calobj instanceof JsonObject)) return null;
		CalendarEntry entry = calendarEntryFromJson((JsonObject) calobj);
		if (entry != null) return new SpecialEvent(entry, tvseq, epriority);
		return null;
	}

	public static Encodable encodeJsonArray(JsonArray jarr, PropertyIdentifier prop, byte typeid) {
		if (prop.equals(PropertyIdentifier.weeklySchedule)) {
			return jsonArrayToWeeklySchedule(jarr, typeid);
		} else if (prop.equals(PropertyIdentifier.exceptionSchedule)) {
			return jsonArrayToExceptionSchedule(jarr, typeid);
		} else if (prop.equals(PropertyIdentifier.dateList)) {
			return jsonArrayToDateList(jarr);	
		}
		return null;
	}
	
	public static SequenceOf<DailySchedule> jsonArrayToWeeklySchedule(JsonArray jarr, byte typeid) {
		SequenceOf<DailySchedule> seq = new SequenceOf<DailySchedule>();
		for (Object obj: jarr) {
			if (obj instanceof String) {
				obj = new JsonArray((String) obj);
			}
			if (obj instanceof JsonArray) {
				SequenceOf<TimeValue> tvseq = new SequenceOf<TimeValue>();
				for (Object tvobj: (JsonArray) obj) {
					if (tvobj instanceof JsonObject) {
						TimeValue tv = timeValueFromJson((JsonObject) tvobj, typeid);
						if (tv != null) tvseq.add(tv);
					}
				}
				seq.add(new DailySchedule(tvseq));
			}
		}
		return seq;
	}

	public static SequenceOf<SpecialEvent> jsonArrayToExceptionSchedule(JsonArray jarr, byte typeid) {
		SequenceOf<SpecialEvent> seq = new SequenceOf<SpecialEvent>();
		for (Object obj: jarr) {
			if (obj instanceof String) {
				obj = new JsonObject((String) obj);
			}
			if (obj instanceof JsonObject) {
				SpecialEvent spec = specialEventFromJson((JsonObject) obj, typeid);
				if (spec != null) seq.add(spec);
			}
		}
		return seq;
	}

	public static SequenceOf<CalendarEntry> jsonArrayToDateList(JsonArray jarr) {
		SequenceOf<CalendarEntry> seq = new SequenceOf<CalendarEntry>();
		for (Object obj: jarr) {
			if (obj instanceof String) {
				obj = new JsonObject((String) obj);
			}
			if (obj instanceof JsonObject) {
				CalendarEntry entry = calendarEntryFromJson((JsonObject) obj);
				if (entry != null) seq.add(entry);
			}
		}
		return seq;
	}
}