/**
 * Global variables
 */
var atonMessagesTable = undefined;
var atonMessagesMap = undefined;
var drawnItems = undefined;

/**
 * The AtoN Messages Table Column Definitions
 * @type {Array}
 */
var nodesColumnDefs = [
{
    data: "atonUID",
    title: "AtoN UID",
    hoverMsg: "The S125 UID",
    placeholder: "The S125 UID"
 }, {
     data: "bbox",
     title: "Bounding Box",
     hoverMsg: "The S125 Bounding Box",
     placeholder: "The S125 Bounding Box",
     visible: false,
     searchable: false
 }, {
    data: "content",
    title: "Content",
    type: "textarea",
    hoverMsg: "The S125 Content",
    placeholder: "The S125 Content",
    width: "70%",
    render: function (data, type, row) {
        return "<textarea style=\"width: 100%; max-height: 300px\" readonly>" + data + "</textarea>";
    }
}];

// Run when the document is ready
$(function () {
    // And re-initialise it
    atonMessagesTable = $('#nodes_table').DataTable({
        processing: true,
        language: {
            processing: '<i class="fa fa-spinner fa-spin fa-3x fa-fw"></i><span class="sr-only">Loading...</span>',
        },
        serverSide: true,
        ajax: {
            type: "POST",
            url: "./api/messages/dt",
            contentType: "application/json",
            data: function (d) {
                return JSON.stringify(d);
            },
            error: function (jqXHR, ajaxOptions, thrownError) {
                console.error(thrownError);
            }
        },
        columns: nodesColumnDefs,
        dom: "<'row'<'col-lg-2 col-md-2'B><'col-lg-2 col-md-2'l><'col'f>><'row'<'col-md-12'rt>><'row'<'col-md-6'i><'col-md-6'p>>",
        select: 'single',
        lengthMenu: [10, 25, 50, 75, 100],
        responsive: true,
        altEditor: true, // Enable altEditor
        buttons: [{
            extend: 'selected', // Bind to Selected row
            text: '<i class="fas fa-map-marked-alt"></i>',
            titleAttr: 'View Message Geometry',
            name: 'messageGeometry', // do not change name
            className: 'message-geometry-toggle',
            action: (e, dt, node, config) => {
                loadMessageGeometry(e, dt, node, config);
            }
        }, {
            extend: 'selected', // Bind to Selected row
            text: '<i class="fas fa-trash-alt"></i>',
            titleAttr: 'Delete Node',
            name: 'delete' // do not change name
        }],
        onDeleteRow: function (datatable, selectedRows, success, error) {
            selectedRows.every(function (rowIdx, tableLoop, rowLoop) {
                $.ajax({
                    type: 'DELETE',
                    url: `./api/messages/uid/${this.data()["atonUID"]}`,
                    success: success,
                    error: error
                });
            });
        }
    });

    // We also need to link the message geometry toggle button with the the modal
    // panel so that by clicking the button the panel pops up. It's easier done
    // with jQuery.
    atonMessagesTable.buttons('.message-geometry-toggle')
        .nodes()
        .attr({ "data-bs-toggle": "modal", "data-bs-target": "#messageGeometryPanel" });

    // Now also initialise the station map before we need it
    atonMessagesMap = L.map('atonMessagesMap').setView([54.910, -3.432], 5);
    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(atonMessagesMap);

    // FeatureGroup is to store editable layers
    drawnItems = new L.FeatureGroup();
    atonMessagesMap.addLayer(drawnItems);

    atonMessagesMap.on('draw:created', function (e) {
        var type = e.layerType;
        var layer = e.layer;

        // Do whatever else you need to. (save to db, add to map etc)
        drawnItems.addLayer(layer);
    });

    // Invalidate the map size on show to fix the presentation
    $('#messageGeometryPanel').on('shown.bs.modal', function() {
        setTimeout(function() {
            atonMessagesMap.invalidateSize();
        }, 10);
    });
});

/**
 * This function will load the message geometry onto the drawnItems variable
 * so that it is shown in the AtoN message maps layers.
 *
 * @param {Event}         event         The event that took place
 * @param {DataTable}     table         The AtoN type table
 * @param {Node}          button        The button node that was pressed
 * @param {Configuration} config        The table configuration
 */
function loadMessageGeometry(event, table, button, config) {
    var idx = table.cell('.selected', 0).index();
    var data = table.rows(idx.row).data();
    var geometry = data[0].bbox;

    // Recreate the drawn items feature group
    drawnItems.clearLayers();
    if(geometry) {
        var geomLayer = L.geoJson(geometry);
        addNonGroupLayers(geomLayer, drawnItems);
        atonMessagesMap.setView(geomLayer.getBounds().getCenter(), 5);
    }
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


