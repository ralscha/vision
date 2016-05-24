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
    name : "joy",
    type : "string"
  }, {
    name : "sorrow",
    type : "string"
  }, {
    name : "anger",
    type : "string"
  }, {
    name : "surprise",
    type : "string"
  }, {
    name : "underExposed",
    type : "string"
  }, {
    name : "blurred",
    type : "string"
  }, {
    name : "headwear",
    type : "string"
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