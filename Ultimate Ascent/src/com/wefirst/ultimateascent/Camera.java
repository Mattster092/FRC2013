package com.wefirst.ultimateascent;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;

public class Camera {

    /*
     * Not sure how the camera and target-finding is going to go down, but it could be used in autonamous mode if the "instructions recorder" does not work properly
     * Also could be used since we might not know where we start
     * If we're using a launcher it might be more difficult to calibrate based on all three starting places
     *
     * The following method gets camera data, and then finds the center co-ordinate and size of all large, white rectangles on the image
     */
    public static void imageGrab(AxisCamera cam, CriteriaCollection cc) {
        ColorImage image;
        try {
            image = cam.getImage();
            BinaryImage thresholdImage = image.thresholdRGB(0, 45, 0, 45, 0, 45);   // keep only white objects
            BinaryImage bigObjectsImage = thresholdImage.removeSmallObjects(false, 2);  // remove small artifacts
            BinaryImage convexHullImage = bigObjectsImage.convexHull(false); // fill in occluded rectangles
            BinaryImage filteredImage = convexHullImage.particleFilter(cc); // find filled in rectangles

            ParticleAnalysisReport[] reports = filteredImage.getOrderedParticleAnalysisReports();  // get list of results
            for (int i = 0; i < reports.length; i++) {                                // print results
                ParticleAnalysisReport r = reports[i];
                System.out.println("Particle: " + i + ":  Center of mass x: " + r.center_mass_x);
            }
            System.out.println(filteredImage.getNumberParticles() + "  " + Timer.getFPGATimestamp());

            filteredImage.free();
            convexHullImage.free();
            bigObjectsImage.free();
            thresholdImage.free();
            image.free();
        } catch (AxisCameraException ex) {
            ex.printStackTrace();
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        }
    }
}
