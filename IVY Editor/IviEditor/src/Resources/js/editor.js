function compileModel(textModel){
    var json = '{"text_model":"' + textModel + '"}';
    createRequest(json, "ModelCompiler", "compile_model", setLog, "", "");
}

function setLog(response){
    document.getElementById("textarea").value = response.result;
    //document.getElementById("textarea").value = "Compilation finished (check above for any error).";
}


var editor = ace.edit("editor");
editor.setTheme("ace/theme/cloud");
editor.session.setMode("ace/mode/javascript");
editor.session.setOption("useWorker", false);


$("#open_file").click(function(){
    $('#file_loader')[0].click();
});

$("#file_loader").change(function(){
    var file = document.getElementById("file_loader").files[0];
    var textType = /text.*/;

    //if (file.type.match(textType)) {
        var reader = new FileReader();

        reader.onload = function(e) {
            editor.setValue(reader.result);
        }

        reader.readAsText(file);
    /*} else {
        document.getElementById("editor").innerText = "File not supported!"
    }*/

});


$("#save_file").click(function(){
    var text = editor.getValue();
    var filename = "Example";
    var textToSaveAsBlob = new Blob([text], {type:"text/plain"});
    var textToSaveAsURL = window.URL.createObjectURL(textToSaveAsBlob);
    var downloadLink = document.createElement("a");
    downloadLink.download = filename;
    downloadLink.innerHTML = "Download File";
    downloadLink.href = textToSaveAsURL;
    downloadLink.onclick = destroyClickedElement;
    downloadLink.style.display = "none";
    document.body.appendChild(downloadLink);

    downloadLink.click();
});

function destroyClickedElement(event)
{
    document.body.removeChild(event.target);
}

//$("#save_as").click(function(){});

$("#undo_action").click(function() {
    editor.undo();
});
$("#redo_action").click(function(){
    editor.redo();
});


$("#compile_action").click(function() {
    //alert(editor.getValue());
    compileModel(editor.getValue());
    //compileModel("BLA");
});


$("#find").click(function() {
    editor.find('main');
});
$("#find_next").click(function() {
    editor.findNext();

});
$("#find_previous").click(function() {
    editor.findPrevious();
});

$("#replace_all").click(function() {
    if($("#replace_field").val() !== "")
    {
        editor.replaceAll($("#replace_field").val());
    }
});

$("#replace").click(function() {
    if($("#replace_field").val() !== "")
    {
        editor.replace($("#replace_field").val());
    }
});

$( "#resizable" ).resizable({
    handles: 'n, s',
    maxHeight: ($( document ).height()/100)*75,
    alsoResizeReverse: '#textarearow'
});


$.ui.plugin.add("resizable", "alsoResizeReverse", {

    start: function() {
        var that = $(this).resizable( "instance" ),
            o = that.options;

        $(o.alsoResizeReverse).each(function() {
            var el = $(this);
            el.data("ui-resizable-alsoresizeReverse", {
                width: parseInt(el.width(), 10), height: parseInt(el.height(), 10),
                left: parseInt(el.css("left"), 10), top: parseInt(el.css("top"), 10)
            });
        });
    },

    resize: function(event, ui) {
        var that = $(this).resizable( "instance" ),
            o = that.options,
            os = that.originalSize,
            op = that.originalPosition,
            delta = {
                height: (that.size.height - os.height) || 0,
                width: (that.size.width - os.width) || 0,
                top: (that.position.top - op.top) || 0,
                left: (that.position.left - op.left) || 0
            };

        $(o.alsoResizeReverse).each(function() {
            var el = $(this), start = $(this).data("ui-resizable-alsoresize-reverse"), style = {},
                css = el.parents(ui.originalElement[0]).length ?
                    [ "width", "height" ] :
                    [ "width", "height", "top", "left" ];

            $.each(css, function(i, prop) {
                var sum = (start[prop] || 0) - (delta[prop] || 0);
                if (sum && sum >= 0) {
                    style[prop] = sum || null;
                }
            });

            el.css(style);
        });
    },

    stop: function() {
        $(this).removeData("resizable-alsoresize-reverse");
    }
});