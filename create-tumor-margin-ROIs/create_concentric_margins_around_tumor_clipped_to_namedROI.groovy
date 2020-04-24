// script that generates 3 concentric margin annotations around the currently selected ROI
// specified by the maximum distance to the border of the selected initial (lesion/tumor) ROI
// the three resulting margins are subsequently clipped to the the ROI given in clippingROIname (defaults to "Tissue")
// Tested on QuPath version 0.2.0-m10-SNAPSHOT
//
// Simon Kalteis 20.04.2020

import qupath.lib.objects.PathObjects


// PARAMETERS

// margin distances in microns
int distOuter = 500
int distMiddle = 200
int distInner = 100

// name of the ROI that is used to clip the created margin ROIs
def clippingROIname = "Tissue"





// START

def server = getCurrentServer()
def cal = server.getPixelCalibration()


// convert distances to length in average pixel
distOuterpx = (int)(distOuter / cal.getAveragedPixelSizeMicrons())
distMiddlepx = (int)(distMiddle / cal.getAveragedPixelSizeMicrons())
distInnerpx = (int)(distInner / cal.getAveragedPixelSizeMicrons())

// get currently selected annotations ROI
def roi = getSelectedROI()


// generate three ROIs concentric to the initial one with specified growth

def geometry_mOuter = roi.getGeometry()
geometry_mOuter = geometry_mOuter.buffer(distOuterpx)
def roi_mOuter = GeometryTools.geometryToROI(geometry_mOuter, roi.getImagePlane())

def geometry_mMiddle = roi.getGeometry()
geometry_mMiddle = geometry_mMiddle.buffer(distMiddlepx)
def roi_mMiddle = GeometryTools.geometryToROI(geometry_mMiddle, roi.getImagePlane())

def geometry_mInner = roi.getGeometry()
geometry_mInner = geometry_mInner.buffer(distInnerpx)
def roi_mInner = GeometryTools.geometryToROI(geometry_mInner, roi.getImagePlane())


// combineROIs subtracts second param from the first one
// outer ROI
def roi_mOuter_cut = RoiTools.combineROIs(roi_mOuter, roi_mMiddle, RoiTools.CombineOp.SUBTRACT)
// middle ROI
def roi_mMiddle_cut = RoiTools.combineROIs(roi_mMiddle, roi_mInner, RoiTools.CombineOp.SUBTRACT)
// inner ROI
def roi_mInner_cut = RoiTools.combineROIs(roi_mInner, roi, RoiTools.CombineOp.SUBTRACT)

// get the outside clipping ROI
def clippingroi = getAnnotationObjects().findAll {it.getName() == clippingROIname}

// clip margin ROIs to outside ROI
def roi_mOuter_cut_clipped = RoiTools.combineROIs(clippingroi[0].getROI(), roi_mOuter_cut, RoiTools.CombineOp.INTERSECT)
def roi_mMiddle_cut_clipped = RoiTools.combineROIs(clippingroi[0].getROI(), roi_mMiddle_cut, RoiTools.CombineOp.INTERSECT)
def roi_mInner_cut_clipped = RoiTools.combineROIs(clippingroi[0].getROI(), roi_mInner_cut, RoiTools.CombineOp.INTERSECT)

// actually generate annotation objects
def annotation_mOuter = PathObjects.createAnnotationObject(roi_mOuter_cut_clipped)
annotation_mOuter.name = "Margin_"+distOuter
def annotation_mMiddle = PathObjects.createAnnotationObject(roi_mMiddle_cut_clipped)
annotation_mMiddle.name = "Margin_"+distMiddle
def annotation_mInner = PathObjects.createAnnotationObject(roi_mInner_cut_clipped)
annotation_mInner.name = "Margin_"+distInner

// add annotations to imageserver
addObject(annotation_mOuter)
addObject(annotation_mMiddle)
addObject(annotation_mInner)