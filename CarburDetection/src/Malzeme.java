import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.*;
import org.opencv.highgui.*;
public class Malzeme {

		public static void main(String args[]){
	    // Load the library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	    
		File folder = new File("/media/ipcvlab/SULEYMAN EKEN/masaustu-6-2/Çalışmalarım/makaleler/10-malzeme paper/malzeme analizi/malzeme_gor");
		File[] listOfFiles = folder.listFiles();

		for (int k = 0; k < listOfFiles.length; k++) {
			// temporary image definitions
		    Mat image = Highgui.imread(folder.toString()+"/"+ listOfFiles[k].getName(), 1);
		    Mat gray = new Mat(image.size(), CvType.CV_8UC4);
		    Mat img_blurr = new Mat(image.size(), CvType.CV_8UC4);
		    Mat img_thresh = new Mat(image.size(), CvType.CV_32F);
		    Mat img_edge = new Mat(image.size(), CvType.CV_8UC4);
		    Mat img_morp = new Mat(image.size(), CvType.CV_8UC4);

		    // Consider the image for processing
		    Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
		    Imgproc.GaussianBlur(gray, img_blurr, new Size(9, 9), 3, 3);
		    //Core.addWeighted(img_blurr, 1.5, img_blurr, -0.5, 0, img_blurr);

		    double otsu_thresh_val = Imgproc.threshold(img_blurr, img_thresh, 0, 255, Imgproc.THRESH_OTSU);
	        double high_thresh_val = otsu_thresh_val, lower_thresh_val = otsu_thresh_val * 0.33;
		    //Imgproc.adaptiveThreshold(img_blurr, img_thresh, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7, 5);
	        Imgproc.Canny(img_thresh, img_edge, lower_thresh_val, high_thresh_val);

	        Imgproc.morphologyEx(img_edge, img_morp, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
	        Highgui.imwrite("/home/ipcvlab/Desktop/test1.png",img_thresh);
	        Highgui.imwrite("/home/ipcvlab/Desktop/test2.png",img_edge);
	        
		    //finding contour
		    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		    Mat hierarchy = new Mat();
		    Imgproc.findContours(img_morp, contours, hierarchy, Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

		    int toplam_alan=0;
		    for(int i=0; i< contours.size();i++){
		        //System.out.println(Imgproc.contourArea(contours.get(i)));
		        //toplam_alan+=Imgproc.contourArea(contours.get(i));
		        //System.out.print(Imgproc.contourArea(contours.get(i))+" ");
		       // if (Imgproc.contourArea(contours.get(i)) > 50 ){
		            Rect rect = Imgproc.boundingRect(contours.get(i));
		            //System.out.println(rect.height);
		            //if (rect.height > 28){
		            //System.out.println(rect.x +","+rect.y+","+rect.height+","+rect.width);
		            	//Core.rectangle(image, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
		            	Imgproc.drawContours(image, contours, i, new Scalar(0, 0, 255), 1, 8, hierarchy, 0, new Point());
		            	//Imgproc.drawContours(image, contours, i, new Scalar(0, 0, 255), -1);  //ici doldur
		           // }
		        //}
		    }

		 // Finding the convex hull -------------------------------------
	        List<MatOfInt> hull = new ArrayList<MatOfInt>();
	        for(int i=0; i < contours.size(); i++){
	            hull.add(new MatOfInt());
	        }
	        for(int i=0; i < hull.size(); i++){
	            Imgproc.convexHull(contours.get(i), hull.get(i));
	        }

	        // Convert MatOfInt to MatOfPoint for drawing convex hull
	        // Loop over all contours
	        List<Point[]> hullpoints = new ArrayList<Point[]>();
	        for(int i=0; i < hull.size(); i++){
	            Point[] points = new Point[hull.get(i).rows()];

	            // Loop over all points that need to be hulled in current contour
	            for(int j=0; j < hull.get(i).rows(); j++){
	                int index = (int)hull.get(i).get(j, 0)[0];
	                points[j] = new Point(contours.get(i).get(index, 0)[0], contours.get(i).get(index, 0)[1]);
	            }

	            hullpoints.add(points);
	        }

	        // Convert Point arrays into MatOfPoint
	        List<MatOfPoint> hullmop = new ArrayList<MatOfPoint>();
	        for(int i=0; i < hullpoints.size(); i++){
	            MatOfPoint mop = new MatOfPoint();
	            mop.fromArray(hullpoints.get(i));
	            hullmop.add(mop);
	        }

	        // Draw contours + hull results
	        Mat overlay = new Mat(image.size(), CvType.CV_8UC3);
	        Scalar color = new Scalar(0, 255, 0);   // Green
	        for(int i=0; i < contours.size(); i++){
	            //Imgproc.drawContours(overlay, contours, i, color);
	            Imgproc.drawContours(overlay, hullmop, i, color, -1);
	            //Imgproc.circle( overlay, center.get(i), (int)radius.get(i, 0)[0], color, 2, 8, 0 );
	        }

	        for(int i=0; i< hullmop.size();i=i+2){ //i=i+2
	        	toplam_alan+=Imgproc.contourArea(hullmop.get(i));
	        	System.out.print(Imgproc.contourArea(hullmop.get(i))+" ");}
	        System.out.println();
	        System.out.println("ortalama area= "+listOfFiles[k].getName()+" "+toplam_alan/hullmop.size());
	        System.out.println("conveks adet= "+listOfFiles[k].getName()+" "+hullmop.size()/2);

		    //-------------end of convex hull----------------------------------------------

	        System.out.println("carbur coverage ratio= "+listOfFiles[k].getName()+" "+(double)toplam_alan/(image.height()*image.width())*100);
		    //System.out.println("total area= "+toplam_alan);
		    //System.out.println("image size= "+image.height()*image.width());
	        System.out.println();
		    Highgui.imwrite("/home/ipcvlab/Desktop/contour.png",image);
		    Highgui.imwrite("/home/ipcvlab/Desktop/convex.png",overlay);
		    /*BufferedImage ekran = Mat2BufferedImage(overlay);
		    displayImage(ekran);*/
		    }
		
		
	}
}