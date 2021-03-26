https://tracinsy.ewi.tudelft.nl/pubtrac/Genius/browser
# Installation guide

## Eclipse + Genius + win.win agent

1. Clone the repository or download the ./Project/Code directory to a local destination
2. Open Eclipse
3. Create a new project in Eclipse:
	- Import > General > Existing Project into Workspace
	- Select root directory > ./Project/Code/win.win
	- Projects > win.win
4. Copy the .Project/Code/.classpath to your project so that Eclipse recognises the application
	```cp .classpath ./Project/Code/win.win```
5. Refresh the project in Eclipse so it finds the main method
	- Package Explorer > win.win (Right Click) > Refresh
6. Update the JRE in Eclipse
	- Package Explorer > win.win (Right Click) > Properties
	- Java Build Path > Libraries > JRE System Library > Edit...
	- System Library > Alternate JRE > Java [1.8*] > Finish > Apply and Close
7. Run the application in Eclipse
	- Package Explorer > win.win (Right Click) > Run As > Java Application
8. Select Java Application
	- Application - genius 
