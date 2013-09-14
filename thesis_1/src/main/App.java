package main;

import controlP5.*;
import org.philhosoft.p8g.svg.P8gGraphicsSVG;
import processing.core.PApplet;
import processing.core.PFont;
import toxi.geom.Rect;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletSpring2D;
import toxi.processing.ToxiclibsSupport;
import util.Color;
import util.XGen;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.text.DecimalFormat;
import java.util.Arrays;

public class App extends PApplet {
	public static final Rect BOUNDS = new Rect(350, 50, 1100, 800), INBOX = new Rect(0, 0, 600, 100).translate(500, 400);
	public static final DecimalFormat DF3 = new DecimalFormat("#.###");
	public static boolean RECORDING = false;
	public static boolean UPDATE_PHYSICS, UPDATE_PHYSVAL, UPDATE_VORONOI;
	public static boolean SHOW_PARTICLES, SHOW_SPRINGS, SHOW_ATTRACTORS;
	public static boolean SHOW_VORONOI, SHOW_VOR_VERTS, SHOW_VOR_INFO, SHOW_VOIDS;
	public static boolean SHOW_INFO, SHOW_NODES;
	public static float ZOOM = 1, SCALE = 10, DRAG = 0.5f;
	public static float VOR_REFRESH = 1;
	public static float SPR_SCALE = 1, SPR_STR = 0.01f;
	public static float ATTR_RAD = 60, ATTR_STR = -0.9f;
	public static float NODE_STR = -1, NODE_SCALE = 1, NODE_PAD = 1;
	//	public static int ITER_A, ITER_B, ITER_C, ITER_D, ITER_ROOT;
//	public static float SIZE_A, SIZE_B, SIZE_C, SIZE_D, SIZE_ROOT;
	public static PApplet P5;
	public static ControlP5 CP5;
	public static PFont UBUNTU10;
	public static ToxiclibsSupport GFX;
	public static PSys PSYS;
	public static VSys VSYS;
	public static Vec2D MOUSE = new Vec2D();
	public static FSys FSYS;
	public static XGen gen = new XGen();
	public static String DRAWMODE = "bezier";
	//	Dong[][] d;	int nx = 10;	int ny = 10;
	public int[] ILIST;
	public float[] SLIST;
	public String[] NLIST;

	public static void main(String[] args) { PApplet.main(new String[]{("main.App")}); }

	public void setup() {
		P5 = this;
		GFX = new ToxiclibsSupport(this);
		CP5 = new ControlP5(this);
		frame.setTitle(this.sketchPath);
		UBUNTU10 = loadFont("./lib/fonts/Ubuntu10.vlw");/*UBUNTU10 = createFont("Ubuntu",10,true);*/
		size(1800, 900);
		frameRate(60);
		smooth(4);
		colorMode(HSB, 100);
		ellipseMode(CENTER);
		textFont(UBUNTU10);
		textAlign(LEFT);
		strokeWeight(1);
		noStroke();
		noFill();
		setupCP5();
	}

	public void draw() {
		background(Color.BG);
		MOUSE.set(mouseX, mouseY);
		noStroke();
		fill(Color.BG_MENUS);
		rect(0, 0, 300, height); rect(1580, 0, width, height);
		noFill();
		pushMatrix();
		translate(-((ZOOM * width) - width) / 2, -((ZOOM * height) - height) / 2); scale(ZOOM);
		if (PSYS != null) PSYS.display();
		if (VSYS != null) VSYS.display();
		if (FSYS != null) FSYS.display(this);
		if (RECORDING) { RECORDING = false; endRecord(); System.out.println("SVG EXPORTED SUCCESSFULLY"); }
		popMatrix();
		CP5.draw();
	}

	void loadFGSYS() {
		try {
			JAXBContext context = JAXBContext.newInstance(FSys.class);
			FSYS = (FSys) context.createUnmarshaller().unmarshal(createInput("./lib/config/flowgraph.xml"));
		} catch (JAXBException e) { println("error parsing xml: "); e.printStackTrace(); System.exit(1); }
		PSYS = new PSys(this);
		VSYS = new VSys(this);
		FSYS.build();
		setupNodes();
		setupRelations();
	}

	void setupNodes() {
		for (FSys.Node c : FSYS.nodes) {
			PSYS.addParticle(c.getVerlet(), c.getBehavior());
			VSYS.addCell(c.getVerlet());
		}
	}

	void setupRelations() {
		for (FSys.Relation r : FSYS.relations) {
			FSys.Node na = FSYS.nodeIndex.get(r.from);
			FSys.Node nb = FSYS.nodeIndex.get(r.to);
			VerletParticle2D va = na.verlet;
			VerletParticle2D vb = nb.verlet;
			float l = na.getRadius() + nb.getRadius();
			PSYS.addSpring(new VerletSpring2D(va, vb, l, 0.01f));
		}
	}

	void addRandom(int cnt) {
		for (int i = 0; i < cnt; i++) {
			Vec2D v = new Vec2D(App.BOUNDS.getRandomPoint());
			PSYS.addAttractor(v);
		}
	}

	void addPerim(int res) {
		for (int i = 0; i < App.BOUNDS.height; i += res) {
			Vec2D vl = new Vec2D(App.BOUNDS.getLeft() + 20, i + App.BOUNDS.getTop());
			Vec2D vr = new Vec2D(App.BOUNDS.getRight() - 20, i + App.BOUNDS.getTop());
			PSYS.addAttractor(vl);
			PSYS.addAttractor(vr);
		}
		for (int j = 0; j < App.BOUNDS.width; j += res) {
			Vec2D vt = new Vec2D(j + App.BOUNDS.getLeft(), App.BOUNDS.getTop() + 20);
			Vec2D vb = new Vec2D(j + App.BOUNDS.getLeft(), App.BOUNDS.getBottom() - 20);
			PSYS.addAttractor(vt);
			PSYS.addAttractor(vb);
		}
	}

	public void keyPressed() {
		switch (key) {
			case '1': DRAWMODE = "none"; break;
			case '2': DRAWMODE = "verts"; break;
			case '3': DRAWMODE = "bezier"; break;
			case '4': DRAWMODE = "poly"; break;
			case '5': DRAWMODE = "info"; break;
			case '6': DRAWMODE = "debug"; break;
		}
	}

	public void mouseMoved() { MOUSE.set(mouseX, mouseY);}

	public void mouseDragged() { if (PSYS != null) PSYS.mouseDragged(); }

	public void mousePressed() { if (PSYS != null) PSYS.mousePressed(); }

	public void mouseReleased() { if (PSYS != null) PSYS.mouseReleased(); }

	void setupCP5() {
		CP5.setFont(new ControlFont(UBUNTU10, 10));
		CP5.setAutoDraw(false);
		CP5.setColorBackground(Color.CP5_BG).setColorForeground(Color.CP5_FG).setColorCaptionLabel(Color.CP5_CAP).setColorActive(Color.CP5_ACT).setColorValueLabel(Color.CP5_VAL);
		FrameRate FPS = CP5.addFrameRate();
		FPS.setInterval(3).setPosition(20, height - 20).draw();
		setupButtons();
		setupToggles();
		setupSliders();
		setupNumbox();
		styleControllers();
	}

	void setupButtons() {
		CP5.begin(20, 10);
		CP5.addButton("quit").linebreak();
		CP5.addButton("regen").linebreak();
		CP5.addButton("load_xml").linebreak();
		CP5.addButton("save_svg").linebreak();
		CP5.addButton("load_def").linebreak();
		CP5.addButton("load_conf").linebreak();
		CP5.addButton("save_def").linebreak();
		CP5.addButton("save_conf").linebreak();
		CP5.addButton("add_rand").linebreak();
		CP5.addButton("add_perim").linebreak();
		CP5.addButton("get_gen").linebreak();
		CP5.end();
	}

	void quit(int theValue) { exit(); }

	void regen(int theValue) {gen.generate();}

	void load_xml(int theValue) { loadFGSYS(); }

	void save_svg(int theValue) { beginRecord(P8gGraphicsSVG.SVG, "./out/svg/print-###.svg"); RECORDING = true;}

	void load_conf(int theValue) { CP5.loadProperties(("./lib/config/config.ser"));}

	void load_def(int theValue) { CP5.loadProperties(("./lib/config/defaults.ser"));}

	void save_conf(int theValue) { CP5.saveProperties(("./lib/config/config.ser")); }

	void save_def(int theValue) { CP5.saveProperties(("./lib/config/defaults.ser")); }

	void add_rand(int theValue) { addRandom(9); }

	void add_perim(int theValue) {addPerim(50);}

	void get_gen(int theValue) {/*gen.config(ILIST,SLIST,NLIST); */
		for (Textfield t : CP5.getAll(Textfield.class)) {t.submit();}
		gen.generate();
		loadFGSYS();
	}

	void setupToggles() {
		CP5.begin(150, 10);
		CP5.addToggle("UPDATE_PHYSICS").linebreak();
		CP5.addToggle("UPDATE_VORONOI").linebreak();
		CP5.addToggle("UPDATE_PHYSVAL").linebreak();
		CP5.addToggle("SHOW_ATTRACTORS").linebreak();
		CP5.addToggle("SHOW_INFO").linebreak();
		CP5.addToggle("SHOW_PARTICLES").linebreak();
		CP5.addToggle("SHOW_SPRINGS").linebreak();
		CP5.addToggle("SHOW_NODES").linebreak();
		CP5.addToggle("SHOW_VORONOI").linebreak();
		CP5.addToggle("SHOW_VOIDS").linebreak();
		CP5.addToggle("SHOW_VOR_VERTS").linebreak();
		CP5.addToggle("SHOW_VOR_INFO").linebreak();
		CP5.end();
	}

	void setupSliders() {
		CP5.begin(20, 10);
		CP5.addSlider("VOR_REFRESH").setRange(1, 9).setNumberOfTickMarks(9).linebreak();
		CP5.addSlider("ZOOM").setRange(0.1f, 5).linebreak();
		CP5.addSlider("DRAG").setRange(0.1f, 1).linebreak();
		CP5.addSlider("SCALE").setRange(1, 20).setNumberOfTickMarks(20).linebreak();
		CP5.addSlider("SPR_SCALE").setRange(0.1f, 2).setNumberOfTickMarks(20).linebreak();
		CP5.addSlider("SPR_STR").setRange(0.01f, 0.03f).linebreak();
		CP5.addSlider("NODE_SCALE").setRange(0.1f, 2).setNumberOfTickMarks(20).linebreak();
		CP5.addSlider("NODE_STR").setRange(-20, 0).linebreak();
		CP5.addSlider("NODE_PAD").setRange(0.1f, 9).linebreak();
		CP5.addSlider("ATTR_RAD").setRange(0.1f, 400).linebreak();
		CP5.addSlider("ATTR_STR").setRange(-4f, 4).linebreak();
		CP5.end();
	}

	void setupNumbox() {
		CP5.addTextfield("iterator").setPosition(20, 10).setText("2 3 4 6").linebreak();
		CP5.addTextfield("sizes").setPosition(20, 30).setText("80 45 10 5").linebreak();
		CP5.addTextfield("names").setPosition(20, 50).setText("A B C D").linebreak();
		CP5.begin(20, 80);
		CP5.addNumberbox("ITER_A").setRange(0, 10).setDirection(Controller.HORIZONTAL).setMultiplier(0.05f).setDecimalPrecision(0).linebreak();
		CP5.addNumberbox("ITER_B").setRange(0, 10).setDirection(Controller.HORIZONTAL).setMultiplier(0.05f).setDecimalPrecision(0).linebreak();
		CP5.addNumberbox("ITER_C").setRange(0, 10).setDirection(Controller.HORIZONTAL).setMultiplier(0.05f).setDecimalPrecision(0).linebreak();
		CP5.addNumberbox("ITER_D").setRange(0, 10).setDirection(Controller.HORIZONTAL).setMultiplier(0.05f).setDecimalPrecision(0).linebreak();
		CP5.addNumberbox("ITER_E").setRange(0, 10).setDirection(Controller.HORIZONTAL).setMultiplier(0.05f).setDecimalPrecision(0).linebreak();
		CP5.end();
	}

	public void iterator(String theText) {
		println("a textfield event for controller 'input' : " + theText);
		String[] temp = splitTokens(theText, " ,.?!:;[]-\"");
		int[] ilist = new int[temp.length];
		for (int i = 0; i < temp.length; i++) {
			ilist[i] = Integer.valueOf(temp[i]);
			System.out.println(ilist[i]);
		} //ILIST = ilist;		System.out.println(Arrays.toString(ILIST));
		gen.setIters(ilist);
	}

	public void sizes(String theText) {
		println("a textfield event for controller 'input' : " + theText);
		String[] temp = splitTokens(theText, " ,.?!:;[]-\"");
		float[] slist = new float[temp.length];
		for (int i = 0; i < temp.length; i++) {
			slist[i] = Float.valueOf(temp[i]);
			System.out.println(slist[i]);
		} //SLIST = slist;		System.out.println(Arrays.toString(SLIST));
		gen.setSizes(slist);
	}

	public void names(String theText) {
		println("a textfield event for controller 'input' : " + theText);
		String[] nlist = splitTokens(theText, " ,.?!:;[]-\"");
		for (String aNlist : nlist) {
			System.out.println(aNlist);
		}
		NLIST = nlist;
		System.out.println(Arrays.toString(NLIST));
		gen.setNames(nlist);
	}

	void styleControllers() {
		//		CP5.loadProperties(("./lib/config/defaults.ser"));
		Group file = CP5.addGroup("FILE").setPosition(0, 20);
		Group config = CP5.addGroup("CONFIG").setPosition(0, 350);
		Group generator = CP5.addGroup("GENERATOR").setPosition(0, 600);
		for (Textfield t : CP5.getAll(Textfield.class)) {
			t.setSize(200, 16).setAutoClear(false).setGroup(generator); t.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).getStyle().setPaddingRight(4);
		} for (Button b : CP5.getAll(Button.class)) {
			b.setSize(80, 16).setGroup(file); b.getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
		} for (Numberbox b : CP5.getAll(Numberbox.class)) {
			b.setSize(80, 16).setGroup(generator); b.getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER); b.getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER);
		} for (Toggle t : CP5.getAll(Toggle.class)) {
			t.setSize(16, 16).setGroup(file); t.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).getStyle().setPaddingLeft(4);
		} for (RadioButton t : CP5.getAll(RadioButton.class)) {
			t.setSize(16, 16).setGroup(file); t.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).getStyle().setPaddingLeft(4);
		} for (Slider s : CP5.getAll(Slider.class)) {
			s.setSize(150, 16).setGroup(config).showTickMarks(false); s.setColorForeground(Color.CP5_ACT).setColorActive(Color.CP5_FG);
			s.getValueLabel().align(ControlP5.RIGHT, ControlP5.CENTER); s.getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).getStyle().setPaddingLeft(4);
		} for (Group g : CP5.getAll(Group.class)) {
			g.setSize(300, 16).setBarHeight(16); g.getCaptionLabel().getStyle().setPaddingLeft(16);
		}
	}
}
//		CP5.addMatrix("myMatrix").setPosition(20, 600).setSize(200, 200).setGrid(nx, ny).setGap(10, 1).setInterval(200).setMode(ControlP5.MULTIPLES).setColorBackground(color(120)).setBackground(color(40)).stop();
	/*		CP5.addSlider("ITER_ROOT").setRange(1, 9).setNumberOfTickMarks(9).linebreak();
		CP5.addSlider("ITER_A").setRange(1, 9).setNumberOfTickMarks(9).linebreak();
		CP5.addSlider("ITER_B").setRange(1, 9).setNumberOfTickMarks(9).linebreak();
		CP5.addSlider("ITER_C").setRange(1, 9).setNumberOfTickMarks(9).linebreak();
		CP5.addSlider("ITER_D").setRange(1, 9).setNumberOfTickMarks(9).linebreak();
		CP5.addSlider("SIZE_ROOT").setRange(1, 100).linebreak();
		CP5.addSlider("SIZE_A").setRange(1, 100).linebreak();
		CP5.addSlider("SIZE_B").setRange(1, 100).linebreak();
		CP5.addSlider("SIZE_C").setRange(1, 100).linebreak();
		CP5.addSlider("SIZE_D").setRange(1, 100).linebreak();*//*
	public void myMatrix(int theX, int theY) {
		println("got it: " + (theX + 1) + ", " + (theY + 1));
//		d[theX][theY].update();
	}

	class Dong {
		float x, y;
		float s0, s1;

		Dong() {
			float f = random(-PI, PI);
			x = cos(f) * random(100, 150);
			y = sin(f) * random(100, 150);
			s0 = random(2, 10);
		}

		void display() {
			s1 += (s0 - s1) * 0.1;
			ellipse(x, y, s1, s1);
		}

		void update() { s1 = 50; }
	}
*/
