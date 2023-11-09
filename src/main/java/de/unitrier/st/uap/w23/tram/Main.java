package de.unitrier.st.uap.w23.tram;
import java.io.File;


import static java.lang.System.exit;


final class Main
{

	private Main(){}
	
	public static void main(String[] argv)
	{


		if(argv.length < 1){
			System.out.println("Please provide the file.");
			exit(1);
		}

		File f = new File(argv[0]);

		if(!f.exists() || f.isDirectory()){
			System.out.println("File does not exist");
			exit(1);
		}

		int settings = 0;

		for (int i=1; i < argv.length; i++){
			switch (argv[i]){
				case "-stepwise":
					settings |= AbstractMachine.STEPWISE;
					settings |= AbstractMachine.LOG_CONSOLE;
					break;
				case "-console":
					settings |= AbstractMachine.LOG_CONSOLE;
					break;
				case "-file":
					settings |= AbstractMachine.LOG_FILE;
					break;
				case "-detailed":
					settings |= AbstractMachine.LOG_DETAILED;
					break;
			}
		}

		Instruction[] code = Assembler.readTRAMCode(f.getAbsolutePath());

		int lineNr=0;
		for(Instruction instr: code) {
			if (instr != null) {
				System.out.println(String.format("%03d ", lineNr) + "| " + instr);
				lineNr++;
			}
		}
		System.out.println("\n");



		AbstractMachine UwUmachine = new AbstractMachine();
		UwUmachine.setSettings(settings);
		UwUmachine.load(code);
		UwUmachine.run();


		if(settings<1) System.out.println(UwUmachine);

	}
}