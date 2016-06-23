#Excelsior IDE for XDS Modula-2 Developers

To develop Excelsior IDE (XDS IDE for short), you can use Eclipse Luna (4.4.2) or newer. It is important to use plugin-development enabled version (now called Eclipse IDE for Eclipse Committers). It includes the set of plugins called PDE (Plugin Development Environment).

###Building and Running from the IDE

* git clone the project from the github. Lets say you cloned the project to c:/xds-ide-sources/ directory.
* Start your Eclipse IDE for Eclipse Committers

Next, you should set up the workspace for development. First of all, you need to add so called Target-Platform. In simplest terms, it specifies the plugin environment you develop your code against.

* Go to 'Main menu/Preferences/Plug-in Development/Target platform', Press Add... button on the right. 'New Target Definition' dialog will pop-up. 
* Select 'Current target', Next. Press  Add... button on the right. Another dialog will pop-up. 
* Under the list titled 'Select a source of plugins' select 'Directory'. 
* On the next page specify the Target platform directory as following: press Browse... button and select c:/xds-ide-sources/target-platform directory. It is important to specify this directory without the leading and trailing spaces, otherwise Eclipse will fail to find the target platform contents.
* Press Next, the list of found plugins will show up.
* Press Finish, New 'Target Definition' should show one more line like:
	c:/xds-ide-sources/target-platform 18 plugins available
* Press Finish at 'New Target Definition'.
* Press OK at Preferences dialog.


Next, import all projects from the c:/xds-ide-sources/product. 

* You can do this via the File/Import/General/Existing projects into Workspace/ (specify c:/xds-ide-sources/product directory).

To launch the XDS IDE instance from the IDE:
* navigate to com.excelsior.xds.ide plugin.
* Right click on com.excelsior.xds.ide.product, Select Run As, then Eclipse Application. 
* The first launch MUST fail. 
* Go to Main menu/Run/Run Configurations. 
* Under Eclipse Application type find com.excelsior.xds.ide.product configuration. Click on it and go to Plug-ins tab.
* Press 'Add Required Plug-ins'.
* Press 'Validate'. Eclipse should report 'No problems were detected'. Otherwise, your configuration is inorrect at some point.
* Press 'Apply' and 'Run'.
* IDE will open.


To build the IDE from command line:
* Go to c:/xds-ide-sources/builder/config
* Let say your computer name is MYCOMPUTER. There, you should create env_MYCOMPUTER.bat and MYCOMPUTER.properties files, using the env_HURRICANE.bat and Hurricane.properties as example.
* Inside MYCOMPUTER.properties specify @XdsSdk@ - it should point to XDS SDK which will be the default XDS IDE sdk. 

!IMPORTANT! - specify pathes with the forward slashes '/', not backslaches '\'!!! 
!IMPORTANT! - path should be intact and should not contain leading and trailing characters!!! So, correct way to specify is:
@XdsSdk@=c:/xds-sdk/xds-sdk-version
Make sure it also has no trailing whitespace.

* Inside MYCOMPUTER.properties it important to specify @EclipseLocation@ variable. It should point to Eclipse 4.4.2 installation (this folder should contain eclipse.exe inside) with unpacked delta-pack inside the dropins folder.
* To obtain delta-pack for your eclipse build installation launch this Eclipse instance, go to About properties and copy Version name and Build-id fields from there. I have

Version: Luna Service Release 2 (4.4.2)
Build id: 20150219-0600

Then go to the http://archive.eclipse.org/eclipse/downloads/. 

Under 'Archived Releases' find your version and click on it. You should fall through to URL like http://archive.eclipse.org/eclipse/downloads/drops4/R-4.4.2-201502041700/. Please note that in my case I have (version=4.4.2, buildId=20150219-0600 ) running, but Eclipse site offers me R-4.4.2-201502041700 version (see URL). In such a case I suggest you to download the correct version of the platform you will build against. For me, it is the eclipse-SDK-4.4.2-win32.zip from the Eclipse SDK section.

Scroll through the page and download eclipse-4.4.2-delta-pack.zip file.

* Move eclipse-4.4.2-delta-pack.zip to the dropins folder of your Eclipse instance you will you to build against. 
* Unpack eclipse-4.4.2-delta-pack.zip there.

* After all this you are ready to launch the build process. Go to c:/xds-ide-sources/builder/ and start the build_local.bat script. @ResultOutputDir@ variable from the MYCOMPUTER.properties is pointing to the directory containing the build result.