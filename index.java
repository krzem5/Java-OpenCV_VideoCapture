import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.lang.Math;
import javax.swing.JComponent;
import javax.swing.JFrame;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;



class Canvas extends JComponent{
	Engine parent;



	public Canvas(Engine p){
		this.parent=p;
	}



	public void paintComponent(Graphics g){
		this.parent.draw(g);
		super.paintComponent(g);
		g.dispose();
	}
}



class Engine{
	public static final int DISPLAY_ID=0;
	public static final GraphicsDevice DEVICE=GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[DISPLAY_ID];
	public static final Rectangle WINDOW_SIZE=DEVICE.getDefaultConfiguration().getBounds();
	public static final int CAM_ID=0;
	public JFrame frame;
	public Canvas canvas;
	public VideoCapture cap;
	public Mat img_mat;
	public BufferedImage img;



	public Engine(){
		System.loadLibrary("./org/opencv_java411");
		Engine cls=this;
		this.frame=new JFrame("Capture");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setUndecorated(true);
		this.frame.setResizable(false);
		DEVICE.setFullScreenWindow(this.frame);
		this.canvas=new Canvas(this);
		this.canvas.setSize(WINDOW_SIZE.width,WINDOW_SIZE.height);
		this.canvas.setPreferredSize(new Dimension(WINDOW_SIZE.width,WINDOW_SIZE.height));
		this.frame.setContentPane(this.canvas);
		this.cap=new VideoCapture(CAM_ID);
		this.img_mat=new Mat();
		new Thread(){
			@Override
			public void run(){
				while (true){
					cls.canvas.repaint();
					try{
						Thread.sleep((int)(1000/60));
					}
					catch (InterruptedException e){}
				}
			}
		}.start();
	}



	public void draw(Graphics g){
		this.grab();
		g.drawImage(this.img,0,0,this.frame);
	}



	public double map_v(double v,double aa,double ab,double ba,double bb){
		return (v-aa)/(ab-aa)*(bb-ba)+ba;
	}



	public Scalar lerp(float v,Scalar a,Scalar b){
		return new Scalar(a.val[0]*(1-v)+b.val[0]*v,a.val[1]*(1-v)+b.val[1]*v,a.val[2]*(1-v)+b.val[2]*v,a.val[3]*(1-v)+b.val[3]*v);
	}



	public void detect(){
		CascadeClassifier faceC=new CascadeClassifier("./data/haarcascades/face.xml");
		CascadeClassifier eyesC=new CascadeClassifier("./data/haarcascades/eyepair_small.xml");
		CascadeClassifier noseC=new CascadeClassifier("./data/haarcascades/nose.xml");
		CascadeClassifier mouthC=new CascadeClassifier("./data/haarcascades/mouth.xml");
		MatOfRect faces=new MatOfRect();
		faceC.detectMultiScale(this.img_mat,faces);
		for (Rect r:faces.toArray()){
			int score=0;
			Mat face=new Mat(this.img_mat,r);
			MatOfRect eyep=new MatOfRect();
			eyesC.detectMultiScale(face,eyep);
			if (eyep.toArray().length>0){
				score+=(int)Math.max(this.map_v(eyep.toArray().length,1,5,33,3),0);
			}
			MatOfRect noses=new MatOfRect();
			noseC.detectMultiScale(face,noses);
			if (noses.toArray().length>0){
				score+=(int)Math.max(this.map_v(noses.toArray().length,1,5,33,3),0);
			}
			MatOfRect mouths=new MatOfRect();
			mouthC.detectMultiScale(face,mouths);
			if (mouths.toArray().length>0){
				score+=(int)Math.max(this.map_v(mouths.toArray().length,1,5,33,3),0);
			}
			Imgproc.putText(this.img_mat,Integer.toString(score)+"%",new Point(r.x,r.y-5),1,0.75,new Scalar(0,0,0,255),1);
			Imgproc.rectangle(this.img_mat,new Point(r.x,r.y),new Point(r.x+r.width,r.y+r.height),this.lerp((float)(score)/100,new Scalar(20,20,219,255),new Scalar(20,220,87,255)),4);
		}
	}



	public void grab(){
		if (this.cap==null){
			return;
		}
		this.cap.read(this.img_mat);
		if (this.img_mat.cols()==0){
			return;
		}
		this.detect();
		this.img=new BufferedImage(this.img_mat.cols(),this.img_mat.rows(),BufferedImage.TYPE_3BYTE_BGR);
		byte[] d=((DataBufferByte)this.img.getRaster().getDataBuffer()).getData();
		this.img_mat.get(0,0,d);
	}
}



public class index{
	public static void main(String[] args){
		new Engine();
	}
}
