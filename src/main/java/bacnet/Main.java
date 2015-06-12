package bacnet;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.serializer.Deserializer;
import org.dsa.iot.dslink.serializer.Serializer;


public class Main extends DSLinkHandler {
	
	public static void main(String[] args) {
		
		args = new String[] { "-b", "http://localhost:8080/conn" };
		DSLinkFactory.startResponder("bacnetResponder", args, new Main());
	}
	
	@Override
	public void onResponderConnected(DSLink link){
		NodeManager manager = link.getNodeManager();
        Node superRoot = manager.getNode("/").getNode();
        Serializer copyser = new Serializer(manager);
		Deserializer copydeser = new Deserializer(manager);
        BacnetLink.start(superRoot, copyser, copydeser);
	}

}
