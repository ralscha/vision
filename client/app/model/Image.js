Ext.define("Vision.model.Image",
{
  extend : "Vision.model.Base",
  requires : [ "Ext.data.proxy.Direct", "Ext.data.identifier.Negative" ],
  identifier : "negative",
  fields : [ {
    name : "id",
    type : "integer"
  }, {
    name : "name",
    type : "string"
  }, {
    name : "type",
    type : "string"
  }, {
    name : "size",
    type : "integer"
  }, {
    name : "thumbnail",
    type : "string",
    persist : false
  }, {
    name : "data",
    type : "string"
  } ],
  hasMany : [ "Label", "Logo", "Landmark", "Text", "Face" ],
  proxy : {
    type : "direct",
    api : {
      read : "imageController.read",
      create : "imageController.update",
      update : "imageController.update"
    },
    reader : {
      rootProperty : "records"
    }
  }
});