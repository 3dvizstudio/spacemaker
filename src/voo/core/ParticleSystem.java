package voo.core;

import processing.core.PApplet;
import toxi.geom.Circle;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletMinDistanceSpring2D;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;
import toxi.physics2d.VerletSpring2D;
import toxi.physics2d.behaviors.AttractionBehavior2D;
import toxi.physics2d.behaviors.ParticleBehavior2D;

import java.util.ArrayList;
import java.util.List;

public class ParticleSystem {
	public static VerletPhysics2D physics;
	public static VerletParticle2D selectedParticle;
	public final List<AttractionBehavior2D> attractors;
	public final List<VerletParticle2D> attractorParticles;
	public final List<VerletParticle2D> particles;
	public final List<VerletMinDistanceSpring2D> minDistSprings;
	public final List<VerletSpring2D> springs;
	protected PApplet p5;
	private AttractionBehavior2D selectedAttractor;

	public ParticleSystem(PApplet $p5) {
		this.p5 = $p5;
		physics = new VerletPhysics2D();
		physics.setDrag(App.DRAG);
		physics.setWorldBounds(App.BOUNDS.copy().scale(0.9f));
		attractors = new ArrayList<>();
		attractorParticles = new ArrayList<>();
		minDistSprings = new ArrayList<>();
		particles = new ArrayList<>();
		springs = new ArrayList<>();
	}

	public void display() {
		if (App.UPDATE_PHYSICS) physics.update();
		if (App.UPDATE_PHYSVAL) updateValues();
		if (App.SHOW_INFO) displayInfo(0xffffffff);
		if (App.SHOW_PARTICLES) drawParticles(0xff888888);
		if (App.SHOW_SPRINGS) drawSrings(0xff888888);
		if (App.SHOW_ATTRACTORS) drawAttractors(0xff444444);
	}

	public void updateValues() {
		physics.setDrag(App.DRAG);
		for (AttractionBehavior2D a : attractors) { a.setRadius(App.ATTRACTOR_RAD); a.setStrength(App.ATTRACTOR_STR); }
		for (VerletSpring2D s : physics.springs) s.setStrength(App.SPRING_STR);
	}

	private void displayInfo(int color) {
		p5.fill(color);
		p5.pushMatrix();
		p5.translate(1600, 50);
		p5.text("Springs: " + physics.springs.size(), 3, 10);
		p5.text("Particles: " + physics.particles.size(), 3, 20);
		p5.text("Drag : " + App.DF3.format(physics.getDrag()), 3, 30);
		p5.text("Separation : " + App.ATTRACTOR_RAD, 3, 40);
		p5.text("Spring Str : " + App.DF3.format(App.SPRING_STR), 3, 50);
		p5.popMatrix();
		p5.noFill();
	}

	private void drawParticles(int color) {
		p5.fill(color);
		for (VerletParticle2D p : physics.particles) {
			if (p == selectedParticle) { p5.ellipse(p.x, p.y, 8, 8); } else p5.ellipse(p.x, p.y, 4, 4);
		} p5.noFill();
	}

	private void drawSrings(int color) {
		p5.stroke(color);
		for (VerletSpring2D s : springs) { p5.line(s.a.x, s.a.y, s.b.x, s.b.y); }
		p5.noStroke();
	}

	private void drawAttractors(int color) {
		p5.stroke(color);
		p5.strokeWeight(2);
		for (AttractionBehavior2D a : attractors) {
			Vec2D n = a.getAttractor();
			int x = (int) n.x;
			int y = (int) n.y;
			if (a == selectedAttractor) { p5.stroke(100); } else p5.stroke(color);
			p5.line(x - 3, y - 3, x + 3, y + 3);
			p5.line(x - 3, y + 3, x + 3, y - 3);
			p5.strokeWeight(1);
			p5.stroke(0xff2b2b2b);
			p5.ellipse(n.x, n.y, a.getRadius() * 2, a.getRadius() * 2);
		}
		p5.noStroke();
	}

	private void lockParticle(Vec2D pos) {
		for (VerletParticle2D p : physics.particles) {
			Circle c = new Circle(p, 10);
			if (c.containsPoint(pos)) { p.lock(); break; }
		}
	}

	public void addRandomAttractors(int count) {
		for (int i = 0; i < count; i++) {
			addAttractor(physics.getWorldBounds().getRandomPoint());
		}
	}

	public void addAttractors(ArrayList<Vec2D> points) {
		for (Vec2D p : points) {addAttractor(p);}
	}

	public void addSpring(VerletSpring2D s) {
		physics.addSpring(s);
		springs.add(s);
	}

	public void addAttractor(Vec2D pos) {
		VerletParticle2D p = new VerletParticle2D(pos);
		AttractionBehavior2D a = new AttractionBehavior2D(p, 200, 1f);
		physics.addParticle(p);
		physics.addBehavior(a);
		attractorParticles.add(p);
		attractors.add(a);
		for (int i = 1; i < attractorParticles.size(); i++) {
			VerletParticle2D pi = attractorParticles.get(i);
			for (int j = 0; j < i; j++) {
				VerletParticle2D pj = attractorParticles.get(j);
				VerletMinDistanceSpring2D s = new VerletMinDistanceSpring2D(pi, pj, 100, 0.1f);
				minDistSprings.add(s);
				physics.addSpring(s);
			}
		}
	}

	public List<AttractionBehavior2D> getAttractors() { return attractors; }

	public VerletPhysics2D getPhysics() { return physics; }

	public VerletParticle2D getSelectedParticle() {return selectedParticle;}

	public void mousePressed() {
		selectedParticle = null;
		for (VerletParticle2D particle : physics.particles) {
			Circle c = new Circle(particle, 10);
			if (c.containsPoint(App.MOUSE)) { selectedParticle = particle; selectedParticle.lock(); break; }
		}
		selectedAttractor = null;
		for (AttractionBehavior2D a : attractors) {
			Circle c = new Circle(a.getAttractor(), 10);
			if (c.containsPoint(App.MOUSE)) { selectedAttractor = a; break; }
		}
	}

	public void mouseReleased() {
		if (selectedParticle != null) {selectedParticle.unlock(); selectedParticle = null;} else if (selectedAttractor != null) {selectedAttractor = null;}
	}

	public void mouseDragged() {
		if (selectedParticle != null) { selectedParticle.set(App.MOUSE); } else if (selectedAttractor != null) { selectedAttractor.getAttractor().set(App.MOUSE);}
	}

	public void keyPressed() {
		if (p5.key == 'f') addAttractor(App.MOUSE); if (p5.key == 'h') lockParticle(App.MOUSE);
	}

	public void addParticle(VerletParticle2D v) { physics.addParticle(v); }

	public void addBehavior(ParticleBehavior2D a) { physics.addBehavior(a); }

	private class Area {
		public String name;
		public float area;
		public int count = 1;

		public Area(String name, float area) { this.name = name; this.area = area; }

		public void add(float v) { this.area += v; this.count++; }
	}
}
//	protected static ArrayList<String> names = new ArrayList<>();
//	protected static ArrayList<Area> areas = new ArrayList<>();
//	protected float grandTotal;
//	protected boolean sorted;
/*		if (sorted) {
			p5.translate(0, 100);
			for (Area a : areas) {
				p5.translate(0, 12);
				p5.text(a.name + " x " + a.count, 0, 0);
				p5.text((int) a.area + " m²", 80, 0);
				p5.text((int) ((a.area / grandTotal) * 100) + "%", 130, 0);
			}
			p5.translate(0, 16);
			p5.text("TOTAL", 0, 0);
			p5.text((int) grandTotal + " m²", 80, 0);
		}*/
//		if (App.UPDATE_SPRINGS) updateSprings();

/*	public void updateSprings() {
		for (int i = 1; i < nodes.size(); i++) {
			Mappable pi = nodes.get(i);
			for (int j = 0; j < i; j++) {
				Mappable pj = nodes.get(j);
				float iw = pi.getRadius();
				float jw = pj.getRadius();
				float w = (iw + jw) / 2;
				VerletSpring2D s = physics.getSpring(pi.getVerlet(), pj.getVerlet());
				if (s != null) s.setRestLength((w * App.SCALE_SPRINGS) + 2);
			}
		}
	}*/
/*	void sort() {
		for (Mappable n : nodes) {
			if (names.contains(n.getName())) {
				Area i = areas.get(names.indexOf(n.getName()));
				i.add(n.getArea());
			} else {
				names.add(n.getName());
				areas.add(new Area(n.getName(), n.getArea()));
			}
			grandTotal += n.getArea();
		}
		sorted = true;
	}*/
/*	public void setRoot(Node rootNode) {
		root = rootNode.getVerlet();
		AttractionBehavior2D a = new AttractionBehavior2D(root, 10, -1.2f);
		physics.addParticle(root);
		physics.addBehavior(a);
	}*/

/*	public void setSelectedItem(Hub h) {
		selectedItem = h;
		selectedItem.updateColors();
	}*/
//	public VerletParticle2D getRoot() {return root;}

/*	public void openFile(String filePath) {
		XML rootXML = p5.loadXML(filePath);
		Hub rootHub = new Hub(null, rootXML, 0, 0);
		rootHub.setVerlet(new VerletParticle2D(new Vec2D(900, 450)));
		rootHub.setArea(rootXML.getFloat("area"));
		rootItem = rootHub;
		rootItem.updateColors();
		setRoot(rootHub);
		for (Mappable n : nodes) {
			physics.addParticle(n.getVerlet());
			physics.addBehavior(n.getBehavior());
			if (n.getParent() != null) {
				n.setSpring(new VerletSpring2D(n.getVerlet(), n.getParent().getVerlet(), (n.getRadius() + n.getParent().getRadius()), 0.01f));
				physics.addSpring(n.getSpring());
			}
			App.VSYS.addCell(n.getVerlet());
		}
		sort();
	}*/
