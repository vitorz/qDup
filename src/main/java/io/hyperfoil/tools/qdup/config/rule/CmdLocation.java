package io.hyperfoil.tools.qdup.config.rule;

import io.hyperfoil.tools.qdup.Stage;

/**
 * The context for where a command is found in a the run configuration.
 * This is used by RunRules for static analysis
 */
public class CmdLocation {

    public static final String FORK_SEPARATOR = "_";

    public static CmdLocation createTmp(){
        return new CmdLocation(
                "",
                Stage.PreSetup,
                "",
                "",
                Position.Watcher
        );
    }

    public static enum Position {
        Watcher(true),
        OnTimer(true),
        OnSignal(true),
        Child(false);

        private boolean isWatching;
        Position(boolean isWatching){
            this.isWatching = isWatching;
        }
        public boolean isWatching(){return isWatching;}
    }

    String roleName;
    Stage stage;
    String scriptName;
    String hostName;
    Position position;
    String forkId;


    public CmdLocation(String roleName, Stage stage, String scriptName, String hostName, Position position) {
        this(roleName,stage,scriptName,hostName,position,scriptName);
    }
    public CmdLocation(String roleName, Stage stage, String scriptName, String hostName, Position position, String forkId) {
        this.roleName = roleName;
        this.stage = stage;
        this.scriptName = scriptName;
        this.hostName = hostName;
        this.position = position;
        this.forkId = forkId;
    }

    public String getRoleName(){return roleName;}
    public Stage getStage(){return stage;}
    public String getScriptName(){return scriptName;}
    public String getHostName(){return hostName;}
    public Position getPosition(){return position;}
    public String getForkId(){return forkId;}

    public CmdLocation newPosition(Position position){
        return new CmdLocation(
                getRoleName(),
                getStage(),
                getScriptName(),
                getHostName(),
                position,
                getForkId()
        );
    }
    public CmdLocation newForkId(String forkId){
        return new CmdLocation(
                getRoleName(),
                getStage(),
                getScriptName(),
                getHostName(),
                getPosition(),
                getForkId()+FORK_SEPARATOR+forkId
        );
    }

}
