import groovy.xml.*

def cli = new CliBuilder(usage:'createUpdateSite.groovy')
cli.r(longOpt:'repo', args:1, argName:'data', 'features and plugins repository')
cli.s(longOpt:'categoryXml', args:1, argName:'data', 'result category.xml')

def options = cli.parse(args)
def repoPath = options.repo;

if (!new File(repoPath).exists()) 
{
	println("repository dir is incorrectly specified");
	return;
}

def categoryXml = options.categoryXml;
new File(categoryXml).getParentFile().mkdirs();

println("repoPath = ${repoPath}, categoryXml=${categoryXml}");

def featureRefs = [];
new File(repoPath + File.separator + 'features').eachFile{file ->
	if(file.isFile() && file.name.matches("(.+)_(.+)[.]jar")){
		featureRefs.add(file.getParentFile().name + File.separator + file.name);
	}
}

def xmlFeatures = [];
Set xmlCategories = [];

def categoryXds = "com.excelsior.xds";
def categoryXdsJre = "com.excelsior.xds.jre";
def categoryEclipse = "org.eclipse";
def categoryOther = "other";

Map<String, CategoryDesc> categoryName2Desc = [:];
categoryName2Desc[categoryXds] = new CategoryDesc(categoryXds, "XDS Modula IDE components", "XDS Modula IDE components description");
categoryName2Desc[categoryXdsJre] = new CategoryDesc(categoryXds, "Oracle JRE", "Oracle JRE description");
categoryName2Desc[categoryEclipse] = new CategoryDesc(categoryEclipse, "Eclipse components", "Eclipse components description");
categoryName2Desc[categoryOther] = new CategoryDesc(categoryOther, "Other", "Other description");

def closure = {
	site {
		featureRefs.each { ref ->
			def regex = /.+[\\](.+)_(\d+[.]\d+[.]\d+).*[.]jar/;
			def matcher = ref  =~ regex;
			if (matcher.matches()) 
			{
				def versionVal = matcher.group(2);
				def idVal = matcher.group(1);
			
				feature(id : idVal, version : versionVal + '.qualifier', url : ref)
				{
					if (idVal.startsWith(categoryXdsJre)) 
					{
						xmlCategories.add(categoryXdsJre);
						category(name : categoryXdsJre) {}
					}
					else if (idVal.startsWith(categoryXds)) 
					{
						xmlCategories.add(categoryXds);
						category(name : categoryXds) {}
					}
					else if (idVal.startsWith("org.eclipse")) 
					{
						xmlCategories.add(categoryEclipse);
						category(name : categoryEclipse) {}
					}
					else {
						xmlCategories.add(categoryOther);
						category(name : categoryOther) {}
					}
				}
			}
		}

		xmlCategories.each { categoryName -> 
			CategoryDesc categoryDesc = categoryName2Desc[categoryName];
			'category-def' (name : categoryName, label : categoryDesc.label) 
			{
				description(categoryDesc.description);
			}
		}
	}
}

writeFile(categoryXml, closure);

class CategoryDesc 
{
	String name;
	String label;
	String description;

	public CategoryDesc(String name, String label, String description) {
		this.name = name;
		this.label = label;
		this.description = description;
	}
}

def writeFile(fileName, closure) {
    def xmlFile = new File(fileName)
    def writer = xmlFile.newWriter()

    def builder = new StreamingMarkupBuilder()
    def Writable writable = builder.bind closure
    writable.writeTo(writer)
}