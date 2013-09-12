package voo.util;

import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.behaviors.AttractionBehavior2D;
import toxi.physics2d.behaviors.ParticleBehavior2D;
import voo.core.App;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;

@XmlRootElement(name = "flowgraph")
public class FlowGraph {
	@XmlElement(name = "node")
	public ArrayList<Node> nodes = new ArrayList<>();
	@XmlElement(name = "rel")
	public ArrayList<Relation> relations = new ArrayList<>();
	@XmlTransient
	public HashMap<Integer, Node> nodeIndex = new HashMap<>();
	@XmlTransient
	public HashMap<Integer, ArrayList<Node>> relationIndex = new HashMap<>();

	public void build() {
		for (Node n : nodes) { nodeIndex.put(n.id, n); }
		for (Relation r : relations) {
			ArrayList<Node> nlist = relationIndex.get(r.from);
			if (nlist == null) {
				nlist = new ArrayList<>();
				relationIndex.put(r.from, nlist);
			}
			nlist.add(nodeIndex.get(r.to));
		}
	}

	public void display() {
		if (App.SHOW_NODES) for (Node n : nodes) n.display();
		if (App.UPDATE_PHYSVAL) update();
		if (App.SHOW_INFO) drawInfo();
	}

	public void update() {
		for (Node n : nodes) n.update();
		for (Relation r : relations) {
			Node na = nodeIndex.get(r.from);
			Node nb = nodeIndex.get(r.to);
			float l = (((na.getRadius() + nb.getRadius()) * App.SCALE_FACTOR) / 2) * App.SCALE_SPRINGS;
			App.PSYS.getPhysics().getSpring(na.getVerlet(), nb.getVerlet()).setRestLength(l);
		}
	}

	private void drawInfo() {
		App.P5.fill(0xffbf3c3c);
		App.P5.pushMatrix();
		App.P5.translate(1600, 150);
		for (Node n : nodes) {
			App.P5.translate(0, 10);
			App.P5.text(n.id, 0, 0);
			App.P5.text(n.name, 30, 0);
			App.P5.text((int) n.size + "m²", 60, 0);
		}
		App.P5.popMatrix();
		App.P5.noFill();
	}

	public Node getNodeForID(int id) { return nodeIndex.get(id); }

	public ArrayList<Node> getKnownPeopleFor(int id) { return relationIndex.get(id); }

	public void setNodes(ArrayList<Node> nodes) {this.nodes = nodes;}

	public void setRelations(ArrayList<Relation> relations) {this.relations = relations;}

	@XmlRootElement(name = "node")
	public static class Node {
		@XmlAttribute
		public int id;
		@XmlAttribute
		public float size;
		@XmlValue
		public String name;
		@XmlTransient
		public VerletParticle2D verlet = new VerletParticle2D(App.INBOX.getRandomPoint());
		@XmlTransient
		public AttractionBehavior2D behavior = new AttractionBehavior2D(verlet, size, -1.2f);
		@XmlTransient
		public float radius = 10;

		public void update() {
			this.radius = (float) (Math.sqrt(size / Math.PI) * App.SCALE_RADIUS);
			this.behavior.setRadius(((radius * App.SCALE_FACTOR) * App.NODE_OFFSET));
			this.behavior.setStrength(App.NODE_STR);
		}

		public void display() {
			if (App.SHOW_NODES) drawNodes();
			if (App.PSYS.getSelectedParticle() == verlet) drawStatusBar(); drawHighlight();
		}

		void drawNodes() {
			App.P5.stroke(0xff333333);
			App.P5.ellipse(verlet.x, verlet.y, radius * App.SCALE_FACTOR, radius * App.SCALE_FACTOR);
			App.P5.noStroke();
		}

		void drawHighlight() {
			App.P5.fill(0xffbf3c3c);
			App.P5.ellipse(verlet.x, verlet.y, radius, radius);
			App.P5.noFill();
		}

		void drawStatusBar() {
			App.P5.fill(0xffffffff);
			App.P5.text(name, 310, 12);
			App.P5.text("[ weight: " + verlet.getWeight() + " ] [ area: " + size + " ] [ rad: " + App.DF3.format(getRadius()) + " ]", 310, App.P5.height - 4);
			App.P5.noFill();
		}

		public float getRadius() { return radius; }

		public ParticleBehavior2D getBehavior() {return behavior;}

		public VerletParticle2D getVerlet() {return verlet;}

		public void setId(int id) {this.id = id;}

		public void setSize(float size) {this.size = size;}

		public void setName(String name) {this.name = name;}
	}

	@XmlRootElement(name = "rel")
	public static class Relation {
		@XmlAttribute
		public int from;
		@XmlAttribute
		public int to;

		public void setTo(int to) {this.to = to;}

		public void setFrom(int from) {this.from = from;}
	}
}

