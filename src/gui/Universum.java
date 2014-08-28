package gui;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent2D;

import javax.media.j3d.Node;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleStripArray;
import javax.swing.JFrame;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * 06.11.2013
 * 
 * @author Christopher Glania MTS_21
 */

@SuppressWarnings("serial")
public class Universum extends JFrame implements KeyListener {

	private ArrayList<Alpha> timingListe = new ArrayList<Alpha>();
	boolean pausiert = false;

	/**
	 * Konstruktor für ein Universum
	 * 
	 * @param title
	 */
	public Universum(String title) {
		super(title);
		init();
		this.requestFocus();
		this.addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			//System.out.println("taste wurde gedrückt");
			if (pausiert == false) {
				allesStop();
			} else {
				allesWeiter();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	private void allesStop() {
		for (Alpha element : timingListe) {
			element.pause();
		}
		pausiert = true;
	}

	private void allesWeiter() {
		for (Alpha element : timingListe) {
			element.resume();
		}
		pausiert = false;
	}

	/**
	 * initialisiert Fenster - Größe des Fensters Wird eingestellt - SceneGraph
	 * wird erstellt
	 */
	private void init() {
		// AnzeigeInfos holen (Farbtiefe, usw...
		GraphicsConfiguration graphConfig = SimpleUniverse
				.getPreferredConfiguration();

		// ein neues Canvas mit den AnzeigeInfos erzeugen und dem Frame
		// hinzufügen
		Canvas3D canvas3D = new Canvas3D(graphConfig);

		// ein neues Universum im Canvas erzeugen und eine Betrachtungsebene
		// erzeugen
		SimpleUniverse universe = new SimpleUniverse(canvas3D);
		universe.getViewingPlatform().setNominalViewingTransform();

		universe.getCanvas().addKeyListener(this);

		this.add("Center", canvas3D);

		// Fenstergröße
		this.setSize(1600, 800);
		// Program soll beendet werden, wenn aus schließen geklickt wird
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		// Sichtbarkeit
		this.setVisible(true);
		// SceneGraph wird erstellt
		createSceneGraph(universe);
	}

	/**
	 * Methode welche die Eigenschaften der Maus steuert - Rotieren mit linker
	 * Maustaste - Zoom mit mittlerer Maustaste (Rädchen) - Verschiebungen mit
	 * rechter Maustaste
	 * 
	 * @return Transformgroup mit Mauseigenschaften
	 */
	private TransformGroup getTgMouseBehaviour() {
		TransformGroup tgMouseBehaviour = new TransformGroup();
		tgMouseBehaviour.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tgMouseBehaviour.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tgMouseBehaviour.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		tgMouseBehaviour.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

		// Behaviours definieren
		// Bounds, innerhalb deren man sich befinden muss
		BoundingSphere behaveBounds = new BoundingSphere();

		// rotieren mit der linken Maustaste
		MouseRotate behavior4 = new MouseRotate(tgMouseBehaviour);
		behavior4.setSchedulingBounds(behaveBounds);
		tgMouseBehaviour.addChild(behavior4);


		// zoomen mit der mitleren Maustaste
		MouseWheelZoom mouseBeh5 = new MouseWheelZoom(tgMouseBehaviour);
		mouseBeh5.setSchedulingBounds(behaveBounds);
		tgMouseBehaviour.addChild(mouseBeh5);

		// verschieben mit der rechten Maustaste
		MouseTranslate mouseBeh6 = new MouseTranslate(tgMouseBehaviour);
		mouseBeh6.setSchedulingBounds(behaveBounds);
		tgMouseBehaviour.addChild(mouseBeh6);

		return tgMouseBehaviour;
	}

	/**
	 * Umlaufbahn wird erzeugt dabei ist wichtig zu wissen das wir 2x(ein
	 * Transform3D, eine TransformGroup, eine Branchrop) erzeugen müssen, da
	 * sonst der Mittelpunkt als äußere Grenze genommen wird
	 * 
	 * @param translation
	 *            - Abstand von Mittelpunkt als Vektor
	 * @param rotationSpeed
	 *            -Rotationsgeschwindigkeit
	 * @param child
	 *            - Planet der Rotiert
	 * @param parent
	 *            -Planet um den Rotiert wird
	 * @return BranchGroup umlaufbahn
	 */
	private BranchGroup createUmlaufbahn(Vector3f translation,
			int rotationSpeed, Group child, BranchGroup parent) {
		// Neue BranchGroup Umlaufbahn wird erzeugt
		BranchGroup bgUmlaufbahn = new BranchGroup();
		// Neues Transform3D wird erzeugt - Beschreibt eine Bewegung
		Transform3D tmpTrafo = new Transform3D();
		// Abstand zum Mittelpunkt wird dem Tranform3D übergeben als Vektor
		tmpTrafo.setTranslation(translation);
		// Neue TransformGroup wird erstellt und das Transform 3D übergeben
		TransformGroup trafo = new TransformGroup(tmpTrafo);

		// Lese- & Schreibrechte für Transformgroup
		trafo.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		trafo.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		trafo.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		// BranchGroup darf mehrere Kinder haben
		bgUmlaufbahn.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

		// Ein Alpha(loopCount, increasingAlphaDuration)Objekt bestimmt
		// den Zeitpunkt der Veränderung eines visuellen Objektes durch
		// einen Interpolator.
		// loopCount-Anzahl der Male, dass das Alpha durchläuft, ein
		// Wert von -1 gibt an, dass das Alpha unbestimmte Zeit
		// durchläuft.
		// increasingAlphaDuration -Zeitraum
		// Alpha timing = new Alpha(-1, 4000);
		Alpha timing = new Alpha(-1, rotationSpeed);

		// Timing wird zur Timingliste hinzugefügt
		timingListe.add(timing);

		// Rotierender Planet wird der Transformgroup hinzugefügt
		trafo.addChild(child);

		// Hier wird eiegntliche Rotation erstellt,
		// Dazu wird dem Rotation Interpolator das timing, die TransformGroup,
		// das Transform3D, der Anfangs- und Endwinkel übergeben
		RotationInterpolator nodeRotator = new RotationInterpolator(timing,
				trafo, tmpTrafo, 0.0f, (float) Math.PI * 2.0f);

		// Rahmen / Grenzen für Rotation werden festgelegt
		nodeRotator.setSchedulingBounds(new BoundingSphere(new Point3d(0.0,
				0.0, 0.0), Float.MAX_VALUE));

		// RotationInterpolator wird der Branchgroup als Kind hinzugefügt
		bgUmlaufbahn.addChild(nodeRotator);

		// Transform3D wird der Branchgroup hinzugefügt
		bgUmlaufbahn.addChild(trafo);

		// Neue Branchgroup wird erzeugt, da bis jetzt ein Planet imme durch den
		// Mittelpunkt Rotiert
		BranchGroup fertigeUmlaufbahn = new BranchGroup();

		// Neues Transform 3D wird erzeugt
		Transform3D tmpTrafo2 = new Transform3D();

		// dem neuen Transform3D werden die entgegengesetzten Werte für Grenzen
		// der Umlaufbahn hinzugefügt
		tmpTrafo2.setTranslation(new Vector3f(-translation.x, -translation.y,
				-translation.z));
		// Neue TaransformGroup wird erzeugt und neues Taransform3D wird
		// übergeben
		TransformGroup trafo2 = new TransformGroup(tmpTrafo2);

		// Neue Transformgroup übernimmt alte Branchroup als Kind
		trafo2.addChild(bgUmlaufbahn);

		// Neue Umlaufbahn übernimmt neue TransformGroup inkl. alte Branchgroup
		fertigeUmlaufbahn.addChild(trafo2);

		// Fertige Umlaufbahn wird übergeben
		return fertigeUmlaufbahn;
	}

	/**
	 * Erstellt das Universum
	 * 
	 * hier werden die Objekte der Planeten und deren Umlaufbahnen erzeugt
	 * 
	 * @param universe
	 * @return Branchgroup mit allen Planenten und Umlaufbahnen
	 */
	private BranchGroup createSceneGraph(SimpleUniverse universe) {

		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		try {
			image = ImageIO.read(getClass().getClassLoader()
					.getResource("milchstrasse.jpg"));
		} catch (IOException e) {
		}
		
		ImageComponent2D image2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGB, image);
		
		Background background = new Background(image2d);
		background.setImageScaleMode(Background.SCALE_FIT_MAX);//SCALE_FIT_MAX);
		background.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), 10));
		
		// Wurzel in der alle Planeten eingefügt werden
		BranchGroup bgRoot = new BranchGroup();

		// TransformGroup mit Mausverhalten
		TransformGroup tgMouseBehaviour = getTgMouseBehaviour();

		// Mond wird erstellt
		BranchGroup Mond = createMoon();

		// Erde wird erstellt mit Bild für Oberfläche, Größe und Geschwindigkeit
		// um sich selbst
		BranchGroup Erde = createPlanet("earthmap1k.jpg", 0.125f, 3000);

		// Sonne wird erstellt
		BranchGroup Sonne = createSonne();

		// Ab jetzt wirds Objektorientiert:
		// Es wurde ein Methode create Planet geschrieben
		// dieser wird Bild Größe und Geschw. um sich selbst übergeben dabei
		// wird aber immer die Selbe Methode verwendet
		BranchGroup Merkur = createPlanet("mercurymap.jpg", 0.0375f, 2000);
		BranchGroup Venus = createPlanet("venusmap.jpg", 0.115f, 2300);
		BranchGroup Mars = createPlanet("marsmap1k.jpg", 0.095f, 2300);
		BranchGroup Jupiter = createPlanet("jupitermap.jpg", 0.35f, 2300);
		BranchGroup Saturn = createPlanet("saturnmap.jpg", 0.3f, 2300);
		BranchGroup SaturnRing = createRing(0.65, 0.4,
				"saturnringtransparent.png");
		BranchGroup Uranus = createPlanet("uranusmap.jpg", 0.15f, 2300);
		BranchGroup UranusRing = createRing(0.45, 0.2,
				"uranusringtransparent.png");
		BranchGroup Neptun = createPlanet("neptunemap.jpg", 0.14f, 2300);

		// Mausverhalten wird nur fpür die Sonne übernommen weil wir Prinzipiell
		// nur diese betrachten
		tgMouseBehaviour.addChild(Sonne);

		// Der Wurzel wird dasMausverhalten übergeben
		bgRoot.addChild(tgMouseBehaviour);
		
		bgRoot.addChild(background);
		
		// dem Universum wird der Wurzelknoten übergeben
		universe.addBranchGraph(bgRoot);

		// Sonne bekommt mehrere Umlaufbahnen als Kinder übergeben
		// Zu jedem Planeten eine
		Sonne.addChild(createUmlaufbahn(new Vector3f(-2.25f, 0f, 0f), 3000,
				Merkur, Sonne));
		Sonne.addChild(createUmlaufbahn(new Vector3f(-3f, 0f, 0f), 8000, Venus,
				Sonne));
		Sonne.addChild(createUmlaufbahn(new Vector3f(-4f, 0f, 0f), 7000, Erde,
				Sonne));
		// die Erde bekommt extra Umlaufbahn für die Sonne
		Erde.addChild(createUmlaufbahn(new Vector3f(-0.3f, 0f, 0f), 1000, Mond,
				Erde));
		Sonne.addChild(createUmlaufbahn(new Vector3f(-5.25f, 0f, 0f), 3000,
				Mars, Sonne));
		Sonne.addChild(createUmlaufbahn(new Vector3f(-7.25f, 0f, 0f), 4000,
				Jupiter, Sonne));

		BranchGroup bgSaturnMitRing = new BranchGroup();

		bgSaturnMitRing.addChild(Saturn);
		bgSaturnMitRing.addChild(SaturnRing);
		//bgSaturnMitRing.addChild(createUmlaufbahn(new Vector3f(0f, 0f, 0f), 100, bgSaturnMitRing, bgSaturnMitRing));
		
		Group gedrehterSaturn = drehePlanet(bgSaturnMitRing, 45,0 , 2300);
		
		Sonne.addChild(createUmlaufbahn(new Vector3f(-9.25f, 0f, 0f), 5300,
				gedrehterSaturn, Sonne));

		BranchGroup bgUranusMitRing = new BranchGroup();
		bgUranusMitRing.addChild(Uranus);
		bgUranusMitRing.addChild(UranusRing);
		Group gedrehterUranus = drehePlanet(bgUranusMitRing, 90,90 , 2300);

		Sonne.addChild(createUmlaufbahn(new Vector3f(-12.25f, 0f, 0f), 4200,
				gedrehterUranus, Sonne));
		Sonne.addChild(createUmlaufbahn(new Vector3f(-13.25f, 0f, 0f), 9000,
				Neptun, Sonne));

		// Wurzelknoten Wird zurückgegeben
		return bgRoot;
	}

	private Group drehePlanet(BranchGroup bgAlt, int winkelInGrad, int winkelInGradSichtZurSonne, int geschwindigkeitUmSichSelbst) {
		Transform3D t3d = new Transform3D();
		Transform3D temp = new Transform3D();

		t3d.rotX((2 * Math.PI / 360) * winkelInGrad);
		temp.rotZ((2 * Math.PI / 360) * winkelInGradSichtZurSonne);
		t3d.mul(temp);
		
		TransformGroup tg = new TransformGroup(t3d);
		
		Alpha timing = new Alpha(-1, geschwindigkeitUmSichSelbst);
		timingListe.add(timing);

		tg.addChild(bgAlt);
		
		// Rotation wird festgelegt
		RotationInterpolator nodeRotator = new RotationInterpolator(timing, tg);
		
		tg.addChild(nodeRotator);
		
		return tg;
	}

	private BranchGroup createRing(double aussenRadius, double innenRadius,
			String bild) {
		BranchGroup bgRing = new BranchGroup();
		int genauigkeit = 98;
		Point3f[] koordinaten = new Point3f[genauigkeit * 2 + 2];
		for (int i = 0; i <= genauigkeit; ++i) {
			double winkel = 2 * Math.PI / genauigkeit * i;
			koordinaten[2 * i] = new Point3f(
					(float) (aussenRadius * Math.sin(winkel)), 0f,
					(float) (aussenRadius * Math.cos(winkel)));
			koordinaten[2 * i + 1] = new Point3f(
					(float) (innenRadius * Math.sin(winkel)), 0f,
					(float) (innenRadius * Math.cos(winkel)));
		}

		int[] streifenlaenge = { koordinaten.length };

		TriangleStripArray myTris = new TriangleStripArray(koordinaten.length,
				GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2,
				streifenlaenge);

		myTris.setCoordinates(0, koordinaten);

		Point2f[] points;
		for (int i = 0; i <= genauigkeit; ++i) {
			points = new Point2f[2];
			points[0] = new Point2f(0f, 0f);
			points[1] = new Point2f(1f, 0f);
			myTris.setTextureCoordinates(2 * i, points);
		}

		Appearance app = new Appearance();
		TextureLoader loader = new TextureLoader(getClass().getClassLoader()
				.getResource(bild), this);
		ImageComponent2D image = loader.getImage();
		Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
				image.getWidth(), image.getHeight());
		texture.setImage(0, image);
		TransparencyAttributes transat = new TransparencyAttributes();
		transat.setTransparencyMode(TransparencyAttributes.BLENDED);
		transat.setTransparency(1f);
		app.setTexture(texture);
		app.setTransparencyAttributes(transat);

	
		Shape3D myShape = new Shape3D(myTris, app);
		Node andereSeite = myShape.cloneTree();
		Transform3D t3d = new Transform3D();
		t3d.rotX(Math.PI);
		TransformGroup tg = new TransformGroup(t3d);
		tg.addChild(andereSeite);
		bgRing.addChild(myShape);
		bgRing.addChild(tg);
		return bgRing;
	}

	/**
	 * Erstellt die Sonne inkl der Rotation um die eigene Achse
	 * 
	 * - Es werden keine Parameter übergeben da diese Methode nur die Sonne
	 * erstellt und alle Ressourcen individuell geholt werden
	 * 
	 * @return Branchgroup Sonne
	 */
	private BranchGroup createSonne() {
		// Erstellt neue Branchroup
		BranchGroup bgSonne = new BranchGroup();

		// Eigenschaften der BranchGroup / Erweiterungen Kinder erlaubt sowie
		// das diese Schreibrechte besitzen
		bgSonne.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		bgSonne.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

		// Neue TransformGroup wird erstellt
		TransformGroup tgSonne = new TransformGroup();

		// Eigenschaften der TransformGroup / Erweiterungen Kinder erlaubt sowie
		// das diese Lese- / Schreibrechte besitzen
		tgSonne.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tgSonne.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tgSonne.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		tgSonne.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

		// Neues Aussehen wird erstellt
		Appearance aussehenSonne = new Appearance();

		// Dem Aussehen wird eine Textur hinzugefügt quasi das Bild
		aussehenSonne.setTexture(new TextureLoader(getClass().getClassLoader()
				.getResource("sunmap.jpg"), this).getTexture());

		// Größe der Sonne
		Sphere sonne = new Sphere(0.5f, Sphere.GENERATE_TEXTURE_COORDS, 100,
				aussehenSonne);

		Alpha timing = new Alpha(-1, 4000);
		timingListe.add(timing);

		// Rotation de Sonne
		RotationInterpolator nodeRotator = new RotationInterpolator(timing,
				tgSonne);

		// Grenzen / Rahmen der Rotation
		nodeRotator.setSchedulingBounds(new BoundingSphere(new Point3d(0.0,
				0.0, 0.0), Float.MAX_VALUE));

		// TransformGroup übernimmt Sonne als Child
		tgSonne.addChild(sonne);

		// BranchGroup übernimmt Rotiation als Child
		bgSonne.addChild(nodeRotator);

		// Branchgroup übernimmt TransformGroup als Child
		bgSonne.addChild(tgSonne);

		// Gibt BranchGroup der Sonne zurück
		return bgSonne;
	}

	/**
	 * Erstellt den Planeten inkl der Rotation um die eigene Achse gegen den
	 * Uhrzeigersinn (von Norden aus gesehen)
	 * 
	 * @param String
	 *            - Pfad für Bild des Planeten
	 * @param float - Größe des Planeten
	 * @param int - Geschwindigkeit um sich selbst
	 * 
	 * @return BranchGroup Planet
	 */
	private BranchGroup createPlanet(String planetenaussehenBild,
			float groessePlanet, int geschwindigkeitUmSichSelbst) {

		// Neue Branchgroup wird erstellt
		BranchGroup bgPlanet = new BranchGroup();

		// Neue TransformGroup wird erstellt
		TransformGroup tgPlanet = new TransformGroup();

		// Eigenschaften der TransformGroup
		tgPlanet.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tgPlanet.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tgPlanet.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		tgPlanet.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

		// Eigenschaften der BanchGroup
		bgPlanet.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		bgPlanet.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

		// Neues Transform 3D wird erzeugt - Beschreibt eine Bewegung
		Transform3D trafo = new Transform3D();

		// Transform 3D wird der TransformGroup hinzugefügt
		tgPlanet.setTransform(trafo);

		// Aussehen für Planet
		Appearance aussehenPlanet = new Appearance();

		// Bild wird auf das Aussehen gelegt
		aussehenPlanet.setTexture(new TextureLoader(getClass().getClassLoader()
				.getResource(planetenaussehenBild), this).getTexture());

		// Neue Kugel wird erstellt / Größe und aussehen werden der Kugel
		// übergeben somit wird Planet erstellt
		Sphere planet = new Sphere(groessePlanet,
				Sphere.GENERATE_TEXTURE_COORDS, 100, aussehenPlanet);

		Alpha timing = new Alpha(-1, geschwindigkeitUmSichSelbst);
		timingListe.add(timing);

		// Rotation wird festgelegt
		RotationInterpolator nodeRotator = new RotationInterpolator(timing,
				tgPlanet);

		// Grenzen der Rotation werden festgelegt
		nodeRotator.setSchedulingBounds(new BoundingSphere(new Point3d(0.0,
				0.0, 0.0), Float.MAX_VALUE));

		// Rotation wird der Branchgroup als Kind übergeben
		bgPlanet.addChild(nodeRotator);

		// planet wird der TransformGroup als Kind übergeben
		tgPlanet.addChild(planet);

		// TransformGroup wird der BranchGroup übergeben
		bgPlanet.addChild(tgPlanet);

		// Gibt BranchGroup Planet zurück
		return bgPlanet;
	}

	/**
	 * Erstellt den Mond ohne Rotation
	 * 
	 * @return BranchGroup Mond
	 */
	private BranchGroup createMoon() {
		float radiusMondDrehung = 1f;

		BranchGroup bgMond = new BranchGroup();
		// Transformgruppe werden zum manipulieren gesetzt

		// Beschreibt eine Bewegung
		Transform3D transform = new Transform3D();

		TransformGroup tgMondRotate = new TransformGroup(transform);
		tgMondRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tgMondRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tgMondRotate.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		tgMondRotate.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

		// Neues Aussehen wird erstellt
		Appearance aussehenMond = new Appearance();

		// Bild wird auf das Aussehen gelegt
		aussehenMond.setTexture(new TextureLoader(getClass().getClassLoader()
				.getResource("moonmap1k.jpg"), this).getTexture());

		// Größe des Mondes
		Sphere mond = new Sphere(0.0375f, Sphere.GENERATE_TEXTURE_COORDS, 100,
				aussehenMond);

		// Würfel wird der transform gruppe hinzugefügt
		tgMondRotate.addChild(mond);

		// Die transform gruppe wird dem branch hinzugefügt
		bgMond.addChild(tgMondRotate);

		// Neues Transform 3D wird erzeugt - Beschreibt eine Bewegung
		Transform3D transMond = new Transform3D();

		// Abstand zum Mittelpunkt wird dem Tranform3D übergeben als Vektor
		transMond.setTranslation(new Vector3f(radiusMondDrehung, 0, 0));

		// Gibt Branchgroup Mond zurück
		return bgMond;
	}

}
