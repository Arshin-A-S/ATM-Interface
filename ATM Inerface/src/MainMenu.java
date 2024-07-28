import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MainMenu extends JFrame{
    private JPanel menuPanel;
    private JButton withdrawButton;
    private JButton depositButton;
    private JButton transferButton;
    private JButton transactionHistoryButton;
    private JButton quitButton;
    private JButton showBalanceButton;

    public MainMenu(LoginPage lp , int userId){
        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new Withdraw(MainMenu.this, userId);
            }
        });

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new Deposit(MainMenu.this, userId);
            }
        });

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new Transfer(MainMenu.this, userId);
            }
        });

        transactionHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new TransactionHistory(MainMenu.this, userId);
            }
        });

        showBalanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ATM", "root", "1234");
                    try{
                        String getBalanceQuery = "select balance from account where user_id=?";
                        PreparedStatement getBalance = con.prepareStatement(getBalanceQuery);
                        getBalance.setInt(1,userId);
                        ResultSet balanceQueryResult = getBalance.executeQuery();
                        balanceQueryResult.next();
                        int balance = balanceQueryResult.getInt("balance");
                        JOptionPane.showMessageDialog(MainMenu.this, "Balance: "+balance, "Balance", JOptionPane.INFORMATION_MESSAGE);

                    }catch(SQLException SqlQueryE){
                        JOptionPane.showMessageDialog(MainMenu.this, "Database Query Error: "+SqlQueryE.getMessage(), "Database Connectrion Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch(Exception SqlConnE){
                    JOptionPane.showMessageDialog(MainMenu.this, "Database Error: "+SqlConnE.getMessage(), "Database Connectrion Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new QuitPage(MainMenu.this);
            }
        });

        this.setContentPane(this.menuPanel);
        this.setTitle("ATM");
        this.setSize(1000,1000);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
