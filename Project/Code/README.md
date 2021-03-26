# Installation guide
## Eclipse + Genius + win.win agent
1. Clone the repository or download the ./Project/Code directory to a local destination
2. Open Eclipse
3. Create a new project in Eclipse:
	- Import > General > Existing Project into Workspace
	- Select root directory > ```./Project/Code/win.win```
	- Projects > win.win
4. Copy ```.Project/Code/.classpath``` to your project root folder ```./Project/Code/win.win```
5. Refresh the project in Eclipse so it finds the classpath and recognises the application
	- Package Explorer > win.win (Right Click) > Refresh
6. Update the JRE in Eclipse
	- Package Explorer > win.win (Right Click) > Properties
	- Java Build Path > Libraries > JRE System Library > Edit...
	- System Library > Alternate JRE > Java [1.8*] > Finish > Apply and Close
7. Run the application in Eclipse
	- Package Explorer > win.win (Right Click) > Run As > Java Application
8. Select Java Application
	- Application - genius

### BOA component and party listings will be created locally after your first Genius run.
We do this to allow for all team members to develop components simultaneously. At this stage, the components are compiled into Genius from local path which is different for every team member. So we ```.gitignore```d the files for easier repository management.
- BOA components: ```.Project/Code/win.win/boarepository.xml```
- BOA parties: ```.Project/Code/win.win/boapartyrepo.xml```

After you have added win.win components and created a party based on them, you should recognise the entries in the component and party listings. These listings were created based on your first run of the application:
```
otto@localhost % grep -R winwin Project/Code/win.win/boa*
Project/Code/win.win/boapartyrepo.xml:            <item classpath="localhost/Project/Code/win.win/bin/winwin_BSTD.class"/>
Project/Code/win.win/boapartyrepo.xml:            <item classpath="localhost/Project/Code/win.win/bin/winwin_ASU.class"/>
Project/Code/win.win/boapartyrepo.xml:            <item classpath="localhost/Project/Code/win.win/bin/winwin_OMHH.class"/>
Project/Code/win.win/boapartyrepo.xml:            <item classpath="localhost/Project/Code/win.win/bin/winwin_OSBB.class"/>
Project/Code/win.win/boarepository.xml:		<biddingstrategy description="winwin_BSTD" classpath="localhost/Project/Code/win.win/bin/winwin_BSTD.class"/>
Project/Code/win.win/boarepository.xml:		<acceptancecondition description="winwin_ASU" classpath="localhost/Project/Code/win.win/bin/winwin_ASU.class"/>
Project/Code/win.win/boarepository.xml:		<acceptancecondition description="winwin_ASN" classpath="localhost/Project/Code/win.win/bin/winwin_ASN.class"/>
Project/Code/win.win/boarepository.xml:		<opponentmodel description="winwin_OMHH" classpath="localhost/Project/Code/win.win/bin/winwin_OMHH.class"/>
Project/Code/win.win/boarepository.xml:		<omstrategy description="winwin_OSBB" classpath="localhost/Project/Code/win.win/bin/winwin_OSBB.class"/>
```

## More resources
https://tracinsy.ewi.tudelft.nl/pubtrac/Genius/browser
