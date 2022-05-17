/**
 * Global variables
 */
var datasetTable = undefined;
var datasetMap = undefined;
var drawnItems = undefined;

/**
 * The AtoN Messages Table Column Definitions
 * @type {Array}
 */
var datasetColumnDefs = [
{
    data: "id",
    title: "ID",
    hoverMsg: "The Dataset ID",
    placeholder: "The Dataset ID",
    type: "hidden",
    visible: false,
    searchable: false
}, {
    data: "datasetIdentificationInformation.encodingSpecification",
    title: "Encoding",
    hoverMsg: "The Dataset Encoding",
    placeholder: "The Dataset Encoding"
 }, {
     data: "datasetIdentificationInformation.encodingSpecificationEdition",
     title: "Encoding Edition",
     hoverMsg: "The Dataset Encoding Edition",
     placeholder: "The Dataset Encoding Edition",
 }, {
    data: "datasetIdentificationInformation.productIdentifier",
    title: "Product Identifier",
    hoverMsg: "The Dataset Product Identifier",
    placeholder: "The Dataset Product Identifier"
}, {
    data: "datasetIdentificationInformation.productEdition",
    title: "Product Edition",
    hoverMsg: "The Dataset Product Edition",
    placeholder: "The Dataset Product Edition"
 }, {
     data: "datasetIdentificationInformation.applicationProfile",
     title: "Application Profile",
     hoverMsg: "The Dataset Application Profile",
     placeholder: "The Dataset Application Profile",
}, {
    data: "datasetIdentificationInformation.datasetFileIdentifier",
    title: "File Identifier",
    hoverMsg: "The Dataset File Identifier",
    placeholder: "The Dataset File Identifier",
 }, {
    data: "geometry",
    title: "Geometry",
    hoverMsg: "The Dataset Geometry",
    placeholder: "The Dataset Geometry",
    type: "hidden",
    visible: false,
    searchable: false
 },{
    data: "datasetIdentificationInformation.datasetAbstract",
    title: "Abstract",
    type: "textarea",
    hoverMsg: "The Dataset Abstract",
    placeholder: "The Dataset Abstract",
    visible: false,
    searchable: false
}];

// Run when the document is ready
$(function () {
    // And re-initialise it
    datasetTable = $('#dataset_table').DataTable({
        processing: true,
        language: {
            processing: '<i class="fa-solid fa-spinner fa-spin fa-3x fa-fw"></i><span class="sr-only">Loading...</span>',
        },
        serverSide: true,
        ajax: {
            type: "POST",
            url: "./api/dataset/dt",
            contentType: "application/json",
            data: function (d) {
                return JSON.stringify(d);
            },
            error: function (jqXHR, ajaxOptions, thrownError) {
                console.error(thrownError);
            }
        },
        columns: datasetColumnDefs,
        dom: "<'row'<'col-lg-2 col-md-2'B><'col-lg-2 col-md-2'l><'col'f>><'row'<'col-md-12'rt>><'row'<'col-md-6'i><'col-md-6'p>>",
        select: 'single',
        lengthMenu: [10, 25, 50, 75, 100],
        responsive: true,
        altEditor: true, // Enable altEditor
        buttons: [{
            text: '<i class="fa-solid fa-plus"></i>',
            titleAttr: 'Add Dataset',
            name: 'add' // do not change name
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fa-solid fa-pen-to-square"></i>',
            titleAttr: 'Edit Dataset',
            name: 'edit' // do not change name
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fa-solid fa-trash-can"></i>',
            titleAttr: 'Delete Dataset',
            name: 'delete' // do not change name
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fa-solid fa-map-location"></i>',
            titleAttr: 'View Dataset Area',
            name: 'datasetGeometry', // do not change name
            className: 'dataset-geometry-toggle',
            action: (e, dt, node, config) => {
                loadDatasetGeometry(e, dt, node, config);
            }
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fa-solid fa-code"></i>',
            titleAttr: 'View S-125 Dataset Content',
            name: 'datasetContent', // do not change name
            className: 'dataset-content-toggle',
            action: (e, dt, node, config) => {
                loadDatasetContent(e, dt, node, config);
            }
        }],
        onAddRow: function (datatable, rowdata, success, error) {
            $.ajax({
                url: './api/dataset',
                type: 'POST',
                contentType: 'application/json; charset=utf-8',
                crossDomain: true,
                dataType: 'json',
                data: JSON.stringify({
                    id: rowdata["id"],
                    datasetIdentificationInformation: {
                        encodingSpecification: rowdata["datasetIdentificationInformation.encodingSpecification"],
                        encodingSpecificationEdition: rowdata["datasetIdentificationInformation.encodingSpecificationEdition"],
                        productIdentifier: rowdata["datasetIdentificationInformation.productIdentifier"],
                        productEdition: rowdata["datasetIdentificationInformation.productEdition"],
                        applicationProfile: rowdata["datasetIdentificationInformation.applicationProfile"],
                        datasetFileIdentifier: rowdata["datasetIdentificationInformation.datasetFileIdentifier"],
                        datasetAbstract: rowdata["datasetIdentificationInformation.datasetAbstract"],
                    },
                    geometry: null
                }),
                success: success,
                error: error
            });
        },
        onEditRow: function (datatable, rowdata, success, error) {
            // The geometry is not read correctly so we need to access it in-direclty
            var idx = datasetTable.cell('.selected', 0).index();
            var data = datasetTable.rows(idx.row).data();
            var geometry = data[0].geometry;
            $.ajax({
                url: `./api/dataset/${rowdata["id"]}`,
                type: 'PUT',
                contentType: 'application/json; charset=utf-8',
                crossDomain: true,
                dataType: 'json',
                data: JSON.stringify({
                    id: rowdata["id"],
                    datasetIdentificationInformation: {
                        encodingSpecification: rowdata["datasetIdentificationInformation.encodingSpecification"],
                        encodingSpecificationEdition: rowdata["datasetIdentificationInformation.encodingSpecificationEdition"],
                        productIdentifier: rowdata["datasetIdentificationInformation.productIdentifier"],
                        productEdition: rowdata["datasetIdentificationInformation.productEdition"],
                        applicationProfile: rowdata["datasetIdentificationInformation.applicationProfile"],
                        datasetFileIdentifier: rowdata["datasetIdentificationInformation.datasetFileIdentifier"],
                        datasetAbstract: rowdata["datasetIdentificationInformation.datasetAbstract"],
                    },
                    geometry: rowdata["geometry"]
                }),
                success: success,
                error: error
            });
        },
        onDeleteRow: function (datatable, selectedRows, success, error) {
            selectedRows.every(function (rowIdx, tableLoop, rowLoop) {
                $.ajax({
                    type: 'DELETE',
                    url: `./api/dataset/${this.data()["id"]}`,
                    crossDomain: true,
                    success: success,
                    error: error
                });
            });
        }
    });

    // We also need to link the aton geometry toggle button with the the modal
    // panel so that by clicking the button the panel pops up. It's easier done
    // with jQuery.
    datasetTable.buttons('.dataset-geometry-toggle')
        .nodes()
        .attr({ "data-bs-toggle": "modal", "data-bs-target": "#datasetGeometryPanel" });

    // We also need to link the aton content toggle button with the the modal
    // panel so that by clicking the button the panel pops up. It's easier done
    // with jQuery.
    datasetTable.buttons('.dataset-content-toggle')
        .nodes()
        .attr({ "data-bs-toggle": "modal", "data-bs-target": "#datasetContentPanel" });

    // Now also initialise the aton geometry map before we need it
    datasetMap = L.map('datasetGeometryMap').setView([54.910, -3.432], 5);
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(datasetMap);

    // FeatureGroup is to store editable layers
    drawnItems = new L.FeatureGroup();
    datasetMap.addLayer(drawnItems);

    datasetMap.on('draw:created', function (e) {
        var type = e.layerType;
        var layer = e.layer;

        // Do whatever else you need to. (save to db, add to map etc)
        drawnItems.addLayer(layer);
    });

    // Invalidate the map size on show to fix the presentation
    $('#datasetGeometryPanel').on('shown.bs.modal', function() {
        setTimeout(function() {
            datasetMap.invalidateSize();
        }, 10);
    });
});

/**
 * This function will load the AtoN geometry onto the drawnItems variable
 * so that it is shown in the AtoN message maps layers.
 *
 * @param {Event}         event         The event that took place
 * @param {DataTable}     table         The AtoN messages table
 * @param {Node}          button        The button node that was pressed
 * @param {Configuration} config        The table configuration
 */
function loadDatasetGeometry(event, table, button, config) {
    var idx = table.cell('.selected', 0).index();
    var data = table.rows(idx.row).data();
    var geometry = data[0].geometry;

    // Recreate the drawn items feature group
    drawnItems.clearLayers();
    if(geometry) {
        var geomLayer = L.geoJson(geometry);
        addNonGroupLayers(geomLayer, drawnItems);
        datasetMap.setView(geomLayer.getBounds().getCenter(), 5);
    }
}

/**
 * This function will load the AtoN content onto the AtoN content dialog text
 * area.
 *
 * @param {Event}         event         The event that took place
 * @param {DataTable}     table         The AtoN messages table
 * @param {Node}          button        The button node that was pressed
 * @param {Configuration} config        The table configuration
 */
function loadDatasetContent(event, table, button, config) {
    var idx = table.cell('.selected', 0).index();
    var data = table.rows(idx.row).data();
    var content = data[0].content;

    // Show the content
    $('#datasetContentTextArea').val(content);

}

// Would benefit from https://github.com/Leaflet/Leaflet/issues/4461
function addNonGroupLayers(sourceLayer, targetGroup) {
    if (sourceLayer instanceof L.LayerGroup) {
        sourceLayer.eachLayer(function(layer) {
            addNonGroupLayers(layer, targetGroup);
        });
    } else {
        targetGroup.addLayer(sourceLayer);
    }
}


