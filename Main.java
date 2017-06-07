import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSetMetaData;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import java.io.IOException;
import java.sql.*;
import java.util.*;


public class Main extends Application {
//Panes and scenes
FlowPane connectPane = new FlowPane();
BorderPane cityPane = new BorderPane();
FlowPane createPane = new FlowPane();
Scene createScene = new Scene(createPane);
Scene sceneconnect = new Scene(connectPane);
Scene cityScene = new Scene(cityPane);
FlowPane deleteCityPane = new FlowPane();
Scene deletecity = new Scene(deleteCityPane);
Connection conn;
ObservableList<ObservableList> data;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Opening scene that prompts user to connect to database
        TextField username = new TextField();
        TextField password = new TextField();
        username.setAlignment(Pos.CENTER);
        password.setAlignment(Pos.CENTER);
        Button connect = new Button("Connect");
        //ActionEvent of button in opening scene that registers drivers and connects you to database
        connect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException ex) {
                    System.out.println("Error: unable to load driver class!");
                    System.exit(1);
                }
                String URL = "jdbc:mysql://localhost:3306/world";
                String name = username.getText();
                String pass = password.getText();

                try {
                    conn = DriverManager.getConnection(URL, name, pass);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                primaryStage.setScene(cityScene);


            }
        });
        //Opening scene for connecting to database with attributes
        Label user = new Label("Username:");
        Label pass = new Label("Password:");
        user.setAlignment(Pos.CENTER);
        pass.setAlignment(Pos.CENTER);
        username.setAlignment(Pos.CENTER);
        password.setAlignment(Pos.CENTER);
        connect.setAlignment(Pos.CENTER);
        VBox connectionBox = new VBox(10, user, username, pass, password, connect);
        connectionBox.setAlignment(Pos.CENTER);
        connectPane.getChildren().addAll(connectionBox);
        connectPane.setMinSize(640, 480);
        connectPane.setAlignment(Pos.CENTER);

        //Scene for view of city database, main scene where records are shown
        Button populate = new Button("Populate/Update");
        Button delete = new Button("Delete");
        Button create = new Button("Create");
        Button clear = new Button("Exit");
        HBox cityButtons = new HBox(populate, delete, create, clear);
        //Main scene attributes
        cityButtons.setAlignment(Pos.CENTER);
        cityButtons.setSpacing(100);
        cityButtons.setPadding(new Insets(10, 50, 30, 50));
        cityPane.setBottom(cityButtons);
        cityPane.setMinSize(1024, 768);
        //Button that closes program
        clear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Platform.setImplicitExit(true);
                Platform.exit();
                System.exit(0);
            }
        });
        populate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Create table and iniatialize it so no duplicate records are added
                TableView<ObservableList> cityTableView = new TableView<>();
                TableColumn ID = new TableColumn("ID");
                TableColumn name = new TableColumn("Name");
                TableColumn countryCode = new TableColumn("Country Code");
                TableColumn region = new TableColumn("Region");
                TableColumn population = new TableColumn("Population");
                ID.prefWidthProperty().bind(cityTableView.widthProperty().divide(2)); // w * 1/4
                name.prefWidthProperty().bind(cityTableView.widthProperty().divide(2)); // w * 1/2
                region.prefWidthProperty().bind(cityTableView.widthProperty().divide(2));
                population.prefWidthProperty().bind(cityTableView.widthProperty().divide(2));
                countryCode.prefWidthProperty().bind(cityTableView.widthProperty().divide(2));
                cityTableView.setMinWidth(1000);
                cityTableView.setPadding(new Insets(10,12,10,12));
                HBox cityTable = new HBox(cityTableView);
                cityPane.setCenter(cityTable);

                Connection c ;
                data = FXCollections.observableArrayList();
                try{

                    //SQL FOR SELECTING ALL OF CUSTOMER
                    String SQL = "SELECT * from world.city";
                    //ResultSet
                    ResultSet rs = conn.createStatement().executeQuery(SQL);

                    /**********************************
                     * TABLE COLUMN ADDED DYNAMICALLY *
                     **********************************/
                    for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
                        //We are using non property style for making dynamic table
                        final int j = i;
                        TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
                        col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                            public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                                return new SimpleStringProperty(param.getValue().get(j).toString());
                            }
                        });
                        cityTableView.getColumns().addAll(col);
                        System.out.println("Column ["+i+"] ");
                    }
                    /********************************
                     * Data added to ObservableList *
                     ********************************/
                    while(rs.next()){
                        //Iterate Row
                        ObservableList<String> row = FXCollections.observableArrayList();
                        for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                            //Iterate Column
                            row.add(rs.getString(i));
                        }
                        data.add(row);
                    }
                    //FINALLY ADDED TO TableView
                    cityTableView.setItems(data);
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println("Error on Building Data");
                }
            }
        });
        //Sends user to record deltion scene
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.setScene(deletecity);
            }
        });
        //Sends user to creation of record scene
        create.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.setScene(createScene);
            }
        });
        //Scene for deletion of records
        Label deleteID = new Label("Enter ID of record to be deleted: ");
        TextField deleIDField = new TextField();
        Button deleIDButton = new Button("Delete Record");
        VBox deleteIDBox = new VBox(deleteID,deleIDField, deleIDButton);
        //Deletion scene attributes
        deleteID.setAlignment(Pos.CENTER);
        deleIDField.setAlignment(Pos.CENTER);
        deleIDButton.setAlignment(Pos.CENTER);
        deleteIDBox.setAlignment(Pos.CENTER);
        deleteCityPane.setAlignment(Pos.CENTER);
        deleteCityPane.getChildren().addAll(deleteIDBox);
        deleteCityPane.setMinSize(640, 480);
        //Deletes record by ID, returns to main scene
        deleIDButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String sql = "DELETE FROM world.city " +
                        "WHERE id = " + deleIDField.getText();
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                primaryStage.setScene(cityScene);
            }

        });
        //Scene for creation of records
        Label createID = new Label("ID");
        TextField createIDField = new TextField();
        Label createName = new Label("Name: ");
        TextField createNameField = new TextField();
        Label createCountryCode = new Label("Country code: ");
        TextField createCountryField = new TextField();
        Label createDistrict = new Label("District: ");
        TextField createDistrictField = new TextField();
        Label createPop = new Label("Population: ");
        TextField createPopField = new TextField();
        Button createRecord = new Button("Create Record");
        VBox createBox =new VBox(createID,createIDField,createName,createNameField,createCountryCode,createCountryField,createDistrict,createDistrictField,createPop,createPopField,createRecord);
        //Creation scene attributes
        createBox.setMinSize(640, 480);
        createID.setAlignment(Pos.CENTER);
        createIDField.setAlignment(Pos.CENTER);
        createName.setAlignment(Pos.CENTER);
        createNameField.setAlignment(Pos.CENTER);
        createCountryCode.setAlignment(Pos.CENTER);
        createCountryField.setAlignment(Pos.CENTER);
        createDistrict.setAlignment(Pos.CENTER);
        createDistrictField.setAlignment(Pos.CENTER);
        createPop.setAlignment(Pos.CENTER);
        createPopField.setAlignment(Pos.CENTER);
        createRecord.setAlignment(Pos.CENTER);
        createBox.setAlignment(Pos.CENTER);
        createPane.getChildren().addAll(createBox);
        createPane.setMinSize(640, 480);
        createPane.setAlignment(Pos.CENTER);
        //Actionevent that creates a new record, returns you to main scene
        createRecord.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                String sql = "insert into world.city values " + "(" + createIDField.getText() + ", \"" + createNameField.getText() + "\", \"" + createCountryField.getText() + "\", \"" + createDistrictField.getText() +
                        "\", " + createPopField.getText() + ")";
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println(sql);
                primaryStage.setScene(cityScene);

            }
        });
        primaryStage.setScene(sceneconnect);
        primaryStage.show();
    }   
    public static void main(String[] args) {
        launch(args);
    }
}
