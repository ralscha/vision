Ext.define("Vision.model.Face",
{
  extend : "Vision.model.Base",
  fields : [ {
    name : "rollAngle",
    type : "number"
  }, {
    name : "panAngle",
    type : "number"
  }, {
    name : "tiltAngle",
    type : "number"
  }, {
    name : "detectionConfidence",
    type : "number"
  }, {
    name : "landmarkingConfidence",
    type : "number"
  }, {
    name : "joyRating",
    type : "number"
  }, {
    name : "sorrowRating",
    type : "number"
  }, {
    name : "angerRating",
    type : "number"
  }, {
    name : "surpriseRating",
    type : "number"
  }, {
    name : "underExposedRating",
    type : "number"
  }, {
    name : "blurredRating",
    type : "number"
  }, {
    name : "headwearRating",
    type : "number"
  } ]
});