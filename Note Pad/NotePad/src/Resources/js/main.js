/*var script = document.createElement('script');
script.src = 'http://code.jquery.com/jquery-1.11.1.min.js';
script.type = 'text/javascript';
document.getElementsByTagName('head')[0].appendChild(script);*/


var token = null;
var message = null;
var map = {};
var pageNavMap = {};

/*
 * Função que recebe um pedido genérico e redireciona o pedido para um dos componentes Java
 * No caso de uma aplicação Nativa o pedido é enviado para o JxBrowser
 * No caso de uma Servidor Web o pedido é enviado para a API REST
*/
function createRequest(json, class_name, method_name, callback, notificationGroup, notificationName){

    // Retirar o valor do User-Agent da pagina Web
    var userAgent = document.getElementById('UserAgent').value = navigator.userAgent;




    // Criar um objeto JSON com o Nome da Classe e do Método que deve ser chamado no Back-End
    /*var jsonOb = null;
    if(json.length === 0){
        jsonOb = '[{"className" : "' + class_name + '", "methodName" : "' + method_name + '"}, {}]';
    }else{
        jsonOb = '[{"className" : "' + class_name + '","methodName" : "' + method_name + '"},' + json + ']';
    }*/


    // Criar um objeto JSON com o Nome da Classe e do Método que deve ser chamado no Back-End
    var jsonOb = null;
    jsonOb = '[{"className" : "' + class_name + '", "methodName" : "' + method_name + '"},';


    // Juntar parametros como parte do JSON caso seja necesario no Back-End para o respetivo metodo a ser chamado
    if(json.length !== 0){
        jsonOb = jsonOb + json + ',';
    }else{
        jsonOb = jsonOb + '{},';
    }

    // Se for necesario notificar um grupo em especifico vamos adicionar essa informacao ao JSON como ultimo paramtero
    if(notificationName.length !== 0 && notificationGroup.length !== 0){
        jsonOb = jsonOb + '{"notificationGroup" : "' + notificationGroup + '", "notificationName" : "' + notificationName + '"}';
    }else{
        jsonOb = jsonOb + '{}';
    }

    // Fechamos o array do JSON para concluir
    jsonOb = jsonOb + ']';


    // Verifica em que caso nos encontramos e enviar o pedido para a respetiva componente da aplicação
    var response = "";
    if(userAgent === "WebView"){
        response = sendFromWebView(jsonOb, callback);
    }else{
        response = sendFromWeb(jsonOb, callback);
    }

    // Devolver a resposta à pagina Web
    return response;
}

/*
function movePage(pageName){
    window.java.move(pageName);
}*/


function navigateTo(pageName){
    //alert(pageName);
    var userAgent = navigator.userAgent;
    if(userAgent === "WebView"){
        window.java.move(pageName);
    }else{
        window.location.href = pageName+".html";
    }
}

/*
 * Esta função está encarregue de chamar o método correto
 * do lado da aplicação JxBrowser
*/
function sendFromWebView(jsonOb, callback){

    var response = window.java.callMethod(jsonOb);

    if(callback != null) {
        callback(response);
    }
}

/*
 * Esta função está encarregue usar Ajax para mandar o pedido
 * diretamente para a API REST do lado do Back-End
*/
function sendFromWeb(jsonOb, callback){

    var response = "";

    $.ajax({
        type: "POST",
        url: "https://ea8b3bdf.ngrok.io/request",
        data: jsonOb,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function(data){
            if(callback != null){
                callback(data);
            }
        },
        failure: function(errMsg) {alert(errMsg);}
    });
}


function currentPageRefresh(){
    location.reload();
}

function socketConnect(){
    client.connect();
}

function registerToGroup(groupName){
    client.send("{\"type\":\"register\",\"group\":\"" + groupName + "\",\"token\":\"" + token + "\"}");
    addGroupToLocalStorage(groupName);
}

function unregisterFromGroup(groupName){
    client.send("{\"type\":\"unregister\",\"group\":\"" + groupName + "\",\"token\":\"" + token + "\"}")
    removeGroupFromLocalStorage(groupName);
}

function registerToGroups(groups){
    //groups = ["testGroup1", "testGroup2", "testGroup3"];
    var jsonString = "{\"type\":\"multiregister\", \"groups\" : [";
    var i;
    for (i = 0; i < groups.length; i++) {
        jsonString = jsonString + groups[i] + ",";
        addGroupToLocalStorage(groups[i]);
    }

    jsonString = jsonString.substring(0, jsonString.length - 1);

    jsonString = jsonString +  "], \"token\":\"" + token + "\"}";

    client.send(jsonString);

    console.log("Groups Registered = " + jsonString);
}

function unregisterFromGroups(groups){
    //groups = ["testGroup1", "testGroup2", "testGroup3"];
    var jsonString = "{\"type\":\"multiunregister\", \"groups\" : [";
    var i;
    for (i = 0; i < groups.length; i++) {
        jsonString = jsonString + groups[i] + ",";
        removeGroupFromLocalStorage(groups[i])
    }

    jsonString = jsonString.substring(0, jsonString.length - 1);

    jsonString = jsonString +  "], \"token\":\"" + token + "\"}";

    client.send(jsonString);

    console.log("Groups UnRegistered = " + jsonString);
}

function unregisterFromAllGroups(){
    var jsonString = "{\"type\":\"unregisterall\", \"token\":\"" + token + "\"}";
    client.send(jsonString);
    //removeAllGroupsFromLocalStorage();
}

function addGroupToLocalStorage(newGroup){
    var storedGroups = JSON.parse(localStorage.getItem("groups"));
    if(storedGroups == null){
        storedGroups = [];
    }
    if(storedGroups.indexOf(newGroup) === -1){
        storedGroups.push(newGroup);
        localStorage.setItem("groups", JSON.stringify(storedGroups));
    }
}

function removeGroupFromLocalStorage(oldGroup){
    var storedGroups = JSON.parse(localStorage.getItem("groups"));
    var index = storedGroups.indexOf(oldGroup);
    while (index > -1) {
        storedGroups.splice(index, 1);
        index = storedGroups.indexOf(oldGroup);
    }
    localStorage.setItem("groups", JSON.stringify(storedGroups));
}

function removeAllGroupsFromLocalStorage(){
    localStorage.setItem("groups", JSON.stringify(""));
}

function registerToPreviousGroups(){
    var storedGroups = JSON.parse(localStorage.getItem("groups"));
    if(storedGroups != null){
        registerToGroups(storedGroups);
    }
}


function parseNavigationFile(){
    // Retirar o valor do User-Agent da pagina Web
        $.getJSON("navigation/test.json", function(json) {
            console.log(json); // this will show the info it in firebug console
            parseJSON(json);
        });
    //}
}

function parseJSON(json){
    var page_navigation = json["page-navigation"];
    for (var key in page_navigation) {
        var navigation_rules = page_navigation[key]["navigation-rules"];
        for (var key1 in navigation_rules) {
            if (navigation_rules.hasOwnProperty(key1)) {
                pageNavMap[key1] = navigation_rules[key1];
            }
        }
    }
}


function notifyGroup(groupName, message){
    client.send("{\"type\":\"notify\",\"group\":\"" + groupName + "\",\"token\":\"" + token + "\",\"message\":\"" + message + "\"}");
}

class WebSocketClient {

    constructor(protocol, hostname, port, endpoint) {

        this.webSocket = null;

        this.protocol = protocol;
        this.hostname = hostname;
        this.port     = port;
        this.endpoint = endpoint;
    }

    getServerUrl() {
        return this.protocol + "://" + this.hostname + ":" + this.port + this.endpoint;
    }

    connect() {
        try {
            this.webSocket = new WebSocket(this.getServerUrl());

            //
            // Implement WebSocket event handlers!
            //
            this.webSocket.onopen = function(event) {
                console.log('onopen::' + JSON.stringify(event, null, 4));
            }

            this.webSocket.onmessage = function(event) {
                var msg = event.data;
                var jsonObject = JSON.parse(msg);
                console.log(jsonObject);
                var type = jsonObject["type"];

                switch(type) {
                    case "newToken":
                        token = jsonObject["token"];
                        console.log("New Token = " + token);
                        registerToPreviousGroups();
                        break;
                    case "newNotification":
                        message = jsonObject["message"];
                        console.log("New Notification = " + message);
                        handleNotifications(message);
                        break;
                    default:
                        console.log("DEFAULT");
                }

                console.log('onmessage::' + JSON.stringify(msg, null, 4));
            }
            this.webSocket.onclose = function(event) {
                console.log('onclose::' + JSON.stringify(event, null, 4));
            }
            this.webSocket.onerror = function(event) {
                console.log('onerror::' + JSON.stringify(event, null, 4));
            }

        } catch (exception) {
            console.error(exception);
        }
    }

    getStatus() {
        return this.webSocket.readyState;
    }

    send(message) {

        if (this.webSocket.readyState === WebSocket.OPEN) {
            this.webSocket.send(message);

        } else {
            console.error('webSocket is not open. readyState=' + this.webSocket.readyState);
        }
    }

    disconnect() {
        if (this.webSocket.readyState === WebSocket.OPEN) {
            this.webSocket.close();

        } else {
            console.error('webSocket is not open. readyState=' + this.webSocket.readyState);
        }
    }
}


var client = new WebSocketClient('ws', '127.0.0.1', 8081, '/endpoint?push=REFRESH');
socketConnect();
client.connect();
//parseNavigationFile();


//This only works inside a browser but not on WebViews
$(document).ready(function() {
    console.log( "ready!" );
    //alert("ready");
    parseNavigationFile();

});

$(document).click(function(e) {
    //alert(e.target.name in pageNavMap);
    if(e.target.name in pageNavMap){
        navigateTo(pageNavMap[e.target.name]);
    }
    /*alert(e);
    alert(e.target.id);
    alert(e.target.name);*/
});

$(window).on('beforeunload', function(){
    console.log("beforeUnload event!");
    unregisterFromAllGroups();
});

