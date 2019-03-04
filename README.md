# JxAppDev
Framework for Hybrid/Desktop application development

# Components
This framework is composed of two parts:

    1. The first component needs to be integrated into a fron-end in order to manage communication with the back-end
        - This component offers the possibility to display a Web front-end inside a Java application using a framework called JxBrowser.
        - It is also in charge of communicating information from the back-end to the front-end (e.g. notification for UI update)
        - Finally this component integarted a JavaScript library that can be used by the developer to communicate with his back-end application by simply calling some specific methods and without further complexity.
    2. The second component lays inside a Tomcat web server and will receive all requests comming from the front-end.
        Here we extract the required information for the request to be handled and will trasmit this information to the Business logic of the application.
        
# Requirements
In order to use this Framework there are some prerequisites:

    1. There user must develop a UI using web technologies to be integrated into the front-end java application (using the JxBrowser this UI will be displayed as in a Web Browser)
    2. The user must develop the Business logic as well as the persistency layer of the application.
    
# Framework flow
This framework does not handle business logic nor generated any UI as mentioned before. This framework in purly in charge of displaying a Web UI inside a Java application, and will handle communication between this UI and the back-end where the logic of the final application is contained.

When a request is created on the front-end by the Web UI:

    1. The user shall call a specific method from the JavaScript library
    2. This messahe is trasmitted by the framewrok to the back-end 
    3. Inside the back-end the framewrok will extract the important information
    4. After information extraction the framework will use Java Reflection to call the proper methods of the business logic
    
# Details
There are some extra steps before using the final application.

    1. Create a navigation file that will indicate the navigation between pages to help the framework
    2. Create a paths.txt file inside the back-end that will indicate all the packages that the framework must access
        in order to call the methods containing the logic to handle all the requests.
        
        
# Examples
To ilustrate the usage of this framewrok there are two examples in their respective directoris.

    1. NotePad
        - Contains a simple notepad application thad allows the user to save/delete/update notes that he can access at any time
    2. Ivy editor
        - Contains a Web version of a text editor used by the IVY project develped by the University of Minho
        
Both of these examples are devided into two components, representing their respestcive front-end and back-end parts.
