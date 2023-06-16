package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Controller implements Initializable {
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		categoryList = FXCollections.observableArrayList(new CategoryItem("Default"));
		comboboxCategory.setItems(categoryList);
		comboboxCategory.setValue(categoryList.get(0));
//		questionList = FXCollections.observableArrayList();
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));		
		addButtonToTable();
		table.setItems(categoryList.get(orNumCategory).getCategoryQuestion());
		
		dragAndDropFile.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if(event.getGestureSource()!=dragAndDropFile&& event.getDragboard().hasFiles()) {
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}
				event.consume();
			}
		});
		
		dragAndDropFile.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				if(db.hasFiles()) {
					List<File> listFile = db.getFiles();
					handleFile(listFile.get(0));
					success=true;
					
				}
				event.setDropCompleted(success);
				
				event.consume();
			}
		});
		comboboxCategory.valueProperty().addListener(new ChangeListener<CategoryItem>() {

			@Override
			public void changed(ObservableValue<? extends CategoryItem> cateGory, CategoryItem oldCategory, CategoryItem newCategory) {
				for(int i = 0; i<categoryList.size();i++) {
					if(categoryList.get(i).equals(newCategory)) {
						orNumCategory = i;
						table.setItems(categoryList.get(i).getCategoryQuestion());
						return;
					}
				}
				
			}
		});		
	}
	@FXML
	private TableView<Question> table;
	@FXML
	private VBox frameCbbCategory;
	@FXML
	private AnchorPane categoryAddWd;
	@FXML
	private TableColumn<Question, String> nameColumn;
	@FXML
	private TextField categoryNameText;
	@FXML
	private VBox dragAndDropFile;
	@FXML
	private TableColumn<Question, Void> actionColumn;
	@FXML
	private Label modeLabel;
	@FXML
	private TextField questionName;
	@FXML
	private Button btnSaveAndEdit;
	@FXML
	private VBox frameAnswers;
	@FXML
	private AnchorPane importFileWd;
	@FXML
	private AnchorPane createQuestionWd;
	@FXML
	private ComboBox<CategoryItem> comboboxCategory;
	@FXML
	private CheckBox checkSubCategories;
	private String modeCreatQuestion = null;
//	private ObservableList<Question> questionList;
	private int orNumCategory = 0;
	
	private ObservableList<CategoryItem> categoryList;
	
	private List<TextField> listTextField = new ArrayList<>();
	
	private List<ComboBox<String>> listCombobox = new ArrayList<>();
	
	private ObservableList<String> grade = FXCollections.observableArrayList("1","0");
	
	private Question questionTable = new Question();
	private void addButtonToTable() {
		Callback<TableColumn<Question, Void>, TableCell<Question, Void>> cellFactory = new Callback<TableColumn<Question,Void>, TableCell<Question,Void>>() {

			@Override
			public TableCell<Question, Void> call(final TableColumn<Question, Void> param) {
				final TableCell<Question, Void> cell = new TableCell<Question,Void>(){
					final Button btn = new Button("Edit");
					@Override
					public void updateItem(Void item, boolean empty) {
						super.updateItem(item,empty);
						if(empty) {
							setGraphic(null);
						}else {
							btn.setOnAction(e->{
								modeLabel.setText("Editing a Multiple choice Question");
								modeCreatQuestion = "edit";
								createQuestionWd.setVisible(true);
								Question question = getTableView().getItems().get(getIndex());
								questionTable = question;
								setQuestion(question);
							}
							);
							setGraphic(btn);
						}
					}
					
				};
				return cell;
			}
		};
		actionColumn.setCellFactory(cellFactory);
	}
	
	public void createQuestion() {
		modeLabel.setText("Create a Multiple choice Question");
		modeCreatQuestion = "create";
		createQuestionWd.setVisible(true);
		setQuestion(null);
		btnSaveAndEdit.setVisible(true);
	}
	

	
	public void blankMoreChoice(){
		double h = frameAnswers.getPrefHeight();
		h+=200;
		int i = listTextField.size()+1;
		frameAnswers.setPrefHeight(h);
		HBox hbox = new HBox();
		hbox.setPrefHeight(100);
		Label numAns = new Label("Choice "+i);
		TextField textAns = new TextField();
		HBox hbox1 = new HBox();
		hbox1.setPrefHeight(100);
		Label labelGrade = new Label("Grade");
		ComboBox<String> cbb = new ComboBox<String>();
		cbb.setItems(grade);
		cbb.setValue("none");
		hbox1.getChildren().addAll(labelGrade,cbb);
		hbox.getChildren().addAll(numAns,textAns);
		listTextField.add(textAns);
		listCombobox.add(cbb);
		frameAnswers.getChildren().addAll(hbox,hbox1);
	}
	
	public void setQuestion(Question question) {
		if(modeCreatQuestion == "edit"){
		questionName.setText(question.getName());
		List<Answer> answers = question.getAnswers();
		int i=1;
		double h = frameAnswers.getPrefHeight();
		listTextField = new ArrayList<>();
		listCombobox = new ArrayList<>();
		for(Answer answer :answers) {
			h+=200;
			frameAnswers.setPrefHeight(h);
			Label numAns = new Label("Choice"+i);
			TextField textAns = new TextField();
			textAns.setText(answer.getAlphabet());
			listTextField.add(textAns);
			HBox hbox = new HBox();
			hbox.setPrefHeight(100);
			hbox.getChildren().addAll(numAns,textAns);
			frameAnswers.getChildren().add(hbox);
			
			HBox hbox1 = new HBox();
			hbox1.setPrefHeight(100);
			Label labelGrade = new Label("Grade");
			ComboBox<String> cbb = new ComboBox<String>();
			cbb.setItems(grade);
			cbb.setValue(answer.getGrade());
			hbox1.getChildren().addAll(labelGrade,cbb);
			listCombobox.add(cbb);
			frameAnswers.getChildren().add(hbox1);
			
			i++;
			}
		}
		if(modeCreatQuestion == "create") {
			listTextField = new ArrayList<>();
			listCombobox = new ArrayList<>();
			frameAnswers.setPrefHeight(400);
			Label numAns = new Label("Choice 1");
			TextField textAns = new TextField();
			listTextField.add(textAns);
			Label numAns1 = new Label("Choice 2");
			TextField textAns1 = new TextField();
			listTextField.add(textAns1);
			HBox hbox = new HBox();
			hbox.setPrefHeight(100);
			hbox.getChildren().addAll(numAns,textAns);
			frameAnswers.getChildren().add(hbox);
			
			HBox hbox2 = new HBox();
			hbox2.setPrefHeight(100);
			Label labelGrade = new Label("Grade");
			ComboBox<String> cbb = new ComboBox<String>();
			cbb.setItems(grade);
			cbb.setValue("none");
			hbox2.getChildren().addAll(labelGrade,cbb);
			listCombobox.add(cbb);
			frameAnswers.getChildren().add(hbox2);
			
			HBox hbox1 = new HBox();
			hbox1.setPrefHeight(100);
			hbox1.getChildren().addAll(numAns1,textAns1);
			frameAnswers.getChildren().add(hbox1);
			
			HBox hbox3 = new HBox();
			hbox3.setPrefHeight(100);
			Label labelGrade1 = new Label("Grade");
			ComboBox<String> cbb1 = new ComboBox<String>();
			cbb1.setItems(grade);
			cbb1.setValue("none");
			hbox3.getChildren().addAll(labelGrade1,cbb1);
			listCombobox.add(cbb1);
			frameAnswers.getChildren().add(hbox3);
		}
	}
	
	public void cancelEdit() {
		questionName.setText(null);
		ObservableList<Node> hb = frameAnswers.getChildren();
		frameAnswers.getChildren().removeAll(hb);
		frameAnswers.setPrefHeight(0);
		createQuestionWd.setVisible(false);
		btnSaveAndEdit.setVisible(false);
	}
	
	public void saveAndEdit() {
		Question qs = new Question();
		qs.setName(questionName.getText());
		List<Answer> as = new ArrayList<>();
		for(int i =0; i<listTextField.size();i++) {
			if(listCombobox.get(i).getValue()=="none") listCombobox.get(i).setValue("0");
			Answer a = new Answer(listTextField.get(i).getText(),listCombobox.get(i).getValue());
			as.add(a);
		}
		qs.setAnswers(as);
		categoryList.get(orNumCategory).getCategoryQuestion().add(qs);
		
		ObservableList<Node> hb = frameAnswers.getChildren();
		frameAnswers.getChildren().removeAll(hb);
		questionName.setText(null);
		listTextField = new ArrayList<>();
		frameAnswers.setPrefHeight(400);
		Label numAns = new Label("Choice 1");
		TextField textAns = new TextField();
		listTextField.add(textAns);
		Label numAns1 = new Label("Choice 2");
		TextField textAns1 = new TextField();
		listTextField.add(textAns1);
		HBox hbox = new HBox();
		hbox.setPrefHeight(100);
		hbox.getChildren().addAll(numAns,textAns);
		frameAnswers.getChildren().add(hbox);
		
		HBox hbox2 = new HBox();
		hbox2.setPrefHeight(100);
		Label labelGrade = new Label("Grade");
		ComboBox<String> cbb = new ComboBox<String>();
		cbb.setItems(grade);
		cbb.setValue("none");
		hbox2.getChildren().addAll(labelGrade,cbb);
		listCombobox.add(cbb);
		frameAnswers.getChildren().add(hbox2);
		
		HBox hbox1 = new HBox();
		hbox1.setPrefHeight(100);
		hbox1.getChildren().addAll(numAns1,textAns1);
		frameAnswers.getChildren().add(hbox1);
		
		HBox hbox3 = new HBox();
		hbox3.setPrefHeight(100);
		Label labelGrade1 = new Label("Grade");
		ComboBox<String> cbb1 = new ComboBox<String>();
		cbb1.setItems(grade);
		cbb1.setValue("none");
		hbox3.getChildren().addAll(labelGrade1,cbb1);
		listCombobox.add(cbb1);
		frameAnswers.getChildren().add(hbox3);
	}
	
	public void saveChanges() {
		if(modeCreatQuestion == "edit") {
		
		Question qs = new Question();
		qs.setName(questionName.getText());
		List<Answer> as = new ArrayList<>();

		for(int i =0;i<listTextField.size();i++) {
			if(listCombobox.get(i).getValue().equals("none")) listCombobox.get(i).setValue("0");
			Answer a = new Answer(listTextField.get(i).getText(),listCombobox.get(i).getValue());

			as.add(a);
		}
		qs.setAnswers(as);
		for(int i =0;i<categoryList.get(orNumCategory).getCategoryQuestion().size();i++) {
			if(categoryList.get(orNumCategory).getCategoryQuestion().get(i).equals(questionTable)) {
				categoryList.get(orNumCategory).getCategoryQuestion().set(i, qs);
			}
		}
		ObservableList<Node> hb = frameAnswers.getChildren();
		questionName.setText(null);
		frameAnswers.getChildren().removeAll(hb);
		frameAnswers.setPrefHeight(0);
		createQuestionWd.setVisible(false);
		}
		if(modeCreatQuestion == "create") {
			ObservableList<Node> hb = frameAnswers.getChildren();
			frameAnswers.getChildren().removeAll(hb);
			frameAnswers.setPrefHeight(0);
			Question qs = new Question();
			qs.setName(questionName.getText());
			String fixNone = "1";
			List<Answer> as = new ArrayList<>();
			for(int i =0;i<listTextField.size();i++) {
				if(listCombobox.get(i).getValue().equals("none")) {
					fixNone= "0";
				}
				Answer a = new Answer(listTextField.get(i).getText(),fixNone);
				as.add(a);
				fixNone="1";
			}
			qs.setAnswers(as);
			questionName.setText(null);
			categoryList.get(orNumCategory).getCategoryQuestion().add(qs);
			createQuestionWd.setVisible(false);
			btnSaveAndEdit.setVisible(false);
		}

	}
	
	public void handleFile(File file) {
		try {
			int lineNum=1,questionNum=0,answerNum=0;
			InputStream is = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader reader = new BufferedReader(isr);
			List<Question> questionsInFile = new ArrayList<>();
			while(true) {
				Question question = new Question();
				List<Answer> as = new ArrayList<>();
				List<Character> alphaAnswer = new ArrayList<>();
				String line = reader.readLine();
				if(line==null) {
					break;
				}
				lineNum++; 
				question.setName(line);
				
				line= reader.readLine();
				
				while(line.charAt(1)=='.'&&line.charAt(2)==' ') {
					if(line.length()<4||line==null) {
						JOptionPane.showMessageDialog(null, "Error at "+lineNum);
						return ;
					}
					
					answerNum++;
					alphaAnswer.add(line.charAt(0));
					String answerInLine = "";
					for(int d = 3;d<line.length();d++) {
						answerInLine+=line.charAt(d);
					}
					Answer answer = new Answer(answerInLine,"0");
					as.add(answer);
					
					lineNum++;
					line= reader.readLine();
					
				}
				if(answerNum<2) {
					JOptionPane.showMessageDialog(null, "Error at "+lineNum);
					return ;
				}
				
				if(!checkNotAnswer(line)) {
					JOptionPane.showMessageDialog(null, "Error at "+lineNum);
					return ;
				}
				char realAnswer = line.charAt(8);
				for(int i=0;i<alphaAnswer.size();i++) {
					if(realAnswer==alphaAnswer.get(i)) {
						as.get(i).setGrade("1");
						break;
					}
				}
				question.setAnswers(as);
				questionsInFile.add(question);
				questionNum++;
				lineNum++;
				line = reader.readLine();
				if(line != null ) {
					if(line.length()!=0) {
						JOptionPane.showMessageDialog(null, "Error at "+lineNum);
						return;
					}
				}
				answerNum=0;
				
			}
			for(Question q : questionsInFile) {
				categoryList.get(orNumCategory).getCategoryQuestion().add(q);
			}
			JOptionPane.showMessageDialog(null, "Success "+questionNum);
			
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	public void chooseFileTxt(ActionEvent e) {
		Node btn = (Node)e.getSource();
		Stage stage = (Stage)btn.getScene().getWindow();
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose file txt");
		File file = fc.showOpenDialog(stage);
		if(file!=null) {
			String filePath = file.toString();
			int n = filePath.length();
			String format="";
			format+=filePath.charAt(n-1);
			format+=filePath.charAt(n-2);
			format+=filePath.charAt(n-3);
			if(!format.equals("txt")) {
				JOptionPane.showMessageDialog(null, "WRONG FORMAT");
			}else {
				handleFile(file);
			}
		}
	}
	public boolean checkNotAnswer(String answer) {
		if(answer.length()!=9) return false;
		else {
			if(answer.charAt(0)!='A') return false;
			if(answer.charAt(1)!='N') return false;
			if(answer.charAt(2)!='S') return false;
			if(answer.charAt(3)!='W') return false;
			if(answer.charAt(4)!='E') return false;
			if(answer.charAt(5)!='R') return false;
			if(answer.charAt(6)!=':') return false;
			if(answer.charAt(7)!=' ') return false;
			return true;
		}
	}
	public void openImportFileWd() {
		importFileWd.setVisible(true);
	}
	public void closeImportFileWd() {
		importFileWd.setVisible(false);
	}
	public void openCategoryAddWd() {
		ComboBox<CategoryItem> q = new ComboBox<CategoryItem>();
		q.setItems(categoryList);
		q.setPrefHeight(44);
		q.setPrefWidth(251);
		q.setValue(categoryList.get(0));
		categoryAddWd.setVisible(true);
		frameCbbCategory.getChildren().add(q);
	}
	public void closeCategoryAddWd() {
		categoryAddWd.setVisible(false);
		ObservableList<Node> q = frameCbbCategory.getChildren();
		frameCbbCategory.getChildren().removeAll(q);
		categoryNameText.setText(null);
	}
	public void addCategoryToCbb(){
		String nameCategory = categoryNameText.getText();
		ObservableList<Node> q = frameCbbCategory.getChildren(); 
		@SuppressWarnings("unchecked")
		ComboBox<CategoryItem> qq =(ComboBox<CategoryItem>) q.get(0);
		CategoryItem cate = qq.getValue();
		int num = 0;
		for(CategoryItem ci : categoryList) {
			if(cate.equals(ci)) {
				break;
			}num++;
		}
		int numSpace = 0;
		String name = categoryList.get(num).getCategory();
		for(int i = 0;i<name.length();i++) {
			if(name.charAt(i)==' ') {
				numSpace++;
			}else {
				break;
			}
		}
		String newName = "";
		if(cate.getCategory()=="Default") {
			newName = nameCategory;
		}else {
			for(int i =0;i<numSpace;i++) {
				newName+=" ";
			}
			newName+="    ";
			newName+=nameCategory;
		}
		categoryList.add(num+1, new CategoryItem(newName));
		categoryAddWd.setVisible(false);
		frameCbbCategory.getChildren().removeAll(q);
		categoryNameText.setText(null);
	}
	public void showSubCategories(ActionEvent event) {
		if(checkSubCategories.isSelected()) {
			for(int i =0;i<categoryList.size()-1;i++) {
				addOrRemoveSubCate(true, i);
			}
		}else {
			for(int i=0;i<categoryList.size()-1;i++) {
				addOrRemoveSubCate(false, i);
			}
		}
		table.setItems(categoryList.get(orNumCategory).getCategoryQuestion());
	}
	
	public void addOrRemoveSubCate(boolean check, int orNumCate) {
		int numSpaceNow = categoryList.get(orNumCate).getCategory().length() - categoryList.get(orNumCate).getCategory().trim().length();
		
		if(check) {
			for(int i = orNumCate+1;i<categoryList.size();i++) {
				int numSpace = categoryList.get(i).getCategory().length() - categoryList.get(i).getCategory().trim().length();
				if(numSpace <= numSpaceNow) break;
				else {
					categoryList.get(orNumCate).getCategoryQuestion().addAll(categoryList.get(i).getCategoryQuestion());
				}
			}
		}else {
			for(int i = orNumCate+1;i<categoryList.size();i++) {
				int numSpace = categoryList.get(i).getCategory().length() - categoryList.get(i).getCategory().trim().length();
				if(numSpace <= numSpaceNow) break;
				else {
					categoryList.get(orNumCate).getCategoryQuestion().removeAll(categoryList.get(i).getCategoryQuestion());
				}
			}

		}
	}
}

	