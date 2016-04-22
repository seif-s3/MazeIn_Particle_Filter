var format_entitiesLineString = new ol.format.GeoJSON();
var features_entitiesLineString = format_entitiesLineString.readFeatures(geojson_entitiesLineString, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3786'});
var jsonSource_entitiesLineString = new ol.source.Vector();
jsonSource_entitiesLineString.addFeatures(features_entitiesLineString);var lyr_entitiesLineString = new ol.layer.Vector({
                source:jsonSource_entitiesLineString, 
                style: style_entitiesLineString,
                title: "entities LineString"
            });

lyr_entitiesLineString.setVisible(true);
var layersList = [lyr_entitiesLineString];
