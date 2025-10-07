package org.perun.registrarprototype.plugins;


import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class TestPluginModule extends Plugin {


    public TestPluginModule(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
      System.out.println("TestPluginModule.start()");
    }

    @Override
    public void stop() {
      System.out.println("TestPluginModule.stop()");
    }
}
