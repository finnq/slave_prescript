package org.jenkinsci.plugins.slave_prescript;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.slaves.ComputerListener;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import jenkins.model.Jenkins;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.Launcher;
import hudson.FilePath;
import hudson.AbortException;
import hudson.Extension;
import hudson.EnvVars;
import hudson.tasks.Shell;

import org.apache.commons.lang.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.lang.InterruptedException;
import java.io.IOException;

@Extension
public class NodeListener extends ComputerListener {

    /*private final String cmd;

    @DataBoundConstructor
    public NodeListener(String cmd) {
        this.cmd = cmd;
    }*/

    public NodeListener() {
    }

    @Override
    public void preLaunch(Computer c, TaskListener taskListener) throws IOException, InterruptedException {
        //Execute script START
        Computer computer = Jenkins.MasterComputer.currentComputer();
        FilePath filePath = Jenkins.getInstance().getRootPath();

        taskListener.getLogger().println("Exec started!");

        executeScript(computer, filePath, taskListener, "./test");
        //Execute script END
    }

    private void executeScript(Computer c, FilePath root, TaskListener taskListener, String cmd) throws IOException, InterruptedException {
        if (StringUtils.isNotBlank(cmd)) {
            taskListener.getLogger().println("Executing script '" + cmd);
            Node node = c.getNode();
            Launcher launcher = root.createLauncher(taskListener);
            Shell s = new Shell(cmd);
            FilePath script = s.createScriptFile(root);
            int r = launcher.launch().cmds(s.buildCommandLine(script)).envs(getEnvironment(node)).stdout(taskListener).pwd(root).join();

            if (r != 0) {
                taskListener.getLogger().println("PreScript failed!");
                throw new AbortException("PreScript failed!");
            }

            taskListener.getLogger().println("PreScript executed successfully.");
        }
    }

    /**
     * Returns the environment variables for the given node.
     * 
     * @param node node to get the environment variables from
     * @return the environment variables for the given node
     */
    private EnvVars getEnvironment(Node node) {
        EnvironmentVariablesNodeProperty env = node.getNodeProperties().get(EnvironmentVariablesNodeProperty.class);
        return env != null ? env.getEnvVars() : new EnvVars();
    }
}
