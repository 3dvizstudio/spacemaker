package main;

import processing.core.PApplet;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.behaviors.AttractionBehavior2D;
import toxi.physics2d.behaviors.ParticleBehavior2D;
import util.Color;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;

@XmlRootElement(name = "flowgraph")
public class FSys {
	@XmlElement(name = "node")
	public ArrayList<Node> nodes = new ArrayList<>();
	@XmlElement(name = "rel")
	public ArrayList<Relation> relations = new ArrayList<>();
	@XmlTransient
	public HashMap<Integer, Node> nodeIndex = new HashMap<>();
	@XmlTransient
	public HashMap<Integer, ArrayList<Node>> relationIndex = new HashMap<>();
	@XmlTransient
	protected PApplet p5;

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

	public void display(PApplet p5) {
		this.p5 = p5;
		if (App.SHOW_NODES) for (Node n : nodes) n.display();
		if (App.UPDATE_PHYSVAL) update();
		if (App.SHOW_INFO) drawInfo();
	}

	void update() {
		for (Node n : nodes) n.update();
		if (App.SCALE != 0) {
			for (Relation r : relations) {
				Node na = nodeIndex.get(r.from);
				Node nb = nodeIndex.get(r.to);
				float l = (((na.getRadius() + nb.getRadius()) * App.SCALE) / 2) * App.SPR_SCALE;
				App.PSYS.getPhysics().getSpring(na.getVerlet(), nb.getVerlet()).setRestLength(l);
			}
		}
	}

	void drawInfo() {
//		for (Node n : nodes) {			n.display();		}
//		p5.pushMatrix();
//		p5.fill(Color.TXT);
//			p5.text(n.id, 0, 0); p5.text(n.name, n.verlet.x, 0); p5.text((int) n.size + "m²", 60, 0);
//		p5.translate(1600, 150);
//		for (Node n : nodes) { p5.translate(0, 10); }
//		p5.noFill();
//		p5.popMatrix();
	}

	public Node getNodeForID(int id) { return nodeIndex.get(id); }

	public ArrayList<Node> getRelForID(int id) { return relationIndex.get(id); }

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
			if (App.NODE_SCALE != 0) { this.radius = (float) (Math.sqrt(size / Math.PI) * App.NODE_SCALE);}
			this.behavior.setRadius(((radius * App.SCALE) + App.NODE_PAD));
			if (App.NODE_STR != 0) this.behavior.setStrength(App.NODE_STR);
		}

		public void display() {
			String drawMode = App.DRAWMODE;
			switch (drawMode) {
				case "none": break;
				case "verts":
					drawNodes();
					break;
				case "bezier":
					drawNodes();
					break;
				case "poly":
					drawNodes();
					drawTags();
					break;
				case "info":
					drawInfo();
					break;
			}
			drawNodes();
			if (App.PSYS.getSelectedParticle() == verlet) { drawInfo(); drawHighlight(); }
		}

		void drawNodes() {
			App.P5.stroke(Color.NODE_S);
			App.P5.ellipse(verlet.x, verlet.y, radius * App.SCALE, radius * App.SCALE);
			App.P5.stroke(Color.NODE_F);
			App.P5.noStroke();
		}

		void drawTags() {
			App.P5.fill(0xffffffff);
			App.P5.textAlign(PApplet.CENTER);
			App.P5.text(name, verlet.x, verlet.y);
			App.P5.text(id, verlet.x, verlet.y + 10);
			App.P5.textAlign(PApplet.LEFT);
			App.P5.noFill();
		}

		void drawHighlight() {
			App.P5.fill(Color.NODE_SEL);
			App.P5.ellipse(verlet.x, verlet.y, radius * 3, radius * 3);
			App.P5.noFill();
		}

		void drawInfo() {
			App.P5.fill(Color.NODE_TXT);
			App.P5.text(name, 310, 12);
			App.P5.text("[ weight: " + verlet.getWeight() + " ] [ area: " + size + " ] [ rad: " + App.DF3.format(getRadius()) + " ]", 310, App.P5.height - 4);
			App.P5.noFill();
		}

		public final String toString() {return Integer.toString(id);}

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

