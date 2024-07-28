import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginPage extends JFrame{
    private JTextField idField1;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JPanel loginPanel;
    private JButton exitButton;
    private Connection con;

    public LoginPage(){
        connectDatabase();
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    int userId;
                    int checkPin;
                    String userName = idField1.getText().strip();
                    int pin = Integer.parseInt(new String(passwordField.getPassword()).strip());
                    String getPinQuery = "select user_pin, user_id from user where user_name=?";
                    PreparedStatement loginStatement = con.prepareStatement(getPinQuery);
                    loginStatement.setString(1,userName);
                    ResultSet loginResult = loginStatement.executeQuery();
                    if(!loginResult.next()){
                        JOptionPane.showMessageDialog(LoginPage.this, "User not found", "Unknown user not found", JOptionPane.ERROR_MESSAGE);
                    } else{
                        checkPin = loginResult.getInt("user_pin");
                        userId = loginResult.getInt("user_id");
                        if(pin==checkPin){
                            dispose();
                            new MainMenu(LoginPage.this,userId);
                        } else{
                            JOptionPane.showMessageDialog(LoginPage.this, "Error: Incorrect PIN!", "Incorrect Pin Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }catch(SQLException sqlE){
                    sqlE.printStackTrace();
                    JOptionPane.showMessageDialog(LoginPage.this, "Error: SQL Query Error :"+ sqlE.getMessage(), "SQL Query Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });

        this.setContentPane(this.loginPanel);
        this.setTitle("ATM");
        this.setSize(750,750);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void connectDatabase(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ATM", "root", "1234");
        } catch(Exception SqlConnE){
            SqlConnE.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Connection Error: "+SqlConnE.getMessage(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
