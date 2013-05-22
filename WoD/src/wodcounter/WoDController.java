package wodcounter;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 


public class WoDController implements Initializable {
	
	@FXML
	private Text LeftCountText;
	@FXML
	private Rectangle LeftCountBack;
	private DropShadow LeftDropShadow;

	@FXML
	private Text RightCountText;
	@FXML
	private Rectangle RightCountBack;
	private DropShadow RightDropShadow;
	
	@FXML
	private Button ConnButton;
	
	@FXML
	private Button ClearButton;
	
	@FXML
	private ComboBox ComportComboBox;

	// タイマ
	private Timer timer;
	private Task task;
	
	// 時間計測
	private int workTime = 0;
	private int notWorkTime = 0;
	private int switchStatus = -1;
	private boolean forceClear = false;

	// シリアルポート
	private SerialPort serialPort = null;
	private InputStream inputStream;
	private OutputStream outputStream;
	private static final int TIME_OUT = 2000;
	private boolean serialReady = false;

	////////////////////////////////////////////////////////
	/// 初期化
	@Override
	public void initialize(URL url, ResourceBundle rb){
		System.out.println("initialized");
		
		// Comboboxをクリアし、シリアルポートをリストアップする
		ComportComboBox.getItems().clear();
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements()){
			CommPortIdentifier curport = (CommPortIdentifier)ports.nextElement();
			ComportComboBox.getItems().add(curport.getName());
			System.out.println(curport.getName());
		}
		ComportComboBox.getSelectionModel().select(0);
		
		// タイマタスク
		timer = new Timer(true);
		startTimer();
		
		// Timeline
		Timeline timeLine = new Timeline(new KeyFrame(Duration.millis(500),
														new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent ev){
				LeftCountText.setText(formatTime(workTime));
				RightCountText.setText(formatTime(notWorkTime));
				if (switchStatus == 1){
					LeftCountBack.setFill(Color.web("ffaf66"));
					RightCountBack.setFill(Color.web("ccf9ff"));
				}else if (switchStatus == 2){
					LeftCountBack.setFill(Color.web("fff1cc"));
					RightCountBack.setFill(Color.web("88b8ff"));
				}else{
					LeftCountBack.setFill(Color.web("fff1cc"));
					RightCountBack.setFill(Color.web("ccf9ff"));
				}
			}
		}));
		timeLine.setCycleCount(Timeline.INDEFINITE);
		timeLine.play();
		
		// テキストのエフェクト
		LeftDropShadow = new DropShadow();
		LeftDropShadow.setOffsetX(1.0);
		LeftDropShadow.setOffsetY(1.0);
		LeftDropShadow.setRadius(4);
		LeftDropShadow.setColor(Color.web("404040"));
		LeftCountText.setEffect(LeftDropShadow);
		RightDropShadow = new DropShadow();
		RightDropShadow.setOffsetX(1.0);
		RightDropShadow.setOffsetY(1.0);
		RightDropShadow.setRadius(4);
		RightDropShadow.setColor(Color.web("404040"));
		RightCountText.setEffect(LeftDropShadow);
	}
	
	////////////////////////////////////////////////////////
	/// Connボタンが押された
	@FXML
	public void handleConnButton(ActionEvent event){
		String selport = (String)ComportComboBox.getValue();
		try {
		// 選択されたポートのportIDを得る
			CommPortIdentifier curport = CommPortIdentifier.getPortIdentifier(selport);
			serialPort = (SerialPort)curport.open(this.getClass().getName(), TIME_OUT);
			serialPort.setSerialPortParams(9600,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();
			System.out.println("serial opened");
			Thread.sleep(2000);
			serialReady = true;
			// Connボタンを押せないようにする
			ConnButton.setDisable(true);
		} catch(Exception e){
			System.err.println(e.toString());
		}
	}

	////////////////////////////////////////////////////////
	/// Clearボタンが押された
	@FXML
	public void handleClearButton(ActionEvent event){
		//System.out.println("Clear Button pushed");
		// Arduinoに状態のリセットを伝える
		try {
			outputStream.write("RSET".getBytes("ISO-8859-1"));
			// リセット情報がすぐに反映されないので
			forceClear = true;
			switchStatus = 0;
		}catch(Exception e){
		}
		// カウンタをリセットする
		workTime = 0;
		notWorkTime = 0;
	}

	////////////////////////////////////////////////////////
	/// タイマの開始
	public void startTimer(){
		if (task == null){
			task = new Task();
		}
		timer.scheduleAtFixedRate(task, 0, 1000);
	}
	
	////////////////////////////////////////////////////////
	/// タイマの停止
	public void stopTimer(){
		task.cancel();
		task = null;
	}
	
	////////////////////////////////////////////////////////
	/// 経過時間を文字列に
	private String formatTime(int t){
		int h = t / 3600;
		int m = (t - h*3600) / 60;
		int s = (t - h*3600 - m*60);
		return String.format("%1$02d:%2$02d:%3$02d", h, m, s);
	}

	////////////////////////////////////////////////////////
	/// ウィンドウクローズの処理
	public void closing(){
		if (serialPort != null){
			serialPort.close();
			serialPort = null;
			System.out.println("serialport closed");
		}
	}
	
	////////////////////////////////////////////////////////
	/// タイマで起動されるタスク
	class Task extends TimerTask {
		@Override
		public void run() {
			byte[] res = new byte[8];
			if (serialReady){
				try {
					outputStream.write("RPSW".getBytes("ISO-8859-1"));
					inputStream.read(res);
					switch(res[0]){
					case 48: 
						switchStatus = 0;
						forceClear = false;
						break;
					case 49:
						if (!forceClear){
							switchStatus = 1;
							workTime++;
						}
						break;
					case 50:
						if (!forceClear){
							switchStatus = 2;
							notWorkTime++;
						}
						break;
					case 51:
						switchStatus = 3;
						forceClear = false;
						break;
					default:
						switchStatus = 0;
						break;
					}
					//System.out.println(switchStatus);
				}catch(Exception e){
				}
			}
		}
		
	}
}
