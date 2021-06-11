import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

public class SpeechApplication extends JFrame {
	
			
	//Declare Buttons
	JButton speechBtn = new JButton("Click this button then speak");
			
	//Declare JPanel
	JPanel panel;
			
	//Declare TextArea
	java.awt.TextArea tArea = new java.awt.TextArea();
			
	///////////////////////////////////////////////////////
		
	//Variables
	private String result = "";
		
	//Threads
	Thread speechThread;
	Thread resourcesThread;
		
	//Live Recognizer
		
	private LiveSpeechRecognizer recognizer;
		
	//Boolean value to determine when to stop speech recognition
	private volatile boolean stopRecognition = true;
	////////////////////////////////////////////////
		
		
		public SpeechApplication(String frameTitle){
			//Set initial frame title, size, background color
			super(frameTitle);
			setSize(500,400);
			
			panel = new JPanel();
			
			//Create Title text
			JLabel title = new JLabel("JarBot: How May He Serve You?");
			title.setBounds(0, 175, 200, 100);
			
			//Set the Bounds for the button
			speechBtn.setBounds(0, 200, 150, 50);
			
			//Add action Listeners to button
			Click onClick = new Click();
			speechBtn.addActionListener(onClick);
			
			//Add title to panel
			panel.add(title);
			
			//Add button to panel
			panel.add(speechBtn);
			
			tArea.setBounds(0, 200, 200, 200);
			panel.add("South", tArea);
			panel.setBackground(new Color(0, 153, 255));
			
			//Add panel to frame
			this.add(panel);
		
			//Set to dispose when the window is closed
			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			
			//make the frame visible
			setVisible(true);
			
			///////////////////Speech stuff/////////////////////////
			
			Configuration config = new Configuration();

			// Set path to acoustic model.
			config.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
			// Set path to dictionary
			config.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
			
			//Set grammar
			config.setGrammarPath("resource:/grammars");
			config.setGrammarName("grammar");
			config.setUseGrammar(true);
			
			//let's try
			try{
				recognizer = new LiveSpeechRecognizer(config);
			}
			catch(IOException ioEx){
			}
			
			//Start recognizing speech. prunes previously cached data
			recognizer.startRecognition(true);
			
				
		}
		
		//The speech thread is for speech recognition. Without it, the application cannot respond
		protected void StartSpeechThread(){
			
			//Check if the thread is alive
			if(speechThread != null && speechThread.isAlive())
				return;
			
			//Initialize the Speech Thread
			//The () -> {} is the definition of the run method within the thread constructor
			speechThread = new Thread(() -> {
				tArea.setText("You can start speaking now....\n");
				stopRecognition = false;
				
				//This try-catch reads the user's speech until the end of speech is reached
				try{
					while(true){
						/*
						 * The while loop will end when end of speech is reached
						 *  The end pointer determines the end of speech
						 *  */
						
						SpeechResult sResult = recognizer.getResult();
						if(sResult != null){
							//The user speech is recognized here
							result = sResult.getHypothesis();
							//Prints out what the user says
							tArea.setText(tArea.getText() + "You said: " + result + "\n");
							makeDecision(result, sResult.getWords());
							
						}
						else{
							tArea.setText(tArea.getText() + "I didn't understand what you said. \n");
						}
						
					}
				}
				catch(Exception ex){
				}
				
				tArea.setText("The speech thread has exited..... \n");
			});
			
			speechThread.start();
		}
		
		//This function is for stopping the speech recognition thread
		public void stopSpeechThread() {
			// Check if the thread is alive before stopping it
			if (speechThread != null && speechThread.isAlive()) {
				stopRecognition = true;
				speechThread.interrupt();
			}
		}


		/*The resources thread is for microphone recognition. 
		   Without it, the application cannot recognize the user voice  */
		protected void StartResourcesThread(){
			
			//Check if the thread is alive
			if(resourcesThread != null && resourcesThread.isAlive())
				return;
			
			//Initialize the Resources Thread
			//The () -> {} is the definition of the run method within the thread constructor
			resourcesThread = new Thread(() -> {
				tArea.setText("You can start speaking now....\n");
				
				//This try-catch determines if the microphone is available
				try{
					while(true){
						//Microphone is available
						if(AudioSystem.isLineSupported(Port.Info.MICROPHONE)){
							
						}
						//Microphone is not available
						else{
	
						}
						
						//Let is sleep for a period
						Thread.sleep(350);
					}
					
				}
				catch(InterruptedException iEX){
					resourcesThread.interrupt();
				}
			});
			
			resourcesThread.start();
		}
		
		//This function is for stopping the resource thread
		public void stopResourcesThread() {
				// Check if the thread is alive before stopping it
				if (resourcesThread != null && resourcesThread.isAlive()) {
					stopRecognition = true;
					resourcesThread.interrupt();
				}
			}
		public int convert(String word) {
			int wordnum = 0;
			String[] arrinwords = word.split(" ");
			int arrinwordsLength = arrinwords.length;
			if ("zero".equals(word))
				return 0;
			if (word.contains("thousand")) {
				int indexofthousand = word.indexOf("thousand");
				// find substring before thousand
				String beforethousand = word.substring(0, indexofthousand);
				// find substring after thousand
				String[] arrbeforethousand = beforethousand.split(" ");
				int arrbeforethousandLength = arrbeforethousand.length;
				// Figure out how many thousands are there that the user stated
				if (arrbeforethousandLength == 2) {
					wordnum = wordnum + 1000 * (wordToNumber(arrbeforethousand[0]) + wordToNumber(arrbeforethousand[1]));
				}
				if (arrbeforethousandLength == 1) {
					wordnum = wordnum + 1000 * (wordToNumber(arrbeforethousand[0]));
				}

			}
			if (word.contains("hundred")) {
				// Find out where the hundred occurs and take the indices at and string before it
				int indexofhundred = word.indexOf("hundred");
				
				String beforehundred = word.substring(0, indexofhundred);

				
				//Find the string after the hundred
				String[] arrbeforehundred = beforehundred.split(" ");
				int arrbeforehundredLength = arrbeforehundred.length;
				wordnum = wordnum + 100 * (wordToNumber(arrbeforehundred[arrbeforehundredLength - 1]));
				String afterhundred = word.substring(indexofhundred + 8);// 7 for 7
																			// char
																			// of
																			// hundred
																			// and 1
																			// space
				
				String[] arrafterhundred = afterhundred.split(" ");
				int arrafterhundredLength = arrafterhundred.length;
				if (arrafterhundredLength == 1) {
					wordnum = wordnum + (wordToNumber(arrafterhundred[0]));
				}
				if (arrafterhundredLength == 2) {
					wordnum = wordnum + (wordToNumber(arrafterhundred[1]) + wordToNumber(arrafterhundred[0]));
				}

			}
			if (!word.contains("thousand") && !word.contains("hundred")) {
				if (arrinwordsLength == 1) {
					wordnum = wordnum + (wordToNumber(arrinwords[0]));
				}
				if (arrinwordsLength == 2) {
					wordnum = wordnum + (wordToNumber(arrinwords[1]) + wordToNumber(arrinwords[0]));
				}
			}

			return wordnum;
		}

		private int wordToNumber(String word) {
			int num = 0;
			switch (word) {
			case "one":
				num = 1;
				break;
			case "two":
				num = 2;
				break;
			case "three":
				num = 3;
				break;
			case "four":
				num = 4;
				break;
			case "five":
				num = 5;
				break;
			case "six":
				num = 6;
				break;
			case "seven":
				num = 7;
				break;
			case "eight":
				num = 8;
				break;
			case "nine":
				num = 9;
				break;
			case "ten":
				num = 10;
				break;
			case "eleven":
				num = 11;
				break;
			case "twelve":
				num = 12;
				break;
			case "thirteen":
				num = 13;
				break;
			case "fourteen":
				num = 14;
				break;
			case "fifteen":
				num = 15;
				break;
			case "sixteen":
				num = 16;
				break;
			case "seventeen":
				num = 17;
				break;
			case "eighteen":
				num = 18;
				break;
			case "nineteen":
				num = 19;
				break;
			case "twenty":
				num = 20;
				break;
			case "thirty":
				num = 30;
				break;
			case "forty":
				num = 40;
				break;
			case "fifty":
				num = 50;
				break;
			case "sixty":
				num = 60;
				break;
			case "seventy":
				num = 70;
				break;
			case "eighty":
				num = 80;
				break;
			case "ninety":
				num = 90;
				break;
			case "hundred":
				num = 100;
				break;
			case "thousand":
				num = 1000;
				break;
			
			}
			return num;
		}
		
		public void makeDecision(String speech, List<WordResult> speechWords) {

			// Check if the speech contains any of these specific phrases

			if (speech.contains("how are you")) {
				tArea.setText(tArea.getText() + "Fine Thanks" + "\n");
				return;
			} else if (speech.contains("hey boss")) {
				tArea.setText(tArea.getText() + "can i have the pizza pliz" + "\n");

			} else if (speech.contains("say hello")) {
				tArea.setText(tArea.getText() + "Hello Friends" + "\n");
				return;
			} else if (speech.contains("say amazing")) {
				tArea.setText(tArea.getText() + "WoW it's amazing!" + "\n");
				return;
			} else if (speech.contains("what day is today")) {
				tArea.setText(tArea.getText() + "A good day" + "\n");
				return;
			} 
			//If the user requests help
			if(speech.contains("help") || speech.contains("<unk>")){
				tArea.setText(
						"Say Browser to open the Browser, and Word to open Microsoft Word \n"
						+ "Say a number [plus,minus,multiply,division] another number for"
						+ " basic calculations \n"
						);
			}
			//Process Specific commands (for Windows 10)
			Process p;
			if (speech.contains("browser")){
				try{
					p = Runtime.getRuntime().exec("MicrosoftEdge.exe");
					
				}
				catch(IOException ioEx){
					tArea.setText(tArea.getText() + "Having Trouble Opening Browser" + "\n");
				}
			}
			else if(speech.contains("word")){
				try{
					p = Runtime.getRuntime().exec("winword.exe");
					
				}
				catch(IOException ioEx){
					tArea.setText(tArea.getText() + "Having Trouble Opening Microsoft Word" + "\n");
				}
			}

			
////////////////// Mathematical Expressions Section //////////////////////////////////////////
			//Determine which expression was said
			String[] array = speech.split("(plus|minus|multiply|division){1}");
			
			// return if user said only one number
			if (array.length < 2)
				return;

			// Find the two numbers
			int number1 = convert(array[0]);
			int number2 = convert(array[1]);

			// Calculation result in int representation
			int calculationResult = 0;
			String symbol = "?";

			// Find the mathematical symbol
			if (speech.contains("plus")) {
				calculationResult = number1 + number2;
				symbol = "+";
			} else if (speech.contains("minus")) {
				calculationResult = number1 - number2;
				symbol = "-";
			} else if (speech.contains("multiply")) {
				calculationResult = number1 * number2;
				symbol = "*";
			} else if (speech.contains("division")) {
				if (number2 == 0)
					return;
				calculationResult = number1 / number2;
				symbol = "/";
			}

			String res = Integer.toString(Math.abs(calculationResult));

			// With words
			tArea.setText("Said:[ " + speech + " ]\n\t\t which after calculation is:[ "
					+ (calculationResult >= 0 ? "" : "minus ") + res + " ] \n");

			// With numbers and math
			tArea.setText("Mathematical expression:[ " + number1 + " " + symbol + " " + number2
					+ "]\n\t\t which after calculation is:[ " + calculationResult + " ]" + "\n");

		}

		
		//Class that implements the actions when the buttons are clicked
				private class Click implements ActionListener{
					
					boolean stopRecognition = false;
					//This function launches the speech Recognizer
					public void actionPerformed(ActionEvent event){
						
						if (event.getSource() == speechBtn){
							if(!stopRecognition){
								tArea.setText(tArea.getText() +
										"Say Browser to open the Browser, and Word to open Microsoft Word \n"
										+ "Say a number [plus,minus,multiply,division] another number for"
										+ " basic calculations \n"
										);
								//Start speech and resources threads
								speechBtn.setText("Click to Disable Speech Recognition");
								StartSpeechThread();
								StartResourcesThread();
							}
							else{
								tArea.setText("Speech Stopped \n");
								speechBtn.setText("Click to Enable Speech Recognition");
								stopSpeechThread();
								stopResourcesThread();
							}
							stopRecognition=!stopRecognition;
							
						}	
					}
					
				}
				
	public static void main(String[] args) {
		//Define Frame
		SpeechApplication rFrame = new SpeechApplication("JarBot");
		rFrame.setLocation(0,0);	
	
	}

}
