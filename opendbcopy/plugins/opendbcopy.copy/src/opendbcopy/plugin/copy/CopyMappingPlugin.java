package opendbcopy.plugin.copy;

import opendbcopy.controller.MainController;
import opendbcopy.plugin.DynamicPluginThread;
import opendbcopy.plugin.PluginMetadata;
import opendbcopy.plugin.exception.PluginException;

public class CopyMappingPlugin extends DynamicPluginThread {

	public CopyMappingPlugin(MainController controller, PluginMetadata plugin) throws PluginException {
		super(controller, plugin);
	}
}
