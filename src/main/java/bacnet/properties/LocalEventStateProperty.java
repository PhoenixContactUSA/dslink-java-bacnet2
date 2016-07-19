package bacnet.properties;

import java.util.Arrays;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Writable;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValuePair;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.bacnet4j.type.enumerated.EventState;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import bacnet.LocalBacnetPoint;


public class LocalEventStateProperty extends LocalBacnetProperty{
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(LocalUnitsProperty.class);
	}

	static final String ATTRIBUTE_EVENT_STATE = "event state";
	static final String ACTION_EDIT = "edit";

	EventState state;

	public LocalEventStateProperty(LocalBacnetPoint point, Node parent, Node node) {
		super(point, parent, node);

	}
	
	public LocalEventStateProperty(ObjectIdentifier oid, PropertyIdentifier pid, LocalBacnetPoint point, Node parent, Node node){
		super(oid, pid, point, parent, node);
		
		bacnetObj.writeProperty(PropertyIdentifier.eventState, EventState.normal);
		node.setValueType(ValueType.makeEnum(enumeratedNames()));
		node.setValue(new Value(EventState.normal.toString()));
		node.setWritable(Writable.WRITE);
		node.getListener().setValueHandler(new SetHandler());
	}

	private  String[] enumeratedNames(){
		String valuesStr = Arrays.toString(EventState.ALL);
		return valuesStr.substring(1, valuesStr.length() - 1).replace(" ", "").split(",");
	}
	
	private class SetHandler implements Handler<ValuePair> {

		@Override
		public void handle(ValuePair event) {
			if (!event.isFromExternalSource()) return;
			Value newVal = event.getCurrent();
			state = parseEventState(newVal.getString());
			
			bacnetObj.writeProperty(PropertyIdentifier.eventState, state);
			
			node.setAttribute(ATTRIBUTE_EVENT_STATE, newVal);
		}
	}


	protected EventState parseEventState(String eventString) {

		for (EventState state : EventState.ALL){
			if(state.toString().equals(eventString)){
				return state;
			}
		}

		return null;
	}
}