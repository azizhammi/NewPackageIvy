package org.processmining.newpackageivy.plugins;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(
        name = "Simple String from Aziz",
        parameterLabels = {},
        returnLabels = { "Output" },
        returnTypes = { String.class }
    )
public class SimpleStringPlugin {

    
    @UITopiaVariant(affiliation = "", author = "Aziz", email = "azizhammi0@gmail.com")
    @PluginVariant(variantLabel = "Get Simple String", requiredParameterLabels = {})
    public String getSimpleString(UIPluginContext context) {
        return "it should be working";
    }
}