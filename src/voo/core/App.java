package voo.core;

import controlP5.*;
import org.philhosoft.p8g.svg.P8gGraphicsSVG;
import processing.core.PApplet;
import processing.core.PFont;
import toxi.geom.Rect;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletSpring2D;
import toxi.processing.ToxiclibsSupport;
import voo.util.FlowGraph;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.text.DecimalFormat;

public class App extends PApplet {
	public static final Rect BOUNDS = new Rect(350, 50, 1100, 800), INBOX = new Rect(0, 0, 600, 100).translate(500, 400);
	public static final DecimalFormat DF3 = new DecimalFormat("#.###");
	public static boolean RECORDING = false;
	public static boolean UPDATE_PHYSICS, UPDATE_PHYSVAL, UPDATE_VORONOI, UPDATE_SPRINGS;
	public static boolean SHOW_PARTICLES, SHOW_SPRINGS, SHOW_ATTRACTORS, SHOW_VORONOI, SHOW_VOR_VERTS;
	public static boolean SHOW_VOR_INFO, SHOW_VOIDS, SHOW_OVERLAY, SHOW_INFO, SHOW_NODES, SHOW_LSYSTEM;
	public static float ZOOM = 1, VOR_REFRESH = 1, SCALE_FACTOR = 10, SCALE_SPRINGS = 1, SCALE_RADIUS = 1;
	public static float DRAG = 0.5f, ATTRACTOR_RAD = 60, ATTRACTOR_STR = -0.9f, SPRING_STR = 0.01f, NODE_STR = -1, NODE_OFFSET = 1;
	public static PApplet P5;
	public static ControlP5 CP5;
	public static PFont UBUNTU;
	public static ToxiclibsSupport GFX;
	public static ParticleSystem PSYS;
	public static VoronoiDiagram VSYS;
	public static Vec2D MOUSE = new Vec2D();
	public static FlowGraph FSYS;

	public static void main(String[] args) { PApplet.main(new String[]{("voo.core.App")}); }

	public void setup() {
		P5 = this;
		GFX = new ToxiclibsSupport(this);
		CP5 = new ControlP5(this);
		frame.setTitle(this.sketchPath);
		size(1800, 900); frameRate(60); smooth(4);
		colorMode(HSB, 100); ellipseMode(CENTER); textAlign(LEFT); noStroke(); noFill();
		textFont(UBUNTU = createFont("../lib/Ubuntu10.vlw", 10, true), 10);
		setupCP5();
	}

	public void draw() {
		background(10);
		MOUSE.set(mouseX, mouseY);
		noStroke();
		fill(0xff171717);
		rect(0, 0, 300, height);
		rect(1580, 0, width, height);
		noFill();
		pushMatrix();
		translate(-((ZOOM * width) - width) / 2, -((ZOOM * height) - height) / 2);
		scale(ZOOM);
		if (VSYS != null) VSYS.display();
		if (PSYS != null) PSYS.display();
		if (FSYS != null) FSYS.display();
		if (RECORDING) { RECORDING = false; endRecord(); System.out.println("SVG EXPORTED SUCCESSFULLY"); }
		popMatrix();
		CP5.draw();
	}

	void loadFGSYS() {
		try {
			JAXBContext context = JAXBContext.newInstance(FlowGraph.class);
			FSYS = (FlowGraph) context.createUnmarshaller().unmarshal(createInput("./lib/flowgraph.xml"));
		} catch (JAXBException e) { println("error parsing xml: "); e.printStackTrace(); System.exit(1); }
		PSYS = new ParticleSystem(this);
		VSYS = new VoronoiDiagram(this);
		FSYS.build();
		for (FlowGraph.Node c : FSYS.nodes) {
			PSYS.addParticle(c.getVerlet());
			PSYS.addBehavior(c.getBehavior());
			VSYS.addCell(c.getVerlet());
		}
		for (FlowGraph.Relation r : FSYS.relations) {
			FlowGraph.Node na = FSYS.nodeIndex.get(r.from);
			FlowGraph.Node nb = FSYS.nodeIndex.get(r.to);
			VerletParticle2D va = na.verlet;
			VerletParticle2D vb = nb.verlet;
			float l = na.getRadius() + nb.getRadius();
			App.PSYS.addSpring(new VerletSpring2D(va, vb, l, 0.01f));
		}
	}

	void setupCP5() {
		CP5.setFont(new ControlFont(UBUNTU, 10));
		CP5.setAutoDraw(false);
		CP5.setColorBackground(0xff222222).setColorForeground(0xff151515).setColorCaptionLabel(0xff444444).setColorActive(0xffbf3c3c);
		FrameRate FPS = CP5.addFrameRate();
		FPS.setInterval(3).setPosition(20, height - 20).setColorValueLabel(0xffaeaeae).draw();
		Group file = CP5.addGroup("FILE").setPosition(0, 20); CP5.begin(20, 10);
		CP5.addButton("quit").linebreak().setGroup(file);
		CP5.addButton("saveSVG").linebreak().setGroup(file);
		CP5.addButton("load").linebreak().setGroup(file);
		CP5.addButton("perimeter").linebreak().setGroup(file);
		CP5.addButton("addrand").linebreak().setGroup(file);
		CP5.end();
		Group config = CP5.addGroup("CONFIG").setPosition(0, 300); CP5.begin(20, 10);
		CP5.addButton("saveDefaults").setGroup(config);
		CP5.addButton("saveConfig").setGroup(config);
		CP5.addButton("loadConfig").setGroup(config).linebreak();
		CP5.addSlider("VOR_REFRESH").setRange(1, 4).setNumberOfTickMarks(4).linebreak();
		CP5.addSlider("ZOOM").setRange(0.1f, 5).linebreak();
		CP5.addSlider("DRAG").setRange(0.1f, 2).linebreak();
		CP5.addSlider("SCALE_FACTOR").setRange(1, 20).linebreak();
		CP5.addSlider("SCALE_SPRINGS").setRange(0.1f, 4).linebreak();
		CP5.addSlider("SCALE_RADIUS").setRange(0.1f, 4).linebreak();
		CP5.addSlider("NODE_OFFSET").setRange(0.1f, 4).linebreak();
		CP5.addSlider("ATTRACTOR_RAD").setRange(0.1f, 400).linebreak();
		CP5.addSlider("ATTRACTOR_STR").setRange(-2f, 21).linebreak();
		CP5.addSlider("SPRING_STR").setRange(0.01f, 1f).linebreak();
		CP5.addSlider("NODE_STR").setRange(-20, 1);
		CP5.end();
		Group debug = CP5.addGroup("DEBUG").setPosition(0, 600); CP5.begin(20, 10);
		CP5.addToggle("UPDATE_PHYSICS"); CP5.addToggle("SHOW_NODES").linebreak();
		CP5.addToggle("UPDATE_VORONOI"); CP5.addToggle("SHOW_VORONOI").linebreak();
		CP5.addToggle("UPDATE_PHYSVAL"); CP5.addToggle("SHOW_PARTICLES").linebreak();
		CP5.addToggle("UPDATE_SPRINGS"); CP5.addToggle("SHOW_SPRINGS").linebreak();
		CP5.addToggle("SHOW_VOR_INFO"); CP5.addToggle("SHOW_VOIDS").linebreak();
		CP5.addToggle("SHOW_ATTRACTORS"); CP5.addToggle("SHOW_VOR_VERTS").linebreak();
		CP5.addToggle("SHOW_OVERLAY"); CP5.addToggle("SHOW_INFO").linebreak();
		CP5.addToggle("SHOW_LSYSTEM").linebreak();
		CP5.end();
//		CP5.loadProperties(("default.ser"));
		for (Textfield t : CP5.getAll(Textfield.class)) {
			t.setSize(170, 16).setAutoClear(false).setGroup(file); t.setColorValueLabel(0xffaeaeae);
			t.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).getStyle().setPaddingRight(4);
		} for (Button b : CP5.getAll(Button.class)) {
			b.setSize(80, 16); b.setColorForeground(0xffbf3c3c).setColorCaptionLabel(0xffaeaeae);
			b.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
		} for (Toggle t : CP5.getAll(Toggle.class)) {
			t.setSize(16, 16).setColorActive(0xffaeaeae).setColorForeground(0xffbf3c3c).setGroup(debug);
			t.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).getStyle().setPaddingLeft(4);
		} for (Slider s : CP5.getAll(Slider.class)) {
			s.setSize(160, 16).setGroup(config);
			s.setColorBackground(0xff222222).setColorForeground(0xff666666).setColorValueLabel(0xffffffff);
			s.getValueLabel().align(ControlP5.RIGHT, ControlP5.CENTER);
			s.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).getStyle().setPaddingLeft(4);
		} for (Group g : CP5.getAll(Group.class)) {
			g.setSize(300, 16).setBarHeight(16); g.setColorBackground(0xff222222).setColorForeground(0xff151515).setColorLabel(0xffaeaeae);
			g.getCaptionLabel().getStyle().setPaddingLeft(16);
		}
	}

	void quit(int theValue) { exit(); }

	void load(int theValue) { loadFGSYS(); }

	void addrand(int theValue) { VSYS.addExtras(9); }

	void perimeter(int theValue) {VSYS.addPerim(50);}

	void saveSVG(int theValue) { beginRecord(P8gGraphicsSVG.SVG, "../out/svg/print-###.svg"); RECORDING = true;}

	void saveDefaults(int theValue) { CP5.saveProperties(("./lib/defaults.ser")); }

	void saveConfig(int theValue) { CP5.saveProperties(("./lib/config.ser")); }

	void loadConfig(int theValue) { CP5.loadProperties(("./lib/config.ser"));}

	public void keyPressed() { }

	public void mouseMoved() { MOUSE.set(mouseX, mouseY);}

	public void mouseDragged() { if (PSYS != null) PSYS.mouseDragged(); }

	public void mousePressed() {if (PSYS != null) PSYS.mousePressed(); }

	public void mouseReleased() { if (PSYS != null) PSYS.mouseReleased(); }
}
