// Create new note
function register(){
    console.log("register");
    var fstn = document.getElementById("fstname").value;
    var lstn = document.getElementById("lstname").value;
    var usr = document.getElementById("usname").value;
    var ph = document.getElementById("phone").value;
    var em = document.getElementById("email").value;
    var pw = document.getElementById("psw").value;
    var pwr = document.getElementById("psw-repeat").value;
    if(pw === pwr){
        var json = '{"username":"' + usr + '", "password": "' + pw + '", "firstname": "' + fstn + '", "lastname": "' + lstn + '", "phone": "' + ph + '", "email": "' + em + '"}';
        createRequest(json, "UserHandler", "createUser", checkRegister, "", "");
    }
}

function checkRegister(response){
    myObj = response;
    if(response.result === "OK"){
        navigateTo("Login");
    }
}

function cancelRegister(){
    navigateTo("Login");
}