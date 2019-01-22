package ru.dmitriymx.mclauncher;

public class Main {

	public static void main(String[] args) {
		Config.init();
		
		/** Основная программа */		
		String param = System.getProperty("sess");
		if(param == null){
			Config.ChangeSTDOUT("launcher.log");
			DbgView();
			System.out.println("Start Launcher");
			MainFrame mf = new MainFrame();
			mf.setVisible(true);
		}else{
			Config.ChangeSTDOUT("client.log");
			DbgView();
			System.out.println("Start Minecraft");
			String[] name_sess = param.split(":",2);
			boolean mode;
			if(name_sess[1].equalsIgnoreCase("0000")){
				mode = false;
				System.out.println("Offline mode");
			}else{
				mode = true;
				System.out.println("Online mode");
			}
			GameModeThread.StartMinecraftApplet(mode, name_sess[0], name_sess[1]);
		}
	}
	
	private static void DbgView(){
		if(System.getProperty("debug", "false").equalsIgnoreCase("true")){
			java.util.Set<Object> keySet = System.getProperties().keySet();
			for(Object key : keySet){System.out.println((String)key+" = "+System.getProperty((String)key));}
		}
	}

}
