import java.io.File;

class Globals {
	static String svnBase;
}

def productDir = properties["productDir"];
Globals.svnBase = properties["svnBase"];
def svnBase2 = Globals.svnBase;
println("productDir = ${productDir}, svnBase=${svnBase2}");

List<File> pluginDirs = getPluginDirectories(productDir);
pluginDirs.each {
	pluginDir -> processPluginDirectory(pluginDir)
}

def processPluginDirectory(File pluginDir) 
{
	String svnRevision = getPluginDirSvnRevision(pluginDir);
	setBuildPropertiesQualifier(pluginDir, svnRevision)
}

def setBuildPropertiesQualifier(File pluginDir, String svnRevision)
{
	def buildPropsPath = pluginDir.getAbsolutePath() +  java.io.File.separator + "build.properties";
	if (new File(buildPropsPath).exists()) 
	{
		String buildPropsText = new File(buildPropsPath).getText();
		def regex = /(?m)^\s*qualifier\s+=\s+none\s*$/;
		String newBuildPropsText = buildPropsText.replaceAll(regex,'qualifier = ' + svnRevision);
		new File(buildPropsPath).setText(newBuildPropsText);
	}
}

def getPluginDirSvnRevision(File pluginDir) 
{
	def command = 'svn info ' +  Globals.svnBase + pluginDir.getName();
	def proc = command.execute()                 // Call *execute* on the string
	
	def svnRevision = null;
	proc.in.eachLine { line -> def tmp = fetchSvnRevision(line); if (tmp != null) svnRevision = tmp; }
	svnRevision;
}

def getPluginDirectories(String productDir) 
{
	def pluginDirs = [];
	new File(productDir).eachFile{file ->
		if(file.isDirectory() && file.name.startsWith("com.excelsior" )){
			pluginDirs.add(file);
		}
	}
	pluginDirs;
}

def fetchSvnRevision(String line)
{
	def matcher = line =~ /^Last Changed Rev: (\d+)\s*$/;
	if (matcher.matches())
	{
		return matcher.group(1);
	}
	return null;
}