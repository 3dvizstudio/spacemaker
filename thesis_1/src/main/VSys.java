package main;

import processing.core.PApplet;
import toxi.geom.Polygon2D;
import toxi.geom.PolygonClipper2D;
import toxi.geom.SutherlandHodgemanClipper;
import toxi.geom.Vec2D;
import toxi.geom.mesh2d.Voronoi;

import java.util.ArrayList;
import java.util.List;

import static util.Color.*;

public class VSys {
	protected PApplet p5;
	protected PolygonClipper2D clipper;
	protected ArrayList<Vec2D> cellSites = new ArrayList<>();
	public static Voronoi voronoi = new Voronoi();
	ArrayList<Polygon2D> cells = new ArrayList<>();

	public VSys(PApplet $p5) {
		this.p5 = $p5;
		clipper = new SutherlandHodgemanClipper(App.BOUNDS);
	}

	public void display() {
		if (App.UPDATE_VORONOI) {
			voronoi = new Voronoi();
			voronoi.addPoints(App.PSYS.getAttractorParticles());
			voronoi.addPoints(App.PSYS.getParticles());
		} if (App.SHOW_VORONOI && voronoi != null) {
			cells = new ArrayList<>();
			for (Polygon2D poly : voronoi.getRegions()) {
				poly = clipper.clipPolygon(poly);
				for (Vec2D v : cellSites) {
					if (poly.containsPoint(v)) {
						cells.add(poly);
					}
				}
			} drawPoly();
		}
	}

	private void drawPoly() {
		String drawMode = App.DRAWMODE;
		switch (drawMode) {
			case "none": break;
			case "verts":
				p5.stroke(VOR_VERTS);
				for (Polygon2D poly : cells) { for (Vec2D vec : poly.vertices) {p5.ellipse(vec.x, vec.y, 4, 4);} }
				p5.noStroke();
				break;
			case "bezier":
				p5.stroke(VOR_CELLS);
				for (Polygon2D poly : cells) {
					List<Vec2D> vec = poly.vertices;
					int count = vec.size();
					p5.beginShape();
					p5.vertex((vec.get(count - 1).x + vec.get(0).x) / 2, (vec.get(count - 1).y + vec.get(0).y) / 2);
					for (int i = 0; i < count; i++) {
						p5.bezierVertex(vec.get(i).x, vec.get(i).y,
						                vec.get(i).x, vec.get(i).y,
						                (vec.get((i + 1) % count).x + vec.get(i).x) / 2,
						                (vec.get((i + 1) % count).y + vec.get(i).y) / 2);
					} p5.endShape(PApplet.CLOSE);
				}
				p5.noStroke();
				break;
			case "poly":
				p5.stroke(VOR_CELLS);
				for (Polygon2D poly : cells) { App.GFX.polygon2D(poly); }
				p5.noStroke();
				break;
			case "info":
				p5.fill(VOR_TXT);
				for (Polygon2D poly : cells) {
					p5.text(poly.getNumVertices() + "." + cells.indexOf(poly), poly.getCentroid().x, poly.getCentroid().y);
				}
				p5.noFill();
				break;
		}
	}

	public void addCell(Vec2D v) { cellSites.add(v); }
}