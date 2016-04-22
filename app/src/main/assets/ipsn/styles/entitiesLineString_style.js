var size = 0;

var styleCache_entitiesLineString={}
var style_entitiesLineString = function(feature, resolution){
    var value = ""
    var size = 0;
    var style = [ new ol.style.Style({
        stroke: new ol.style.Stroke({color: "rgba(121,182,133,1.0)", lineDash: null, lineCap: 'square', lineJoin: 'bevel', width: 0})
    })];
    if ("" !== null) {
        var labelText = String("");
    } else {
        var labelText = ""
    }
    var key = value + "_" + labelText

    if (!styleCache_entitiesLineString[key]){
        var text = new ol.style.Text({
              font: '10px Calibri,sans-serif',
              text: labelText,
              textBaseline: "center",
              textAlign: "left",
              offsetX: 5,
              offsetY: 3,
              fill: new ol.style.Fill({
                color: "rgba(None, None, None, 255)"
              }),
            });
        styleCache_entitiesLineString[key] = new ol.style.Style({"text": text})
    }
    var allStyles = [styleCache_entitiesLineString[key]];
    allStyles.push.apply(allStyles, style);
    return allStyles;
};