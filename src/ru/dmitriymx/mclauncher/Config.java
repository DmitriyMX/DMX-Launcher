package ru.dmitriymx.mclauncher;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Config {	
	public static final int LAUNCHER_VERSION	= 13; // 11-05-2012 17:46
	public static final String WORK_DIR			= "dmitriymx";
	
	public static final String URL_CLIENT	  =	"http://mc.dy9.ru/client/";
	public static final String URL_AUTH		  = "http://mc.dy9.ru:9001/auth.mc";
	public static final String URL_MINE_CHECK = "http://mc.dy9.ru:9001/checkmine.mc";
	
	public static final String[] MINECRAFT_JARS = {"minecraft.jar", "lwjgl.jar", "lwjgl_util.jar", "jinput.jar"};
	
	public static final String[] SERVER_NAME = {"Protected Server", "[ OFF-LINE ]"};
	public static final String[] SERVER_IP   = {"mc.dy9.ru:25565",  ""};

	public static String NATIVE_LIBRARY;
	public static String MINECRAFT_PATH;
	public static String MINECRAFT_BINPATH;
	
	public static int CONF_SERVER_ID = 0;
	public static String CONF_LAST_LOGIN = "";
	public static int CONF_MAX_RAM = 512;
	public static int CONF_MIN_RAM = 512;
	//public static boolean CONF_X64_MODE = false;
	public static int CONF_MULTI_CORE = 1;
	
	public static void init(){
		/** MINECRAFT_PATH */
		/** NATIVE_LIBRARY */
		String userHome = System.getProperty("user.home", ".");
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win")){
			NATIVE_LIBRARY = "windows_natives.zip";
			String appData = System.getenv("APPDATA");
			if(appData != null)
				MINECRAFT_PATH = new File(appData, "." + WORK_DIR).getPath();
			else
				MINECRAFT_PATH = new File(userHome, "." + WORK_DIR).getPath();
		}else if(osName.contains("linux")){
			NATIVE_LIBRARY = "linux_natives.zip";
			MINECRAFT_PATH = new File(userHome, "." + WORK_DIR).getPath();
		}else if(osName.contains("mac")){
			NATIVE_LIBRARY = "mac_natives.zip";
			MINECRAFT_PATH = new File(userHome, "Library/Application Support/" + WORK_DIR).getPath();
		}else{
			MINECRAFT_PATH = new File(userHome, WORK_DIR).getPath(); 
		}
		
		/** MINECRAFT_BINPATH */
		MINECRAFT_BINPATH = MINECRAFT_PATH + File.separator + "bin";
		
		/** Загрузка параметров */
		LoadConf();
	}
	
	/** Перенаправление вывода */
	public static void ChangeSTDOUT(String log_file){
		try{
			java.io.File log = new java.io.File(Config.MINECRAFT_PATH,log_file);
			if(!log.getParentFile().exists()){
				log.getParentFile().mkdirs();
			}
			java.io.PrintStream ps = new java.io.PrintStream(log);
			System.setErr(ps);
			System.setOut(ps);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** Загрузка параметров */
	public static void LoadConf(){
		File conf = new File(MINECRAFT_PATH,"launcher.conf");
		if(!conf.exists()){
			return;
		}
		try{
			BufferedReader buff = new BufferedReader(new FileReader(conf));
			String line;
			while((line = buff.readLine()) != null){
				String[] key_val = line.split("=",2);
				key_val[0] = key_val[0].trim();
				if(key_val[0].equalsIgnoreCase("server")){
					CONF_SERVER_ID = Integer.parseInt(key_val[1].trim());
				}else if(key_val[0].equalsIgnoreCase("login")){
					CONF_LAST_LOGIN = key_val[1].trim();
				}else if(key_val[0].equalsIgnoreCase("ram_max")){
					CONF_MAX_RAM = Integer.parseInt(key_val[1].trim());
				}else if(key_val[0].equalsIgnoreCase("ram_min")){
					CONF_MIN_RAM = Integer.parseInt(key_val[1].trim());
				}else if(key_val[0].equalsIgnoreCase("multi_core")){
					CONF_MULTI_CORE = Integer.parseInt(key_val[1].trim());
				}/*else if(key_val[0].equalsIgnoreCase("x64mode")){
					if(key_val[1].trim().equalsIgnoreCase("true")){
						CONF_X64_MODE = true;
					}
				}*/
				
			}
			buff.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** Сохранение параметров */
	public static void SaveConf(){
		File path = new File(MINECRAFT_PATH);
		if(!path.exists()){
			path.mkdirs();
		}
		try{
			BufferedWriter buff = new BufferedWriter(new FileWriter(new File(MINECRAFT_PATH, "launcher.conf")));
			buff.write("login="+CONF_LAST_LOGIN+"\n");
			buff.write("server="+String.valueOf(CONF_SERVER_ID)+"\n");
			buff.write("ram_max="+String.valueOf(CONF_MAX_RAM)+"\n");
			buff.write("ram_min="+String.valueOf(CONF_MIN_RAM)+"\n");
			buff.write("multi_core="+String.valueOf(CONF_MULTI_CORE)+"\n");
			/*if(CONF_X64_MODE){
				buff.write("x64mode=true\n");
			}else{
				buff.write("x64mode=false\n");
			}*/
			buff.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
