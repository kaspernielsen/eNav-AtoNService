/**
 * Global variables
 */
var stationsNodesTable = undefined;

/**
 * The Station Nodes Table Column Definitions
 * @type {Array}
 */
var nodesColumnDefs = [{
    data: "id",
    title: "ID",
    hoverMsg: "The Node ID",
    placeholder: "The Node ID",
    visible: false,
    searchable: false
}, {
    data: "uid",
    title: "UID",
    hoverMsg: "The Node UID",
    placeholder: "The Node UID"
 }, {
    data: "type",
    title: "Type",
    hoverMsg: "The Node Type",
    placeholder: "The Node Type"
 }, {
    data: "message",
    title: "Message",
    type: "textarea",
    hoverMsg: "The Node Content",
    placeholder: "The Node Content",
    width: "70%",
    render: function (data, type, row) {
        return "<textarea style=\"width: 100%; max-height: 300px\" readonly>" + data + "</textarea>";
    }
}];

// Run when the document is ready
$(function () {
    // And re-initialise it
    stationsNodesTable = $('#nodes_table').DataTable({
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
        dom: "<'row'<'col-lg-1 col-md-2'B><'col-lg-2 col-md-2'l><'col-lg-9 col-md-8'f>><'row'<'col-md-12'rt>><'row'<'col-md-6'i><'col-md-6'p>>",
        select: 'single',
        lengthMenu: [10, 25, 50, 75, 100],
        responsive: true,
        altEditor: true, // Enable altEditor
        buttons: [{
            extend: 'selected', // Bind to Selected row
            text: '<i class="fas fa-trash-alt"></i>',
            titleAttr: 'Delete Node',
            name: 'delete' // do not change name
        }],
        onDeleteRow: function (datatable, selectedRows, success, error) {
            selectedRows.every(function (rowIdx, tableLoop, rowLoop) {
                $.ajax({
                    type: 'DELETE',
                    url: `./api/messages/${this.data()["id"]}`,
                    success: success,
                    error: error
                });
            });
        }
    });
});

