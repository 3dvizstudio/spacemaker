package voo.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class XMLGenerator {
	private static final String FLOWGRAPH_XML = "../in/flowgraph.xml";

	public static void main(String[] args) throws JAXBException, IOException {
		ArrayList<FlowGraph.Node> nodes = new ArrayList<>();
		ArrayList<FlowGraph.Relation> relations = new ArrayList<>();
		for (int i = 0; i <= 100; i++) {
			FlowGraph.Node ni = new FlowGraph.Node();
			ni.setId(i);
			ni.setName("name");
			// ni.setSize(5 + (int) (Math.random() * ((400 - 5) + 1)));
			ni.setSize(40);
			nodes.add(ni);
		}
		for (int i = 0; i <= 90; i += 10) {
			FlowGraph.Relation ri = new FlowGraph.Relation(); ri.setFrom(i); ri.setTo(i + 10); relations.add(ri);
			FlowGraph.Relation rii = new FlowGraph.Relation(); rii.setFrom(i); rii.setTo(i + 1); relations.add(rii);
			FlowGraph.Relation riii = new FlowGraph.Relation(); riii.setFrom(i + 1); riii.setTo(i + 2); relations.add(riii);
			FlowGraph.Relation rviii = new FlowGraph.Relation(); rviii.setFrom(i + 2); rviii.setTo(i + 7); relations.add(rviii);
			FlowGraph.Relation rix = new FlowGraph.Relation(); rix.setFrom(i + 2); rix.setTo(i + 8); relations.add(rix);
			FlowGraph.Relation rx = new FlowGraph.Relation(); rx.setFrom(i + 2); rx.setTo(i + 9); relations.add(rx);
			FlowGraph.Relation riv = new FlowGraph.Relation(); riv.setFrom(i + 1); riv.setTo(i + 3); relations.add(riv);
			FlowGraph.Relation rv = new FlowGraph.Relation(); rv.setFrom(i + 3); rv.setTo(i + 4); relations.add(rv);
			FlowGraph.Relation rvi = new FlowGraph.Relation(); rvi.setFrom(i + 3); rvi.setTo(i + 5); relations.add(rvi);
			FlowGraph.Relation rvii = new FlowGraph.Relation(); rvii.setFrom(i + 3); rvii.setTo(i + 6); relations.add(rvii);
		}
		FlowGraph flowgraph = new FlowGraph();
		flowgraph.setNodes(nodes);
		flowgraph.setRelations(relations);
		JAXBContext context = JAXBContext.newInstance(FlowGraph.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(flowgraph, System.out);
		m.marshal(flowgraph, new File(FLOWGRAPH_XML));
	}
}
		/*	for (int i = 0; i <= 100; i++) {
			Relation ri = new Relation();
			if (i <= 100) {ri.setFrom(i);}
			else {ri.setFrom(0 + (int) (Math.random() * ((100 - 0) + 1))); }
			ri.setTo(0 + (int) (Math.random() * ((100 - 0) + 1)));
			if (ri.from != ri.to) { relations.add(ri);} else {
				System.out.println("Removed a bad link");}
		}*/

			/*Relation rj = new Relation();
			ri.setFrom(i);rj.setTo(i+10);
			relations.add(rj);*/
		/*	for (int j = i+1;j<=i+5;j++){
				Relation ri = new Relation();
				ri.setFrom(i);
				ri.setTo(j);
				relations.add(ri);
			}*/
// get variables from our xml file, created before
/*		System.out.println();
		System.out.println("Output from our XML File: ");
		Unmarshaller um = context.createUnmarshaller();
		FlowGraph flowgraph2 = (FlowGraph) um.unmarshal(new FileReader(FLOWGRAPH_XML));
		ArrayList<Node> list = flowgraph2.getNodeList();
		for (Node n : list) {
			System.out.println("Node: " + n.name + " from " + n.getRadius());
		}*/
/*	public static ArrayList<Node> generateNodes(int count, float minSize, float maxSize) {
		ArrayList<Node> nodes = new ArrayList<>();
		for (int i = 0; i <= count; i++) {
			Node ni = new Node();
			ni.setId(i);
			ni.setName("name");
			ni.setSize(50 + (int) (Math.random() * ((100 - 50) + 1)));
			nodes.add(ni);
		}
		return nodes;
	}

	public static ArrayList<Relation> generateRelations(int nodeCount, int relCount) {
		ArrayList<Relation> relations = new ArrayList<>();
		for (int i = 0; i <=relCount; i++) {
			if (i >nodeCount)i-=nodeCount;
			Relation ri = new Relation();
			ri.setFrom(i);
			ri.setTo(0 + (int) (Math.random() * ((nodeCount - 0) + 1)));
			relations.add(ri);
		}
		return relations;
	}*/
/*		for (int i = 0; i <= 20; i++) {
			Node ni = new Node();
			ni.setId(i);
			ni.setName("name");
			ni.setSize(50 + (int) (Math.random() * ((100 - 50) + 1)));
			nodeList.add(ni);
		}
		for (int i = 0; i < 20; i++) {
			Relation ri = new Relation();
			ri.setFrom(i);
			ri.setTo(0 + (int) (Math.random() * ((20 - 0) + 1)));
			relations.add(ri);
		}*/
/*		nodeList=generateNodes(20,30,150);
		relations=generateRelations(20,25);*/