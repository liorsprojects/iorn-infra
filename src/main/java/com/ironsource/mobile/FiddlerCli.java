package com.ironsource.mobile;

import jsystem.extensions.analyzers.text.FindText;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.system.SystemObjectImpl;

import com.aqua.sysobj.conn.CliCommand;
import com.aqua.sysobj.conn.WindowsDefaultCliConnection;

public class FiddlerCli extends SystemObjectImpl {

	WindowsDefaultCliConnection cli;

//	public static void main(String[] args) throws Exception{
//		FiddlerCli cli = new FiddlerCli();
//		cli.startFiddler();
//		Thread.sleep(5000);
//		cli.stopFiddler();
//		
//	}
//
//	public FiddlerCli() throws Exception {
//		// TODO Auto-generated constructor stub
//		cli = new WindowsDefaultCliConnection();
//		cli.setHost("127.0.0.1");
//		cli.connect();
//	}

	@Override
	public void init() throws Exception {
		super.init();
		cli = new WindowsDefaultCliConnection();
		cli.setHost("127.0.0.1");
		cli.setUser("RAMDOR\\lior_g");
		cli.setPassword("1121lgLG");
		cli.connect();
	}

	public void startFiddler() throws Exception {
		CliCommand cmd = new CliCommand("IronSourceFiddler.Client.exe");
		cmd.setTimeout(1500);
		cmd.setIgnoreErrors(true);
		cli.handleCliCommand("Starting Fiddler Proxy", cmd);
	}

	public void stopFiddler() throws Exception {
		CliCommand cmd = new CliCommand("Q");
		cli.handleCliCommand("Shutting Down Fiddler", cmd);
		cli.analyze(new FindText("Shutting down..."));
	}

}
