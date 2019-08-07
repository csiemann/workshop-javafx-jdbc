package gui.util;

import javafx.scene.control.TextField;

public class Constrains {
	public static void setTextFieldInteger(TextField txt) {
		txt.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*")) 
				txt.setText(oldValue);
		});
	}

	public static void setTextFieldMaxLenght(TextField txt, int max) {
		txt.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && newValue.length() > max) 
				txt.setText(oldValue);
		});
	}

	public static void setTextFieldEmail(TextField txt) {
		txt.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("(\\w|\\d|\\.)*([\\@])?(\\w*)(([\\.])?(\\w*)?)*")) 
				txt.setText(oldValue);
		});
	}
	
	public static void setTextFieldDouble(TextField txt) {
		txt.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*([\\.]\\d*)?")) 
				txt.setText(oldValue);
		});
	}
	
	public static void setTextFieldDouble(TextField txt,int max) {
		txt.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*([\\.])?(\\d{1,"+max+"})?"))
				txt.setText(oldValue);
		});
	}
}
