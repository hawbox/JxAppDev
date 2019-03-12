// Create new note
function loginUser(){
    var uname = document.getElementById("uname").value;
    var pw = document.getElementById("psw").value;
    var json = '{"user_id":"' + uname + '", "user_pw": "' + pw + '"}';
    createRequest(json, "UserHandler", "loginUser", checkLogin, "", "");
}

function checkLogin(response){
    myObj = response;
    if(response.result === "OK"){
        localStorage.setItem("user_id",response.user_id);
        navigateTo("NotePadList");
    }
}
function moveTo(){
    navigateTo("Register");
}