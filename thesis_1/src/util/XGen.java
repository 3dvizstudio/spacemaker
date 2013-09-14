package util;

import main.FSys;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class XGen {
	private static final String FLOWGRAPH_XML = "./lib/config/flowgraph.xml";
	ArrayList<FSys.Node> nodes;
	ArrayList<FSys.Relation> relations;
	int[] iters = {2, 2, 5, 7};
	float[] sizes = {40, 30, 20, 10};
	String[] names = {"A", "B", "C", "D", "E"};

	public void config(int[] iters, float[] sizes, String[] names) {
		this.iters = iters; this.sizes = sizes; this.names = names;
	}

	//	public static void main(String args[]) {
	public void generate() {
		nodes = new ArrayList<>();
		relations = new ArrayList<>();
		int index = 0;
		HashMap<FSys.Node, String> map = new HashMap<>();
		for (int i = 0; i < iters[0]; i++) {
			FSys.Node n = new FSys.Node(); n.setId(index); n.setName(names[0]); n.setSize(sizes[0]);
			nodes.add(n);
			if (i > 0) { FSys.Relation r = new FSys.Relation(); r.setFrom(n.id); r.setTo(i - 1); relations.add(r); }
			map.put(n, names[0]);
			index++;
		} for (int i = 1; i < iters.length; i++) {
			ArrayList<FSys.Node> temp = new ArrayList<>();
			for (FSys.Node node : nodes) {
				if (node.name.equals(names[i - 1])) {
					for (int k = 0; k < iters[i]; k++) {
						FSys.Node n = new FSys.Node(); n.setId(index); n.setName(names[i]); n.setSize(sizes[i]);
						FSys.Relation r = new FSys.Relation(); r.setFrom(node.id); r.setTo(index);
						temp.add(n); relations.add(r); map.put(n, names[i]);
						index++;
					}
				}
			} nodes.addAll(temp);
		}
		for (FSys.Node n : map.keySet()) { System.out.println(n.name + map.get(n)); }
		for (FSys.Relation r : relations) { System.out.println(nodes.get(r.from).name + "." + r.from + ":" + nodes.get(r.to).name + "." + r.to); }
		System.out.println(map.keySet()); System.out.println(map.values()); System.out.println(map.entrySet());
		marshal();
	}

	void marshal() {
		FSys flowgraph = new FSys();
		flowgraph.setNodes(nodes);
		flowgraph.setRelations(relations);
		try {
			JAXBContext context = JAXBContext.newInstance(FSys.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(flowgraph, System.out);
			m.marshal(flowgraph, new File(FLOWGRAPH_XML));
		} catch (JAXBException e) { System.out.println("error parsing xml: "); e.printStackTrace(); System.exit(1); }
	}

	public void setIters(int[] iters) { this.iters = iters; }

	public void setSizes(float[] sizes) { this.sizes = sizes; }

	public void setNames(String[] names) { this.names = names; }
}
