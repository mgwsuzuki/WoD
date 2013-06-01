
package wodcounter;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WoD extends Application {

	private WoDController controller;

	@Override
	public void start(Stage stage) throws Exception{
        stage.setTitle("WoD Time Couner");

        // GUIをFXMLから読み込む場合(普通に書いた場合)
        //Parent root = FXMLLoader.load(getClass().getResource("WoD.fxml"));
        // GUIをFXMLから読みつつコントローラを取得する場合
		//FXMLLoader loader = new FXMLLoader(getClass().getResource("/WoD.fxml"));	// maven用
		FXMLLoader loader = new FXMLLoader(getClass().getResource("WoD.fxml"));
        Parent root = (Parent)loader.load();

        controller = (WoDController)loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);

        // ウィンドウがクローズされるイベント
        // このイベントをコントローラで捕捉できればいらないんだが
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e){
            	controller.closing();
            	System.out.println("Window closed");
            }
        });

        stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
