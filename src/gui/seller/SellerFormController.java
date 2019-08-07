package gui.seller;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constrains;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidateException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;
	private SellerService service;
	private DepartmentService departmentService;
	private ObservableList<Department> obsListDepartment;
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private Label labelErrorName;
	@FXML
	private TextField txtEmail;
	@FXML
	private Label labelErrorEmail;
	@FXML
	private DatePicker dpBirthDate;
	@FXML
	private Label labelErrorBirthDate;
	@FXML
	private TextField txtBaseSalary;
	@FXML
	private Label labelErrorBaseSalary;
	@FXML
	private ComboBox<Department> comboBoxDepartment;
	@FXML
	private Label labelErrorDepartment;
	@FXML
	private Button btSave;
	@FXML
	private Button btCancel;

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			emptyErrorMessages();
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		} catch (ValidateException e) {
			setErrorMessages(e.getErrors());
		}

	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	private Seller getFormData() {
		Seller obj = new Seller();

		ValidateException exception = new ValidateException("Validation error");
		if (txtName.getText() == null || txtName.getText().trim().equals(""))
			exception.addError("name", "Field can't be empty");
		if (txtEmail.getText() == null || txtEmail.getText().trim().equals(""))
			exception.addError("email", "Field can't be empty");
		if (dpBirthDate.getValue() == null || dpBirthDate.getValue().isAfter(LocalDate.now()))
			exception.addError("birthDate", "Enter your birth date");
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().equals(""))
			exception.addError("baseSalary", "Field can't be empty or no base salary");
		if (comboBoxDepartment.getSelectionModel().getSelectedItem() == null)
			exception.addError("department", "Select a department");

		obj.setId(Utils.tryParseToInt(txtId.getText()));
		obj.setName(txtName.getText());
		obj.setEmail(txtEmail.getText());
		obj.setBirthDate(dpBirthDate.getValue() == null ? null
				: Date.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
		obj.setBaseSalary(txtBaseSalary.getText() == null || txtBaseSalary.getText().equals("") ? null
				: Double.parseDouble(txtBaseSalary.getText()));
		obj.setDepartment(comboBoxDepartment.getValue());
		if (exception.getErrors().size() != 0) {
			throw exception;
		}
		System.out.println(obj.getBirthDate());
		System.out.println(dpBirthDate.getValue());
		return obj;
	}

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setSellerService(SellerService service) {
		this.service = service;
	}

	public void setDepartmentService(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeNode();
	}

	private void initializeNode() {
		Constrains.setTextFieldInteger(txtId);
		Constrains.setTextFieldMaxLenght(txtName, 40);
		Constrains.setTextFieldEmail(txtEmail);
		Constrains.setTextFieldDouble(txtBaseSalary, 2);
	}

	public synchronized void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		if (entity.getBirthDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(entity.getBirthDate());
			dpBirthDate.setValue(LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH)));
		}
		txtBaseSalary.setText(String.valueOf(entity.getBaseSalary()));
		setComboBoxDepartment();
	}

	private void setComboBoxDepartment() {
		if (departmentService == null) {
			throw new IllegalStateException("DepartmentService was null");
		}
		List<Department> list = departmentService.findAll();
		obsListDepartment = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsListDepartment);
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			};
		};
		if (entity.getDepartment()!=null) {
			comboBoxDepartment.getSelectionModel().select(entity.getDepartment());
		}
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		if (fields.contains("name"))
			labelErrorName.setText(errors.get("name"));
		if (fields.contains("email"))
			labelErrorEmail.setText(errors.get("email"));
		if (fields.contains("birthDate"))
			labelErrorBirthDate.setText(errors.get("birthDate"));
		if (fields.contains("baseSalary"))
			labelErrorBaseSalary.setText(errors.get("baseSalary"));
		if (fields.contains("department"))
			labelErrorDepartment.setText(errors.get("department"));
	}

	private void emptyErrorMessages() {
		labelErrorName.setText(null);
		labelErrorEmail.setText(null);
		labelErrorBirthDate.setText(null);
		labelErrorBaseSalary.setText(null);
		labelErrorDepartment.setText(null);
	}
}
