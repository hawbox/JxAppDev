var myObj = null;
var currentSelected = null;
var toDelete = null;
var user_id = null;

function setUId(){
    //alert(localStorage.getItem("user_id"));
    user_id = localStorage.getItem("user_id");
    listNotes();
}

/*****
 Here we have a set of function that will be the equivalent to create, edit, delete and list all notes.
 This functions main goal is to invoke the JSON String creator method with the corresponding parameters.
 When the we have a valid json these methods will be in charge of invoking the external js library that
 will handle the rest. To call the external library we require a Valid JSON in string format, that should
 contain all the field that might be necessary, as well as the class and method name that need to be
 called on the server side.
 Here we can also call several method to make changed in the UI like for example clean out Notes table.
 *****/

// Create new note
function newNote(){
    var noteName = document.getElementById("newnotelabel").value;
    var note = document.getElementById("newnote").value;
    var json = '{"notename":"' + noteName + '", "note": "' + note + '", "user_id": "'+user_id+'"}';
    createRequest(json, "NoteHandler", "createNote", null,"testGroup1", "refresh-table");
}

// List all notes
function listNotes(){
    var json = '{"user_id": "' + user_id + '"}';
    createRequest(json, "NoteHandler", "listNotesByUser", handleTable, "", "");
}

function handleTable(response){
    myObj = response;
    var table = document.getElementById("notetablebody");
    clearTable();
    for (x in myObj) {
        addRow(table, myObj[x]);
    }
}

// Edit Some note
function editNote(){
    var noteName = document.getElementById("editnotelabel").value;
    var note = document.getElementById("editnote").value;
    var json = '{"notename":"' + noteName + '", "note": "' + note + '", "id": "' + String(currentSelected.id) +'"}';
    createRequest(json, "NoteHandler", "editNote", null, "testGroup1", "refresh-table");
    //listNotes();

}

// Delete Some note
function deleteNote(){
    var json = '{"id":"' + String(toDelete) +'"}';
    createRequest(json, "NoteHandler", "deleteNote", null, "testGroup1", "refresh-table");
    //notifyGroup('test', 'refresh-table');
    //listNotes();
}


/****
 Table Handler Functions
 Here we have some functions that can be used to update the Notes inside out table
 *****/

// This function allows us to remove all elements of the table
function clearTable(){
    $("#notetablebody").empty();
}

// This function allows us to add a new row into the table by simply passing the table reference and the JSON object
// that contains all the information to be added into the table
function addRow(table, jsonObj){

    // Create an empty <tr> element and add it to the 1st position of the table:
    var row = table.insertRow();

    row.id = "row" + jsonObj.id;

    // Insert new cells (<td> elements) at the 1st and 2nd position of the "new" <tr> element:
    var cell1 = row.insertCell(0);
    var cell2 = row.insertCell(1);
    cell2.classList.add("action-cell");

    /*
    var deleteButton = document.createElement("BUTTON");
    deleteButton.id = "deletebutton" + jsonObj.id;
    deleteButton.classList.add("btn-delete");
    deleteButton.innerHTML = "Delete";
    deleteButton.addEventListener('click', function() {
        $("#myModalDelete").modal("show");
        toDelete = String(jsonObj.id);
    }, false);*/

    var anchor = document.createElement("A");
    anchor.classList.add("btn", "icon-btn", "btn-danger");
    var span = document.createElement("SPAN");
    span.classList.add("glyphicon", "btn-glyphicon", "glyphicon-trash", "img-circle", "text-danger");
    anchor.appendChild(span);
    var label = document.createElement("LABEL");
    label.innerText = "Delete";
    anchor.appendChild(label);

    anchor.addEventListener('click', function() {
        $("#myModalDelete").modal("show");
        toDelete = String(jsonObj.id);
    }, false);


    // Add some text to the new cells:
    cell1.innerHTML = "<a href='' data-toggle='modal' data-target='#myModal2' onclick='setValues("+jsonObj.id+");'>" + jsonObj.notename + "</a>";
    //cell2.appendChild(deleteButton);
    cell2.appendChild(anchor);
}


/*****
 Here we have som function that will handle everything requires for the models where the Notes will be created,
 or edited.
 *****/

// This function presets the values into the fields when selecting a note that has previously been created
function setValues(id){

    for (x in myObj) {
        if(myObj[x].id === id){
            if(typeof myObj[x].notename !== "undefined"){
                document.getElementById("editnotelabel").value = myObj[x].notename;
            }else{
                document.getElementById("editnotelabel").value = "";
            }
            if(typeof myObj[x].note !== "undefined"){
                document.getElementById("editnote").value = myObj[x].note;
            }else{
                document.getElementById("editnote").value = "";
            }
            currentSelected = myObj[x];

            break;
        }
    }
}


function handleNotifications(notification){
    switch(notification) {
        case "refresh-table":
            listNotes();
            console.log("Notification = " + notification);
            break;
        case "other-example":
            console.log("Notification = Other example");
            break;
        default:
            console.log("DEFAULT");
    }
}