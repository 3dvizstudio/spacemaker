package main;

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

import static util.Color.*;

public class PSys {
	public static VerletPhysics2D physics;
	public static VerletParticle2D selectedParticle;
	public final List<AttractionBehavior2D> attractors;
	public final List<VerletParticle2D> attractorParticles;
	public final List<VerletParticle2D> particles;
	public final List<VerletMinDistanceSpring2D> minDistSprings;
	public final List<VerletSpring2D> springs;
	protected PApplet p5;
	private AttractionBehavior2D selectedAttractor;

	public PSys(PApplet $p5) {
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
		if (App.SHOW_INFO) displayInfo();
		if (App.SHOW_PARTICLES) drawParticles();
		if (App.SHOW_SPRINGS) drawSrings();
		if (App.SHOW_ATTRACTORS) drawAttractors();
	}

	public void updateValues() {
		physics.setDrag(App.DRAG);
		for (AttractionBehavior2D a : attractors) { a.setRadius(App.ATTR_RAD); a.setStrength(App.ATTR_STR); }
		for (VerletSpring2D s : physics.springs) s.setStrength(App.SPR_STR);
	}

	private void displayInfo() {
		p5.fill(PHYS_TXT);
		p5.pushMatrix();
		p5.translate(1600, 50);
		p5.text("Springs: " + physics.springs.size(), 3, 10);
		p5.text("Particles: " + physics.particles.size(), 3, 20);
		p5.text("Drag : " + App.DF3.format(physics.getDrag()), 3, 30);
		p5.text("Separation : " + App.ATTR_RAD, 3, 40);
		p5.text("Spring Str : " + App.DF3.format(App.SPR_STR), 3, 50);
		p5.popMatrix();
		p5.noFill();
	}

	private void drawParticles() {
		p5.fill(PHYS_PTCL);
		for (VerletParticle2D p : physics.particles) {
			if (p == selectedParticle) { p5.ellipse(p.x, p.y, 8, 8); }
			else { p5.ellipse(p.x, p.y, 4, 4); }
		}
		p5.noFill();
	}

	private void drawSrings() {
		p5.stroke(PHYS_SPR);
		for (VerletSpring2D s : springs) { p5.line(s.a.x, s.a.y, s.b.x, s.b.y); }
		p5.noStroke();
	}

	private void drawAttractors() {
		p5.stroke(PHYS_ATTR);
		for (AttractionBehavior2D a : attractors) {
			Vec2D n = a.getAttractor();
			if (a == selectedAttractor) { p5.stroke(100); }
			else { p5.stroke(PHYS_ATTR); }
			p5.line(n.x - 3, n.y - 3, n.x + 3, n.y + 3);
			p5.line(n.x - 3, n.y + 3, n.x + 3, n.y - 3);
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

	public void addParticle(VerletParticle2D v, ParticleBehavior2D b) {
		physics.addParticle(v);
		particles.add(v);
		physics.addBehavior(b);
	}

	public void mousePressed() {
		selectedParticle = null;
		for (VerletParticle2D particle : particles) {
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
		if (selectedParticle != null) {selectedParticle.unlock(); selectedParticle = null;}
		else if (selectedAttractor != null) {selectedAttractor = null;}
	}

	public void mouseDragged() {
		if (selectedParticle != null) { selectedParticle.set(App.MOUSE); }
		else if (selectedAttractor != null) { selectedAttractor.getAttractor().set(App.MOUSE);}
	}

	public void addMinDistSprings() {
		for (int i = 1; i < particles.size(); i++) {
			VerletParticle2D pi = particles.get(i);
			for (int j = 0; j < i; j++) {
				VerletParticle2D pj = particles.get(j);
				VerletMinDistanceSpring2D s = new VerletMinDistanceSpring2D(pi, pj, 10, 0.1f);
				minDistSprings.add(s);
				physics.addSpring(s);
			}
		}
	}

	public void keyPressed() { if (p5.key == 'f') addAttractor(App.MOUSE); if (p5.key == 'h') lockParticle(App.MOUSE); }

	public VerletPhysics2D getPhysics() { return physics; }

	public VerletParticle2D getSelectedParticle() {return selectedParticle;}

	public List<VerletParticle2D> getParticles() { return particles; }

	public List<VerletParticle2D> getAttractorParticles() {return attractorParticles;}
}

